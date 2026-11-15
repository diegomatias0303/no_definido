package com.example.no_definido.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.IllustratedGameItem
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.MemoryCardModel
import com.example.no_definido.ui.viewmodel.MemoryFlipResult
import com.example.no_definido.ui.viewmodel.MemoryMatchViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MemoryMatchGameScreen(
    activeUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemoryMatchViewModel = viewModel()
) {
    val soundHelper = rememberSoundHelper()
    val uiState by viewModel.uiState.collectAsState()
    val starsEarned by viewModel.starsEarned.collectAsState()

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    LaunchedEffect(uiState.currentRound) {
        if (!uiState.isCompleted) {
            delay(350)
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    // Voltear de regreso automáticamente cuando dos cartas no coinciden
    LaunchedEffect(uiState.hasPendingMismatch) {
        if (uiState.hasPendingMismatch) {
            delay(950)
            viewModel.clearMismatch()
        }
    }

    // Avanzar automáticamente a la siguiente ronda tras completar todas las parejas de la ronda
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isCompleted) {
            delay(1300)
            if (viewModel.nextRound()) {
                soundHelper.playPop()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B4B))
    ) {
        val screenH = maxHeight
        val screenW = maxWidth

        // 1. FONDO ILUSTRADO CON FILTRO MÁGICO DE BIBLIOTECA DEL CALMÉCAC
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = "Salón del Memorama",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x991E1B4B),
                            Color(0xBB311042),
                            Color(0xCC1E1B4B)
                        )
                    )
                )
        )

        // 2. BARRA SUPERIOR VISUAL
        VisualGameTopBar(
            currentStars = starsEarned,
            totalStars = 3,
            onBackToRoom = onBack,
            onSpeakHint = {
                soundHelper.speak(viewModel.getCurrentHintSpeech())
            },
            onReset = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            }
        )

        // 3. TABLERO DE CARTAS MÁGICAS (2 filas)
        val totalCards = uiState.cards.size
        val columns = (totalCards + 1) / 2
        val cardHeight = (screenH * 0.34f).coerceIn(92.dp, 148.dp)
        val cardWidth = (cardHeight * 0.86f).coerceAtMost((screenW * 0.82f) / columns.coerceAtLeast(2))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val topRow = uiState.cards.take(columns)
            val bottomRow = uiState.cards.drop(columns)

            listOf(topRow, bottomRow).forEach { rowCards ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowCards.forEach { card ->
                        MagicMemoryCard(
                            card = card,
                            cardWidth = cardWidth,
                            cardHeight = cardHeight,
                            onClick = {
                                when (val result = viewModel.flipCard(card.cardId)) {
                                    is MemoryFlipResult.FirstCardFlipped -> {
                                        soundHelper.playPop()
                                        soundHelper.speak(result.card.spokenName)
                                    }
                                    is MemoryFlipResult.PairMatched -> {
                                        soundHelper.playSuccess()
                                        val msg = if (result.isGameFinished) {
                                            "${result.card.spokenMatchCelebration} ¡Completaste todo el Memorama del Calmécac y ganaste las 3 estrellas doradas!"
                                        } else if (result.isRoundFinished) {
                                            "${result.card.spokenMatchCelebration} ¡Excelente memoria! ¡Ganaste una estrella!"
                                        } else {
                                            result.card.spokenMatchCelebration
                                        }
                                        soundHelper.speak(msg)
                                    }
                                    is MemoryFlipResult.PairMismatch -> {
                                        soundHelper.playGentleTryAgain()
                                        soundHelper.speak("${result.secondCard.spokenName}. ¡Sigue buscando su pareja!")
                                    }
                                    MemoryFlipResult.Ignored -> Unit
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. CELEBRACIÓN VISUAL AL COMPLETAR LAS 3 RONDAS
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive,
            bigVisualBadge = "📚 🃏 ✨ 🏆 🌟",
            onPlayAgain = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = onBack
        )
    }
}

@Composable
private fun MagicMemoryCard(
    card: MemoryCardModel,
    cardWidth: Dp,
    cardHeight: Dp,
    onClick: () -> Unit
) {
    val isRevealed = card.isFaceUp || card.isMatched

    val flipRotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "memoryCardFlip"
    )

    val bounceScale by animateFloatAsState(
        targetValue = if (card.isMatched) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "memoryCardScale"
    )

    val borderColor = when {
        card.isMatched -> Color(0xFFFFD54F)
        isRevealed -> Color(0xFF38BDF8)
        else -> Color(0xFFFFB300)
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isRevealed) Color(0xFFFFFDE7) else Color(0xFF312E81),
        border = BorderStroke(if (card.isMatched) 5.dp else 3.5.dp, borderColor),
        shadowElevation = if (isRevealed) 12.dp else 7.dp,
        modifier = Modifier
            .size(width = cardWidth, height = cardHeight)
            .scale(bounceScale)
            .graphicsLayer {
                rotationY = flipRotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !isRevealed) { onClick() }
    ) {
        if (flipRotation <= 90f) {
            // Dorso del Códice Mágico
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4338CA), Color(0xFF1E1B4B), Color(0xFF0F766E))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    val w = size.width
                    val h = size.height
                    val cx = w * 0.5f
                    val cy = h * 0.5f
                    val r = minOf(w, h) * 0.34f

                    // Marco interior dorado
                    drawRoundRect(
                        color = Color(0x66FFD54F),
                        size = size,
                        style = Stroke(width = 3f)
                    )

                    // Sol Mexica en el reverso de la carta
                    for (i in 0 until 8) {
                        val angle = (i * 45.0) * PI / 180.0
                        val x2 = cx + cos(angle).toFloat() * (r * 1.38f)
                        val y2 = cy + sin(angle).toFloat() * (r * 1.38f)
                        drawLine(
                            color = Color(0xFFFFD54F),
                            start = Offset(cx, cy),
                            end = Offset(x2, y2),
                            strokeWidth = 4f
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFEE58), Color(0xFFFF8F00)),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0xFF26A69A),
                        radius = r * 0.45f,
                        center = Offset(cx, cy)
                    )
                }
            }
        } else {
            // Frente revelado con el objeto ilustrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        if (card.isMatched) {
                            Brush.radialGradient(
                                listOf(Color(0xFFFFF9C4), Color(0xFFFFECB3))
                            )
                        } else {
                            Brush.radialGradient(
                                listOf(Color.White, Color(0xFFFFF8E1))
                            )
                        }
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                IllustratedGameItem(
                    itemId = card.itemId,
                    modifier = Modifier.fillMaxSize(0.84f)
                )

                if (card.isMatched) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD54F),
                        border = BorderStroke(2.dp, Color(0xFF3E2723)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "⭐", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
