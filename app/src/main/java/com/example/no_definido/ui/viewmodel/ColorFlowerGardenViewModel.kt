package com.example.no_definido.ui.viewmodel

import androidx.compose.ui.graphics.Color
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

enum class GardenShape {
    CIRCLE,
    SQUARE,
    TRIANGLE,
    DIAMOND,
    STAR,
    OVAL,
    HEART,
    PENTAGON
}

data class FlowerShapeItem(
    val id: String,
    val shape: GardenShape,
    val colorHex: Long,
    val darkBorderHex: Long,
    val nameEs: String,
    val spokenName: String,
    val spokenSuccess: String,
    val hintSpeech: String
) {
    val color: Color get() = Color(colorHex)
    val darkBorder: Color get() = Color(darkBorderHex)
}

data class FlowerGardenRound(
    val roundNumber: Int,
    val targetStars: Int,
    val items: List<FlowerShapeItem>,
    val promptSpeech: String,
    val completionSpeech: String
) {
    val totalCount: Int get() = items.size
}

sealed class FlowerMatchResult {
    data class Correct(
        val flower: FlowerShapeItem,
        val potId: String,
        val isRoundFinished: Boolean = false,
        val isGameFinished: Boolean = false,
        val currentStars: Int = 0,
        val remainingCount: Int = 0
    ) : FlowerMatchResult()

    data class Mismatch(
        val flower: FlowerShapeItem,
        val targetPotId: String,
        val expectedPotId: String,
        val hintSpeech: String
    ) : FlowerMatchResult()

    object Ignored : FlowerMatchResult()

    val isSuccess: Boolean get() = this is Correct
}

data class ColorFlowerGardenUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val starsEarned: Int = 0,
    val totalFlowersInRound: Int = 3,
    val remainingFlowers: List<FlowerShapeItem> = emptyList(),
    val matchedFlowers: List<FlowerShapeItem> = emptyList(),
    val activePots: List<FlowerShapeItem> = emptyList(),
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val promptSpeech: String = "",
    val activeUserId: String = "jugador_invitado",
    val score: Int = 0,
    val attempts: Int = 0,
    val lastMatchedFlowerId: String? = null
) {
    val remainingTrayFlowers: List<FlowerShapeItem> get() = remainingFlowers
    val matchedFlowerIds: Set<String> get() = matchedFlowers.map { it.id }.toSet()
}

class ColorFlowerGardenViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    companion object {
        val ITEM_RED_CIRCLE = FlowerShapeItem(
            id = "red_circle",
            shape = GardenShape.CIRCLE,
            colorHex = 0xFFE53935,
            darkBorderHex = 0xFFB71C1C,
            nameEs = "el círculo rojo",
            spokenName = "Círculo rojo",
            spokenSuccess = "¡Muy bien! ¡El círculo rojo en la flor roja!",
            hintSpeech = "Busca la maceta con el círculo rojo."
        )

        val ITEM_BLUE_SQUARE = FlowerShapeItem(
            id = "blue_square",
            shape = GardenShape.SQUARE,
            colorHex = 0xFF1E88E5,
            darkBorderHex = 0xFF0D47A1,
            nameEs = "el cuadrado azul",
            spokenName = "Cuadrado azul",
            spokenSuccess = "¡Genial! ¡El cuadrado azul en la flor azul!",
            hintSpeech = "Busca la maceta con el cuadrado azul."
        )

        val ITEM_YELLOW_TRIANGLE = FlowerShapeItem(
            id = "yellow_triangle",
            shape = GardenShape.TRIANGLE,
            colorHex = 0xFFFFB300,
            darkBorderHex = 0xFFE65100,
            nameEs = "el triángulo amarillo",
            spokenName = "Triángulo amarillo",
            spokenSuccess = "¡Súper! ¡El triángulo amarillo en la flor amarilla!",
            hintSpeech = "Busca la maceta con el triángulo amarillo."
        )

        val ITEM_GREEN_DIAMOND = FlowerShapeItem(
            id = "green_diamond",
            shape = GardenShape.DIAMOND,
            colorHex = 0xFF43A047,
            darkBorderHex = 0xFF1B5E20,
            nameEs = "el rombo verde",
            spokenName = "Rombo verde",
            spokenSuccess = "¡Excelente! ¡El rombo verde en la flor verde!",
            hintSpeech = "Busca la maceta con el rombo verde."
        )

        val ITEM_ORANGE_STAR = FlowerShapeItem(
            id = "orange_star",
            shape = GardenShape.STAR,
            colorHex = 0xFFFB8C00,
            darkBorderHex = 0xFFBF360C,
            nameEs = "la estrella naranja",
            spokenName = "Estrella naranja",
            spokenSuccess = "¡Fantástico! ¡La estrella naranja en la flor naranja!",
            hintSpeech = "Busca la maceta con la estrella naranja."
        )

        val ITEM_PURPLE_OVAL = FlowerShapeItem(
            id = "purple_oval",
            shape = GardenShape.OVAL,
            colorHex = 0xFF8E24AA,
            darkBorderHex = 0xFF4A148C,
            nameEs = "el óvalo morado",
            spokenName = "Óvalo morado",
            spokenSuccess = "¡Maravilloso! ¡El óvalo morado en la flor morada!",
            hintSpeech = "Busca la maceta con el óvalo morado."
        )

        val ITEM_PINK_HEART = FlowerShapeItem(
            id = "pink_heart",
            shape = GardenShape.HEART,
            colorHex = 0xFFEC407A,
            darkBorderHex = 0xFF880E4F,
            nameEs = "el corazón rosa",
            spokenName = "Corazón rosa",
            spokenSuccess = "¡Hermoso! ¡El corazón rosa en la flor rosa!",
            hintSpeech = "Busca la maceta con el corazón rosa."
        )

        val ITEM_CYAN_PENTAGON = FlowerShapeItem(
            id = "cyan_pentagon",
            shape = GardenShape.PENTAGON,
            colorHex = 0xFF00ACC1,
            darkBorderHex = 0xFF006064,
            nameEs = "el pentágono cian",
            spokenName = "Pentágono cian",
            spokenSuccess = "¡Increíble! ¡El pentágono cian en la flor cian!",
            hintSpeech = "Busca la maceta con el pentágono cian."
        )

        val ALL_FLOWERS = listOf(
            ITEM_RED_CIRCLE,
            ITEM_BLUE_SQUARE,
            ITEM_YELLOW_TRIANGLE,
            ITEM_GREEN_DIAMOND,
            ITEM_ORANGE_STAR,
            ITEM_PURPLE_OVAL,
            ITEM_PINK_HEART,
            ITEM_CYAN_PENTAGON
        )

        val ROUNDS: List<FlowerGardenRound> = listOf(
            FlowerGardenRound(
                roundNumber = 1,
                targetStars = 1,
                items = listOf(ITEM_RED_CIRCLE, ITEM_BLUE_SQUARE, ITEM_YELLOW_TRIANGLE),
                promptSpeech = "¡Coloca cada flor en la maceta con su misma forma y color!",
                completionSpeech = "¡Estupendo! Florecieron las tres macetas. ¡Ganaste tu primera estrella!"
            ),
            FlowerGardenRound(
                roundNumber = 2,
                targetStars = 2,
                items = listOf(ITEM_RED_CIRCLE, ITEM_BLUE_SQUARE, ITEM_YELLOW_TRIANGLE, ITEM_GREEN_DIAMOND, ITEM_ORANGE_STAR),
                promptSpeech = "¡Ronda dos! El jardín florece con cinco macetas de colores. ¡Empareja cada figura con su flor!",
                completionSpeech = "¡Hermoso! Cinco flores abiertas en el jardín. ¡Ganaste dos estrellas!"
            ),
            FlowerGardenRound(
                roundNumber = 3,
                targetStars = 3,
                items = listOf(
                    ITEM_RED_CIRCLE, ITEM_BLUE_SQUARE, ITEM_YELLOW_TRIANGLE, ITEM_GREEN_DIAMOND,
                    ITEM_ORANGE_STAR, ITEM_PURPLE_OVAL, ITEM_PINK_HEART, ITEM_CYAN_PENTAGON
                ),
                promptSpeech = "¡Ronda final! Ocho hermosas flores esperan por ti. ¡Encuentra la forma y color de cada una!",
                completionSpeech = "¡Bravo! ¡Hiciste florecer todo el jardín de Cal-mé-cac! ¡Eres un guardián de la naturaleza! ¡Ganaste las 3 estrellas doradas!"
            )
        )

        fun generateRandomRounds(): List<FlowerGardenRound> {
            val r1Items = ALL_FLOWERS.shuffled().take(3)
            val r2Items = ALL_FLOWERS.shuffled().take(5)
            val r3Items = ALL_FLOWERS.shuffled()
            return listOf(
                FlowerGardenRound(
                    roundNumber = 1,
                    targetStars = 1,
                    items = r1Items,
                    promptSpeech = "¡Coloca cada flor en la maceta con su misma forma y color!",
                    completionSpeech = "¡Estupendo! Florecieron las tres macetas. ¡Ganaste tu primera estrella!"
                ),
                FlowerGardenRound(
                    roundNumber = 2,
                    targetStars = 2,
                    items = r2Items,
                    promptSpeech = "¡Ronda dos! El jardín florece con cinco macetas de colores. ¡Empareja cada figura con su flor!",
                    completionSpeech = "¡Hermoso! Cinco flores abiertas en el jardín. ¡Ganaste dos estrellas!"
                ),
                FlowerGardenRound(
                    roundNumber = 3,
                    targetStars = 3,
                    items = r3Items,
                    promptSpeech = "¡Ronda final! Ocho hermosas flores esperan por ti. ¡Encuentra la forma y color de cada una!",
                    completionSpeech = "¡Bravo! ¡Hiciste florecer todo el jardín de Cal-mé-cac! ¡Eres un guardián de la naturaleza! ¡Ganaste las 3 estrellas doradas!"
                )
            )
        }

        private fun shuffleTrayDistinctFromPots(pots: List<FlowerShapeItem>): List<FlowerShapeItem> {
            if (pots.size <= 1) return pots
            var candidate = pots.shuffled()
            var guard = 0
            while (candidate == pots && guard < 6) {
                candidate = pots.shuffled()
                guard++
            }
            return candidate
        }

        private fun createInitialState(
            rounds: List<FlowerGardenRound> = ROUNDS,
            shuffleTray: Boolean = false
        ): ColorFlowerGardenUiState {
            val r1 = rounds[0]
            val trayItems = if (shuffleTray) shuffleTrayDistinctFromPots(r1.items) else r1.items
            return ColorFlowerGardenUiState(
                currentRound = 1,
                totalRounds = rounds.size,
                starsEarned = 0,
                totalFlowersInRound = r1.items.size,
                remainingFlowers = trayItems,
                matchedFlowers = emptyList(),
                activePots = r1.items,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = r1.promptSpeech,
                score = 0,
                attempts = 0,
                lastMatchedFlowerId = null
            )
        }
    }

    private var activeRounds: List<FlowerGardenRound> = ROUNDS
    private var isRandomizedMode: Boolean = false

    private val _uiState = MutableStateFlow(createInitialState(activeRounds, isRandomizedMode))
    val uiState: StateFlow<ColorFlowerGardenUiState> = _uiState.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    val totalRounds: Int = 3

    private val _starsEarned = MutableStateFlow(0)
    val starsEarned: StateFlow<Int> = _starsEarned.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    fun setActiveUserId(userId: String) {
        _uiState.update { it.copy(activeUserId = userId) }
    }

    fun matchFlowerToPot(flowerId: String, potId: String): FlowerMatchResult {
        val state = _uiState.value

        // Guardas
        if (state.isCompleted) return FlowerMatchResult.Ignored

        val flower = state.remainingFlowers.find { it.id == flowerId } ?: return FlowerMatchResult.Ignored
        val targetPot = state.activePots.find { it.id == potId } ?: return FlowerMatchResult.Ignored

        if (flowerId == potId) {
            val newRemaining = state.remainingFlowers.filterNot { it.id == flowerId }
            val newMatched = state.matchedFlowers + flower
            val isRoundFinished = newRemaining.isEmpty()
            val isFinalRound = state.currentRound >= totalRounds
            val isGameFinished = isRoundFinished && isFinalRound
            val updatedStars = if (isRoundFinished) maxOf(state.starsEarned, state.currentRound) else state.starsEarned

            _starsEarned.value = updatedStars

            if (isGameFinished) {
                _isCompleted.value = true
                _celebrationActive.value = true
                saveWinProgress()
            }

            _uiState.update {
                it.copy(
                    remainingFlowers = newRemaining,
                    matchedFlowers = newMatched,
                    starsEarned = updatedStars,
                    isRoundCompleted = isRoundFinished,
                    isCompleted = isGameFinished,
                    celebrationActive = isGameFinished,
                    score = it.score + 20,
                    lastMatchedFlowerId = flowerId
                )
            }

            return FlowerMatchResult.Correct(
                flower = flower,
                potId = potId,
                isRoundFinished = isRoundFinished,
                isGameFinished = isGameFinished,
                currentStars = updatedStars,
                remainingCount = newRemaining.size
            )
        } else {
            _uiState.update { it.copy(attempts = it.attempts + 1) }
            val hint = "¡Casi! Esta maceta tiene ${targetPot.spokenName}. Busca la flor ${flower.spokenName}."
            return FlowerMatchResult.Mismatch(
                flower = flower,
                targetPotId = potId,
                expectedPotId = flower.id,
                hintSpeech = hint
            )
        }
    }

    fun isFlowerMatch(flowerId: String, potId: String): Boolean {
        return matchFlowerToPot(flowerId, potId) is FlowerMatchResult.Correct
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false

        val nextRoundNum = state.currentRound + 1
        val nextRoundData = activeRounds.getOrNull(nextRoundNum - 1) ?: return false
        val trayItems = if (isRandomizedMode) shuffleTrayDistinctFromPots(nextRoundData.items) else nextRoundData.items

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                totalFlowersInRound = nextRoundData.items.size,
                remainingFlowers = trayItems,
                matchedFlowers = emptyList(),
                activePots = nextRoundData.items,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = nextRoundData.promptSpeech,
                lastMatchedFlowerId = null
            )
        }
        return true
    }

    fun dismissCelebration() {
        _celebrationActive.value = false
        _uiState.update { it.copy(celebrationActive = false) }
    }

    fun resetGame(randomize: Boolean = false) {
        val userId = _uiState.value.activeUserId
        isRandomizedMode = randomize
        activeRounds = if (randomize) generateRandomRounds() else ROUNDS
        _currentRound.value = 1
        _starsEarned.value = 0
        _isCompleted.value = false
        _celebrationActive.value = false
        _uiState.value = createInitialState(activeRounds, isRandomizedMode).copy(activeUserId = userId)
    }

    fun getCurrentHintSpeech(): String {
        val state = _uiState.value
        val next = state.remainingFlowers.firstOrNull() ?: return "¡Empareja las flores con sus macetas!"
        return "Busca la maceta con la misma forma y color que ${next.spokenName}."
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
