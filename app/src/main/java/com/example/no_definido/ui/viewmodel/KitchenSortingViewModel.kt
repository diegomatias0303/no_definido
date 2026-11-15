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

enum class KitchenTarget {
    FRIDGE,
    SHELF
}

data class KitchenSortItem(
    val id: String,
    val spokenName: String,
    val target: KitchenTarget,
    val spokenTouchPrompt: String,
    val spokenSuccess: String
)

data class KitchenSortingRound(
    val roundNumber: Int,
    val targetStars: Int,
    val fridgeItems: List<KitchenSortItem>,
    val shelfItems: List<KitchenSortItem>,
    val promptSpeech: String,
    val completionSpeech: String,
    val shuffledTray: List<KitchenSortItem>? = null
) {
    val totalItems: Int get() = fridgeItems.size + shelfItems.size
    val allItems: List<KitchenSortItem> get() = shuffledTray ?: (fridgeItems + shelfItems)
}

sealed class KitchenDropResult {
    data class Correct(
        val item: KitchenSortItem,
        val target: KitchenTarget,
        val isRoundFinished: Boolean = false,
        val isGameFinished: Boolean = false,
        val currentStars: Int = 0,
        val remainingCount: Int = 0
    ) : KitchenDropResult()

    data class Incorrect(
        val item: KitchenSortItem,
        val attemptedTarget: KitchenTarget,
        val expectedTarget: KitchenTarget = attemptedTarget,
        val hintSpeech: String = ""
    ) : KitchenDropResult() {
        val targetAttempted: KitchenTarget get() = attemptedTarget
    }

    object Ignored : KitchenDropResult()
}

data class KitchenSortingUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val starsEarned: Int = 0,
    val roundTotalItems: Int = 4,
    val remainingTrayItems: List<KitchenSortItem> = emptyList(),
    val fridgePlacedItems: List<KitchenSortItem> = emptyList(),
    val shelfPlacedItems: List<KitchenSortItem> = emptyList(),
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val promptSpeech: String = "",
    val activeUserId: String = "jugador_invitado",
    val score: Int = 0,
    val attempts: Int = 0
) {
    val trayItems: List<KitchenSortItem> get() = remainingTrayItems
    val fridgeItemsPlaced: List<KitchenSortItem> get() = fridgePlacedItems
    val shelfItemsPlaced: List<KitchenSortItem> get() = shelfPlacedItems

    val totalItemsInRound: Int
        get() = remainingTrayItems.size + fridgePlacedItems.size + shelfPlacedItems.size

    val placedCount: Int
        get() = fridgePlacedItems.size + shelfPlacedItems.size
}

class KitchenSortingViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    companion object {
        val ALL_FRIDGE_ITEMS = listOf(
            KitchenSortItem(
                id = "meat",
                spokenName = "la carne",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "La carne va en el refrigerador.",
                spokenSuccess = "¡Muy bien! La carne va en el refrigerador."
            ),
            KitchenSortItem(
                id = "chicken",
                spokenName = "el pollo",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "El pollo va en el refrigerador.",
                spokenSuccess = "¡Excelente! El pollo se guarda frío en el refrigerador."
            ),
            KitchenSortItem(
                id = "fish",
                spokenName = "el pescado",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "El pescado va en el refrigerador.",
                spokenSuccess = "¡Súper! El pescado fresco va en el refrigerador."
            ),
            KitchenSortItem(
                id = "cheese",
                spokenName = "el queso",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "El queso va en el refrigerador.",
                spokenSuccess = "¡Eso es! El queso va en el refrigerador."
            ),
            KitchenSortItem(
                id = "milk",
                spokenName = "la leche",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "La leche va en el refrigerador.",
                spokenSuccess = "¡Genial! La leche va en el refrigerador."
            ),
            KitchenSortItem(
                id = "watermelon",
                spokenName = "la sandía fresca",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "La sandía va en el refrigerador.",
                spokenSuccess = "¡Qué rica! La sandía se guarda fría en el refrigerador."
            ),
            KitchenSortItem(
                id = "apple_fruit",
                spokenName = "la manzana roja",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "La manzana va en el refrigerador.",
                spokenSuccess = "¡Súper! La manzana fresca va en el refrigerador."
            ),
            KitchenSortItem(
                id = "eggs",
                spokenName = "los huevos",
                target = KitchenTarget.FRIDGE,
                spokenTouchPrompt = "Los huevos van en el refrigerador.",
                spokenSuccess = "¡Excelente! Los huevos van en el refrigerador."
            )
        )

        val ALL_SHELF_ITEMS = listOf(
            KitchenSortItem(
                id = "glass",
                spokenName = "el vaso",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "El vaso va en el estante.",
                spokenSuccess = "¡Muy bien! El vaso va en la estantería."
            ),
            KitchenSortItem(
                id = "plate",
                spokenName = "el plato",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "El plato va en el estante.",
                spokenSuccess = "¡Excelente! Los platos van en la estantería."
            ),
            KitchenSortItem(
                id = "mug",
                spokenName = "la taza",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "La taza va en el estante.",
                spokenSuccess = "¡Genial! La taza va en la estantería."
            ),
            KitchenSortItem(
                id = "bowl",
                spokenName = "el tazón",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "El tazón va en el estante.",
                spokenSuccess = "¡Muy bien! El tazón va en la estantería."
            ),
            KitchenSortItem(
                id = "pan",
                spokenName = "la sartén",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "La sartén va en el estante.",
                spokenSuccess = "¡Bravo! La sartén se guarda en la estantería."
            ),
            KitchenSortItem(
                id = "cutlery",
                spokenName = "los cubiertos",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "La cuchara y el tenedor van en el estante.",
                spokenSuccess = "¡Muy bien! Los cubiertos van en la estantería."
            ),
            KitchenSortItem(
                id = "teapot",
                spokenName = "la tetera",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "La tetera va en el estante.",
                spokenSuccess = "¡Excelente! La tetera va en la estantería."
            ),
            KitchenSortItem(
                id = "cooking_pot",
                spokenName = "la olla",
                target = KitchenTarget.SHELF,
                spokenTouchPrompt = "La olla va en el estante.",
                spokenSuccess = "¡Genial! La olla se guarda en la estantería."
            )
        )

        private fun getItem(id: String): KitchenSortItem {
            return (ALL_FRIDGE_ITEMS + ALL_SHELF_ITEMS).first { it.id == id }
        }

        val ROUNDS = listOf(
            KitchenSortingRound(
                roundNumber = 1,
                targetStars = 1,
                fridgeItems = listOf(getItem("meat"), getItem("milk")),
                shelfItems = listOf(getItem("glass"), getItem("plate")),
                promptSpeech = "¡Vamos a ordenar la cocina! Guarda los alimentos en el refrigerador y los vasos en la estantería.",
                completionSpeech = "¡Muy bien! Ordenaste la cocina y ganaste una estrella."
            ),
            KitchenSortingRound(
                roundNumber = 2,
                targetStars = 2,
                fridgeItems = listOf(getItem("chicken"), getItem("cheese"), getItem("fish")),
                shelfItems = listOf(getItem("mug"), getItem("bowl"), getItem("pan")),
                promptSpeech = "¡Ronda dos! Acomoda tres alimentos en el refrigerador y tres objetos en la estantería.",
                completionSpeech = "¡Excelente! Guardaste todo en su lugar y ganaste dos estrellas."
            ),
            KitchenSortingRound(
                roundNumber = 3,
                targetStars = 3,
                fridgeItems = listOf(getItem("meat"), getItem("chicken"), getItem("fish"), getItem("milk")),
                shelfItems = listOf(getItem("glass"), getItem("plate"), getItem("mug"), getItem("pan")),
                promptSpeech = "¡Último reto! Ronda tres: Ordena todos los alimentos y utensilios de la cocina.",
                completionSpeech = "¡Bravo! ¡Toda la cocina quedó ordenada! ¡Ganaste las tres estrellas!"
            )
        )

        fun generateRandomRounds(random: Random = Random.Default): List<KitchenSortingRound> {
            fun buildRound(roundNum: Int, perTargetCount: Int, prompt: String, completion: String): KitchenSortingRound {
                val f = ALL_FRIDGE_ITEMS.shuffled(random).take(perTargetCount)
                val s = ALL_SHELF_ITEMS.shuffled(random).take(perTargetCount)
                return KitchenSortingRound(
                    roundNumber = roundNum,
                    targetStars = roundNum,
                    fridgeItems = f,
                    shelfItems = s,
                    promptSpeech = prompt,
                    completionSpeech = completion,
                    shuffledTray = (f + s).shuffled(random)
                )
            }

            return listOf(
                buildRound(
                    1, 2,
                    "¡Vamos a ordenar la cocina! Guarda los alimentos en el refrigerador y los utensilios en la estantería.",
                    "¡Muy bien! Ordenaste la cocina y ganaste una estrella."
                ),
                buildRound(
                    2, 3,
                    "¡Ronda dos! Acomoda tres alimentos en el refrigerador y tres utensilios en la estantería.",
                    "¡Excelente! Guardaste todo en su lugar y ganaste dos estrellas."
                ),
                buildRound(
                    3, 4,
                    "¡Último reto! Ronda tres: Ordena todos los alimentos y utensilios de la cocina.",
                    "¡Bravo! ¡Toda la cocina quedó ordenada! ¡Ganaste las tres estrellas!"
                )
            )
        }
    }

    val totalRounds: Int = 3
    private var activeRounds: List<KitchenSortingRound> = ROUNDS

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _starsEarned = MutableStateFlow(0)
    val starsEarned: StateFlow<Int> = _starsEarned.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<KitchenSortingUiState> = _uiState.asStateFlow()

    private fun createInitialState(): KitchenSortingUiState {
        val r1 = activeRounds[0]
        return KitchenSortingUiState(
            currentRound = 1,
            totalRounds = 3,
            starsEarned = 0,
            roundTotalItems = r1.totalItems,
            remainingTrayItems = r1.allItems,
            fridgePlacedItems = emptyList(),
            shelfPlacedItems = emptyList(),
            isRoundCompleted = false,
            isCompleted = false,
            celebrationActive = false,
            promptSpeech = r1.promptSpeech,
            score = 0,
            attempts = 0
        )
    }

    fun setActiveUserId(userId: String) {
        _uiState.update { it.copy(activeUserId = userId) }
    }

    fun onItemDropped(itemId: String, attemptedTarget: KitchenTarget): KitchenDropResult {
        return dropItem(itemId, attemptedTarget)
    }

    fun dropItem(itemId: String, attemptedTarget: KitchenTarget): KitchenDropResult {
        val state = _uiState.value
        if (state.isCompleted) return KitchenDropResult.Ignored

        val item = state.remainingTrayItems.find { it.id == itemId } ?: return KitchenDropResult.Ignored

        if (attemptedTarget == item.target) {
            val newRemaining = state.remainingTrayItems.filterNot { it.id == itemId }
            val newFridge = if (item.target == KitchenTarget.FRIDGE) state.fridgePlacedItems + item else state.fridgePlacedItems
            val newShelf = if (item.target == KitchenTarget.SHELF) state.shelfPlacedItems + item else state.shelfPlacedItems

            val isRoundFinished = newRemaining.isEmpty()
            val isFinalRound = state.currentRound >= totalRounds
            val isGameFinished = isRoundFinished && isFinalRound
            val updatedStars = if (isRoundFinished) maxOf(state.starsEarned, state.currentRound) else state.starsEarned

            _starsEarned.value = updatedStars
            _isCompleted.value = isGameFinished
            _celebrationActive.value = isGameFinished

            _uiState.update {
                it.copy(
                    remainingTrayItems = newRemaining,
                    fridgePlacedItems = newFridge,
                    shelfPlacedItems = newShelf,
                    starsEarned = updatedStars,
                    isRoundCompleted = isRoundFinished,
                    isCompleted = isGameFinished,
                    celebrationActive = isGameFinished,
                    score = it.score + 20
                )
            }

            if (isGameFinished) {
                saveWinProgress()
            }

            return KitchenDropResult.Correct(
                item = item,
                target = attemptedTarget,
                isRoundFinished = isRoundFinished,
                isGameFinished = isGameFinished,
                currentStars = updatedStars,
                remainingCount = newRemaining.size
            )
        } else {
            _uiState.update { it.copy(attempts = it.attempts + 1) }
            val hint = if (item.target == KitchenTarget.FRIDGE) {
                "¡Casi! ${item.spokenName} necesita frío en el refrigerador."
            } else {
                "¡Casi! ${item.spokenName} se acomoda en la estantería."
            }
            return KitchenDropResult.Incorrect(
                item = item,
                attemptedTarget = attemptedTarget,
                expectedTarget = item.target,
                hintSpeech = hint
            )
        }
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false

        val nextRoundNum = state.currentRound + 1
        val nextRoundData = activeRounds.getOrNull(nextRoundNum - 1) ?: return false

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                roundTotalItems = nextRoundData.totalItems,
                remainingTrayItems = nextRoundData.allItems,
                fridgePlacedItems = emptyList(),
                shelfPlacedItems = emptyList(),
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = nextRoundData.promptSpeech
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
        activeRounds = if (randomize) generateRandomRounds() else ROUNDS
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
            // JVM environment protection
        }
    }
}
