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
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.no_definido.R
import com.example.no_definido.data.HouseRoomId
import com.example.no_definido.data.VisualHouseItem
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.IllustratedGameItem
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.ActivityViewModel
import com.example.no_definido.ui.viewmodel.DropResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ActivityEngineScreen(
    viewModel: ActivityViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    activeUserId: String = "jugador_invitado"
) {
    val uiState by viewModel.uiState.collectAsState()
    val soundHelper = rememberSoundHelper()

    val roomBoundsMap = remember { mutableStateMapOf<HouseRoomId, Rect>() }

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    // Guía de voz al cambiar de ronda o al inicio
    LaunchedEffect(uiState.currentRound) {
        val roundData = uiState.currentRoundData
        if (roundData != null) {
            soundHelper.speak(roundData.spokenIntroduction)
            delay(1200)
            uiState.currentItem?.let { item ->
                soundHelper.speak(item.spokenPrompt)
            }
        }
    }

    // Guía de voz al cambiar de objeto dentro de la misma ronda
    LaunchedEffect(uiState.currentItemIndex) {
        if (uiState.currentItemIndex > 0 && !uiState.isRoundCompleted && !uiState.isActivityCompleted) {
            uiState.currentItem?.let { item ->
                delay(300)
                soundHelper.speak(item.spokenPrompt)
            }
        }
    }

    // Auto-avance suave entre rondas progresivas
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isActivityCompleted) {
            delay(2000)
            viewModel.nextRound()
        }
    }

    fun handleDropInRoom(roomChosen: HouseRoomId) {
        val currentItem = uiState.currentItem ?: return
        if (uiState.isRoundCompleted || uiState.isActivityCompleted) return

        if (!uiState.activeRooms.contains(roomChosen)) {
            soundHelper.playGentleTryAgain()
            soundHelper.speak("¡Esa habitación no está activa en esta ronda! Prueba en otra.")
            return
        }

        when (val result = viewModel.onItemDroppedInRoom(roomChosen)) {
            is DropResult.Correct -> {
                soundHelper.playSuccess()
                if (result.isGameFinished) {
                    soundHelper.speak("¡Felicidades! ¡Acomodaste toda la casa y ganaste las tres estrellas!")
                } else if (result.isRoundFinished) {
                    val completionMsg = uiState.currentRoundData?.spokenCompletion
                        ?: "¡Ronda completada! ¡Ganaste una estrella!"
                    soundHelper.speak("${currentItem.spokenSuccess} $completionMsg")
                } else {
                    soundHelper.speak(currentItem.spokenSuccess)
                }
            }
            is DropResult.Incorrect -> {
                soundHelper.playGentleTryAgain()
                soundHelper.speak("¡Casi! Mira bien los muebles de cada cuarto.")
            }
            DropResult.Ignored -> {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "housePulse")
    val roomBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "roomBorderAlpha"
    )
    val itemFloatY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "itemFloatY"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEF3C7))
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // 1. FONDO DE LA CASA CORTADA EN 4 HABITACIONES AMUEBLADAS (Cero texto)
        Image(
            painter = painterResource(id = R.drawable.bg_house_rooms),
            contentDescription = "Casa Interactiva",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. BARRA SUPERIOR VISUAL (ESTRELLAS 1..3 POR RONDA)
        VisualGameTopBar(
            currentStars = uiState.starsEarned,
            totalStars = uiState.totalRounds,
            onBackToRoom = onBack,
            onSpeakHint = {
                uiState.currentItem?.let { soundHelper.speak(it.spokenPrompt) }
            },
            onReset = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            }
        )

        // 3. LAS 4 HABITACIONES INTERACTIVAS SOBRE LA CASA ILUSTRADA
        // Arriba-Izquierda: RECÁMARA (Cama)
        val isBedroomActive = uiState.activeRooms.contains(HouseRoomId.BEDROOM)
        HouseRoomZone(
            roomId = HouseRoomId.BEDROOM,
            placedItems = uiState.placedItemsByRoom[HouseRoomId.BEDROOM] ?: emptyList(),
            isActive = isBedroomActive,
            glowColor = Color(0xFFF472B6).copy(alpha = if (isBedroomActive) roomBorderAlpha else 0.15f),
            modifier = Modifier
                .offset(x = screenW * 0.28f, y = screenH * 0.17f)
                .size(width = screenW * 0.32f, height = screenH * 0.34f),
            onBoundsUpdated = { roomBoundsMap[HouseRoomId.BEDROOM] = it },
            onRoomTapped = { handleDropInRoom(HouseRoomId.BEDROOM) }
        )

        // Arriba-Derecha: BAÑO (Bañera y Lavabo)
        val isBathroomActive = uiState.activeRooms.contains(HouseRoomId.BATHROOM)
        HouseRoomZone(
            roomId = HouseRoomId.BATHROOM,
            placedItems = uiState.placedItemsByRoom[HouseRoomId.BATHROOM] ?: emptyList(),
            isActive = isBathroomActive,
            glowColor = Color(0xFF38BDF8).copy(alpha = if (isBathroomActive) roomBorderAlpha else 0.15f),
            modifier = Modifier
                .offset(x = screenW * 0.64f, y = screenH * 0.17f)
                .size(width = screenW * 0.33f, height = screenH * 0.34f),
            onBoundsUpdated = { roomBoundsMap[HouseRoomId.BATHROOM] = it },
            onRoomTapped = { handleDropInRoom(HouseRoomId.BATHROOM) }
        )

        // Abajo-Izquierda: COCINA (Estufa)
        val isKitchenActive = uiState.activeRooms.contains(HouseRoomId.KITCHEN)
        HouseRoomZone(
            roomId = HouseRoomId.KITCHEN,
            placedItems = uiState.placedItemsByRoom[HouseRoomId.KITCHEN] ?: emptyList(),
            isActive = isKitchenActive,
            glowColor = Color(0xFFFBBF24).copy(alpha = if (isKitchenActive) roomBorderAlpha else 0.15f),
            modifier = Modifier
                .offset(x = screenW * 0.28f, y = screenH * 0.56f)
                .size(width = screenW * 0.32f, height = screenH * 0.38f),
            onBoundsUpdated = { roomBoundsMap[HouseRoomId.KITCHEN] = it },
            onRoomTapped = { handleDropInRoom(HouseRoomId.KITCHEN) }
        )

        // Abajo-Derecha: SALA (Sillón y TV)
        val isLivingRoomActive = uiState.activeRooms.contains(HouseRoomId.LIVING_ROOM)
        HouseRoomZone(
            roomId = HouseRoomId.LIVING_ROOM,
            placedItems = uiState.placedItemsByRoom[HouseRoomId.LIVING_ROOM] ?: emptyList(),
            isActive = isLivingRoomActive,
            glowColor = Color(0xFF34D399).copy(alpha = if (isLivingRoomActive) roomBorderAlpha else 0.15f),
            modifier = Modifier
                .offset(x = screenW * 0.64f, y = screenH * 0.56f)
                .size(width = screenW * 0.33f, height = screenH * 0.38f),
            onBoundsUpdated = { roomBoundsMap[HouseRoomId.LIVING_ROOM] = it },
            onRoomTapped = { handleDropInRoom(HouseRoomId.LIVING_ROOM) }
        )

        // 4. OBJETO ACTUAL A LA IZQUIERDA (EN EL JARDÍN/PÓRTICO DE LA CASA) PARA ARRASTRAR A SU HABITACIÓN
        val currentItem = uiState.currentItem
        if (!uiState.isActivityCompleted && currentItem != null) {
            val itemBubbleSize = (screenH * 0.28f).coerceIn(96.dp, 138.dp)
            val spawnX = (screenW * 0.13f) - (itemBubbleSize / 2f)
            val spawnY = (screenH * 0.54f) - (itemBubbleSize / 2f)

            key(currentItem.id, uiState.currentRound, uiState.currentItemIndex) {
                DraggableHouseItem(
                    item = currentItem,
                    bubbleSize = itemBubbleSize,
                    idleFloatY = itemFloatY,
                    roomBoundsMap = roomBoundsMap,
                    modifier = Modifier
                        .offset(x = spawnX, y = spawnY)
                        .zIndex(45f),
                    onPickedUp = {
                        soundHelper.playPop()
                        soundHelper.speak(currentItem.spokenName)
                    },
                    onDroppedInRoom = { room ->
                        handleDropInRoom(room)
                    }
                )
            }
        }

        // 5. CELEBRACIÓN VISUAL AL COMPLETAR TODAS LAS RONDAS
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive || uiState.showRewardDialog,
            bigVisualBadge = "🏠 🪥 🛏️ 🍳 ✨",
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
private fun HouseRoomZone(
    roomId: HouseRoomId,
    placedItems: List<String>,
    isActive: Boolean,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onBoundsUpdated: (Rect) -> Unit,
    onRoomTapped: () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = if (isActive) 3.5.dp else 1.5.dp,
                color = glowColor,
                shape = RoundedCornerShape(16.dp)
            )
            .onGloballyPositioned { coords ->
                onBoundsUpdated(coords.boundsInWindow())
            }
            .clickable { onRoomTapped() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            placedItems.forEach { placedId ->
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(tween(300)) + fadeIn()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.88f),
                        border = BorderStroke(2.dp, Color(0xFF3E2723)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IllustratedGameItem(itemId = placedId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableHouseItem(
    item: VisualHouseItem,
    bubbleSize: androidx.compose.ui.unit.Dp,
    idleFloatY: Float,
    roomBoundsMap: Map<HouseRoomId, Rect>,
    modifier: Modifier = Modifier,
    onPickedUp: () -> Unit,
    onDroppedInRoom: (HouseRoomId) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentBounds by remember { mutableStateOf<Rect?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        animationSpec = tween(180),
        label = "houseItemScale"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(animOffsetX.value.roundToInt(), animOffsetY.value.roundToInt()) }
            .size(bubbleSize)
            .scale(scale)
            .graphicsLayer {
                translationY = if (isDragging) 0f else idleFloatY
            }
            .onGloballyPositioned { coords ->
                currentBounds = coords.boundsInWindow()
            }
            .pointerInput(roomBoundsMap.size) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onPickedUp()
                    },
                    onDragEnd = {
                        isDragging = false
                        val itemRect = currentBounds
                        var matchedRoom: HouseRoomId? = null
                        if (itemRect != null) {
                            matchedRoom = roomBoundsMap.entries.firstOrNull { (_, roomRect) ->
                                val inflated = Rect(
                                    left = roomRect.left - roomRect.width * 0.15f,
                                    top = roomRect.top - roomRect.height * 0.12f,
                                    right = roomRect.right + roomRect.width * 0.15f,
                                    bottom = roomRect.bottom + roomRect.height * 0.12f
                                )
                                inflated.contains(itemRect.center) || inflated.overlaps(itemRect)
                            }?.key
                        }

                        if (matchedRoom != null) {
                            onDroppedInRoom(matchedRoom)
                        }

                        // Rebote elástico suave de vuelta al jardín
                        coroutineScope.launch {
                            animOffsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        coroutineScope.launch {
                            animOffsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch { animOffsetX.snapTo(0f) }
                        coroutineScope.launch { animOffsetY.snapTo(0f) }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        animOffsetX.snapTo(animOffsetX.value + dragAmount.x)
                        animOffsetY.snapTo(animOffsetY.value + dragAmount.y)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFFDE7),
            border = BorderStroke(4.dp, Color(0xFFFFB300)),
            shadowElevation = if (isDragging) 16.dp else 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                IllustratedGameItem(itemId = item.id)
            }
        }
    }
}
