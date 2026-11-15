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

data class BubbleItemEntry(
    val itemId: String,
    val spokenSingular: String,
    val spokenPlural: String
)

data class FloatingBubbleNode(
    val bubbleId: String,
    val itemId: String,
    val spokenName: String,
    val isTarget: Boolean,
    val xFraction: Float,
    val yFraction: Float,
    val colorTintHex: Long,
    val isPopped: Boolean = false
)

sealed class BubbleTapResult {
    data class PoppedTarget(
        val bubble: FloatingBubbleNode,
        val poppedCount: Int,
        val targetCount: Int,
        val countWord: String,
        val isRoundFinished: Boolean,
        val isGameFinished: Boolean
    ) : BubbleTapResult()

    data class WrongBubble(
        val bubble: FloatingBubbleNode,
        val hintSpeech: String
    ) : BubbleTapResult()

    object Ignored : BubbleTapResult()
}

data class BubblePopUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val targetEntry: BubbleItemEntry,
    val targetCount: Int = 3,
    val poppedCount: Int = 0,
    val bubbles: List<FloatingBubbleNode> = emptyList(),
    val starsEarned: Int = 0,
    val isRoundCompleted: Boolean = false,
    val isCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val promptSpeech: String = "",
    val completionSpeech: String = "",
    val activeUserId: String = "jugador_invitado",
    val score: Int = 0,
    val attempts: Int = 0
)

class BubblePopViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    companion object {
        val SPANISH_COUNT_WORDS = listOf(
            "", "¡Una!", "¡Dos!", "¡Tres!", "¡Cuatro!", "¡Cinco!", "¡Seis!"
        )

        val BUBBLE_TINTS = listOf(
            0xFF38BDF8L,
            0xFFF472B6L,
            0xFFA78BFAL,
            0xFF34D399L,
            0xFFFBBF24L,
            0xFF60A5FAL,
            0xFFFB7185L,
            0xFF2DD4BFL
        )

        val ITEM_POOL: List<BubbleItemEntry> = listOf(
            BubbleItemEntry("rubber_duck", "el patito de hule", "patitos de hule"),
            BubbleItemEntry("toy_car", "el carrito rojo", "carritos rojos"),
            BubbleItemEntry("sunflower", "el girasol dorado", "girasoles dorados"),
            BubbleItemEntry("teddy", "el osito de peluche", "ositos de peluche"),
            BubbleItemEntry("ball", "la pelota de colores", "pelotas de colores"),
            BubbleItemEntry("watermelon", "la rebanada de sandía", "rebanadas de sandía"),
            BubbleItemEntry("jade_stone", "la gema de jade", "gemas de jade"),
            BubbleItemEntry("soap", "el jabón de burbujas", "jabones de burbujas"),
            BubbleItemEntry("apple_fruit", "la manzana roja", "manzanas rojas"),
            BubbleItemEntry("storybook", "el libro mágico", "libros mágicos")
        )

        // Coordenadas distribuidas armónicamente para evitar solapamientos en landscape
        val BUBBLE_SLOTS: List<Pair<Float, Float>> = listOf(
            0.16f to 0.34f,
            0.36f to 0.28f,
            0.58f to 0.32f,
            0.80f to 0.30f,
            0.22f to 0.66f,
            0.46f to 0.62f,
            0.68f to 0.68f,
            0.85f to 0.60f
        )

        fun buildRoundState(
            roundNumber: Int,
            targetEntry: BubbleItemEntry,
            distractorEntries: List<BubbleItemEntry>,
            targetCount: Int,
            randomizePositions: Boolean
        ): Triple<List<FloatingBubbleNode>, String, String> {
            val distractorCount = when (roundNumber) {
                1 -> 2
                2 -> 3
                else -> 3
            }
            val totalBubbles = (targetCount + distractorCount).coerceAtMost(BUBBLE_SLOTS.size)
            val slots = if (randomizePositions) BUBBLE_SLOTS.shuffled().take(totalBubbles) else BUBBLE_SLOTS.take(totalBubbles)

            val rawItems = mutableListOf<Pair<BubbleItemEntry, Boolean>>()
            repeat(targetCount) {
                rawItems.add(targetEntry to true)
            }
            repeat(totalBubbles - targetCount) { idx ->
                val distractor = distractorEntries[idx % distractorEntries.size]
                rawItems.add(distractor to false)
            }

            val orderedItems = if (randomizePositions) rawItems.shuffled() else rawItems
            val nodes = orderedItems.mapIndexed { idx, (entry, isTarget) ->
                val (x, y) = slots[idx]
                FloatingBubbleNode(
                    bubbleId = "bubble_r${roundNumber}_$idx",
                    itemId = entry.itemId,
                    spokenName = entry.spokenSingular,
                    isTarget = isTarget,
                    xFraction = x,
                    yFraction = y,
                    colorTintHex = BUBBLE_TINTS[idx % BUBBLE_TINTS.size],
                    isPopped = false
                )
            }

            val prompt = when (roundNumber) {
                1 -> "¡Burbujas mágicas! Toca y revienta las $targetCount burbujas que tienen ${targetEntry.spokenSingular}."
                2 -> "¡Ronda dos! Encuentra y revienta las $targetCount burbujas con ${targetEntry.spokenSingular}."
                else -> "¡Última ronda! Revienta las $targetCount burbujas que guardan ${targetEntry.spokenSingular}."
            }
            val completion = when (roundNumber) {
                1 -> "¡Plop! ¡Atrapaste los $targetCount ${targetEntry.spokenPlural}! ¡Ganaste una estrella!"
                2 -> "¡Súper! ¡Reventaste todas las burbujas de ${targetEntry.spokenPlural}! ¡Dos estrellas!"
                else -> "¡Bravo! ¡Reventaste todas las burbujas mágicas y ganaste las 3 estrellas doradas!"
            }

            return Triple(nodes, prompt, completion)
        }
    }

    private var isRandomizedMode: Boolean = false
    private var roundTargets: List<BubbleItemEntry> = ITEM_POOL.take(3)
    private var roundTargetCounts: List<Int> = listOf(3, 4, 5)

    private fun createStateForRound(
        roundNumber: Int,
        starsEarned: Int,
        activeUserId: String,
        score: Int
    ): BubblePopUiState {
        val target = roundTargets[(roundNumber - 1).coerceIn(0, roundTargets.lastIndex)]
        val targetCount = roundTargetCounts[(roundNumber - 1).coerceIn(0, roundTargetCounts.lastIndex)]
        val distractors = ITEM_POOL.filter { it.itemId != target.itemId }.let {
            if (isRandomizedMode) it.shuffled() else it
        }
        val (nodes, prompt, completion) = buildRoundState(
            roundNumber = roundNumber,
            targetEntry = target,
            distractorEntries = distractors,
            targetCount = targetCount,
            randomizePositions = isRandomizedMode
        )
        return BubblePopUiState(
            currentRound = roundNumber,
            totalRounds = 3,
            targetEntry = target,
            targetCount = targetCount,
            poppedCount = 0,
            bubbles = nodes,
            starsEarned = starsEarned,
            isRoundCompleted = false,
            isCompleted = false,
            celebrationActive = false,
            promptSpeech = prompt,
            completionSpeech = completion,
            activeUserId = activeUserId,
            score = score
        )
    }

    private val _uiState = MutableStateFlow(
        createStateForRound(roundNumber = 1, starsEarned = 0, activeUserId = "jugador_invitado", score = 0)
    )
    val uiState: StateFlow<BubblePopUiState> = _uiState.asStateFlow()

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

    fun tapBubble(bubbleId: String): BubbleTapResult {
        val state = _uiState.value
        if (state.isCompleted || state.isRoundCompleted) return BubbleTapResult.Ignored

        val bubble = state.bubbles.find { it.bubbleId == bubbleId } ?: return BubbleTapResult.Ignored
        if (bubble.isPopped) return BubbleTapResult.Ignored

        if (bubble.isTarget) {
            val nextPopped = state.poppedCount + 1
            val isRoundFinished = nextPopped >= state.targetCount
            val isFinalRound = state.currentRound >= totalRounds
            val isGameFinished = isRoundFinished && isFinalRound
            val updatedStars = if (isRoundFinished) maxOf(state.starsEarned, state.currentRound) else state.starsEarned
            val countWord = SPANISH_COUNT_WORDS.getOrElse(nextPopped) { "¡$nextPopped!" }

            val updatedBubbles = state.bubbles.map {
                if (it.bubbleId == bubbleId) it.copy(isPopped = true) else it
            }

            _starsEarned.value = updatedStars
            if (isGameFinished) {
                _isCompleted.value = true
                _celebrationActive.value = true
                saveWinProgress()
            }

            _uiState.update {
                it.copy(
                    poppedCount = nextPopped,
                    bubbles = updatedBubbles,
                    starsEarned = updatedStars,
                    isRoundCompleted = isRoundFinished,
                    isCompleted = isGameFinished,
                    celebrationActive = isGameFinished,
                    score = it.score + 20,
                    attempts = it.attempts + 1
                )
            }

            return BubbleTapResult.PoppedTarget(
                bubble = bubble.copy(isPopped = true),
                poppedCount = nextPopped,
                targetCount = state.targetCount,
                countWord = countWord,
                isRoundFinished = isRoundFinished,
                isGameFinished = isGameFinished
            )
        } else {
            _uiState.update { it.copy(attempts = it.attempts + 1) }
            val hint = "¡Esa burbuja tiene ${bubble.spokenName}! Busca las burbujas con ${state.targetEntry.spokenSingular}."
            return BubbleTapResult.WrongBubble(
                bubble = bubble,
                hintSpeech = hint
            )
        }
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false

        val nextRoundNum = state.currentRound + 1
        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.value = createStateForRound(
            roundNumber = nextRoundNum,
            starsEarned = state.starsEarned,
            activeUserId = state.activeUserId,
            score = state.score
        )
        return true
    }

    fun resetGame(randomize: Boolean = false) {
        val userId = _uiState.value.activeUserId
        isRandomizedMode = randomize
        if (randomize) {
            roundTargets = ITEM_POOL.shuffled().take(3)
            val c1 = listOf(2, 3).random()
            val c2 = listOf(3, 4).filter { it >= c1 }.random()
            val c3 = listOf(4, 5).filter { it >= c2 }.random()
            roundTargetCounts = listOf(c1, c2, c3)
        } else {
            roundTargets = ITEM_POOL.take(3)
            roundTargetCounts = listOf(3, 4, 5)
        }

        _currentRound.value = 1
        _starsEarned.value = 0
        _isCompleted.value = false
        _celebrationActive.value = false
        _uiState.value = createStateForRound(
            roundNumber = 1,
            starsEarned = 0,
            activeUserId = userId,
            score = 0
        )
    }

    fun getCurrentHintSpeech(): String {
        val state = _uiState.value
        val remaining = (state.targetCount - state.poppedCount).coerceAtLeast(0)
        return "Busca las burbujas que tienen ${state.targetEntry.spokenSingular}. ¡Faltan $remaining burbujas por reventar!"
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
