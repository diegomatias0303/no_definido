package com.example.no_definido.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.BeachBallAndBubblesInRoomObject
import com.example.no_definido.ui.components.ButterflyColorsInRoomObject
import com.example.no_definido.ui.components.KitchenFridgeInRoomObject
import com.example.no_definido.ui.components.MagicBooksInRoomObject
import com.example.no_definido.ui.components.TeddyAndHomeItemsInRoomObject
import com.example.no_definido.ui.components.WallClockInRoomObject
import com.example.no_definido.ui.components.WoodenTrainInRoomObject
import com.example.no_definido.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FirstPersonPanoramicRoomScreen(
    onOpenAppleGame: () -> Unit,
    onOpenObjectGame: () -> Unit,
    onOpenClockGame: () -> Unit,
    onOpenKitchenGame: () -> Unit,
    onOpenTrainGame: () -> Unit,
    onOpenGardenGame: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenMemoryGame: () -> Unit = {},
    onOpenBubbleGame: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    val userProgress by homeViewModel.userProgress.collectAsState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val soundHelper = rememberSoundHelper()

    // Identificador del objeto de minijuego que se está elevando al ser tocado
    var activatingObjectId by remember { mutableStateOf<String?>(null) }

    // Estados interactivos de ambientación en la sala ("interactuar con más cosas de la sala")
    var isNightLampMode by remember { mutableStateOf(false) }
    var isDeskLampLit by remember { mutableStateOf(false) }
    var isShelfCarZooming by remember { mutableStateOf(false) }
    var isCushionBouncing by remember { mutableStateOf(false) }
    var isWindowRainbowActive by remember { mutableStateOf(false) }

    // Cero animaciones infinitas en reposo: los objetos permanecen apoyados naturalmente en el piso/muebles
    val sharedGlowPulseState: State<Float>? = null
    val sharedFloatYState: State<Float>? = null

    LaunchedEffect(Unit) {
        delay(500)
        soundHelper.speak("¡Bienvenido a Cal-mé-cac! Desliza a los lados para explorar toda la sala y toca los objetos para jugar.")
    }

    fun triggerObjectOpen(objectId: String, spokenIntro: String, onNavigate: () -> Unit) {
        if (activatingObjectId == null) {
            coroutineScope.launch {
                activatingObjectId = objectId
                soundHelper.playPop()
                soundHelper.speak(spokenIntro)
                delay(420)
                onNavigate()
                activatingObjectId = null
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFB5E5FC))
    ) {
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val availableHeightDp = maxHeight

        // Proporción exacta de la sala única continua back.jpeg (2096 x 768 = 2.7292:1)
        val panoramaAspectRatio = 2096f / 768f
        val panoramicWidthDp = maxOf(availableHeightDp * panoramaAspectRatio, maxWidth * 1.48f)
        val panoramicWidthPx = with(density) { panoramicWidthDp.toPx() }

        val minOffset = -(panoramicWidthPx - availableWidthPx).coerceAtLeast(1f)
        val maxOffset = 0f

        // Iniciar centrado sobre el sillón amarillo y la mesa blanca con la canasta de manzanas
        val initialOffset = (minOffset * 0.50f)
        var targetOffsetX by remember(panoramicWidthPx, availableWidthPx) {
            mutableFloatStateOf(initialOffset)
        }

        val animatedOffsetXState = animateFloatAsState(
            targetValue = targetOffsetX.coerceIn(minOffset, maxOffset),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "fnafPanoramicCameraOffset"
        )

        val dragSensitivity = 1.55f
        val stepPanAmountPx = availableWidthPx * 0.36f

        // Tamaños responsivos de los objetos físicos integrados a escala real de los muebles de la sala
        val basketSize = (availableHeightDp * 0.25f).coerceIn(88.dp, 130.dp)
        val clockSize = (availableHeightDp * 0.20f).coerceIn(70.dp, 102.dp)
        val teddySize = (availableHeightDp * 0.19f).coerceIn(64.dp, 94.dp)
        val booksSize = (availableHeightDp * 0.19f).coerceIn(64.dp, 94.dp)
        val trainSize = (availableHeightDp * 0.22f).coerceIn(76.dp, 112.dp)
        val kitchenCornerWidth = (availableHeightDp * 0.42f).coerceIn(136.dp, 200.dp)
        val kitchenCornerHeight = (availableHeightDp * 0.48f).coerceIn(156.dp, 228.dp)
        val butterflySize = (availableHeightDp * 0.13f).coerceIn(48.dp, 68.dp)
        val beachBallSize = (availableHeightDp * 0.21f).coerceIn(72.dp, 106.dp)

        // 1. LIENZO PANORÁMICO DESLIZABLE (wrapContentWidth(unbounded = true) evita el recorte negro de Compose)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(minOffset, maxOffset) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        targetOffsetX = (targetOffsetX + dragAmount * dragSensitivity)
                            .coerceIn(minOffset, maxOffset)
                    }
                },
            contentAlignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start, unbounded = true)
                    .width(panoramicWidthDp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        // Traslación 100% en GPU sin recomponer el árbol de objetos al deslizar
                        translationX = animatedOffsetXState.value
                    },
                contentAlignment = Alignment.TopStart
            ) {
                // Fondo panorámico continuo de una sola sala (2096x768) sin cortes ni bordes negros
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Sala panorámica interactiva Cal-mé-cac",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                // =========================================================================
                // ELEMENTOS AMBIENTALES DE LA SALA (INTEGRADOS DIRECTAMENTE EN EL FONDO)
                // =========================================================================

                // A. CARRITOS DE LA REPISA SUPERIOR DEL LIBRERO (Usa los carritos ya pintados en back.jpeg)
                val shelfCarsXDp = panoramicWidthDp * 0.234f
                val shelfCarsYDp = availableHeightDp * 0.580f
                Box(
                    modifier = Modifier
                        .offset(x = shelfCarsXDp, y = shelfCarsYDp)
                        .size(
                            width = panoramicWidthDp * 0.102f,
                            height = availableHeightDp * 0.090f
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                soundHelper.playPop()
                                soundHelper.speak("¡Brum, brum! ¡Los carritos de juguete corren por la repisa!")
                                isShelfCarZooming = true
                                delay(850)
                                isShelfCarZooming = false
                            }
                        }
                ) {
                    AnimatedVisibility(
                        visible = isShelfCarZooming,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(250))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Líneas de velocidad doradas junto a los carritos de la repisa
                            drawLine(
                                color = Color(0xFFFFEE58),
                                start = Offset(w * 0.08f, h * 0.35f),
                                end = Offset(w * 0.38f, h * 0.35f),
                                strokeWidth = 5f
                            )
                            drawLine(
                                color = Color(0xFFFFEE58),
                                start = Offset(w * 0.14f, h * 0.62f),
                                end = Offset(w * 0.48f, h * 0.62f),
                                strokeWidth = 5f
                            )
                            drawCircle(
                                color = Color(0xAAFFF59D),
                                radius = h * 0.22f,
                                center = Offset(w * 0.78f, h * 0.48f)
                            )
                        }
                    }
                }

                // B. LAMPARITA ROSA DE LECTURA SOBRE EL LIBRERO AMARILLO
                val deskLampXDp = panoramicWidthDp * 0.228f
                val deskLampYDp = availableHeightDp * 0.395f
                Box(
                    modifier = Modifier
                        .offset(x = deskLampXDp, y = deskLampYDp)
                        .size((availableHeightDp * 0.16f).coerceIn(56.dp, 86.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isDeskLampLit = !isDeskLampLit
                            soundHelper.playPop()
                            if (isDeskLampLit) {
                                soundHelper.speak("¡Clic! Encendiste la lamparita rosa para leer cuentos.")
                            } else {
                                soundHelper.speak("¡Clic! Apagaste la lamparita de lectura.")
                            }
                        }
                ) {
                    if (isDeskLampLit) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val beamPath = Path().apply {
                                moveTo(w * 0.5f, h * 0.25f)
                                lineTo(-w * 0.55f, h * 1.45f)
                                lineTo(w * 1.35f, h * 1.45f)
                                close()
                            }
                            drawPath(
                                path = beamPath,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color(0x99FFF59D),
                                        Color(0x55FFD54F),
                                        Color.Transparent
                                    )
                                )
                            )
                            drawCircle(
                                color = Color(0xAAFFF59D),
                                radius = w * 0.45f,
                                center = Offset(w * 0.5f, h * 0.35f)
                            )
                        }
                    }
                }

                // C. LÁMPARA COLGANTE DEL TECHO (Alterna Modo Noche Estrellada / Luz de Día)
                val ceilingLampXDp = panoramicWidthDp * 0.478f
                val ceilingLampYDp = availableHeightDp * 0.01f
                Box(
                    modifier = Modifier
                        .offset(x = ceilingLampXDp, y = ceilingLampYDp)
                        .size((availableHeightDp * 0.20f).coerceIn(68.dp, 104.dp))
                        .zIndex(15f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isNightLampMode = !isNightLampMode
                            soundHelper.playPop()
                            if (isNightLampMode) {
                                soundHelper.speak("¡Clic! Modo noche mágica con estrellitas brillantes.")
                            } else {
                                soundHelper.speak("¡Clic! Volvió el sol radiante a la sala.")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isNightLampMode) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width * 0.5f
                            val cy = size.height * 0.62f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xAAFFF59D), Color(0x44FFD54F), Color.Transparent)
                                ),
                                radius = size.minDimension * 0.65f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                // D. COJINES DEL SILLÓN AMARILLO (Usa los cojines morado y rosa ya pintados en back.jpeg)
                val cushionXDp = panoramicWidthDp * 0.400f
                val cushionYDp = availableHeightDp * 0.500f
                Box(
                    modifier = Modifier
                        .offset(x = cushionXDp, y = cushionYDp)
                        .size(
                            width = panoramicWidthDp * 0.076f,
                            height = availableHeightDp * 0.160f
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                isCushionBouncing = true
                                soundHelper.playPop()
                                soundHelper.speak("¡Boing, boing! ¡Qué suaves están los cojines del sillón!")
                                delay(450)
                                isCushionBouncing = false
                            }
                        }
                ) {
                    AnimatedVisibility(
                        visible = isCushionBouncing,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(250))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(Color(0x88FFF59D), Color.Transparent)
                                ),
                                radius = size.minDimension * 0.55f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f)
                            )
                        }
                    }
                }

                // E. VENTANA PANORÁMICA CENTRAL (Despliega un arcoíris animado en el cielo al tocarla)
                val windowXDp = panoramicWidthDp * 0.508f
                val windowYDp = availableHeightDp * 0.27f
                Box(
                    modifier = Modifier
                        .offset(x = windowXDp, y = windowYDp)
                        .size(
                            width = (panoramicWidthDp * 0.14f).coerceIn(110.dp, 210.dp),
                            height = (availableHeightDp * 0.20f).coerceIn(70.dp, 125.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                isWindowRainbowActive = !isWindowRainbowActive
                                soundHelper.playSuccess()
                                soundHelper.speak("¡Mira por la ventana! ¡Salió un hermoso arcoíris mágico!")
                            }
                        }
                ) {
                    AnimatedVisibility(
                        visible = isWindowRainbowActive,
                        enter = fadeIn(tween(400)),
                        exit = fadeOut(tween(400))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rainbowColors = listOf(
                                Color(0xCCEF4444),
                                Color(0xCCF97316),
                                Color(0xCCEAB308),
                                Color(0xCC22C55E),
                                Color(0xCC3B82F6),
                                Color(0xCCA855F7)
                            )
                            rainbowColors.forEachIndexed { idx, color ->
                                val inset = idx * 7.5f
                                drawArc(
                                    color = color,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(inset + 10f, inset + 10f),
                                    size = Size(
                                        (size.width - (inset + 10f) * 2f).coerceAtLeast(10f),
                                        (size.height * 1.4f - (inset + 10f) * 2f).coerceAtLeast(10f)
                                    ),
                                    style = Stroke(width = 7f)
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // 8 OBJETOS FÍSICOS DE MINIJUEGOS APOYADOS NATURALMENTE EN LA SALA
                // =========================================================================
                val leftFloorSurfaceYDp = availableHeightDp * 0.928f
                val rugSurfaceYDp = availableHeightDp * 0.948f
                val rightFloorSurfaceYDp = availableHeightDp * 0.945f

                // OBJETO 1: RELOJ DE PARED (Empotrado sobre la pared azul izquierda con sombra de pared)
                val clockXDp = panoramicWidthDp * 0.082f
                val clockYDp = availableHeightDp * 0.14f

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "clock",
                    glowColor = Color(0xFF38BDF8),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = clockXDp, y = clockYDp)
                        .size(clockSize),
                    onClick = {
                        triggerObjectOpen("clock", "¡El reloj del sol y la luna!", onOpenClockGame)
                    }
                ) {
                    WallClockInRoomObject()
                }

                // OBJETO 2: OSITO DE PELUCHE (Sentado sobre el piso de madera izquierdo + ositos de la repisa media)
                val teddyXDp = panoramicWidthDp * 0.045f
                val teddyYDp = leftFloorSurfaceYDp - (teddySize * 0.90f)

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "house",
                    glowColor = Color(0xFFA855F7),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = teddyXDp, y = teddyYDp)
                        .size(teddySize),
                    onClick = {
                        triggerObjectOpen("house", "¡Los objetos de la casa!", onOpenObjectGame)
                    }
                ) {
                    TeddyAndHomeItemsInRoomObject()
                }

                // Zona interactiva adicional: los ositos azul y rosa ya pintados en la repisa media del librero
                Box(
                    modifier = Modifier
                        .offset(
                            x = panoramicWidthDp * 0.234f,
                            y = availableHeightDp * 0.670f
                        )
                        .size(
                            width = panoramicWidthDp * 0.102f,
                            height = availableHeightDp * 0.079f
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerObjectOpen("house", "¡Los objetos de la casa!", onOpenObjectGame)
                        }
                )

                // OBJETO 3: LIBROS DE CUENTOS (Apoyados sobre el piso junto al librero + libros de la repisa inferior)
                val booksXDp = panoramicWidthDp * 0.125f
                val booksYDp = leftFloorSurfaceYDp - (booksSize * 0.84f)

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "memory",
                    glowColor = Color(0xFFF59E0B),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = booksXDp, y = booksYDp)
                        .size(booksSize),
                    onClick = {
                        triggerObjectOpen(
                            "memory",
                            "¡Los libros mágicos del memorama!",
                            onOpenMemoryGame
                        )
                    }
                ) {
                    MagicBooksInRoomObject()
                }

                // Zona interactiva adicional: los libros amarillo, rosa y azul ya pintados en la repisa inferior del librero
                Box(
                    modifier = Modifier
                        .offset(
                            x = panoramicWidthDp * 0.234f,
                            y = availableHeightDp * 0.749f
                        )
                        .size(
                            width = panoramicWidthDp * 0.105f,
                            height = availableHeightDp * 0.118f
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerObjectOpen(
                                "memory",
                                "¡Los libros mágicos del memorama!",
                                onOpenMemoryGame
                            )
                        }
                )

                // OBJETO 4: TREN DE MADERA (Apoyado firmemente sobre la alfombra beige frente al sillón)
                val trainXDp = panoramicWidthDp * 0.338f
                val trainYDp = rugSurfaceYDp - (trainSize * 0.86f)

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "train",
                    glowColor = Color(0xFF4ADE80),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = trainXDp, y = trainYDp)
                        .size(trainSize),
                    onClick = {
                        triggerObjectOpen("train", "¡El tren de los números!", onOpenTrainGame)
                    }
                ) {
                    WoodenTrainInRoomObject()
                }

                // OBJETO 5: CESTA CON MANZANAS (Perfectamente apoyada sobre el centro de la mesa blanca)
                // En back.jpeg (2096x768), el centro exacto de la mesa blanca está en X = 50.38% (1056/2096), Y = 78.4%
                // cesta.png tiene su base opaca en el 92% de su altura
                val basketXDp = (panoramicWidthDp * 0.5038f) - (basketSize * 0.50f)
                val tableSurfaceYDp = availableHeightDp * 0.795f
                val basketYDp = tableSurfaceYDp - (basketSize * 0.92f)

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "basket",
                    glowColor = Color(0xFFFBBF24),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    glowRadiusMultiplier = 1.30f,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = basketXDp, y = basketYDp)
                        .size(basketSize),
                    onClick = {
                        triggerObjectOpen("basket", "¡La cesta de manzanas!", onOpenAppleGame)
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Sombra de contacto suave sobre la superficie blanca de la mesa
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            drawOval(
                                color = Color(0x383E2723),
                                topLeft = Offset(size.width * 0.16f, size.height * 0.84f),
                                size = Size(size.width * 0.68f, size.height * 0.11f)
                            )
                        }
                        // Parte trasera de la cesta
                        Image(
                            painter = painterResource(id = R.drawable.cesta),
                            contentDescription = "Cesta de Manzanas",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        // 3 manzanas rojas brillantes asomándose dentro de la cesta
                        Image(
                            painter = painterResource(id = R.drawable.apple),
                            contentDescription = null,
                            modifier = Modifier
                                .size(basketSize * 0.28f)
                                .offset(x = (-basketSize * 0.13f), y = basketSize * 0.04f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.apple),
                            contentDescription = null,
                            modifier = Modifier
                                .size(basketSize * 0.28f)
                                .offset(x = (basketSize * 0.13f), y = basketSize * 0.04f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.apple),
                            contentDescription = null,
                            modifier = Modifier
                                .size(basketSize * 0.30f)
                                .offset(x = 0.dp, y = basketSize * 0.01f)
                        )
                        // Frente tejido de la cesta para que las manzanas queden adentro
                        Image(
                            painter = painterResource(id = R.drawable.cesta_front),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // OBJETO 6: MACETAS DE FLORES Y MARIPOSA POSADA EN LOS PÉTALOS (Jardín de Colores)
                val butterflyXDp = panoramicWidthDp * 0.692f
                val butterflyYDp = availableHeightDp * 0.455f

                // Zona interactiva sobre las dos macetas con flores apoyadas en el piso (x = 0.687..0.766, y = 0.495..0.836)
                Box(
                    modifier = Modifier
                        .offset(
                            x = panoramicWidthDp * 0.687f,
                            y = availableHeightDp * 0.495f
                        )
                        .size(
                            width = panoramicWidthDp * 0.079f,
                            height = availableHeightDp * 0.341f
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerObjectOpen("garden", "¡Las flores de colores!", onOpenGardenGame)
                        }
                )

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "garden",
                    glowColor = Color(0xFFF472B6),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = butterflyXDp, y = butterflyYDp)
                        .size(butterflySize),
                    onClick = {
                        triggerObjectOpen("garden", "¡Las flores de colores!", onOpenGardenGame)
                    }
                ) {
                    ButterflyColorsInRoomObject()
                }

                // OBJETO 7: PELOTA DE PLAYA EN EL PISO CON BURBUJAS (Usa la pelota ya pintada en back.jpeg en x=0.763..0.813, y=0.799..0.935)
                val beachBallXDp = panoramicWidthDp * 0.758f
                val beachBallYDp = availableHeightDp * 0.735f

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "bubbles",
                    glowColor = Color(0xFF38BDF8),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = beachBallXDp, y = beachBallYDp)
                        .size(beachBallSize),
                    onClick = {
                        triggerObjectOpen(
                            "bubbles",
                            "¡La pelota de burbujas mágicas!",
                            onOpenBubbleGame
                        )
                    }
                ) {
                    BeachBallAndBubblesInRoomObject()
                }

                // OBJETO 8: REFRIGERADOR Y ALACENA DE COCINA (Apoyados directamente sobre el piso derecho en y = 0.945f)
                val kitchenXDp = panoramicWidthDp * 0.822f
                val kitchenYDp = rightFloorSurfaceYDp - (kitchenCornerHeight * 0.90f)

                ElevatingRoomObject(
                    isElevating = activatingObjectId == "kitchen",
                    glowColor = Color(0xFF22D3EE),
                    glowPulseState = sharedGlowPulseState,
                    floatYState = sharedFloatYState,
                    enableIdleFloating = false,
                    modifier = Modifier
                        .offset(x = kitchenXDp, y = kitchenYDp)
                        .size(width = kitchenCornerWidth, height = kitchenCornerHeight),
                    onClick = {
                        triggerObjectOpen("kitchen", "¡El refrigerador y la cocina!", onOpenKitchenGame)
                    }
                ) {
                    KitchenFridgeInRoomObject()
                }

                // CAPA DE AMBIENTACIÓN NOCTURNA ESTRELLADA (Si el niño encendió la lámpara de techo)
                AnimatedVisibility(
                    visible = isNightLampMode,
                    enter = fadeIn(tween(450)),
                    exit = fadeOut(tween(450)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0x660F172A),
                                    Color(0x441E1B4B),
                                    Color(0x330F172A)
                                )
                            )
                        )
                        val starPositions = listOf(
                            0.10f to 0.14f, 0.24f to 0.10f, 0.37f to 0.16f,
                            0.48f to 0.12f, 0.61f to 0.14f, 0.74f to 0.11f,
                            0.86f to 0.15f, 0.93f to 0.12f
                        )
                        starPositions.forEach { (sx, sy) ->
                            val center = Offset(size.width * sx, size.height * sy)
                            drawCircle(
                                color = Color(0xAAFFF59D),
                                radius = 10f,
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.5f,
                                center = center
                            )
                        }
                    }
                }
            }
        }

        // 2. ZONAS LATERALES DE DESLIZAMIENTO ESTILO FNAF 3 (◀ Mirar Izquierda / Mirar Derecha ▶)
        val canPanLeft = targetOffsetX < (maxOffset - 8f)
        val canPanRight = targetOffsetX > (minOffset + 8f)

        if (canPanLeft) {
            Surface(
                shape = CircleShape,
                color = Color(0xAA0F172A),
                border = BorderStroke(2.5.dp, Color(0xCC38BDF8)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(54.dp)
                    .zIndex(25f)
                    .clickable {
                        soundHelper.playPop()
                        targetOffsetX = (targetOffsetX + stepPanAmountPx).coerceIn(minOffset, maxOffset)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Deslizar cámara a la izquierda",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        if (canPanRight) {
            Surface(
                shape = CircleShape,
                color = Color(0xAA0F172A),
                border = BorderStroke(2.5.dp, Color(0xCC38BDF8)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(54.dp)
                    .zIndex(25f)
                    .clickable {
                        soundHelper.playPop()
                        targetOffsetX = (targetOffsetX - stepPanAmountPx).coerceIn(minOffset, maxOffset)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Deslizar cámara a la derecha",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // 3. RADAR PANORÁMICO INFERIOR (Lee animatedOffsetXState dentro de offset lambda para cero recomposiciones)
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0x990F172A),
            border = BorderStroke(1.5.dp, Color(0x6638BDF8)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .width(180.dp)
                .height(14.dp)
                .zIndex(25f)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp)) {
                val thumbWidth = 46.dp
                val trackTravelPx = with(density) { (172.dp - thumbWidth).toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            val norm = ((-animatedOffsetXState.value) / (-minOffset).coerceAtLeast(1f)).coerceIn(0f, 1f)
                            IntOffset((trackTravelPx * norm).roundToInt(), 0)
                        }
                        .width(thumbWidth)
                        .fillMaxHeight()
                        .background(Color(0xFF38BDF8), RoundedCornerShape(50))
                )
            }
        }

        // 4. INSIGNIA VISUAL DE ESTRELLAS Y MANZANAS (ESQUINA SUPERIOR IZQUIERDA, CERO PALABRAS)
        userProgress?.let { progress ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCC0F172A),
                border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 12.dp, start = 14.dp)
                    .zIndex(30f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ ${progress.totalStars}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🍎 ${progress.applesCollectedTotal}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF87171)
                    )
                }
            }
        }

        // 5. BOTONES ICÓNICOS DISCRETOS EN ESQUINA SUPERIOR DERECHA (VOZ Y SALIR, SIN TEXTO)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 14.dp)
                .zIndex(30f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xCC0F172A),
                border = BorderStroke(2.dp, Color(0xFF38BDF8)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        soundHelper.speak("¡Desliza a la izquierda o a la derecha como una cámara panorámica! Puedes tocar los libros mágicos, el reloj, el osito, el tren, la cesta de manzanas en la mesa, la mariposa, la pelota de burbujas o la cocina.")
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Escuchar guía",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xCC0F172A),
                border = BorderStroke(2.dp, Color(0xFFF87171)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onLogout() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cambiar jugador",
                        tint = Color(0xFFFCA5A5),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Envuelve cualquier objeto físico de la sala integrado naturalmente en el fondo.
 * En reposo NO dibuja círculos ni halos flotantes detrás del objeto (para que se vea 100% parte
 * del mobiliario/piso con su propia sombra de contacto); sólo al tocarlo se eleva suavemente.
 */
@Composable
fun ElevatingRoomObject(
    isElevating: Boolean,
    glowColor: Color,
    modifier: Modifier = Modifier,
    glowPulseState: State<Float>? = null,
    floatYState: State<Float>? = null,
    glowRadiusMultiplier: Float = 1.15f,
    enableIdleFloating: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val elevationY by animateFloatAsState(
        targetValue = if (isElevating) -20f else 0f,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
        label = "objectElevationY"
    )
    val tapScale by animateFloatAsState(
        targetValue = if (isElevating) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
        label = "objectTapScale"
    )

    GlowingInteractiveObject(
        glowColor = glowColor,
        glowPulseState = glowPulseState,
        floatYState = floatYState,
        glowRadiusMultiplier = glowRadiusMultiplier,
        enableIdleFloating = enableIdleFloating,
        showTapHighlight = isElevating,
        modifier = modifier.graphicsLayer {
            translationY = elevationY
            scaleX = tapScale
            scaleY = tapScale
        },
        onClick = onClick,
        content = content
    )
}

@Composable
fun GlowingInteractiveObject(
    glowColor: Color,
    modifier: Modifier = Modifier,
    glowPulseState: State<Float>? = null,
    floatYState: State<Float>? = null,
    glowRadiusMultiplier: Float = 1.15f,
    enableIdleFloating: Boolean = false,
    showTapHighlight: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                if (enableIdleFloating && floatYState != null) {
                    translationY = floatYState.value
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showTapHighlight) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = glowPulseState?.value ?: 0.5f
                        val s = (0.95f + 0.12f * p) * glowRadiusMultiplier
                        scaleX = s
                        scaleY = s
                    }
                    .drawWithCache {
                        val radius = (size.minDimension / 2f).coerceAtLeast(1f)
                        val brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.50f),
                                glowColor.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = radius
                        )
                        onDrawBehind {
                            drawCircle(brush = brush, radius = radius)
                        }
                    }
            )
        }

        content()
    }
}
