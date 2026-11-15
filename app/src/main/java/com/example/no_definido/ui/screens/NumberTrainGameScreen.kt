package com.example.no_definido.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.IllustratedGameItem
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.components.WoodenTrainInRoomObject
import com.example.no_definido.ui.viewmodel.NumberTrainViewModel
import com.example.no_definido.ui.viewmodel.TrainAnimationState
import com.example.no_definido.ui.viewmodel.TrainDropResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NumberTrainGameScreen(
    activeUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NumberTrainViewModel = viewModel()
) {
    val soundHelper = rememberSoundHelper()
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val starsEarned by viewModel.starsEarned.collectAsState()
    val trainAnimationState by viewModel.trainAnimationState.collectAsState()

    var wagonBounds by remember { mutableStateOf<Rect?>(null) }
    var bottomTrayBounds by remember { mutableStateOf<Rect?>(null) }

    val trainSlideOffset = remember { Animatable(0f) }

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    LaunchedEffect(uiState.currentRound) {
        if (!uiState.isCompleted) {
            delay(350)
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    // Máquina de estados animada para salida y llegada fluida del tren entre rondas
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isCompleted) {
            // 1. Pausa breve para que el niño vea el vagón lleno
            delay(650)
            soundHelper.playSuccess()

            // 2. Salida: El tren acelera hacia la izquierda fuera de la pantalla
            trainSlideOffset.animateTo(
                targetValue = -2500f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )

            // 3. Avanza de ronda en el ViewModel mientras está oculto
            viewModel.nextRound()

            // 4. Snap invisible al borde derecho fuera de pantalla
            trainSlideOffset.snapTo(2500f)

            // 5. Llegada fluida desde la derecha hacia el centro
            trainSlideOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )

            // 6. Llegada finalizada: se reactivan los gestos y se anuncia la nueva ronda
            viewModel.onArrivalComplete()
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    fun addOneCargo() {
        if (uiState.isRoundCompleted || uiState.isCompleted || trainAnimationState != TrainAnimationState.IDLE) return
        val result = viewModel.loadCargo()
        if (result is TrainDropResult.Loaded) {
            soundHelper.playSuccess()
            if (result.isGameFinished || result.isRoundFinished) {
                soundHelper.speak("${result.countSpokenWord} ${uiState.currentRoundData.completionSpeech}")
            } else {
                soundHelper.speak(result.countSpokenWord)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF38BDF8))
    ) {
        val screenH = maxHeight
        val screenW = maxWidth
        val screenWidthPx = with(LocalDensity.current) { screenW.toPx() }

        // 1. FONDO DEL PRADO MEXICA CON VÍAS DE TREN
        Image(
            painter = painterResource(id = R.drawable.bg_train_meadow),
            contentDescription = "Vías del tren",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. BARRA SUPERIOR VISUAL (3 Estrellas del Contrato Mini-Game)
        VisualGameTopBar(
            currentStars = starsEarned,
            totalStars = 3,
            onBackToRoom = onBack,
            onSpeakHint = {
                soundHelper.speak(viewModel.getCurrentHintSpeech())
            },
            onReset = {
                soundHelper.playPop()
                coroutineScope.launch {
                    trainSlideOffset.snapTo(0f)
                }
                viewModel.resetGame(randomize = true)
            }
        )

        // 3. TREN Y VAGÓN DE CARGA CON ANIMACIÓN CONTINUA DESDE/HACIA LA PRADERA
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-14).dp)
                .offset { IntOffset(trainSlideOffset.value.roundToInt(), 0) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Locomotora Mexica
                Box(
                    modifier = Modifier.size((screenH * 0.36f).coerceIn(120.dp, 175.dp))
                ) {
                    WoodenTrainInRoomObject()
                }

                // Vagón de carga con Número Objetivo y Casillas Discretas
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Medallón con el Número Objetivo gigante
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD54F),
                        border = BorderStroke(4.dp, Color(0xFF3E2723)),
                        shadowElevation = 10.dp,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${uiState.targetCount}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Caja del vagón: Casillas discretas (1 fila en R1 y R2, 2 filas x 4 columnas en R3)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFFF8E1),
                        border = BorderStroke(5.dp, Color(0xFF8D6E63)),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                wagonBounds = coords.boundsInWindow()
                            }
                    ) {
                        if (uiState.targetCount <= 5) {
                            // Rondas 1 y 2: Fila única espaciosa
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(uiState.targetCount) { slotIdx ->
                                    val isFilled = slotIdx < uiState.loadedCount
                                    val slotSize = (screenH * 0.16f).coerceIn(50.dp, 72.dp)
                                    WagonSlot(
                                        slotIdx = slotIdx,
                                        isFilled = isFilled,
                                        cargoItemId = uiState.currentCargoItem.id,
                                        slotSize = slotSize
                                    )
                                }
                            }
                        } else {
                            // Ronda 3 (6 a 8 objetos): Rejilla de 2 filas equilibradas
                            val topRowCount = (uiState.targetCount + 1) / 2
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (slotIdx in 0 until topRowCount) {
                                        val isFilled = slotIdx < uiState.loadedCount
                                        val slotSize = (screenH * 0.12f).coerceIn(42.dp, 56.dp)
                                        WagonSlot(
                                            slotIdx = slotIdx,
                                            isFilled = isFilled,
                                            cargoItemId = uiState.currentCargoItem.id,
                                            slotSize = slotSize
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (slotIdx in topRowCount until uiState.targetCount) {
                                        val isFilled = slotIdx < uiState.loadedCount
                                        val slotSize = (screenH * 0.12f).coerceIn(42.dp, 56.dp)
                                        WagonSlot(
                                            slotIdx = slotIdx,
                                            isFilled = isFilled,
                                            cargoItemId = uiState.currentCargoItem.id,
                                            slotSize = slotSize
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Ruedas del vagón
                    Row(
                        modifier = Modifier.width(220.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(2) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF5350),
                                border = BorderStroke(3.5.dp, Color(0xFF3E2723)),
                                modifier = Modifier.size(32.dp)
                            ) {}
                        }
                    }
                }
            }
        }

        // 4. BANDEJA INFERIOR DE OBJETOS ARRASTRABLES Y TOCABLES
        val itemSize = (screenH * 0.18f).coerceIn(64.dp, 88.dp)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .zIndex(30f)
                .onGloballyPositioned { coords ->
                    bottomTrayBounds = coords.boundsInWindow()
                }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(uiState.remainingToLoad) { idx ->
                    key("${uiState.currentRound}_${uiState.loadedCount}_$idx") {
                        DraggableTrainCargoItem(
                            itemId = uiState.currentCargoItem.id,
                            itemSize = itemSize,
                            wagonBounds = wagonBounds,
                            bottomTrayBounds = bottomTrayBounds,
                            isInteractionEnabled = trainAnimationState == TrainAnimationState.IDLE && !uiState.isRoundCompleted,
                            onPickedUp = {
                                soundHelper.playPop()
                                soundHelper.speak(uiState.currentCargoItem.spokenTouchPrompt)
                            },
                            onLoaded = { addOneCargo() }
                        )
                    }
                }
            }
        }

        // 5. CELEBRACIÓN VISUAL AL COMPLETAR TODOS LOS TRENES (M4 Grand Win)
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive,
            bigVisualBadge = "🚂 🌽 🏺 💎 ✨",
            onPlayAgain = {
                soundHelper.playPop()
                coroutineScope.launch {
                    trainSlideOffset.snapTo(0f)
                }
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = onBack
        )
    }
}

@Composable
private fun WagonSlot(
    slotIdx: Int,
    isFilled: Boolean,
    cargoItemId: String,
    slotSize: Dp
) {
    Box(
        modifier = Modifier
            .size(slotSize)
            .background(
                color = if (isFilled) Color(0xFFFFECB3) else Color(0xFFE2E8F0),
                shape = CircleShape
            )
            .border(
                width = 2.5.dp,
                color = if (isFilled) Color(0xFFFFB300) else Color(0xFF94A3B8),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isFilled,
            enter = scaleIn(tween(260)) + fadeIn()
        ) {
            IllustratedGameItem(
                itemId = cargoItemId,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            )
        }
        if (!isFilled) {
            Text(
                text = "${slotIdx + 1}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun DraggableTrainCargoItem(
    itemId: String,
    itemSize: Dp,
    wagonBounds: Rect?,
    bottomTrayBounds: Rect?,
    isInteractionEnabled: Boolean,
    onPickedUp: () -> Unit,
    onLoaded: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentBounds by remember { mutableStateOf<Rect?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.28f else 1.0f,
        animationSpec = tween(180),
        label = "trainCargoScale"
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
            .clickable(enabled = isInteractionEnabled) {
                // Accesibilidad táctil directa: tocar para cargar inmediatamente
                onLoaded()
            }
            .pointerInput(wagonBounds, bottomTrayBounds, isInteractionEnabled) {
                if (!isInteractionEnabled) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onPickedUp()
                    },
                    onDragEnd = {
                        isDragging = false
                        val itemRect = currentBounds
                        val targetRect = wagonBounds
                        val trayRect = bottomTrayBounds
                        val minClearancePx = with(density) { 40.dp.toPx() }

                        // Clamping estricto: el objeto debe haberse arrastrado hacia arriba fuera de la bandeja
                        val clearedTray = (offsetY.value < -minClearancePx) ||
                            (trayRect != null && itemRect != null && itemRect.center.y < (trayRect.top - 12f))

                        val isValidDrop = clearedTray &&
                            itemRect != null &&
                            targetRect != null &&
                            targetRect.contains(itemRect.center)

                        if (isValidDrop) {
                            onLoaded()
                            coroutineScope.launch {
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                            }
                        } else {
                            // Retorno elástico táctil (Spring physics) si se suelta fuera o no libró la bandeja
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
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            launch {
                                offsetX.animateTo(0f, spring(0.65f, Spring.StiffnessMediumLow))
                            }
                            launch {
                                offsetY.animateTo(0f, spring(0.65f, Spring.StiffnessMediumLow))
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
            color = Color(0xFFFFFDE7),
            border = BorderStroke(3.dp, Color(0xFF3E2723)),
            shadowElevation = if (isDragging) 12.dp else 6.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                IllustratedGameItem(itemId = itemId)
            }
        }
    }
}
