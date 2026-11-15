package com.example.no_definido.ui.components

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Barra superior flotante 100% visual (sin menús ni textos largos) para todas las actividades.
 * Solo muestra botones icónicos grandes (Casa, Estrellas de avance, Voz de ayuda y Reiniciar).
 */
@Composable
fun VisualGameTopBar(
    currentStars: Int,
    totalStars: Int,
    onBackToRoom: () -> Unit,
    onSpeakHint: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .zIndex(50f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón grande de regresar a la sala (Icono de Casa)
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFB300),
            border = BorderStroke(3.dp, Color(0xFF3E2723)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(54.dp)
                .clickable { onBackToRoom() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Sala",
                    tint = Color(0xFF3E2723),
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Indicador puramente visual de estrellas conseguidas
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xCC1E293B),
            border = BorderStroke(2.5.dp, Color(0xFFFFD54F)),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalStars.coerceAtMost(10)) { index ->
                    val earned = index < currentStars
                    val starScale by animateFloatAsState(
                        targetValue = if (earned) 1.22f else 0.88f,
                        animationSpec = tween(350, easing = FastOutSlowInEasing),
                        label = "star_$index"
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (earned) Color(0xFFFFD54F) else Color(0xFF64748B),
                        modifier = Modifier
                            .size(28.dp)
                            .scale(starScale)
                    )
                }
            }
        }

        // Botones de ayuda auditiva (Voz) y Reiniciar
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF29B6F6),
                border = BorderStroke(3.dp, Color(0xFF3E2723)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(52.dp)
                    .clickable { onSpeakHint() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Escuchar guía",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF66BB6A),
                border = BorderStroke(3.dp, Color(0xFF3E2723)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(52.dp)
                    .clickable { onReset() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reiniciar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Celebración visual interactiva cuando el niño completa cualquier actividad.
 * Muestra la mascota de Cal-mé-cac, estrellas animadas y botones gigantes de Jugar de nuevo o Casa.
 */
@Composable
fun VisualCelebrationOverlay(
    visible: Boolean,
    bigVisualBadge: String = "🌟",
    onPlayAgain: () -> Unit,
    onBackToRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebrationPulse")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(400)),
        exit = fadeOut(tween(250)),
        modifier = modifier
            .fillMaxSize()
            .zIndex(200f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(36.dp),
                color = Color(0xFFFFFDE7),
                border = BorderStroke(5.dp, Color(0xFFFFB300)),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .width(420.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 3 Estrellas gigantes brillantes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.graphicsLayer {
                            scaleX = starScale
                            scaleY = starScale
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(54.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier
                                .size(72.dp)
                                .offset(y = (-8).dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Mascota Ajolote Mexica celebrando
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .graphicsLayer { translationY = bounceY },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFF176).copy(alpha = 0.7f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        CalmecacAxolotlMascot(modifier = Modifier.size(110.dp))
                    }

                    Text(
                        text = bigVisualBadge,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dos botones puramente visuales y gigantes: Jugar otra vez vs Volver a la sala
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Jugar Otra Vez
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF43A047),
                            border = BorderStroke(4.dp, Color(0xFF3E2723)),
                            shadowElevation = 10.dp,
                            modifier = Modifier
                                .size(76.dp)
                                .clickable { onPlayAgain() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Jugar otra vez",
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        // Botón Volver a la Sala
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFB300),
                            border = BorderStroke(4.dp, Color(0xFF3E2723)),
                            shadowElevation = 10.dp,
                            modifier = Modifier
                                .size(76.dp)
                                .clickable { onBackToRoom() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Ir a la sala",
                                    tint = Color(0xFF3E2723),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
