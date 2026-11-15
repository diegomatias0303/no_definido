package com.example.no_definido.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.IllustratedGameItem
import com.example.no_definido.ui.components.VisualCelebrationOverlay
import com.example.no_definido.ui.components.VisualGameTopBar
import com.example.no_definido.ui.viewmodel.ClockRoundData
import com.example.no_definido.ui.viewmodel.ClockTimeOfDay
import com.example.no_definido.ui.viewmodel.InteractiveClockViewModel
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

typealias ClockChallenge = ClockRoundData

@Composable
fun InteractiveClockGameScreen(
    activeUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InteractiveClockViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val soundHelper = rememberSoundHelper()
    val density = LocalDensity.current

    LaunchedEffect(activeUserId) {
        viewModel.setActiveUserId(activeUserId)
    }

    // Spoken prompt on round start or update
    LaunchedEffect(uiState.currentRound, uiState.promptSpeech) {
        delay(450)
        if (!uiState.isCompleted && uiState.promptSpeech.isNotBlank()) {
            soundHelper.speak(uiState.promptSpeech)
        }
    }

    // Auto-advance between rounds after success (1.8s delay)
    LaunchedEffect(uiState.isRoundCompleted, uiState.currentRound) {
        if (uiState.isRoundCompleted && !uiState.isCompleted) {
            delay(1800)
            viewModel.nextRound()
        }
    }

    // 5 distinct sky transitions
    val targetTopColor = Color(uiState.timeOfDay.topColorHex)
    val targetBottomColor = Color(uiState.timeOfDay.bottomColorHex)

    val skyTopColor by animateColorAsState(
        targetValue = targetTopColor,
        animationSpec = tween(700),
        label = "skyTop"
    )
    val skyBottomColor by animateColorAsState(
        targetValue = targetBottomColor,
        animationSpec = tween(700),
        label = "skyBottom"
    )

    // Moving sun/moon positions
    val celestialOffsetFactor by animateOffsetAsState(
        targetValue = Offset(uiState.timeOfDay.sunCenterXFactor, uiState.timeOfDay.sunCenterYFactor),
        animationSpec = tween(700),
        label = "celestialOffset"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "clockTransitions")
    val targetPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "targetPulse"
    )

    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    // Shortest-arc hour hand rotation to prevent 330° backward wrap-around glitch
    var targetHandAngle by remember { mutableFloatStateOf((uiState.selectedHour % 12) * 30f) }
    LaunchedEffect(uiState.selectedHour) {
        val targetInCircle = (uiState.selectedHour % 12) * 30f
        val currentNorm = ((targetHandAngle % 360f) + 360f) % 360f
        var delta = targetInCircle - currentNorm
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        targetHandAngle += delta
    }

    val animatedHandAngle by animateFloatAsState(
        targetValue = targetHandAngle,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "hourHandAngle"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(skyTopColor, skyBottomColor)
                )
            )
    ) {
        val screenH = maxHeight

        // Fondo celestial dinámico con Sol / Luna y Estrellas en movimiento
        Canvas(modifier = Modifier.fillMaxSize()) {
            val celestialCenter = Offset(
                size.width * celestialOffsetFactor.x,
                size.height * celestialOffsetFactor.y
            )

            if (uiState.timeOfDay.isNight) {
                // 8 Estrellas titilantes
                val starPositions = listOf(
                    Offset(size.width * 0.28f, size.height * 0.12f),
                    Offset(size.width * 0.45f, size.height * 0.18f),
                    Offset(size.width * 0.65f, size.height * 0.14f),
                    Offset(size.width * 0.75f, size.height * 0.22f),
                    Offset(size.width * 0.85f, size.height * 0.16f),
                    Offset(size.width * 0.92f, size.height * 0.30f),
                    Offset(size.width * 0.35f, size.height * 0.34f),
                    Offset(size.width * 0.55f, size.height * 0.08f)
                )
                starPositions.forEach { pos ->
                    drawCircle(
                        color = Color(0xFFFEF08A).copy(alpha = starAlpha),
                        radius = 6.5f,
                        center = pos
                    )
                }

                // Luna creciente luminosa
                drawCircle(
                    color = Color(0xFFFEF08A).copy(alpha = 0.30f),
                    radius = 58f,
                    center = celestialCenter
                )
                drawCircle(
                    color = Color(0xFFFEF08A),
                    radius = 46f,
                    center = celestialCenter
                )
                // Corte de la luna creciente con el fondo nocturno
                drawCircle(
                    color = targetTopColor,
                    radius = 38f,
                    center = Offset(celestialCenter.x + 16f, celestialCenter.y - 8f)
                )
            } else {
                when (uiState.timeOfDay) {
                    ClockTimeOfDay.MORNING -> {
                        // Sol amanecer dorado suave
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.40f),
                            radius = 68f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = 0.50f),
                            radius = 54f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFFACC15),
                            radius = 44f,
                            center = celestialCenter
                        )
                    }
                    ClockTimeOfDay.NOON -> {
                        // Sol cenital brillante e intenso
                        drawCircle(
                            color = Color(0xFFFEF08A).copy(alpha = 0.50f),
                            radius = 85f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.60f),
                            radius = 68f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFFACC15),
                            radius = 52f,
                            center = celestialCenter
                        )
                    }
                    ClockTimeOfDay.AFTERNOON -> {
                        // Sol cálido de media tarde
                        drawCircle(
                            color = Color(0xFFFBBF24).copy(alpha = 0.40f),
                            radius = 75f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = 0.55f),
                            radius = 60f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFFACC15),
                            radius = 48f,
                            center = celestialCenter
                        )
                    }
                    ClockTimeOfDay.SUNSET -> {
                        // Sol atardecer con halo crepuscular carmesí/naranja
                        drawCircle(
                            color = Color(0xFFF43F5E).copy(alpha = 0.40f),
                            radius = 90f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFEA580C).copy(alpha = 0.55f),
                            radius = 68f,
                            center = celestialCenter
                        )
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = 46f,
                            center = celestialCenter
                        )
                    }
                    ClockTimeOfDay.NIGHT -> {
                        // Handled above
                    }
                }
            }
        }

        // 1. BARRA SUPERIOR VISUAL (3 estrellas de progreso)
        VisualGameTopBar(
            currentStars = uiState.starsEarned,
            totalStars = viewModel.totalRounds,
            onBackToRoom = onBack,
            onSpeakHint = {
                soundHelper.speak(uiState.promptSpeech)
            },
            onReset = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            }
        )

        // 2. CONTENIDO PRINCIPAL: META VISUAL (IZQUIERDA) + RELOJ ANALÓGICO GIGANTE (DERECHA)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentChallenge = uiState.currentChallenge

            // IZQUIERDA: META VISUAL (Ilustración + Reloj digital grande)
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xEEFFFFFF),
                border = BorderStroke(4.dp, Color(0xFF3E2723)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .clickable {
                        soundHelper.speak(uiState.promptSpeech)
                    }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentChallenge.emojiScene,
                        fontSize = 34.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.size((screenH * 0.22f).coerceIn(72.dp, 105.dp))) {
                        IllustratedGameItem(itemId = currentChallenge.visualItemId)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pantalla de reloj digital con número objetivo
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(3.dp, Color(0xFFFFB300))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.targetHour}",
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = ":00",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // DERECHA: RELOJ ANALÓGICO GIGANTE INTERACTIVO
            val clockDiameter = (screenH * 0.78f).coerceIn(240.dp, 350.dp)
            val clockRadiusPx = with(density) { (clockDiameter / 2f).toPx() }
            var dragHourCandidate by remember { mutableIntStateOf(uiState.selectedHour) }

            Box(
                modifier = Modifier
                    .size(clockDiameter)
                    .pointerInput(uiState.targetHour, uiState.isRoundCompleted, uiState.isCompleted) {
                        detectDragGestures(
                            onDragStart = {
                                dragHourCandidate = uiState.selectedHour
                            },
                            onDragEnd = {
                                if (uiState.isRoundCompleted || uiState.isCompleted) return@detectDragGestures
                                val matched = viewModel.setHour(dragHourCandidate)
                                if (matched) {
                                    soundHelper.playSuccess()
                                    soundHelper.speak(uiState.currentChallenge.spokenWin)
                                } else {
                                    soundHelper.playPop()
                                    soundHelper.speak("$dragHourCandidate")
                                }
                            }
                        ) { change, _ ->
                            change.consume()
                            val dx = change.position.x - clockRadiusPx
                            val dy = change.position.y - clockRadiusPx
                            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                            // Evitar fluctuaciones en el eje central
                            if (distance >= clockRadiusPx * 0.20f) {
                                var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                if (deg < 0f) deg += 360f
                                val snappedHour = (((deg / 30f).roundToInt() % 12).let { if (it == 0) 12 else it })
                                if (snappedHour != dragHourCandidate) {
                                    dragHourCandidate = snappedHour
                                    soundHelper.playPop()
                                    viewModel.setHour(snappedHour)
                                }
                            }
                        }
                    }
                    .pointerInput(uiState.targetHour, uiState.isRoundCompleted, uiState.isCompleted) {
                        detectTapGestures { tapOffset ->
                            if (uiState.isRoundCompleted || uiState.isCompleted) return@detectTapGestures
                            val dx = tapOffset.x - clockRadiusPx
                            val dy = tapOffset.y - clockRadiusPx
                            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                            if (distance >= clockRadiusPx * 0.20f) {
                                var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                if (deg < 0f) deg += 360f
                                val tappedHour = (((deg / 30f).roundToInt() % 12).let { if (it == 0) 12 else it })
                                val matched = viewModel.setHour(tappedHour)
                                if (matched) {
                                    soundHelper.playSuccess()
                                    soundHelper.speak(uiState.currentChallenge.spokenWin)
                                } else {
                                    soundHelper.playPop()
                                    soundHelper.speak("$tappedHour")
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Carátula y manecillas dibujadas en Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerR = size.minDimension / 2f

                    // Borde exterior dorado
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17)),
                            center = center,
                            radius = outerR
                        ),
                        radius = outerR,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF3E2723),
                        radius = outerR,
                        center = center,
                        style = Stroke(width = 10f)
                    )

                    // Fondo crema de la carátula
                    val faceR = outerR * 0.88f
                    drawCircle(
                        color = Color(0xFFFFFDE7),
                        radius = faceR,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF3E2723),
                        radius = faceR,
                        center = center,
                        style = Stroke(width = 6f)
                    )

                    // Manecilla de minutos fija en las 12 (:00)
                    drawLine(
                        color = Color(0xFF1E88E5),
                        start = center,
                        end = Offset(center.x, center.y - faceR * 0.64f),
                        strokeWidth = 12f,
                        cap = StrokeCap.Round
                    )

                    // Manecilla de la hora con animación shortest-arc suave
                    rotate(degrees = animatedHandAngle, pivot = center) {
                        drawLine(
                            color = Color(0xFF3E2723),
                            start = center,
                            end = Offset(center.x, center.y - faceR * 0.48f),
                            strokeWidth = 22f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFE53935),
                            start = center,
                            end = Offset(center.x, center.y - faceR * 0.48f),
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )
                        // Puntero redondo dorado
                        drawCircle(
                            color = Color(0xFFFFCA28),
                            radius = 16f,
                            center = Offset(center.x, center.y - faceR * 0.48f)
                        )
                        drawCircle(
                            color = Color(0xFF3E2723),
                            radius = 16f,
                            center = Offset(center.x, center.y - faceR * 0.48f),
                            style = Stroke(width = 5f)
                        )
                    }

                    // Eje central
                    drawCircle(color = Color(0xFFFFB300), radius = 16f, center = center)
                    drawCircle(color = Color(0xFF3E2723), radius = 16f, center = center, style = Stroke(width = 6f))
                }

                // 12 Botones numéricos alrededor de la carátula (48dp+ tap targets accesibles)
                val numberRingRadiusDp = clockDiameter * 0.34f
                val badgeSize = (clockDiameter * 0.17f).coerceIn(48.dp, 60.dp)

                for (hour in 1..12) {
                    val angleRad = Math.toRadians((hour * 30 - 90).toDouble())
                    val xOffset = numberRingRadiusDp * cos(angleRad).toFloat()
                    val yOffset = numberRingRadiusDp * sin(angleRad).toFloat()

                    val isTargetHour = hour == uiState.targetHour
                    val isCurrentlyPointed = hour == uiState.selectedHour

                    val badgeScale = when {
                        isCurrentlyPointed && isTargetHour -> 1.25f
                        isTargetHour -> targetPulse
                        isCurrentlyPointed -> 1.12f
                        else -> 1.0f
                    }

                    val badgeColor = when {
                        isCurrentlyPointed && isTargetHour -> Color(0xFF43A047)
                        isTargetHour -> Color(0xFFFFF176)
                        isCurrentlyPointed -> Color(0xFF29B6F6)
                        else -> Color.White
                    }

                    Surface(
                        shape = CircleShape,
                        color = badgeColor,
                        border = BorderStroke(
                            width = if (isTargetHour) 3.5.dp else 2.dp,
                            color = if (isTargetHour) Color(0xFFF57F17) else Color(0xFF3E2723)
                        ),
                        shadowElevation = if (isTargetHour || isCurrentlyPointed) 6.dp else 2.dp,
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .size(badgeSize)
                            .minimumInteractiveComponentSize()
                            .scale(badgeScale)
                            .clickable {
                                if (uiState.isRoundCompleted || uiState.isCompleted) return@clickable
                                val matched = viewModel.setHour(hour)
                                if (matched) {
                                    soundHelper.playSuccess()
                                    soundHelper.speak(uiState.currentChallenge.spokenWin)
                                } else {
                                    soundHelper.playPop()
                                    soundHelper.speak("$hour")
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$hour",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCurrentlyPointed && isTargetHour) Color.White else Color(0xFF3E2723)
                            )
                        }
                    }
                }
            }
        }

        // 3. CELEBRACIÓN VISUAL AL COMPLETAR LA ACTIVIDAD (3 estrellas de oro)
        VisualCelebrationOverlay(
            visible = uiState.celebrationActive || uiState.isCompleted,
            bigVisualBadge = "🕰️ ☀️ 🌙 ✨",
            onPlayAgain = {
                soundHelper.playPop()
                viewModel.resetGame(randomize = true)
            },
            onBackToRoom = onBack
        )
    }
}
