package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.ProgressRepository
import com.example.no_definido.data.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class TreeAppleSlot(
    val id: Int,
    val relX: Float, // Posición relativa en la copa del árbol (0.0 .. 1.0)
    val relY: Float,
    val isCollected: Boolean = false
)

data class AppleOrchardRound(
    val roundNumber: Int,
    val targetCount: Int,
    val slots: List<TreeAppleSlot>,
    val promptSpeech: String,
    val completionSpeech: String
)

data class AppleBasketUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val targetCount: Int = 3,
    val currentCount: Int = 0,
    val starsEarned: Int = 0,
    val treeApples: List<TreeAppleSlot> = emptyList(),
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val showRewardDialog: Boolean = false,
    val lastCountedNumber: Int? = null,
    val promptSpeech: String = "",
    val activeUserId: String = "jugador_invitado"
)

class AppleBasketViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    companion object {
        // Pool de 10 posiciones bien separadas (>= 0.12f de distancia mínima) en la copa del árbol
        val CANOPY_POOL = listOf(
            Pair(0.36f, 0.24f),
            Pair(0.49f, 0.14f),
            Pair(0.63f, 0.15f),
            Pair(0.73f, 0.25f),
            Pair(0.45f, 0.34f),
            Pair(0.58f, 0.28f),
            Pair(0.68f, 0.39f),
            Pair(0.34f, 0.38f),
            Pair(0.53f, 0.41f)
        )

        fun defaultRounds(): List<AppleOrchardRound> = listOf(
            AppleOrchardRound(
                roundNumber = 1,
                targetCount = 3,
                slots = listOf(
                    TreeAppleSlot(id = 0, relX = 0.44f, relY = 0.24f),
                    TreeAppleSlot(id = 1, relX = 0.55f, relY = 0.19f),
                    TreeAppleSlot(id = 2, relX = 0.64f, relY = 0.29f)
                ),
                promptSpeech = "¡Vamos a recolectar manzanas! Pon 3 manzanas en la canasta.",
                completionSpeech = "¡Muy bien! Contaste 3 manzanas y ganaste una estrella."
            ),
            AppleOrchardRound(
                roundNumber = 2,
                targetCount = 5,
                slots = listOf(
                    TreeAppleSlot(id = 0, relX = 0.42f, relY = 0.22f),
                    TreeAppleSlot(id = 1, relX = 0.53f, relY = 0.16f),
                    TreeAppleSlot(id = 2, relX = 0.65f, relY = 0.22f),
                    TreeAppleSlot(id = 3, relX = 0.46f, relY = 0.35f),
                    TreeAppleSlot(id = 4, relX = 0.58f, relY = 0.38f)
                ),
                promptSpeech = "¡Excelente! Ahora pon 5 manzanas en la canasta.",
                completionSpeech = "¡Genial! Contaste 5 manzanas y ganaste dos estrellas."
            ),
            AppleOrchardRound(
                roundNumber = 3,
                targetCount = 7,
                slots = listOf(
                    TreeAppleSlot(id = 0, relX = 0.40f, relY = 0.22f),
                    TreeAppleSlot(id = 1, relX = 0.50f, relY = 0.14f),
                    TreeAppleSlot(id = 2, relX = 0.63f, relY = 0.16f),
                    TreeAppleSlot(id = 3, relX = 0.71f, relY = 0.26f),
                    TreeAppleSlot(id = 4, relX = 0.46f, relY = 0.33f),
                    TreeAppleSlot(id = 5, relX = 0.58f, relY = 0.28f),
                    TreeAppleSlot(id = 6, relX = 0.64f, relY = 0.39f)
                ),
                promptSpeech = "¡Último reto! Pon 7 manzanas en la canasta.",
                completionSpeech = "¡Bravo! Contaste todas las manzanas del huerto y ganaste 3 estrellas."
            )
        )

        fun generateRandomRounds(random: Random = Random.Default): List<AppleOrchardRound> {
            val r1Count = listOf(2, 3, 4).random(random)
            val r2Count = listOf(5, 6).random(random)
            val r3Count = listOf(7, 8).random(random)

            fun pickSlots(count: Int): List<TreeAppleSlot> {
                return CANOPY_POOL.shuffled(random).take(count).mapIndexed { index, (x, y) ->
                    TreeAppleSlot(id = index, relX = x, relY = y)
                }
            }

            return listOf(
                AppleOrchardRound(
                    roundNumber = 1,
                    targetCount = r1Count,
                    slots = pickSlots(r1Count),
                    promptSpeech = "¡Vamos a recolectar manzanas! Pon $r1Count manzanas en la canasta.",
                    completionSpeech = "¡Muy bien! Contaste $r1Count manzanas y ganaste una estrella."
                ),
                AppleOrchardRound(
                    roundNumber = 2,
                    targetCount = r2Count,
                    slots = pickSlots(r2Count),
                    promptSpeech = "¡Excelente! Ahora pon $r2Count manzanas en la canasta.",
                    completionSpeech = "¡Genial! Contaste $r2Count manzanas y ganaste dos estrellas."
                ),
                AppleOrchardRound(
                    roundNumber = 3,
                    targetCount = r3Count,
                    slots = pickSlots(r3Count),
                    promptSpeech = "¡Último reto! Pon $r3Count manzanas en la canasta.",
                    completionSpeech = "¡Bravo! Contaste las $r3Count manzanas del huerto y ganaste 3 estrellas."
                )
            )
        }
    }

    private var rounds: List<AppleOrchardRound> = defaultRounds()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<AppleBasketUiState> = _uiState.asStateFlow()

    // Mini-Game ViewModel Contract
    val totalRounds: Int = 3
    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _starsEarned = MutableStateFlow(0)
    val starsEarned: StateFlow<Int> = _starsEarned.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    private fun createInitialState(): AppleBasketUiState {
        val r1 = rounds[0]
        return AppleBasketUiState(
            currentRound = 1,
            totalRounds = 3,
            targetCount = r1.targetCount,
            currentCount = 0,
            starsEarned = 0,
            treeApples = r1.slots,
            isRoundCompleted = false,
            isCompleted = false,
            celebrationActive = false,
            showRewardDialog = false,
            lastCountedNumber = null,
            promptSpeech = r1.promptSpeech
        )
    }

    fun setActiveUserId(userId: String) {
        _uiState.update { it.copy(activeUserId = userId) }
    }

    fun dropApple(appleId: Int): Int? = collectApple(appleId)

    fun collectApple(appleId: Int): Int? {
        val state = _uiState.value
        val targetApple = state.treeApples.find { it.id == appleId } ?: return null
        if (targetApple.isCollected || state.isRoundCompleted || state.isCompleted) return null

        val updatedApples = state.treeApples.map {
            if (it.id == appleId) it.copy(isCollected = true) else it
        }
        val newCount = updatedApples.count { it.isCollected }
        val isRoundFinished = newCount >= state.targetCount
        val isFinalRound = state.currentRound >= totalRounds
        val isGameFinished = isRoundFinished && isFinalRound
        val updatedStars = if (isRoundFinished) maxOf(state.starsEarned, state.currentRound) else state.starsEarned

        _starsEarned.value = updatedStars
        _isCompleted.value = isGameFinished
        _celebrationActive.value = isGameFinished

        _uiState.update {
            it.copy(
                treeApples = updatedApples,
                currentCount = newCount,
                lastCountedNumber = newCount,
                starsEarned = updatedStars,
                isRoundCompleted = isRoundFinished,
                isCompleted = isGameFinished,
                celebrationActive = isGameFinished,
                showRewardDialog = isGameFinished
            )
        }

        if (isGameFinished) {
            saveWinProgress()
        }

        return newCount
    }

    fun addApple() {
        val nextUncollected = _uiState.value.treeApples.firstOrNull { !it.isCollected }
        if (nextUncollected != null) {
            collectApple(nextUncollected.id)
        }
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false

        val nextRoundNum = state.currentRound + 1
        val nextRoundData = rounds.getOrNull(nextRoundNum - 1) ?: return false

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                targetCount = nextRoundData.targetCount,
                currentCount = 0,
                treeApples = nextRoundData.slots,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                showRewardDialog = false,
                lastCountedNumber = null,
                promptSpeech = nextRoundData.promptSpeech
            )
        }
        return true
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
                    val current = repo.getProgress(_uiState.value.activeUserId) ?: UserProgress(userId = _uiState.value.activeUserId)
                    repo.saveProgress(
                        current.copy(
                            applesCollectedTotal = current.applesCollectedTotal + 15,
                            objectGamesCompleted = current.objectGamesCompleted + 1,
                            totalScore = current.totalScore + 60,
                            totalStars = current.totalStars + 3,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } catch (_: Throwable) {
                    // Modo offline
                }
            }
        } catch (_: Throwable) {
            // Manejo seguro en entorno JVM
        }
    }

    fun dismissRewardDialog() {
        _celebrationActive.value = false
        _uiState.update { it.copy(showRewardDialog = false, celebrationActive = false) }
    }

    fun dismissCelebration() {
        dismissRewardDialog()
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
}
