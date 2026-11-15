package com.example.no_definido.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.IllustratedGameItem
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.BubblePopViewModel
import com.example.no_definido.ui.viewmodel.BubbleTapResult
import com.example.no_definido.ui.viewmodel.FloatingBubbleNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BubblePopGameScreen(
    activeUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BubblePopViewModel = viewModel()
) {
    val soundHelper = rememberSoundHelper()
    val uiState by viewModel.uiState.collectAsState()
    val starsEarned by viewModel.starsEarned.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "bubbleFloatTransition")
    val floatWave by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatWave"
    )

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    LaunchedEffect(uiState.currentRound) {
        if (!uiState.isCompleted) {
            delay(350)
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    // Avanzar automáticamente a la siguiente ronda tras reventar todas las burbujas objetivo
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
            .background(Color(0xFF0EA5E9))
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // 1. FONDO DE JARDÍN Y CIELO CON DESTELLOS DE BURBUJAS
        Image(
            painter = painterResource(id = R.drawable.bg_train_meadow),
            contentDescription = "Jardín de Burbujas",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x440284C7),
                            Color(0x2238BDF8),
                            Color(0x550F172A)
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

        // 3. MEDALLÓN VISUAL DEL OBJETIVO ACTUAL (Arriba al centro-izquierda)
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xEEFFFDE7),
            border = BorderStroke(4.dp, Color(0xFFFFB300)),
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .zIndex(35f)
                .clickable {
                    soundHelper.playPop()
                    soundHelper.speak(viewModel.getCurrentHintSpeech())
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE0F2FE), CircleShape)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IllustratedGameItem(itemId = uiState.targetEntry.itemId)
                }

                Text(
                    text = "${uiState.poppedCount} / ${uiState.targetCount}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        // 4. BURBUJAS FLOTANTES EN PANTALLA
        val bubbleSize = (screenH * 0.25f).coerceIn(88.dp, 128.dp)

        uiState.bubbles.forEachIndexed { idx, bubble ->
            val phaseSign = if (idx % 2 == 0) 1f else -1f
            val dy = (floatWave * phaseSign).dp
            val dx = (floatWave * 0.45f * -phaseSign).dp

            val posX = (screenW * bubble.xFraction) - (bubbleSize * 0.5f) + dx
            val posY = (screenH * bubble.yFraction) - (bubbleSize * 0.5f) + dy

            Box(
                modifier = Modifier
                    .offset(x = posX, y = posY)
                    .size(bubbleSize)
                    .zIndex(20f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = !bubble.isPopped,
                    exit = scaleOut(targetScale = 1.45f, animationSpec = tween(220)) + fadeOut(tween(200))
                ) {
                    InteractiveFloatingBubble(
                        bubble = bubble,
                        size = bubbleSize,
                        onTap = {
                            when (val res = viewModel.tapBubble(bubble.bubbleId)) {
                                is BubbleTapResult.PoppedTarget -> {
                                    soundHelper.playPop()
                                    if (res.isGameFinished || res.isRoundFinished) {
                                        soundHelper.playSuccess()
                                        soundHelper.speak("${res.countWord} ${uiState.completionSpeech}")
                                    } else {
                                        soundHelper.speak(res.countWord)
                                    }
                                }
                                is BubbleTapResult.WrongBubble -> {
                                    soundHelper.playGentleTryAgain()
                                    soundHelper.speak(res.hintSpeech)
                                }
                                BubbleTapResult.Ignored -> Unit
                            }
                        }
                    )
                }
            }
        }

        // 5. CELEBRACIÓN VISUAL AL COMPLETAR LAS 3 RONDAS
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive,
            bigVisualBadge = "🫧 🏖️ 🦆 ✨ 🌟",
            onPlayAgain = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = onBack
        )
    }
}

@Composable
private fun InteractiveFloatingBubble(
    bubble: FloatingBubbleNode,
    size: Dp,
    onTap: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var wobbleActive by remember { mutableStateOf(false) }

    val wobbleScale by animateFloatAsState(
        targetValue = if (wobbleActive) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bubbleWobble"
    )

    val tintColor = Color(bubble.colorTintHex)

    Box(
        modifier = Modifier
            .size(size)
            .scale(wobbleScale)
            .clickable {
                if (!bubble.isTarget) {
                    coroutineScope.launch {
                        wobbleActive = true
                        delay(180)
                        wobbleActive = false
                    }
                }
                onTap()
            },
        contentAlignment = Alignment.Center
    ) {
        // Esfera de cristal iridiscente de la burbuja
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension * 0.48f
            val center = Offset(this.size.width * 0.5f, this.size.height * 0.5f)

            // Relleno translúcido con gradiente radial
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55FFFFFF),
                        tintColor.copy(alpha = 0.38f),
                        tintColor.copy(alpha = 0.72f)
                    ),
                    center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                    radius = r * 1.1f
                ),
                radius = r,
                center = center
            )

            // Aro exterior nacarado
            drawCircle(
                color = Color.White.copy(alpha = 0.88f),
                radius = r,
                center = center,
                style = Stroke(width = 5f)
            )
            drawCircle(
                color = tintColor,
                radius = r * 0.94f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Brillo especular superior izquierdo (efecto burbuja de jabón 3D)
            drawOval(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(center.x - r * 0.62f, center.y - r * 0.68f),
                size = androidx.compose.ui.geometry.Size(r * 0.46f, r * 0.26f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.70f),
                radius = r * 0.09f,
                center = Offset(center.x + r * 0.48f, center.y + r * 0.48f)
            )
        }

        // Objeto ilustrado flotando dentro de la burbuja
        IllustratedGameItem(
            itemId = bubble.itemId,
            modifier = Modifier
                .fillMaxSize(0.62f)
                .padding(4.dp)
        )
    }
}
