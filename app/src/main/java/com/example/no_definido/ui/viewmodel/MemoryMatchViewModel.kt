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

data class MemoryItemCatalogEntry(
    val itemId: String,
    val spokenName: String,
    val spokenMatchCelebration: String
)

data class MemoryCardModel(
    val cardId: String,
    val itemId: String,
    val spokenName: String,
    val spokenMatchCelebration: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

sealed class MemoryFlipResult {
    data class FirstCardFlipped(val card: MemoryCardModel) : MemoryFlipResult()
    data class PairMatched(
        val card: MemoryCardModel,
        val isRoundFinished: Boolean,
        val isGameFinished: Boolean,
        val starsEarned: Int
    ) : MemoryFlipResult()
    data class PairMismatch(
        val firstCard: MemoryCardModel,
        val secondCard: MemoryCardModel
    ) : MemoryFlipResult()
    object Ignored : MemoryFlipResult()
}

data class MemoryMatchUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val cards: List<MemoryCardModel> = emptyList(),
    val firstSelectedCardId: String? = null,
    val secondSelectedCardId: String? = null,
    val hasPendingMismatch: Boolean = false,
    val matchedPairsCount: Int = 0,
    val totalPairsInRound: Int = 2,
    val starsEarned: Int = 0,
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val promptSpeech: String = "",
    val activeUserId: String = "jugador_invitado",
    val score: Int = 0,
    val attempts: Int = 0
)

class MemoryMatchViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    companion object {
        val CATALOG: List<MemoryItemCatalogEntry> = listOf(
            MemoryItemCatalogEntry("teddy", "El osito de peluche", "¡Encontraste el par de ositos de peluche!"),
            MemoryItemCatalogEntry("storybook", "El libro de cuentos", "¡Par de libros mágicos encontrado!"),
            MemoryItemCatalogEntry("rubber_duck", "El patito de hule", "¡Cuac cuac! ¡Encontraste los dos patitos!"),
            MemoryItemCatalogEntry("toy_car", "El carrito rojo", "¡Brum brum! ¡Encontraste los dos carritos!"),
            MemoryItemCatalogEntry("sunflower", "El girasol dorado", "¡Qué brillo! ¡Encontraste los dos girasoles!"),
            MemoryItemCatalogEntry("watermelon", "La rebanada de sandía", "¡Qué rico! ¡Par de sandías frescas!"),
            MemoryItemCatalogEntry("jade_stone", "La gema de jade", "¡Brillante! ¡Encontraste el par de gemas de jade!"),
            MemoryItemCatalogEntry("corn", "La mazorca de maíz", "¡Excelente! ¡Par de mazorcas doradas!"),
            MemoryItemCatalogEntry("toothbrush", "El cepillo de dientes", "¡Sonrisa limpia! ¡Encontraste los dos cepillos!"),
            MemoryItemCatalogEntry("pillow", "La almohada suave", "¡Dulces sueños! ¡Par de almohadas encontrado!"),
            MemoryItemCatalogEntry("milk", "La botella de leche", "¡Muy bien! ¡Encontraste las dos botellas de leche!"),
            MemoryItemCatalogEntry("apple_fruit", "La manzana roja", "¡Delicioso! ¡Par de manzanas rojas!")
        )

        fun pairsForRound(roundNumber: Int): Int = when (roundNumber) {
            1 -> 2 // 4 cartas
            2 -> 3 // 6 cartas
            else -> 4 // 8 cartas
        }

        fun promptForRound(roundNumber: Int, pairsCount: Int): String = when (roundNumber) {
            1 -> "¡Memorama del Calmécac! Toca las cartas mágicas para encontrar las $pairsCount parejas iguales."
            2 -> "¡Ronda dos! Encuentra las $pairsCount parejas escondidas en los códices mágicos."
            else -> "¡Último reto! Descubre las $pairsCount parejas del gran memorama."
        }

        fun buildDeckForRound(roundNumber: Int, randomize: Boolean): List<MemoryCardModel> {
            val pairCount = pairsForRound(roundNumber)
            val chosenItems = if (randomize) {
                CATALOG.shuffled().take(pairCount)
            } else {
                val startIndex = ((roundNumber - 1) * 2) % CATALOG.size
                (0 until pairCount).map { offset ->
                    CATALOG[(startIndex + offset) % CATALOG.size]
                }
            }

            val rawCards = chosenItems.flatMap { entry ->
                listOf(
                    MemoryCardModel(
                        cardId = "${entry.itemId}_A",
                        itemId = entry.itemId,
                        spokenName = entry.spokenName,
                        spokenMatchCelebration = entry.spokenMatchCelebration
                    ),
                    MemoryCardModel(
                        cardId = "${entry.itemId}_B",
                        itemId = entry.itemId,
                        spokenName = entry.spokenName,
                        spokenMatchCelebration = entry.spokenMatchCelebration
                    )
                )
            }

            return if (randomize) rawCards.shuffled() else rawCards
        }

        private fun createInitialState(randomize: Boolean = false): MemoryMatchUiState {
            val deck = buildDeckForRound(1, randomize)
            val pairs = pairsForRound(1)
            return MemoryMatchUiState(
                currentRound = 1,
                totalRounds = 3,
                cards = deck,
                firstSelectedCardId = null,
                secondSelectedCardId = null,
                hasPendingMismatch = false,
                matchedPairsCount = 0,
                totalPairsInRound = pairs,
                starsEarned = 0,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = promptForRound(1, pairs),
                score = 0,
                attempts = 0
            )
        }
    }

    private var isRandomizedMode: Boolean = false

    private val _uiState = MutableStateFlow(createInitialState(randomize = false))
    val uiState: StateFlow<MemoryMatchUiState> = _uiState.asStateFlow()

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

    fun flipCard(cardId: String): MemoryFlipResult {
        val state = _uiState.value
        if (state.isCompleted || state.isRoundCompleted || state.hasPendingMismatch) {
            return MemoryFlipResult.Ignored
        }

        val targetCard = state.cards.find { it.cardId == cardId } ?: return MemoryFlipResult.Ignored
        if (targetCard.isFaceUp || targetCard.isMatched) {
            return MemoryFlipResult.Ignored
        }

        val firstId = state.firstSelectedCardId
        if (firstId == null) {
            val updatedCards = state.cards.map {
                if (it.cardId == cardId) it.copy(isFaceUp = true) else it
            }
            val flipped = updatedCards.first { it.cardId == cardId }
            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    firstSelectedCardId = cardId
                )
            }
            return MemoryFlipResult.FirstCardFlipped(flipped)
        }

        val firstCard = state.cards.find { it.cardId == firstId } ?: return MemoryFlipResult.Ignored
        if (firstCard.cardId == cardId) return MemoryFlipResult.Ignored

        if (firstCard.itemId == targetCard.itemId) {
            // ¡Par encontrado!
            val updatedCards = state.cards.map {
                if (it.cardId == firstId || it.cardId == cardId) {
                    it.copy(isFaceUp = true, isMatched = true)
                } else {
                    it
                }
            }
            val newMatchedPairs = state.matchedPairsCount + 1
            val isRoundFinished = newMatchedPairs >= state.totalPairsInRound
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
                    cards = updatedCards,
                    firstSelectedCardId = null,
                    secondSelectedCardId = null,
                    hasPendingMismatch = false,
                    matchedPairsCount = newMatchedPairs,
                    starsEarned = updatedStars,
                    isRoundCompleted = isRoundFinished,
                    isCompleted = isGameFinished,
                    celebrationActive = isGameFinished,
                    score = it.score + 25,
                    attempts = it.attempts + 1
                )
            }

            return MemoryFlipResult.PairMatched(
                card = targetCard.copy(isFaceUp = true, isMatched = true),
                isRoundFinished = isRoundFinished,
                isGameFinished = isGameFinished,
                starsEarned = updatedStars
            )
        } else {
            // No coinciden: se muestran ambas brevemente antes de voltearlas de regreso
            val updatedCards = state.cards.map {
                if (it.cardId == cardId) it.copy(isFaceUp = true) else it
            }
            val secondFlipped = updatedCards.first { it.cardId == cardId }
            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    secondSelectedCardId = cardId,
                    hasPendingMismatch = true,
                    attempts = it.attempts + 1
                )
            }
            return MemoryFlipResult.PairMismatch(
                firstCard = firstCard,
                secondCard = secondFlipped
            )
        }
    }

    fun clearMismatch() {
        val state = _uiState.value
        if (!state.hasPendingMismatch) return
        val firstId = state.firstSelectedCardId
        val secondId = state.secondSelectedCardId
        val resetCards = state.cards.map { card ->
            if ((card.cardId == firstId || card.cardId == secondId) && !card.isMatched) {
                card.copy(isFaceUp = false)
            } else {
                card
            }
        }
        _uiState.update {
            it.copy(
                cards = resetCards,
                firstSelectedCardId = null,
                secondSelectedCardId = null,
                hasPendingMismatch = false
            )
        }
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false

        val nextRoundNum = state.currentRound + 1
        val nextPairs = pairsForRound(nextRoundNum)
        val nextDeck = buildDeckForRound(nextRoundNum, isRandomizedMode)

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                cards = nextDeck,
                firstSelectedCardId = null,
                secondSelectedCardId = null,
                hasPendingMismatch = false,
                matchedPairsCount = 0,
                totalPairsInRound = nextPairs,
                isRoundCompleted = false,
                isCompleted = false,
                celebrationActive = false,
                promptSpeech = promptForRound(nextRoundNum, nextPairs)
            )
        }
        return true
    }

    fun resetGame(randomize: Boolean = false) {
        val userId = _uiState.value.activeUserId
        isRandomizedMode = randomize
        _currentRound.value = 1
        _starsEarned.value = 0
        _isCompleted.value = false
        _celebrationActive.value = false
        _uiState.value = createInitialState(randomize = isRandomizedMode).copy(activeUserId = userId)
    }

    fun getCurrentHintSpeech(): String {
        val state = _uiState.value
        val remaining = (state.totalPairsInRound - state.matchedPairsCount).coerceAtLeast(0)
        return "Toca dos cartas mágicas para voltearlas y encontrar su pareja igual. ¡Te faltan $remaining parejas!"
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
                            totalScore = cur.totalScore + 75,
                            totalStars = cur.totalStars + 3,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } catch (_: Throwable) {
                    // Modo offline seguro
                }
            }
        } catch (_: Throwable) {
            // Protección JVM
        }
    }
}
