package com.example.no_definido.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.AppleBasketViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun AppleBasketGameScreen(
    viewModel: AppleBasketViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val soundHelper = rememberSoundHelper()

    var basketBounds by remember { mutableStateOf<Rect?>(null) }
    var showHandTutorial by remember { mutableStateOf(true) }

    val numberNames = listOf(
        "", "¡Uno!", "¡Dos!", "¡Tres!", "¡Cuatro!", "¡Cinco!", "¡Seis!", "¡Siete!", "¡Ocho!", "¡Nueve!", "¡Diez!"
    )

    // Guía de voz al iniciar cada ronda
    LaunchedEffect(uiState.currentRound) {
        if (uiState.promptSpeech.isNotBlank()) {
            soundHelper.speak(uiState.promptSpeech)
        }
        if (uiState.currentRound == 1) {
            showHandTutorial = true
            delay(4500)
            showHandTutorial = false
        } else {
            showHandTutorial = false
        }
    }

    // Auto-avance suave entre rondas progresivas
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isCompleted) {
            delay(1800)
            viewModel.nextRound()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orchardAnim")
    val handProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "handTutorialProgress"
    )
    val appleSway by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "appleSway"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // 1. FONDO DEL HUERTO SIN ELEMENTOS DUPLICADOS
        Image(
            painter = painterResource(id = R.drawable.tree),
            contentDescription = "Huerto de manzanas",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. BARRA VISUAL SUPERIOR (SIN TEXTO NI MENÚS - ESTRELLAS POR RONDA)
        VisualGameTopBar(
            currentStars = uiState.starsEarned,
            totalStars = uiState.totalRounds,
            onBackToRoom = onBack,
            onSpeakHint = {
                soundHelper.speak(uiState.promptSpeech.ifBlank { "¡Lleva las manzanas del árbol hacia la canasta para contar!" })
                showHandTutorial = true
            },
            onReset = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            }
        )

        // 3. CESTA DE MANZANAS APOYADA EN EL PRADO IZQUIERDO
        val basketSize = (screenH * 0.42f).coerceIn(145.dp, 210.dp)
        val basketX = screenW * 0.12f
        val basketY = screenH * 0.50f

        Box(
            modifier = Modifier
                .offset(x = basketX, y = basketY)
                .size(basketSize)
                .onGloballyPositioned { coordinates ->
                    basketBounds = coordinates.boundsInWindow()
                },
            contentAlignment = Alignment.Center
        ) {
            // Sombra suave bajo la cesta
            Box(
                modifier = Modifier
                    .size(width = basketSize * 0.8f, height = basketSize * 0.18f)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            // Fondo de la cesta
            Image(
                painter = painterResource(id = R.drawable.cesta),
                contentDescription = "Cesta",
                modifier = Modifier.fillMaxSize()
            )

            // 8 posiciones distribuidas para hasta 8 manzanas sin solaparse
            val applePositions = listOf(
                Pair((-30).dp, 14.dp),     // Fila inferior izquierda
                Pair(30.dp, 14.dp),      // Fila inferior derecha
                Pair(0.dp, 18.dp),       // Fila inferior centro
                Pair((-22).dp, 2.dp),    // Fila media izquierda
                Pair(22.dp, 2.dp),      // Fila media derecha
                Pair((-16).dp, (-14).dp), // Fila superior izquierda
                Pair(16.dp, (-14).dp),   // Fila superior derecha
                Pair(0.dp, (-6).dp)      // Fila media-alta centro
            )

            repeat(uiState.targetCount) { index ->
                val (xPos, yPos) = applePositions.getOrElse(index) { Pair(0.dp, 0.dp) }
                val isCollected = index < uiState.currentCount

                val appleScale by animateFloatAsState(
                    targetValue = if (isCollected) 1.0f else 0.0f,
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "basket_apple_$index"
                )

                if (appleScale > 0.01f) {
                    Image(
                        painter = painterResource(id = R.drawable.apple),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = xPos, y = yPos)
                            .size(basketSize * 0.27f)
                            .scale(appleScale)
                    )
                }
            }

            // Capa frontal de la cesta para que las manzanas queden adentro
            Image(
                painter = painterResource(id = R.drawable.cesta_front),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            // Globo numérico visual sobre la cesta
            AnimatedVisibility(
                visible = uiState.currentCount > 0,
                enter = scaleIn(tween(300)) + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD54F),
                    border = BorderStroke(4.dp, Color(0xFF3E2723)),
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${uiState.currentCount}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3E2723)
                        )
                    }
                }
            }
        }

        // 4. MANZANAS COLGANDO DIRECTAMENTE EN LA COPA DEL ÁRBOL
        val appleItemSize = (screenH * 0.16f).coerceIn(50.dp, 80.dp)
        uiState.treeApples.forEach { slot ->
            key(slot.id, uiState.currentRound) {
                val appleStartX = (screenW * slot.relX) - (appleItemSize / 2f)
                val appleStartY = (screenH * slot.relY) - (appleItemSize / 2f)

                AnimatedVisibility(
                    visible = !slot.isCollected,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .offset(x = appleStartX, y = appleStartY)
                        .zIndex(20f)
                ) {
                    DraggableTreeApple(
                        appleSize = appleItemSize,
                        idleRotation = appleSway,
                        basketBounds = basketBounds,
                        onPickedUp = {
                            showHandTutorial = false
                            soundHelper.playPop()
                        },
                        onMissed = {
                            soundHelper.playGentleTryAgain()
                        },
                        onDroppedInBasket = {
                            val newCount = viewModel.dropApple(slot.id)
                            if (newCount != null) {
                                soundHelper.playSuccess()
                                val spoken = numberNames.getOrElse(newCount) { "$newCount" }
                                if (newCount == uiState.targetCount) {
                                    if (uiState.currentRound < uiState.totalRounds) {
                                        soundHelper.speak("$spoken ¡Muy bien! ¡Ganaste una estrella!")
                                    } else {
                                        soundHelper.speak("$spoken ¡Bravo! ¡Contaste todas las manzanas del huerto y ganaste 3 estrellas!")
                                    }
                                } else {
                                    soundHelper.speak(spoken)
                                }
                            }
                        }
                    )
                }
            }
        }

        // 5. DEMOSTRACIÓN VISUAL CON MANITA ANIMADA (SIN TEXTO)
        if (showHandTutorial && uiState.currentCount == 0 && uiState.currentRound == 1) {
            val startX = screenW * 0.47f
            val startY = screenH * 0.35f
            val endX = basketX + (basketSize * 0.35f)
            val endY = basketY + (basketSize * 0.35f)

            val curX = startX + (endX - startX) * handProgress
            val curY = startY + (endY - startY) * handProgress

            Image(
                painter = painterResource(id = R.drawable.hand_apple),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = curX, y = curY)
                    .size(78.dp)
                    .graphicsLayer {
                        alpha = 1f - (handProgress * 0.25f)
                    }
                    .zIndex(35f)
            )
        }

        // 6. CELEBRACIÓN VISUAL AL COMPLETAR TODAS LAS RONDAS
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive || uiState.showRewardDialog,
            bigVisualBadge = "🍎 ⭐⭐⭐",
            onPlayAgain = {
                soundHelper.playPop()
                viewModel.dismissRewardDialog()
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = {
                viewModel.dismissRewardDialog()
                onBack()
            }
        )
    }
}

@Composable
private fun DraggableTreeApple(
    appleSize: androidx.compose.ui.unit.Dp,
    idleRotation: Float,
    basketBounds: Rect?,
    onPickedUp: () -> Unit,
    onMissed: () -> Unit,
    onDroppedInBasket: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentWindowBounds by remember { mutableStateOf<Rect?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.32f else 1.0f,
        animationSpec = tween(180),
        label = "appleDragScale"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(appleSize)
            .scale(scale)
            .graphicsLayer {
                rotationZ = if (isDragging) 0f else idleRotation
            }
            .zIndex(if (isDragging) 40f else 10f)
            .onGloballyPositioned { coordinates ->
                currentWindowBounds = coordinates.boundsInWindow()
            }
            .pointerInput(basketBounds) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onPickedUp()
                    },
                    onDragEnd = {
                        isDragging = false
                        val appleRect = currentWindowBounds
                        val targetRect = basketBounds
                        if (appleRect != null && targetRect != null) {
                            val inflatedBasket = Rect(
                                left = targetRect.left - targetRect.width * 0.20f,
                                top = targetRect.top - targetRect.height * 0.28f, // 28% de margen superior para soltar cómodamente
                                right = targetRect.right + targetRect.width * 0.20f,
                                bottom = targetRect.bottom + targetRect.height * 0.18f
                            )
                            if (inflatedBasket.contains(appleRect.center)) {
                                onDroppedInBasket()
                            } else {
                                onMissed()
                            }
                        }
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.apple),
            contentDescription = "Manzana",
            modifier = Modifier.fillMaxSize()
        )
    }
}
