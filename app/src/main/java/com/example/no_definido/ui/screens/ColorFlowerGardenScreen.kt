package com.example.no_definido.ui.screens

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.ColorFlowerGardenViewModel
import com.example.no_definido.ui.viewmodel.FlowerMatchResult
import com.example.no_definido.ui.viewmodel.FlowerShapeItem
import com.example.no_definido.ui.viewmodel.GardenShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ColorFlowerGardenScreen(
    activeUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ColorFlowerGardenViewModel = viewModel()
) {
    val soundHelper = rememberSoundHelper()
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val starsEarned by viewModel.starsEarned.collectAsState()

    val potBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    var bottomTrayBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    LaunchedEffect(uiState.currentRound) {
        if (!uiState.isCompleted) {
            delay(350)
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    // Progresión automática de ronda tras completar todas las flores de la ronda actual
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isCompleted) {
            delay(1200) // Breve pausa para apreciar el jardín floreciendo
            val roundData = ColorFlowerGardenViewModel.ROUNDS.getOrNull(uiState.currentRound - 1)
            if (roundData != null) {
                soundHelper.speak(roundData.completionSpeech)
            }
            delay(1800)
            viewModel.nextRound()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flowerSway")
    val flowerSway by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flowerSway"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF81C784))
    ) {
        val screenH = maxHeight
        val screenW = maxWidth

        // 1. FONDO DEL JARDÍN MEXICA SOLEADO
        Image(
            painter = painterResource(id = R.drawable.tree),
            contentDescription = "Jardín de colores",
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
                viewModel.resetGame(randomize = true)
            }
        )

        // 3. MACETAS DE FLORES EN EL CENTRO CON TALLAJE DINÁMICO Y PÉTALOS FLORECIENTES
        val potCount = uiState.activePots.size.coerceAtLeast(1)
        val potSpacing: Dp = if (potCount <= 3) 24.dp else if (potCount <= 5) 14.dp else 8.dp
        val potSize: Dp = ((screenW - 48.dp - potSpacing * (potCount - 1)) / potCount).coerceIn(66.dp, 135.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 58.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila de macetas (3, 5 u 8 macetas adaptadas al ancho en paisaje)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(potSpacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.activePots.forEach { potItem ->
                    val isMatched = uiState.matchedFlowers.any { it.id == potItem.id }

                    Box(
                        modifier = Modifier
                            .size(potSize)
                            .onGloballyPositioned { coords ->
                                potBoundsMap[potItem.id] = coords.boundsInWindow()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FlowerPotWithBloom(
                            potItem = potItem,
                            isMatched = isMatched,
                            swayOffset = flowerSway
                        )
                    }
                }
            }

            // 4. BANDEJA INFERIOR DE FLORES DE COLORES PARA ARRASTRAR A SU MACETA
            val badgeSize: Dp = (potSize * 0.78f).coerceIn(52.dp, 84.dp)
            val trayFlowers = uiState.remainingFlowers

            Box(
                modifier = Modifier
                    .zIndex(35f)
                    .onGloballyPositioned { coords ->
                        bottomTrayBounds = coords.boundsInWindow()
                    }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trayFlowers.forEach { flowerItem ->
                        key(flowerItem.id) {
                            DraggableColorShapeBadge(
                                flower = flowerItem,
                                badgeSize = badgeSize,
                                potBoundsMap = potBoundsMap,
                                bottomTrayBounds = bottomTrayBounds,
                                onPickedUp = {
                                    soundHelper.playPop()
                                    soundHelper.speak(flowerItem.spokenName)
                                },
                                onMatched = { targetPotId ->
                                    val matchResult = viewModel.matchFlowerToPot(flowerItem.id, targetPotId)
                                    if (matchResult is FlowerMatchResult.Correct) {
                                        soundHelper.playSuccess()
                                        soundHelper.speak(flowerItem.spokenSuccess)
                                    } else if (matchResult is FlowerMatchResult.Mismatch) {
                                        soundHelper.playGentleTryAgain()
                                        soundHelper.speak(matchResult.hintSpeech)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. CELEBRACIÓN VISUAL AL COMPLETAR EL JARDÍN (Grand Win 3 Estrellas)
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive,
            bigVisualBadge = "🌸 🔴 🟦 🔺 ⭐ 💎 ✨",
            onPlayAgain = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = onBack
        )
    }
}

@Composable
private fun FlowerPotWithBloom(
    potItem: FlowerShapeItem,
    isMatched: Boolean,
    swayOffset: Float
) {
    val bloomProgress by animateFloatAsState(
        targetValue = if (isMatched) 1.0f else 0.0f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessLow
        ),
        label = "petalBloom"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.045f).coerceIn(3.5f, 6.5f)

        // 1. Tallo verde
        drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(w * 0.5f, h * 0.40f),
            end = Offset(w * 0.5f, h * 0.68f),
            strokeWidth = stroke * 2.2f
        )

        // 2. Maceta de barro terracota
        val potPath = Path().apply {
            moveTo(w * 0.26f, h * 0.64f)
            lineTo(w * 0.74f, h * 0.64f)
            lineTo(w * 0.65f, h * 0.94f)
            lineTo(w * 0.35f, h * 0.94f)
            close()
        }
        drawPath(potPath, color = Color(0xFFE65100))
        drawPath(potPath, color = Color(0xFF3E2723), style = Stroke(stroke, join = StrokeJoin.Round))

        // Borde superior de la maceta
        drawRoundRect(
            color = Color(0xFFFF8A65),
            topLeft = Offset(w * 0.22f, h * 0.59f),
            size = Size(w * 0.56f, h * 0.08f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = Color(0xFF3E2723),
            topLeft = Offset(w * 0.22f, h * 0.59f),
            size = Size(w * 0.56f, h * 0.08f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(stroke)
        )

        // 3. Pétalos florecientes de la flor con apertura animada radial
        val flowerCenter = Offset(w * 0.5f + swayOffset, h * 0.32f)
        val petalBaseRadius = w * (0.05f + 0.09f * bloomProgress)
        val petalDist = w * (0.08f + 0.10f * bloomProgress)

        for (p in 0 until 6) {
            val rad = Math.toRadians((p * 60).toDouble())
            val px = flowerCenter.x + (cos(rad) * petalDist).toFloat()
            val py = flowerCenter.y + (sin(rad) * petalDist).toFloat()
            drawCircle(
                color = if (isMatched) potItem.color else potItem.color.copy(alpha = 0.35f),
                radius = petalBaseRadius,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color(0xFF3E2723),
                radius = petalBaseRadius,
                center = Offset(px, py),
                style = Stroke(stroke * 0.85f)
            )
        }

        // 4. Centro de la flor con aureola
        val centerRadius = w * 0.17f
        drawCircle(color = Color(0xFFFFFDE7), radius = centerRadius, center = flowerCenter)
        drawCircle(color = Color(0xFF3E2723), radius = centerRadius, center = flowerCenter, style = Stroke(stroke))

        // 5. Silueta de la figura en el centro (transparente si no está emparejada, llena si ya floreció)
        drawGardenShape(
            shape = potItem.shape,
            color = potItem.color,
            darkBorder = potItem.darkBorder,
            center = flowerCenter,
            radius = w * 0.105f,
            filled = isMatched,
            strokeWidth = stroke
        )
    }
}

@Composable
private fun DraggableColorShapeBadge(
    flower: FlowerShapeItem,
    badgeSize: Dp,
    potBoundsMap: Map<String, Rect>,
    bottomTrayBounds: Rect?,
    onPickedUp: () -> Unit,
    onMatched: (String) -> Unit
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
        label = "shapeScale"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .size(badgeSize)
            .scale(scale)
            .zIndex(if (isDragging) 60f else 10f)
            .onGloballyPositioned { coords ->
                currentBounds = coords.boundsInWindow()
            }
            .pointerInput(potBoundsMap.size, bottomTrayBounds) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onPickedUp()
                    },
                    onDragEnd = {
                        isDragging = false
                        val itemRect = currentBounds
                        val trayTop = bottomTrayBounds?.top ?: Float.MAX_VALUE
                        val safetyMarginPx = with(density) { 10.dp.toPx() }

                        // Clamping de seguridad estricto: el centro de la flor debe estar POR ENCIMA de la bandeja
                        if (itemRect != null && itemRect.center.y < (trayTop - safetyMarginPx)) {
                            val flowerCenter = itemRect.center
                            val hitPot = potBoundsMap.entries.firstOrNull { (_, potRect) ->
                                val safeHitbox = Rect(
                                    left = potRect.left - potRect.width * 0.12f,
                                    top = potRect.top - potRect.height * 0.12f,
                                    right = potRect.right + potRect.width * 0.12f,
                                    bottom = minOf(potRect.bottom, trayTop - safetyMarginPx)
                                )
                                safeHitbox.contains(flowerCenter)
                            }?.key

                            if (hitPot != null) {
                                onMatched(hitPot)
                            }
                        }

                        // Retorno elástico físico (Spring return) hacia la posición original
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
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFFFFDE7),
            border = BorderStroke(3.5.dp, Color(0xFF3E2723)),
            shadowElevation = if (isDragging) 14.dp else 6.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = (size.width * 0.045f).coerceIn(3.5f, 6.5f)
                drawGardenShape(
                    shape = flower.shape,
                    color = flower.color,
                    darkBorder = flower.darkBorder,
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.34f,
                    filled = true,
                    strokeWidth = stroke
                )
            }
        }
    }
}

private fun DrawScope.drawGardenShape(
    shape: GardenShape,
    color: Color,
    darkBorder: Color,
    center: Offset,
    radius: Float,
    filled: Boolean,
    strokeWidth: Float = 5.5f
) {
    val fillColor = if (filled) color else color.copy(alpha = 0.28f)
    val strokeColor = if (filled) Color(0xFF3E2723) else darkBorder

    when (shape) {
        GardenShape.CIRCLE -> {
            drawCircle(color = fillColor, radius = radius, center = center)
            drawCircle(color = strokeColor, radius = radius, center = center, style = Stroke(strokeWidth))
        }
        GardenShape.SQUARE -> {
            val side = radius * 1.6f
            val topLeft = Offset(center.x - side / 2f, center.y - side / 2f)
            drawRoundRect(
                color = fillColor,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = strokeColor,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(strokeWidth)
            )
        }
        GardenShape.TRIANGLE -> {
            val path = Path().apply {
                moveTo(center.x, center.y - radius * 1.05f)
                lineTo(center.x + radius * 1.0f, center.y + radius * 0.85f)
                lineTo(center.x - radius * 1.0f, center.y + radius * 0.85f)
                close()
            }
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
        GardenShape.DIAMOND -> {
            val path = Path().apply {
                moveTo(center.x, center.y - radius * 1.15f)
                lineTo(center.x + radius * 0.95f, center.y)
                lineTo(center.x, center.y + radius * 1.15f)
                lineTo(center.x - radius * 0.95f, center.y)
                close()
            }
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
        GardenShape.STAR -> {
            val path = Path().apply {
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) radius * 1.15f else radius * 0.50f
                    val angle = Math.toRadians((i * 36.0 - 90.0))
                    val x = center.x + (cos(angle) * r).toFloat()
                    val y = center.y + (sin(angle) * r).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
        GardenShape.OVAL -> {
            val ovalW = radius * 2.2f
            val ovalH = radius * 1.4f
            val topLeft = Offset(center.x - ovalW / 2f, center.y - ovalH / 2f)
            drawOval(color = fillColor, topLeft = topLeft, size = Size(ovalW, ovalH))
            drawOval(color = strokeColor, topLeft = topLeft, size = Size(ovalW, ovalH), style = Stroke(strokeWidth))
        }
        GardenShape.HEART -> {
            val path = Path().apply {
                val h = radius * 1.1f
                val w = radius * 1.1f
                moveTo(center.x, center.y + h * 0.7f)
                cubicTo(
                    center.x - w * 1.2f, center.y + h * 0.1f,
                    center.x - w * 1.2f, center.y - h * 0.8f,
                    center.x, center.y - h * 0.35f
                )
                cubicTo(
                    center.x + w * 1.2f, center.y - h * 0.8f,
                    center.x + w * 1.2f, center.y + h * 0.1f,
                    center.x, center.y + h * 0.7f
                )
                close()
            }
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
        GardenShape.PENTAGON -> {
            val path = Path().apply {
                for (i in 0 until 5) {
                    val angle = Math.toRadians((i * 72.0 - 90.0))
                    val x = center.x + (cos(angle) * radius * 1.1f).toFloat()
                    val y = center.y + (sin(angle) * radius * 1.1f).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, color = fillColor)
            drawPath(path, color = strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
    }
}
