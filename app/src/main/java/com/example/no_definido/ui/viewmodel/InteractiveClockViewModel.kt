package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.ProgressRepository
import com.example.no_definido.data.UserProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ClockTimeOfDay(
    val displayName: String,
    val topColorHex: Long,
    val bottomColorHex: Long,
    val sunCenterXFactor: Float,
    val sunCenterYFactor: Float,
    val isNight: Boolean
) {
    MORNING("Mañana", 0xFFFDBA74, 0xFFFED7AA, 0.14f, 0.38f, false),
    NOON("Mediodía", 0xFF38BDF8, 0xFFBAE6FD, 0.28f, 0.15f, false),
    AFTERNOON("Tarde", 0xFF60A5FA, 0xFFFDE047, 0.68f, 0.28f, false),
    SUNSET("Atardecer", 0xFF7C3AED, 0xFFEA580C, 0.84f, 0.50f, false),
    NIGHT("Noche", 0xFF0F172A, 0xFF1E1B4B, 0.15f, 0.22f, true);

    companion object {
        fun normalizeHour(hour: Int): Int {
            if (hour in 1..12) return hour
            val mod = ((hour % 12) + 12) % 12
            return if (mod == 0) 12 else mod
        }

        fun fromHour(hour: Int): ClockTimeOfDay {
            val h = normalizeHour(hour)
            return when (h) {
                7, 8 -> MORNING
                10, 11, 12, 1 -> NOON
                2, 3, 4 -> AFTERNOON
                5, 6 -> SUNSET
                else -> NIGHT
            }
        }
    }
}

data class ClockRoundData(
    val roundNumber: Int,
    val targetHour: Int,
    val timeOfDay: ClockTimeOfDay,
    val visualItemId: String,
    val emojiScene: String,
    val spokenPrompt: String,
    val spokenWin: String
) {
    val isNight: Boolean get() = timeOfDay.isNight
}

data class InteractiveClockUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val targetHour: Int = 8,
    val selectedHour: Int = 12,
    val currentRoundData: ClockRoundData? = null,
    val timeOfDay: ClockTimeOfDay = ClockTimeOfDay.MORNING,
    val starsEarned: Int = 0,
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val showRewardDialog: Boolean = false,
    val promptSpeech: String = "",
    val activeUserId: String = "jugador_invitado",
    val score: Int = 0,
    val attempts: Int = 0
) {
    val currentChallenge: ClockRoundData
        get() = currentRoundData ?: ClockRoundData(
            roundNumber = 1,
            targetHour = 8,
            timeOfDay = ClockTimeOfDay.MORNING,
            visualItemId = "milk",
            emojiScene = "☀️ 🥛",
            spokenPrompt = promptSpeech,
            spokenWin = ""
        )
}

class InteractiveClockViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    val totalRounds: Int = 3

    companion object {
        val ALL_ROUTINES = listOf(
            ClockRoundData(1, 7, ClockTimeOfDay.MORNING, "toothbrush", "🌅 🪥", "¡Son las 7 de la mañana! Pon el reloj en el número 7 para cepillarnos los dientes.", "¡Muy bien! ¡A las siete nos cepillamos los dientes!"),
            ClockRoundData(1, 8, ClockTimeOfDay.MORNING, "milk", "☀️ 🥛", "¡Es de mañana! Mueve la manecilla al número 8 para la hora del desayuno.", "¡Muy bien! ¡Son las ocho en punto!"),
            ClockRoundData(1, 10, ClockTimeOfDay.NOON, "apple_fruit", "🌤️ 🍎", "¡Son las 10 de la mañana! Pon el reloj en el número 10 para comer una fruta.", "¡Excelente! ¡A las diez comemos fruta fresca!"),
            ClockRoundData(2, 12, ClockTimeOfDay.NOON, "plate", "🌞 🍽️", "¡Es el mediodía! Pon el reloj en el número 12 para la hora del almuerzo.", "¡Excelente! ¡Son las doce en punto!"),
            ClockRoundData(2, 1, ClockTimeOfDay.NOON, "storybook", "☀️ 📖", "¡Es la 1 de la tarde! Mueve la manecilla al número 1 para leer un cuento.", "¡Genial! ¡Es la una en punto!"),
            ClockRoundData(2, 3, ClockTimeOfDay.AFTERNOON, "ball", "🌤️ ⚽", "¡Son las 3 de la tarde! Pon el reloj en el número 3 para salir a jugar con la pelota.", "¡Súper! ¡Son las tres de la tarde!"),
            ClockRoundData(2, 4, ClockTimeOfDay.AFTERNOON, "toy_car", "🌤️ 🚗", "¡Son las 4 de la tarde! Mueve el reloj al número 4 para jugar con los carritos.", "¡Fantástico! ¡Son las cuatro en punto!"),
            ClockRoundData(3, 6, ClockTimeOfDay.SUNSET, "mug", "🌇 ☕", "¡Llegó el atardecer! Pon el reloj en el número 6 para tomar chocolate caliente.", "¡Bravo! ¡Son las seis de la tarde!"),
            ClockRoundData(3, 8, ClockTimeOfDay.NIGHT, "pillow", "🌙 ⭐", "¡Llegó la noche! Mueve el reloj al número 8 para la hora de dormir bajo las estrellas.", "¡Bravo! ¡Son las ocho de la noche!"),
            ClockRoundData(3, 9, ClockTimeOfDay.NIGHT, "teddy", "🌌 🧸", "¡Son las 9 de la noche! Pon el reloj en el número 9 para abrazar al osito y soñar.", "¡Dulces sueños! ¡Son las nueve de la noche!")
        )

        fun defaultRounds(): List<ClockRoundData> = listOf(
            ClockRoundData(
                roundNumber = 1,
                targetHour = 8,
                timeOfDay = ClockTimeOfDay.MORNING,
                visualItemId = "milk",
                emojiScene = "☀️ 🥛",
                spokenPrompt = "¡Es de mañana! Mueve la manecilla al número 8 para la hora del desayuno.",
                spokenWin = "¡Muy bien! ¡Son las ocho en punto y ganaste tu primera estrella!"
            ),
            ClockRoundData(
                roundNumber = 2,
                targetHour = 12,
                timeOfDay = ClockTimeOfDay.NOON,
                visualItemId = "plate",
                emojiScene = "🌞 🍽️",
                spokenPrompt = "¡Es el mediodía! Pon el reloj en el número 12 para la hora del almuerzo.",
                spokenWin = "¡Excelente! ¡Son las doce en punto y ganaste dos estrellas!"
            ),
            ClockRoundData(
                roundNumber = 3,
                targetHour = 8,
                timeOfDay = ClockTimeOfDay.NIGHT,
                visualItemId = "pillow",
                emojiScene = "🌙 ⭐",
                spokenPrompt = "¡Llegó la noche! Mueve el reloj al número 8 para la hora de dormir bajo las estrellas.",
                spokenWin = "¡Bravo! ¡Son las ocho de la noche, completaste el reloj y ganaste 3 estrellas!"
            )
        )

        fun generateRandomRounds(random: Random = Random.Default): List<ClockRoundData> {
            val morningPool = ALL_ROUTINES.filter { it.timeOfDay == ClockTimeOfDay.MORNING || it.targetHour == 10 }
            val dayPool = ALL_ROUTINES.filter { it.timeOfDay == ClockTimeOfDay.NOON || it.timeOfDay == ClockTimeOfDay.AFTERNOON }
            val eveningPool = ALL_ROUTINES.filter { it.timeOfDay == ClockTimeOfDay.SUNSET || it.timeOfDay == ClockTimeOfDay.NIGHT }

            val r1 = morningPool.random(random).copy(roundNumber = 1)
            val r2 = dayPool.filter { it.targetHour != r1.targetHour }.ifEmpty { dayPool }.random(random).copy(roundNumber = 2)
            val r3 = eveningPool.filter { it.targetHour != r2.targetHour }.ifEmpty { eveningPool }.random(random).copy(
                roundNumber = 3,
                spokenWin = "¡Bravo! ¡Completaste todas las horas del reloj y ganaste las 3 estrellas!"
            )
            return listOf(r1, r2, r3)
        }
    }

    private var rounds: List<ClockRoundData> = defaultRounds()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _starsEarned = MutableStateFlow(0)
    val starsEarned: StateFlow<Int> = _starsEarned.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<InteractiveClockUiState> = _uiState.asStateFlow()

    private fun createInitialState(): InteractiveClockUiState {
        val r1 = rounds[0]
        val startHour = if (r1.targetHour == 12) 6 else 12
        return InteractiveClockUiState(
            currentRound = 1,
            totalRounds = 3,
            targetHour = r1.targetHour,
            selectedHour = startHour,
            currentRoundData = r1,
            timeOfDay = r1.timeOfDay,
            starsEarned = 0,
            isRoundCompleted = false,
            isCompleted = false,
            celebrationActive = false,
            showRewardDialog = false,
            promptSpeech = r1.spokenPrompt,
            score = 0,
            attempts = 0
        )
    }

    fun setActiveUserId(userId: String) {
        _uiState.update { it.copy(activeUserId = userId) }
    }

    fun setHour(hour: Int): Boolean {
        val normalizedHour = ClockTimeOfDay.normalizeHour(hour)
        val dynamicSky = ClockTimeOfDay.fromHour(normalizedHour)

        val state = _uiState.value
        if (state.isCompleted) {
            _uiState.update { it.copy(selectedHour = normalizedHour, timeOfDay = dynamicSky) }
            return false
        }

        val target = state.targetHour
        val isMatch = normalizedHour == target

        if (isMatch) {
            val isFinalRound = state.currentRound >= totalRounds
            val updatedStars = maxOf(state.starsEarned, state.currentRound)

            _starsEarned.value = updatedStars
            _isCompleted.value = isFinalRound
            _celebrationActive.value = isFinalRound

            _uiState.update {
                it.copy(
                    selectedHour = normalizedHour,
                    timeOfDay = state.currentRoundData?.timeOfDay ?: dynamicSky,
                    starsEarned = updatedStars,
                    isRoundCompleted = true,
                    isCompleted = isFinalRound,
                    celebrationActive = isFinalRound,
                    showRewardDialog = isFinalRound,
                    score = it.score + 25
                )
            }

            if (isFinalRound) {
                saveWinProgress()
            }
            return true
        } else {
            _uiState.update {
                it.copy(
                    selectedHour = normalizedHour,
                    timeOfDay = dynamicSky,
                    attempts = it.attempts + 1
                )
            }
            return false
        }
    }

    fun verifyRoutine(): Boolean {
        val state = _uiState.value
        if (state.isCompleted) return true
        if (state.isRoundCompleted && state.selectedHour == state.targetHour) return true

        return setHour(state.selectedHour)
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false
        val nextRoundNum = state.currentRound + 1
        val roundData = rounds.getOrNull(nextRoundNum - 1) ?: return false

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                targetHour = roundData.targetHour,
                selectedHour = 12,
                currentRoundData = roundData,
                timeOfDay = roundData.timeOfDay,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                showRewardDialog = false,
                promptSpeech = roundData.spokenPrompt
            )
        }
        return true
    }

    fun dismissRewardDialog() {
        _celebrationActive.value = false
        _uiState.update { it.copy(showRewardDialog = false, celebrationActive = false) }
    }

    fun dismissCelebration() {
        _celebrationActive.value = false
        _uiState.update { it.copy(celebrationActive = false) }
    }

    fun resetGame(randomize: Boolean = false) {
        val userId = _uiState.value.activeUserId
        rounds = if (randomize) generateRandomRounds() else defaultRounds()
        _currentRound.value = 1
        _starsEarned.value = 0
        _isCompleted.value = false
        _celebrationActive.value = false
        _uiState.value = createInitialState().copy(activeUserId = userId)
    }

    private fun saveWinProgress() {
        val scope = customScope ?: try {
            viewModelScope
        } catch (_: Throwable) {
            null
        } ?: return

        try {
            scope.launch(Dispatchers.IO) {
                try {
                    val repo = progressRepo ?: FirebaseProgressRepository()
                    val cur = repo.getProgress(_uiState.value.activeUserId) ?: UserProgress(userId = _uiState.value.activeUserId)
                    repo.saveProgress(
                        cur.copy(
                            objectGamesCompleted = cur.objectGamesCompleted + 1,
                            totalScore = cur.totalScore + 60,
                            totalStars = cur.totalStars + 3,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } catch (_: Throwable) {
                    // Safe offline fallback
                }
            }
        } catch (_: Throwable) {
            // Safe JVM test environment
        }
    }
}
