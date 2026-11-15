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

enum class TrainAnimationState {
    ARRIVING,      // El tren llega deslizándose desde la derecha (+screenWidth -> 0f)
    IDLE,          // Estacionado en la pradera mexica; interacción táctil activa
    DEPARTING,     // El tren sale acelerando hacia la izquierda (0f -> -screenWidth)
    COMPLETED      // Todas las rondas completadas con celebración activa
}

data class TrainCargoItem(
    val id: String,
    val spokenName: String,
    val spokenPlural: String,
    val spokenTouchPrompt: String,
    val spokenSuccess: String
)

data class TrainRoundData(
    val roundNumber: Int,
    val targetCount: Int,
    val cargoItem: TrainCargoItem,
    val targetStars: Int,
    val promptSpeech: String,
    val completionSpeech: String
)

sealed class TrainDropResult {
    data class Loaded(
        val count: Int,
        val countSpokenWord: String,
        val isRoundFinished: Boolean = false,
        val isGameFinished: Boolean = false
    ) : TrainDropResult()

    object Ignored : TrainDropResult()
    object AlreadyLoadedOrFull : TrainDropResult()

    val isSuccess: Boolean get() = this is Loaded
}

data class NumberTrainUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val targetCount: Int = 3,
    val loadedCount: Int = 0,
    val starsEarned: Int = 0,
    val currentCargoItem: TrainCargoItem,
    val currentRoundData: TrainRoundData,
    val trainAnimationState: TrainAnimationState = TrainAnimationState.IDLE,
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val promptSpeech: String = "",
    val activeUserId: String = "jugador_invitado",
    val score: Int = 0
) {
    val isWagonFull: Boolean get() = loadedCount >= targetCount
    val remainingToLoad: Int get() = (targetCount - loadedCount).coerceAtLeast(0)
}

class NumberTrainViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    companion object {
        val SPANISH_COUNT_WORDS = listOf(
            "", "¡Uno!", "¡Dos!", "¡Tres!", "¡Cuatro!",
            "¡Cinco!", "¡Seis!", "¡Siete!", "¡Ocho!"
        )

        val ALL_CARGO_ITEMS: List<TrainCargoItem> = listOf(
            TrainCargoItem(
                id = "corn",
                spokenName = "la mazorca de maíz",
                spokenPlural = "mazorcas de maíz dorado",
                spokenTouchPrompt = "Mazorca de maíz",
                spokenSuccess = "¡Mazorca dorada al vagón!"
            ),
            TrainCargoItem(
                id = "clay_pot",
                spokenName = "la vasija de barro",
                spokenPlural = "vasijas de barro",
                spokenTouchPrompt = "Vasija de barro",
                spokenSuccess = "¡Vasija artesanal al vagón!"
            ),
            TrainCargoItem(
                id = "jade_stone",
                spokenName = "la gema de jade",
                spokenPlural = "gemas de jade",
                spokenTouchPrompt = "Piedra de jade",
                spokenSuccess = "¡Gema de jade brillante al vagón!"
            ),
            TrainCargoItem(
                id = "cacao",
                spokenName = "la mazorca de cacao",
                spokenPlural = "mazorcas de cacao real",
                spokenTouchPrompt = "Cacao real",
                spokenSuccess = "¡Cacao aromático al vagón!"
            ),
            TrainCargoItem(
                id = "sunflower",
                spokenName = "el girasol dorado",
                spokenPlural = "girasoles dorados",
                spokenTouchPrompt = "Girasol dorado",
                spokenSuccess = "¡Girasol radiante al vagón!"
            )
        )

        val ROUNDS: List<TrainRoundData> = listOf(
            TrainRoundData(
                roundNumber = 1,
                targetCount = 3,
                cargoItem = ALL_CARGO_ITEMS[0],
                targetStars = 1,
                promptSpeech = "¡Sube 3 mazorcas de maíz dorado al vagón del tren!",
                completionSpeech = "¡El tren está lleno! ¡Buen viaje! ¡Chu chuuu! ¡Ganaste tu primera estrella!"
            ),
            TrainRoundData(
                roundNumber = 2,
                targetCount = 5,
                cargoItem = ALL_CARGO_ITEMS[1],
                targetStars = 2,
                promptSpeech = "¡Ronda dos! Carga 5 vasijas de barro en el tren.",
                completionSpeech = "¡Vagón completo! ¡El tren parte con las 5 vasijas! ¡Chu chuuu! ¡Dos estrellas!"
            ),
            TrainRoundData(
                roundNumber = 3,
                targetCount = 8,
                cargoItem = ALL_CARGO_ITEMS[2],
                targetStars = 3,
                promptSpeech = "¡Último reto! Sube 8 gemas de jade relucientes al tren.",
                completionSpeech = "¡Bravo! ¡El tren de jade está completo! ¡Buen viaje por el Anáhuac! ¡Cargaste todos los trenes y ganaste las 3 estrellas doradas!"
            )
        )

        fun generateRandomRounds(): List<TrainRoundData> {
            val selectedCargo = ALL_CARGO_ITEMS.shuffled().take(3)
            val c1 = listOf(2, 3, 4).random()
            val c2 = listOf(4, 5, 6).filter { it > c1 }.random()
            val c3 = listOf(6, 7, 8).filter { it > c2 }.random()
            val counts = listOf(c1, c2, c3)

            return counts.mapIndexed { idx, targetCount ->
                val roundNum = idx + 1
                val cargo = selectedCargo[idx]
                val prompt = when (roundNum) {
                    1 -> "¡Sube $targetCount ${cargo.spokenPlural} al vagón del tren!"
                    2 -> "¡Ronda dos! Carga $targetCount ${cargo.spokenPlural} en el tren."
                    else -> "¡Último reto! Sube $targetCount ${cargo.spokenPlural} al tren."
                }
                val completion = when (roundNum) {
                    1 -> "¡El tren está lleno con $targetCount ${cargo.spokenPlural}! ¡Chu chuuu! ¡Ganaste tu primera estrella!"
                    2 -> "¡Vagón completo! ¡El tren parte con $targetCount ${cargo.spokenPlural}! ¡Dos estrellas!"
                    else -> "¡Bravo! ¡Cargaste las $targetCount ${cargo.spokenPlural} y ganaste las 3 estrellas doradas!"
                }
                TrainRoundData(
                    roundNumber = roundNum,
                    targetCount = targetCount,
                    cargoItem = cargo,
                    targetStars = roundNum,
                    promptSpeech = prompt,
                    completionSpeech = completion
                )
            }
        }

        private fun createInitialState(rounds: List<TrainRoundData> = ROUNDS): NumberTrainUiState {
            val r1 = rounds[0]
            return NumberTrainUiState(
                currentRound = 1,
                totalRounds = rounds.size,
                targetCount = r1.targetCount,
                loadedCount = 0,
                starsEarned = 0,
                currentCargoItem = r1.cargoItem,
                currentRoundData = r1,
                trainAnimationState = TrainAnimationState.IDLE,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = r1.promptSpeech,
                score = 0
            )
        }
    }

    private var activeRounds: List<TrainRoundData> = ROUNDS

    private val _uiState = MutableStateFlow(createInitialState(activeRounds))
    val uiState: StateFlow<NumberTrainUiState> = _uiState.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    val totalRounds: Int = 3

    private val _starsEarned = MutableStateFlow(0)
    val starsEarned: StateFlow<Int> = _starsEarned.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    private val _trainAnimationState = MutableStateFlow(TrainAnimationState.IDLE)
    val trainAnimationState: StateFlow<TrainAnimationState> = _trainAnimationState.asStateFlow()

    fun setActiveUserId(userId: String) {
        _uiState.update { it.copy(activeUserId = userId) }
    }

    fun loadCargo(itemId: String? = null): TrainDropResult {
        val state = _uiState.value

        // Guardas: juego ya completado o vagón lleno
        if (state.isCompleted) return TrainDropResult.Ignored
        if (state.loadedCount >= state.targetCount) return TrainDropResult.Ignored

        // Si se provee itemId, debe coincidir con el objeto de la ronda actual
        if (itemId != null && itemId != state.currentCargoItem.id) {
            return TrainDropResult.Ignored
        }

        val nextLoadedCount = state.loadedCount + 1
        val isRoundFinished = nextLoadedCount >= state.targetCount
        val isFinalRound = state.currentRound >= totalRounds
        val isGameFinished = isRoundFinished && isFinalRound
        val updatedStars = if (isRoundFinished) maxOf(state.starsEarned, state.currentRoundData.targetStars) else state.starsEarned
        val countWord = SPANISH_COUNT_WORDS.getOrElse(nextLoadedCount) { "$nextLoadedCount" }

        _starsEarned.value = updatedStars

        if (isGameFinished) {
            _isCompleted.value = true
            _celebrationActive.value = true
            _trainAnimationState.value = TrainAnimationState.COMPLETED
            saveWinProgress()
        } else if (isRoundFinished) {
            _trainAnimationState.value = TrainAnimationState.DEPARTING
        }

        _uiState.update {
            it.copy(
                loadedCount = nextLoadedCount,
                starsEarned = updatedStars,
                isRoundCompleted = isRoundFinished,
                isCompleted = isGameFinished,
                celebrationActive = isGameFinished,
                score = it.score + 20,
                trainAnimationState = if (isGameFinished) TrainAnimationState.COMPLETED else if (isRoundFinished) TrainAnimationState.DEPARTING else it.trainAnimationState
            )
        }

        return TrainDropResult.Loaded(
            count = nextLoadedCount,
            countSpokenWord = countWord,
            isRoundFinished = isRoundFinished,
            isGameFinished = isGameFinished
        )
    }

    fun loadCargoItem(itemId: String): Boolean {
        return loadCargo(itemId) is TrainDropResult.Loaded
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false

        val nextRoundNum = state.currentRound + 1
        val nextRoundData = activeRounds.getOrNull(nextRoundNum - 1) ?: return false

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false
        _trainAnimationState.value = TrainAnimationState.ARRIVING

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                targetCount = nextRoundData.targetCount,
                loadedCount = 0,
                currentCargoItem = nextRoundData.cargoItem,
                currentRoundData = nextRoundData,
                trainAnimationState = TrainAnimationState.ARRIVING,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = nextRoundData.promptSpeech
            )
        }
        return true
    }

    fun onArrivalComplete() {
        _trainAnimationState.value = TrainAnimationState.IDLE
        _uiState.update { it.copy(trainAnimationState = TrainAnimationState.IDLE) }
    }

    fun dismissCelebration() {
        _celebrationActive.value = false
        _uiState.update { it.copy(celebrationActive = false) }
    }

    fun resetGame(randomize: Boolean = false) {
        val userId = _uiState.value.activeUserId
        activeRounds = if (randomize) generateRandomRounds() else ROUNDS
        _currentRound.value = 1
        _starsEarned.value = 0
        _isCompleted.value = false
        _celebrationActive.value = false
        _trainAnimationState.value = TrainAnimationState.IDLE
        _uiState.value = createInitialState(activeRounds).copy(activeUserId = userId)
    }

    fun getCurrentHintSpeech(): String {
        val state = _uiState.value
        return "El tren necesita ${state.targetCount} ${state.currentCargoItem.spokenPlural}. Llevas ${state.loadedCount}. ¡Faltan ${state.remainingToLoad}!"
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
                    // Modo offline seguro
                }
            }
        } catch (_: Throwable) {
            // Protección de entorno JVM
        }
    }
}
