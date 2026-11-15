package com.example.no_definido.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.IllustratedGameItem
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.KitchenDropResult
import com.example.no_definido.ui.viewmodel.KitchenSortItem
import com.example.no_definido.ui.viewmodel.KitchenSortingViewModel
import com.example.no_definido.ui.viewmodel.KitchenTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

typealias KitchenTarget = com.example.no_definido.ui.viewmodel.KitchenTarget
typealias KitchenSortItem = com.example.no_definido.ui.viewmodel.KitchenSortItem

@Composable
fun KitchenSortingGameScreen(
    activeUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KitchenSortingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val soundHelper = rememberSoundHelper()

    var fridgeBounds by remember { mutableStateOf<Rect?>(null) }
    var shelfBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    // Spoken prompt on round start
    LaunchedEffect(uiState.currentRound, uiState.promptSpeech) {
        delay(500)
        if (!uiState.isCompleted && uiState.promptSpeech.isNotBlank()) {
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    // Auto-advance between rounds after completing (1.8s delay)
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isCompleted) {
            delay(1800)
            viewModel.nextRound()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "kitchenGlow")
    val zonePulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zonePulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // 1. FONDO DE LA COCINA
        Image(
            painter = painterResource(id = R.drawable.bg_kitchen),
            contentDescription = "Cocina Interactiva",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. BARRA VISUAL SUPERIOR (3 estrellas de progreso)
        VisualGameTopBar(
            currentStars = uiState.starsEarned,
            totalStars = viewModel.totalRounds,
            onBackToRoom = onBack,
            onSpeakHint = {
                val nextItem = uiState.remainingTrayItems.firstOrNull()
                if (nextItem != null) {
                    val dest = if (nextItem.target == KitchenTarget.FRIDGE) "el refrigerador azul" else "la estantería de madera"
                    soundHelper.speak("Arrastra ${nextItem.spokenName} hacia $dest.")
                } else {
                    soundHelper.speak("¡Ya ordenaste toda la cocina!")
                }
            },
            onReset = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            }
        )

        // 3. ZONA DESTINO IZQUIERDA: REFRIGERADOR (5 SLOTS INDEPENDIENTES)
        val fridgeZoneX = screenW * 0.09f
        val fridgeZoneY = screenH * 0.43f
        val fridgeZoneW = screenW * 0.20f
        val fridgeZoneH = screenH * 0.42f

        Box(
            modifier = Modifier
                .offset(x = fridgeZoneX, y = fridgeZoneY)
                .size(width = fridgeZoneW, height = fridgeZoneH)
                .border(
                    width = 3.dp,
                    color = Color(0xFF29B6F6).copy(alpha = zonePulse),
                    shape = RoundedCornerShape(16.dp)
                )
                .onGloballyPositioned { coords ->
                    fridgeBounds = coords.boundsInWindow()
                }
        ) {
            // 5 coordenadas fijas sin solapamiento garantizadas en todas las resoluciones
            val fridgeSlotPositions = listOf(
                Pair(fridgeZoneW * 0.12f, fridgeZoneH * 0.06f), // Repisa Superior Izquierda
                Pair(fridgeZoneW * 0.54f, fridgeZoneH * 0.06f), // Repisa Superior Derecha
                Pair(fridgeZoneW * 0.12f, fridgeZoneH * 0.39f), // Repisa Inferior Izquierda
                Pair(fridgeZoneW * 0.54f, fridgeZoneH * 0.39f), // Repisa Inferior Derecha
                Pair(fridgeZoneW * 0.33f, fridgeZoneH * 0.72f)  // Cajón Inferior
            )

            uiState.fridgePlacedItems.forEachIndexed { index, placedItem ->
                val (sx, sy) = fridgeSlotPositions.getOrElse(index) { Pair(10.dp, 10.dp) }
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    modifier = Modifier
                        .offset(x = sx, y = sy)
                        .size((fridgeZoneH * 0.28f).coerceIn(44.dp, 64.dp))
                ) {
                    IllustratedGameItem(itemId = placedItem.id)
                }
            }
        }

        // 4. ZONA DESTINO DERECHA: ESTANTERÍA DE MADERA (5 SLOTS INDEPENDIENTES)
        val shelfZoneX = screenW * 0.73f
        val shelfZoneY = screenH * 0.26f
        val shelfZoneW = screenW * 0.21f
        val shelfZoneH = screenH * 0.62f

        Box(
            modifier = Modifier
                .offset(x = shelfZoneX, y = shelfZoneY)
                .size(width = shelfZoneW, height = shelfZoneH)
                .border(
                    width = 3.dp,
                    color = Color(0xFFFFB300).copy(alpha = zonePulse),
                    shape = RoundedCornerShape(16.dp)
                )
                .onGloballyPositioned { coords ->
                    shelfBounds = coords.boundsInWindow()
                }
        ) {
            // 5 coordenadas fijas sin solapamiento
            val shelfSlotPositions = listOf(
                Pair(shelfZoneW * 0.14f, shelfZoneH * 0.07f), // Repisa Alta Izquierda
                Pair(shelfZoneW * 0.54f, shelfZoneH * 0.07f), // Repisa Alta Derecha
                Pair(shelfZoneW * 0.14f, shelfZoneH * 0.38f), // Repisa Media Izquierda
                Pair(shelfZoneW * 0.54f, shelfZoneH * 0.38f), // Repisa Media Derecha
                Pair(shelfZoneW * 0.34f, shelfZoneH * 0.70f)  // Repisa Baja
            )

            uiState.shelfPlacedItems.forEachIndexed { index, placedItem ->
                val (sx, sy) = shelfSlotPositions.getOrElse(index) { Pair(10.dp, 10.dp) }
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    modifier = Modifier
                        .offset(x = sx, y = sy)
                        .size((shelfZoneH * 0.22f).coerceIn(46.dp, 66.dp))
                ) {
                    IllustratedGameItem(itemId = placedItem.id)
                }
            }
        }

        // 5. BANDEJA INFERIOR: OBJETOS DISPONIBLES PARA ARRASTRAR
        val visibleTrayItems = uiState.remainingTrayItems.take(4)
        val trayItemSize = (screenH * 0.20f).coerceIn(68.dp, 92.dp)
        val trayPaddingBottom = (screenH * 0.05f).coerceAtLeast(14.dp)
        val trayTopBoundaryPx = with(LocalDensity.current) {
            (screenH - trayPaddingBottom - trayItemSize - 8.dp).toPx()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = trayPaddingBottom)
                .zIndex(30f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                visibleTrayItems.forEach { item ->
                    key(item.id) {
                        DraggableKitchenObject(
                            item = item,
                            itemSize = trayItemSize,
                            fridgeBounds = fridgeBounds,
                            shelfBounds = shelfBounds,
                            trayTopBoundaryPx = trayTopBoundaryPx,
                            onPickedUp = {
                                soundHelper.playPop()
                                soundHelper.speak(item.spokenTouchPrompt)
                            },
                            onDroppedTarget = { droppedTarget ->
                                val result = viewModel.dropItem(item.id, droppedTarget)
                                when (result) {
                                    is KitchenDropResult.Correct -> {
                                        soundHelper.playSuccess()
                                        if (result.isRoundFinished && !result.isGameFinished) {
                                            soundHelper.speak("¡Muy bien! Ordenaste la cocina y ganaste una estrella.")
                                        } else if (result.isGameFinished) {
                                            soundHelper.speak("¡Bravo! ¡Toda la cocina quedó ordenada! ¡Ganaste las tres estrellas!")
                                        } else {
                                            soundHelper.speak(item.spokenSuccess)
                                        }
                                    }
                                    is KitchenDropResult.Incorrect -> {
                                        soundHelper.playGentleTryAgain()
                                        soundHelper.speak(result.hintSpeech)
                                    }
                                    KitchenDropResult.Ignored -> {
                                        // Ignore
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // 6. CELEBRACIÓN VISUAL AL COMPLETAR EL JUEGO (3 estrellas de oro)
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive || uiState.isCompleted,
            bigVisualBadge = "🥩 🥛 🍽️ ✨",
            onPlayAgain = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = onBack
        )
    }
}

@Composable
private fun DraggableKitchenObject(
    item: KitchenSortItem,
    itemSize: androidx.compose.ui.unit.Dp,
    fridgeBounds: Rect?,
    shelfBounds: Rect?,
    trayTopBoundaryPx: Float = Float.MAX_VALUE,
    onPickedUp: () -> Unit,
    onDroppedTarget: (KitchenTarget) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentBounds by remember { mutableStateOf<Rect?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.28f else 1.0f,
        animationSpec = tween(180),
        label = "kitchenDragScale"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .size(itemSize)
            .scale(scale)
            .zIndex(if (isDragging) 60f else 10f)
            .onGloballyPositioned { coords ->
                currentBounds = coords.boundsInWindow()
            }
            .pointerInput(fridgeBounds, shelfBounds) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onPickedUp()
                    },
                    onDragEnd = {
                        isDragging = false
                        val itemRect = currentBounds
                        val fRect = fridgeBounds
                        val sRect = shelfBounds

                        var droppedTarget: KitchenTarget? = null

                        if (itemRect != null) {
                            // Hitbox inferior acotada (clamped) para no invadir la bandeja inferior
                            val safeFridgeHitbox = fRect?.let {
                                Rect(
                                    left = it.left - it.width * 0.25f,
                                    top = it.top - it.height * 0.20f,
                                    right = it.right + it.width * 0.15f,
                                    bottom = minOf(it.bottom, trayTopBoundaryPx)
                                )
                            }
                            val safeShelfHitbox = sRect?.let {
                                Rect(
                                    left = it.left - it.width * 0.15f,
                                    top = it.top - it.height * 0.20f,
                                    right = it.right + it.width * 0.25f,
                                    bottom = minOf(it.bottom, trayTopBoundaryPx)
                                )
                            }

                            val center = itemRect.center
                            droppedTarget = when {
                                safeFridgeHitbox?.contains(center) == true -> KitchenTarget.FRIDGE
                                safeShelfHitbox?.contains(center) == true -> KitchenTarget.SHELF
                                else -> null
                            }
                        }

                        if (droppedTarget != null) {
                            onDroppedTarget(droppedTarget)
                        }

                        // Animación suave de retorno con efecto resorte táctil (spring snap)
                        coroutineScope.launch {
                            launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.65f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.65f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.65f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.65f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFFDE7).copy(alpha = 0.92f),
            border = BorderStroke(3.dp, Color(0xFF3E2723)),
            shadowElevation = if (isDragging) 14.dp else 6.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                IllustratedGameItem(itemId = item.id)
            }
        }
    }
}
