package com.example.no_definido.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class HotspotTarget(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val xRatio: Float,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun FirstPersonPanoramicRoomScreen(
    onOpenAppleGame: () -> Unit,
    onOpenObjectGame: () -> Unit,
    onOpenAreaExplorer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val hotspots = listOf(
        HotspotTarget(
            id = "apples",
            title = "🍎 Cesta de Manzanas",
            subtitle = "Toca para contar manzanas",
            icon = Icons.Default.ShoppingBasket,
            xRatio = 0.15f,
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = onOpenAppleGame
        ),
        HotspotTarget(
            id = "hygiene",
            title = "🪥 Espejo del Baño",
            subtitle = "Aprende hábitos de limpieza",
            icon = Icons.Default.CleaningServices,
            xRatio = 0.48f,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = onOpenObjectGame
        ),
        HotspotTarget(
            id = "bookshelf",
            title = "📚 Estantería de Libros",
            subtitle = "Explora las habitaciones",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            xRatio = 0.82f,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = onOpenAreaExplorer
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "👁️ Vista 1ª Persona (Pellizca para zoom, arrastra 👈👉)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
        ) {
            val availableWidthPx = with(density) { maxWidth.toPx() }
            val availableHeightDp = maxHeight

            val panoramicWidthDp = (maxWidth * 2.8f).coerceAtLeast(1800.dp)
            val panoramicWidthPx = with(density) { panoramicWidthDp.toPx() }

            val cardHeight = (availableHeightDp * 0.58f).coerceIn(100.dp, 160.dp)
            val cardWidth = (cardHeight * 1.3f).coerceIn(140.dp, 220.dp)
            val iconSize = (cardHeight * 0.28f).coerceIn(24.dp, 40.dp)
            val titleFontSize = if (availableHeightDp < 360.dp) 14.sp else 16.sp
            val subtitleFontSize = if (availableHeightDp < 360.dp) 10.sp else 12.sp

            val sensitivity = 1.8f

            var offsetX by remember { mutableFloatStateOf(-panoramicWidthPx * 0.25f) }
            var zoomScale by remember { mutableFloatStateOf(1.0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.85f, 1.4f)

                            val minOffset = -(panoramicWidthPx * zoomScale - availableWidthPx)
                            val maxOffset = 0f
                            offsetX = (offsetX + pan.x * sensitivity).coerceIn(minOffset, maxOffset)
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(panoramicWidthDp)
                        .fillMaxHeight()
                        .graphicsLayer(
                            scaleX = zoomScale,
                            scaleY = zoomScale
                        )
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF334155),
                                    Color(0xFF1E293B),
                                    Color(0xFF475569),
                                    Color(0xFF1E293B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    hotspots.forEach { hotspot ->
                        val cardXOffsetDp = panoramicWidthDp * hotspot.xRatio

                        Surface(
                            modifier = Modifier
                                .offset(x = cardXOffsetDp)
                                .size(width = cardWidth, height = cardHeight)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { hotspot.onClick() }
                                .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                            color = hotspot.color,
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = hotspot.icon,
                                    contentDescription = hotspot.title,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(iconSize)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = hotspot.title,
                                    fontSize = titleFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = hotspot.subtitle,
                                    fontSize = subtitleFontSize,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
