package com.example.no_definido.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.no_definido.R
import kotlin.math.cos
import kotlin.math.sin

private val CartoonOutline = Color(0xFF3E2723)

/**
 * 1. RELOJ DE PARED INTERACTIVO (Empotrado naturalmente en la pared de la sala)
 * Optimizado con caché de capa en GPU (graphicsLayer) para cero sobrecarga por fotograma.
 */
@Composable
fun WallClockInRoomObject(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.040f).coerceAtLeast(3f)
        val center = Offset(w * 0.50f, h * 0.54f)
        val radius = w * 0.40f

        // Clavo dorado y argolla triangular de sujeción en la pared
        val nailCenter = Offset(center.x, center.y - radius * 1.14f)
        val hangerPath = Path().apply {
            moveTo(nailCenter.x, nailCenter.y)
            lineTo(center.x - radius * 0.22f, center.y - radius * 0.92f)
            lineTo(center.x + radius * 0.22f, center.y - radius * 0.92f)
            close()
        }
        drawPath(
            path = hangerPath,
            color = CartoonOutline,
            style = Stroke(width = stroke * 0.75f, join = StrokeJoin.Round)
        )
        drawCircle(color = Color(0xFFFFCA28), radius = stroke * 1.1f, center = nailCenter)
        drawCircle(color = CartoonOutline, radius = stroke * 1.1f, center = nailCenter, style = Stroke(stroke * 0.65f))

        // Sombra proyectada directamente sobre la pared azul detrás del reloj
        drawCircle(
            color = Color(0x2E1E293B),
            radius = radius * 1.03f,
            center = Offset(center.x + w * 0.028f, center.y + h * 0.038f)
        )

        // Marco exterior de madera cálida / dorado (estilo del mobiliario de la sala)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17)),
                center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                radius = radius * 1.2f
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = CartoonOutline,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        // Carátula crema interior
        val faceRadius = radius * 0.80f
        drawCircle(
            color = Color(0xFFFFFDE7),
            radius = faceRadius,
            center = center
        )
        drawCircle(
            color = CartoonOutline,
            radius = faceRadius,
            center = center,
            style = Stroke(width = stroke * 0.7f)
        )

        // 12 marcas de hora coloridas
        for (i in 0 until 12) {
            val angleRad = Math.toRadians((i * 30 - 90).toDouble())
            val dotDist = faceRadius * 0.78f
            val dotPos = Offset(
                x = center.x + (cos(angleRad) * dotDist).toFloat(),
                y = center.y + (sin(angleRad) * dotDist).toFloat()
            )
            val isCardinal = i % 3 == 0
            drawCircle(
                color = if (isCardinal) Color(0xFFE53935) else Color(0xFF3949AB),
                radius = if (isCardinal) stroke * 0.9f else stroke * 0.55f,
                center = dotPos
            )
        }

        // Manecilla de la hora
        rotate(degrees = 60f, pivot = center) {
            drawLine(
                color = Color(0xFFD32F2F),
                start = center,
                end = Offset(center.x, center.y - faceRadius * 0.46f),
                strokeWidth = stroke * 1.15f,
                cap = StrokeCap.Round
            )
        }

        // Manecilla de los minutos
        rotate(degrees = 0f, pivot = center) {
            drawLine(
                color = Color(0xFF1E88E5),
                start = center,
                end = Offset(center.x, center.y - faceRadius * 0.66f),
                strokeWidth = stroke * 0.9f,
                cap = StrokeCap.Round
            )
        }

        // Centro del reloj
        drawCircle(color = Color(0xFFFFB300), radius = stroke * 1.2f, center = center)
        drawCircle(color = CartoonOutline, radius = stroke * 1.2f, center = center, style = Stroke(stroke * 0.6f))

        // Brillo de cristal
        drawArc(
            color = Color.White.copy(alpha = 0.55f),
            startAngle = 200f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = Offset(center.x - faceRadius * 0.7f, center.y - faceRadius * 0.7f),
            size = Size(faceRadius * 1.4f, faceRadius * 1.4f),
            style = Stroke(width = stroke * 0.9f, cap = StrokeCap.Round)
        )
    }
}

/**
 * 2. REFRIGERADOR Y ALACENA DE COCINA APOYADOS EN EL PISO (Objeto en la sala principal)
 */
@Composable
fun KitchenFridgeInRoomObject(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.032f).coerceAtLeast(3f)

        // Sombra de contacto continua en el piso de madera bajo el refri y la alacena
        drawOval(
            color = Color(0x3D3E2723),
            topLeft = Offset(w * 0.03f, h * 0.85f),
            size = Size(w * 0.94f, h * 0.11f)
        )

        // =========================================================
        // A. REFRIGERADOR RETRO TURQUESA APOYADO EN EL PISO (Izquierda)
        // =========================================================
        val fridgeLeft = w * 0.07f
        val fridgeTop = h * 0.08f
        val fridgeW = w * 0.45f
        val fridgeH = h * 0.82f

        // Patitas inferiores del refrigerador apoyadas en el piso
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(fridgeLeft + w * 0.04f, fridgeTop + fridgeH - h * 0.02f),
            size = Size(w * 0.08f, h * 0.04f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(fridgeLeft + fridgeW - w * 0.12f, fridgeTop + fridgeH - h * 0.02f),
            size = Size(w * 0.08f, h * 0.04f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Lateral 3D oscuro del refrigerador
        drawRoundRect(
            color = Color(0xFF00ACC1),
            topLeft = Offset(fridgeLeft - w * 0.025f, fridgeTop + h * 0.015f),
            size = Size(fridgeW, fridgeH),
            cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
        )

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFB2EBF2), Color(0xFF4DD0E1), Color(0xFF26C6DA))
            ),
            topLeft = Offset(fridgeLeft, fridgeTop),
            size = Size(fridgeW, fridgeH),
            cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(fridgeLeft, fridgeTop),
            size = Size(fridgeW, fridgeH),
            cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
            style = Stroke(width = stroke)
        )

        // Brillo vertical izquierdo del refri
        drawLine(
            color = Color.White.copy(alpha = 0.65f),
            start = Offset(fridgeLeft + w * 0.05f, fridgeTop + h * 0.06f),
            end = Offset(fridgeLeft + w * 0.05f, fridgeTop + fridgeH - h * 0.06f),
            strokeWidth = stroke * 0.85f,
            cap = StrokeCap.Round
        )

        // División congelador / refrigerador
        val splitY = fridgeTop + fridgeH * 0.34f
        drawLine(
            color = CartoonOutline,
            start = Offset(fridgeLeft, splitY),
            end = Offset(fridgeLeft + fridgeW, splitY),
            strokeWidth = stroke
        )

        // Manijas cromadas doradas
        listOf(
            Pair(fridgeTop + fridgeH * 0.12f, fridgeH * 0.15f),
            Pair(splitY + fridgeH * 0.08f, fridgeH * 0.22f)
        ).forEach { (hy, hh) ->
            drawRoundRect(
                color = Color(0xFFFFF59D),
                topLeft = Offset(fridgeLeft + fridgeW * 0.74f, hy),
                size = Size(w * 0.055f, hh),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = CartoonOutline,
                topLeft = Offset(fridgeLeft + fridgeW * 0.74f, hy),
                size = Size(w * 0.055f, hh),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(stroke * 0.7f)
            )
        }

        // Imán de manzanita en la puerta del refri
        drawCircle(
            color = Color(0xFFEF5350),
            radius = w * 0.042f,
            center = Offset(fridgeLeft + fridgeW * 0.38f, fridgeTop + fridgeH * 0.18f)
        )
        drawCircle(
            color = CartoonOutline,
            radius = w * 0.042f,
            center = Offset(fridgeLeft + fridgeW * 0.38f, fridgeTop + fridgeH * 0.18f),
            style = Stroke(stroke * 0.55f)
        )

        // =========================================================
        // B. MUEBLE ALACENA DE MADERA APOYADO EN EL PISO (Derecha)
        // =========================================================
        val cabLeft = w * 0.55f
        val cabTop = h * 0.22f
        val cabW = w * 0.39f
        val cabH = h * 0.68f // Termina exactamente en h * 0.90f (al nivel del piso junto al refri)

        // Cuerpo exterior de madera cálida (mismo tono amarillo/miel del librero de la sala)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB74D), Color(0xFFFFA726))
            ),
            topLeft = Offset(cabLeft, cabTop),
            size = Size(cabW, cabH),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(cabLeft, cabTop),
            size = Size(cabW, cabH),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
            style = Stroke(width = stroke)
        )

        // Nicho interior abierto de las repisas superiores
        val nicheLeft = cabLeft + w * 0.035f
        val nicheTop = cabTop + h * 0.04f
        val nicheW = cabW - w * 0.07f
        val nicheH = cabH * 0.54f
        drawRoundRect(
            color = Color(0xFFFFF3E0),
            topLeft = Offset(nicheLeft, nicheTop),
            size = Size(nicheW, nicheH),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(nicheLeft, nicheTop),
            size = Size(nicheW, nicheH),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
            style = Stroke(width = stroke * 0.75f)
        )

        val midShelfY = nicheTop + nicheH * 0.50f

        // Plato de cerámica apoyado en la repisa superior del mueble
        val plateRadius = w * 0.082f
        val plateCenter = Offset(nicheLeft + nicheW * 0.50f, midShelfY - plateRadius * 0.95f)
        drawCircle(color = Color.White, radius = plateRadius, center = plateCenter)
        drawCircle(
            color = Color(0xFFEF5350),
            radius = plateRadius * 0.65f,
            center = plateCenter,
            style = Stroke(stroke * 0.55f)
        )
        drawCircle(
            color = CartoonOutline,
            radius = plateRadius,
            center = plateCenter,
            style = Stroke(stroke * 0.75f)
        )

        // Tabla divisoria de la repisa media
        drawLine(
            color = Color(0xFFFFB74D),
            start = Offset(nicheLeft, midShelfY),
            end = Offset(nicheLeft + nicheW, midShelfY),
            strokeWidth = stroke * 1.5f
        )
        drawLine(
            color = CartoonOutline,
            start = Offset(nicheLeft, midShelfY),
            end = Offset(nicheLeft + nicheW, midShelfY),
            strokeWidth = stroke * 0.8f
        )

        // Vaso de agua apoyado en la repisa inferior del nicho
        val glassW = w * 0.10f
        val glassH = nicheH * 0.36f
        val glassLeft = nicheLeft + nicheW * 0.34f
        val glassTop = nicheTop + nicheH - glassH - h * 0.01f
        drawRoundRect(
            color = Color(0xFFB3E5FC),
            topLeft = Offset(glassLeft, glassTop),
            size = Size(glassW, glassH),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(glassLeft, glassTop),
            size = Size(glassW, glassH),
            cornerRadius = CornerRadius(5f, 5f),
            style = Stroke(stroke * 0.75f)
        )

        // Puerta inferior de la alacena con pomo redondo
        val doorTop = nicheTop + nicheH + h * 0.03f
        val doorH = (cabTop + cabH) - doorTop - h * 0.03f
        drawRoundRect(
            color = Color(0xFFFFE082),
            topLeft = Offset(nicheLeft, doorTop),
            size = Size(nicheW, doorH),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(nicheLeft, doorTop),
            size = Size(nicheW, doorH),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(stroke * 0.75f)
        )
        drawCircle(
            color = Color(0xFFFF8F00),
            radius = w * 0.025f,
            center = Offset(nicheLeft + nicheW * 0.80f, doorTop + doorH * 0.50f)
        )
        drawCircle(
            color = CartoonOutline,
            radius = w * 0.025f,
            center = Offset(nicheLeft + nicheW * 0.80f, doorTop + doorH * 0.50f),
            style = Stroke(stroke * 0.55f)
        )
    }
}

/**
 * 3. TREN DE MADERA CON NÚMEROS (Objeto en la alfombra de la sala)
 */
@Composable
fun WoodenTrainInRoomObject(modifier: Modifier = Modifier) {
    val puffOffset = -4f

    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.036f).coerceAtLeast(3f)

        // Sombra bajo el tren
        drawOval(
            color = Color.Black.copy(alpha = 0.22f),
            topLeft = Offset(w * 0.06f, h * 0.78f),
            size = Size(w * 0.88f, h * 0.12f)
        )

        // Nubes de vapor alegres saliendo de la chimenea
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.07f,
            center = Offset(w * 0.26f, h * 0.13f + puffOffset * 0.5f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = w * 0.05f,
            center = Offset(w * 0.35f, h * 0.07f + puffOffset)
        )

        // Chimenea con borde ancho
        drawRoundRect(
            color = Color(0xFFEF5350),
            topLeft = Offset(w * 0.20f, h * 0.22f),
            size = Size(w * 0.12f, h * 0.24f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.20f, h * 0.22f),
            size = Size(w * 0.12f, h * 0.24f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(stroke)
        )
        drawRoundRect(
            color = Color(0xFFFFCA28),
            topLeft = Offset(w * 0.18f, h * 0.19f),
            size = Size(w * 0.16f, h * 0.06f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.18f, h * 0.19f),
            size = Size(w * 0.16f, h * 0.06f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(stroke * 0.8f)
        )

        // Cabina trasera
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFB300))),
            topLeft = Offset(w * 0.46f, h * 0.24f),
            size = Size(w * 0.36f, h * 0.48f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.46f, h * 0.24f),
            size = Size(w * 0.36f, h * 0.48f),
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(stroke)
        )

        // Techo de la cabina
        drawRoundRect(
            color = Color(0xFFEF5350),
            topLeft = Offset(w * 0.43f, h * 0.20f),
            size = Size(w * 0.42f, h * 0.07f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.43f, h * 0.20f),
            size = Size(w * 0.42f, h * 0.07f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(stroke * 0.85f)
        )

        // Ventana de la cabina
        drawRoundRect(
            color = Color(0xFFE1F5FE),
            topLeft = Offset(w * 0.53f, h * 0.31f),
            size = Size(w * 0.22f, h * 0.18f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.53f, h * 0.31f),
            size = Size(w * 0.22f, h * 0.18f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(stroke * 0.8f)
        )

        // Motor delantero azul con franja dorada
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))),
            topLeft = Offset(w * 0.14f, h * 0.42f),
            size = Size(w * 0.42f, h * 0.30f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.14f, h * 0.42f),
            size = Size(w * 0.42f, h * 0.30f),
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(stroke)
        )

        // Defensa delantera roja
        val bumperPath = Path().apply {
            moveTo(w * 0.14f, h * 0.54f)
            lineTo(w * 0.04f, h * 0.72f)
            lineTo(w * 0.14f, h * 0.72f)
            close()
        }
        drawPath(bumperPath, color = Color(0xFFEF5350))
        drawPath(bumperPath, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

        // Ruedas y biela conectora
        val wheelY = h * 0.74f
        listOf(w * 0.28f, w * 0.52f, w * 0.72f).forEach { wx ->
            drawCircle(color = Color(0xFFEF5350), radius = w * 0.10f, center = Offset(wx, wheelY))
            drawCircle(color = CartoonOutline, radius = w * 0.10f, center = Offset(wx, wheelY), style = Stroke(stroke))
            drawCircle(color = Color(0xFFFFF176), radius = w * 0.045f, center = Offset(wx, wheelY))
            drawCircle(color = CartoonOutline, radius = w * 0.045f, center = Offset(wx, wheelY), style = Stroke(stroke * 0.5f))
        }
        drawLine(
            color = Color(0xFFFFEE58),
            start = Offset(w * 0.28f, wheelY),
            end = Offset(w * 0.72f, wheelY),
            strokeWidth = stroke * 0.9f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 4. OSITO DE PELUCHE SENTADO EN EL PISO JUNTO AL LIBRERO (Abre Mi Casa en Orden)
 */
@Composable
fun TeddyAndHomeItemsInRoomObject(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        // Sombra de contacto cálida sobre el piso de madera
        drawOval(
            color = Color(0x3D3E2723),
            topLeft = Offset(w * 0.14f, h * 0.81f),
            size = Size(w * 0.72f, h * 0.13f)
        )
        drawMiniTeddy(w, h, (w * 0.04f).coerceAtLeast(3f))
    }
}

/**
 * 5. MARIPOSA POSADA SOBRE LAS FLORES (Abre Jardín de Colores y Formas)
 */
@Composable
fun ButterflyColorsInRoomObject(modifier: Modifier = Modifier) {
    val wingFlap = 1.0f

    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.038f).coerceAtLeast(3f)
        val cx = w * 0.5f
        val cy = h * 0.52f

        // Sombra suave bajo la mariposa posada sobre los pétalos
        drawOval(
            color = Color(0x333E2723),
            topLeft = Offset(cx - w * 0.22f, cy + h * 0.20f),
            size = Size(w * 0.44f, h * 0.12f)
        )

        // Antenas curvas
        val leftAntenna = Path().apply {
            moveTo(cx - w * 0.02f, cy - h * 0.22f)
            cubicTo(cx - w * 0.10f, cy - h * 0.38f, cx - w * 0.18f, cy - h * 0.38f, cx - w * 0.20f, cy - h * 0.32f)
        }
        val rightAntenna = Path().apply {
            moveTo(cx + w * 0.02f, cy - h * 0.22f)
            cubicTo(cx + w * 0.10f, cy - h * 0.38f, cx + w * 0.18f, cy - h * 0.38f, cx + w * 0.20f, cy - h * 0.32f)
        }
        drawPath(leftAntenna, color = CartoonOutline, style = Stroke(stroke * 0.7f, cap = StrokeCap.Round))
        drawPath(rightAntenna, color = CartoonOutline, style = Stroke(stroke * 0.7f, cap = StrokeCap.Round))
        drawCircle(color = Color(0xFFFFD54F), radius = w * 0.03f, center = Offset(cx - w * 0.20f, cy - h * 0.32f))
        drawCircle(color = Color(0xFFFFD54F), radius = w * 0.03f, center = Offset(cx + w * 0.20f, cy - h * 0.32f))

        // Alas superiores con gradiente
        val topWingW = w * 0.36f * wingFlap
        val topWingH = h * 0.36f
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0xFFFF80AB), Color(0xFFF50057))),
            topLeft = Offset(cx - topWingW, cy - topWingH * 0.95f),
            size = Size(topWingW, topWingH)
        )
        drawOval(
            color = CartoonOutline,
            topLeft = Offset(cx - topWingW, cy - topWingH * 0.95f),
            size = Size(topWingW, topWingH),
            style = Stroke(stroke)
        )
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0xFFFF80AB), Color(0xFFF50057))),
            topLeft = Offset(cx, cy - topWingH * 0.95f),
            size = Size(topWingW, topWingH)
        )
        drawOval(
            color = CartoonOutline,
            topLeft = Offset(cx, cy - topWingH * 0.95f),
            size = Size(topWingW, topWingH),
            style = Stroke(stroke)
        )

        // Alas inferiores
        val botWingW = w * 0.28f * wingFlap
        val botWingH = h * 0.30f
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0xFF80D8FF), Color(0xFF00B0FF))),
            topLeft = Offset(cx - botWingW, cy - botWingH * 0.1f),
            size = Size(botWingW, botWingH)
        )
        drawOval(
            color = CartoonOutline,
            topLeft = Offset(cx - botWingW, cy - botWingH * 0.1f),
            size = Size(botWingW, botWingH),
            style = Stroke(stroke)
        )
        drawOval(
            brush = Brush.radialGradient(listOf(Color(0xFF80D8FF), Color(0xFF00B0FF))),
            topLeft = Offset(cx, cy - botWingH * 0.1f),
            size = Size(botWingW, botWingH)
        )
        drawOval(
            color = CartoonOutline,
            topLeft = Offset(cx, cy - botWingH * 0.1f),
            size = Size(botWingW, botWingH),
            style = Stroke(stroke)
        )

        // Motas doradas y blancas en las alas
        drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.065f, center = Offset(cx - topWingW * 0.52f, cy - topWingH * 0.48f))
        drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.065f, center = Offset(cx + topWingW * 0.52f, cy - topWingH * 0.48f))
        drawCircle(color = Color.White, radius = w * 0.035f, center = Offset(cx - botWingW * 0.48f, cy + botWingH * 0.36f))
        drawCircle(color = Color.White, radius = w * 0.035f, center = Offset(cx + botWingW * 0.48f, cy + botWingH * 0.36f))

        // Cuerpo de la mariposa
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(cx - w * 0.048f, cy - h * 0.25f),
            size = Size(w * 0.096f, h * 0.52f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(cx - w * 0.048f, cy - h * 0.25f),
            size = Size(w * 0.096f, h * 0.52f),
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(stroke * 0.8f)
        )
    }
}

/**
 * 6. PILA DE LIBROS DE CUENTOS APOYADA EN EL PISO JUNTO AL LIBRERO (Abre el Memorama del Calmécac)
 */
@Composable
fun MagicBooksInRoomObject(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.038f).coerceAtLeast(3f)

        // Sombra de contacto firme sobre el piso de madera bajo la pila de libros
        drawOval(
            color = Color(0x3D3E2723),
            topLeft = Offset(w * 0.08f, h * 0.76f),
            size = Size(w * 0.84f, h * 0.13f)
        )

        // Libro inferior azul turquesa apoyado en el piso
        drawRoundRect(
            color = Color(0xFF26C6DA),
            topLeft = Offset(w * 0.12f, h * 0.64f),
            size = Size(w * 0.76f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFFFFDE7),
            topLeft = Offset(w * 0.18f, h * 0.67f),
            size = Size(w * 0.66f, h * 0.12f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.12f, h * 0.64f),
            size = Size(w * 0.76f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(stroke)
        )

        // Libro medio rosa/magenta
        drawRoundRect(
            color = Color(0xFFEC407A),
            topLeft = Offset(w * 0.16f, h * 0.46f),
            size = Size(w * 0.68f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFFFF9C4),
            topLeft = Offset(w * 0.22f, h * 0.49f),
            size = Size(w * 0.58f, h * 0.12f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.16f, h * 0.46f),
            size = Size(w * 0.68f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(stroke)
        )

        // Libro superior dorado apoyado sobre la pila
        drawRoundRect(
            color = Color(0xFFFFCA28),
            topLeft = Offset(w * 0.20f, h * 0.28f),
            size = Size(w * 0.60f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.20f, h * 0.28f),
            size = Size(w * 0.60f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(stroke)
        )

        // Emblema de estrella grabado en el lomo del libro superior (no flotando en el aire)
        drawSparkleStar(
            center = Offset(w * 0.50f, h * 0.37f),
            radius = w * 0.065f,
            color = Color(0xFFFFF9C4),
            stroke = stroke * 0.55f
        )

        // Separador rojo colgando sobre los libros
        val ribbon = Path().apply {
            moveTo(w * 0.46f, h * 0.46f)
            lineTo(w * 0.54f, h * 0.46f)
            lineTo(w * 0.54f, h * 0.62f)
            lineTo(w * 0.50f, h * 0.57f)
            lineTo(w * 0.46f, h * 0.62f)
            close()
        }
        drawPath(ribbon, color = Color(0xFFE53935))
        drawPath(ribbon, color = CartoonOutline, style = Stroke(stroke * 0.65f, join = StrokeJoin.Round))
    }
}

/**
 * 7. BURBUJAS DE JABÓN INTEGRÁNDOSE SOBRE LA PELOTA DE PLAYA DEL FONDO (Abre Burbujas Mágicas)
 * No duplica la pelota que ya está pintada en el piso de back.jpeg; dibuja las burbujas
 * traslúcidas saliendo de ella para una integración 100% natural.
 */
@Composable
fun BeachBallAndBubblesInRoomObject(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.036f).coerceAtLeast(2.5f)

        // Burbujas de jabón iridiscentes saliendo suavemente desde la pelota en el piso
        drawGlossyBubble(Offset(w * 0.26f, h * 0.42f), w * 0.13f, stroke * 0.60f)
        drawGlossyBubble(Offset(w * 0.74f, h * 0.32f), w * 0.15f, stroke * 0.65f)
        drawGlossyBubble(Offset(w * 0.48f, h * 0.16f), w * 0.10f, stroke * 0.55f)
        drawGlossyBubble(Offset(w * 0.68f, h * 0.62f), w * 0.08f, stroke * 0.50f)
    }
}

/**
 * ILUSTRACIONES DE OBJETOS INDIVIDUALES (Cocina, Hogar, Ferrocarril, Memorama y Burbujas)
 * Rediseñados completamente con estilo cartoon rico en detalles, volumen 3D, sombras y brillos.
 */
@Composable
fun IllustratedGameItem(
    itemId: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.040f).coerceAtLeast(3.2f)

        when (itemId) {
            // === ALIMENTOS DEL REFRIGERADOR ===
            "meat" -> drawMeatSteak(w, h, stroke)
            "milk" -> drawMilkCarton(w, h, stroke)
            "cheese" -> drawCheeseWedge(w, h, stroke)
            "fish" -> drawFreshFish(w, h, stroke)
            "chicken" -> drawChickenLeg(w, h, stroke)
            "watermelon" -> drawWatermelonSlice(w, h, stroke)
            "apple_fruit", "apple" -> drawOrchardApple(w, h, stroke)
            "eggs" -> drawFreshEggs(w, h, stroke)

            // === OBJETOS DE LA ESTANTERÍA / COCINA ===
            "glass" -> drawWaterGlass(w, h, stroke)
            "plate" -> drawCeramicPlate(w, h, stroke)
            "mug" -> drawWarmMug(w, h, stroke)
            "pan" -> drawCookingPan(w, h, stroke)
            "bowl" -> drawSoupBowl(w, h, stroke)
            "cutlery" -> drawForkAndSpoon(w, h, stroke)
            "teapot" -> drawRedTeapot(w, h, stroke)
            "cooking_pot" -> drawCookingPot(w, h, stroke)

            // === OBJETOS DEL HOGAR (BAÑO, RECÁMARA, SALA) ===
            "toothbrush" -> drawToothbrush(w, h, stroke)
            "soap" -> drawSoapBar(w, h, stroke)
            "towel" -> drawCleanTowel(w, h, stroke)
            "rubber_duck" -> drawRubberDuck(w, h, stroke)
            "shampoo" -> drawShampooBottle(w, h, stroke)

            "pillow" -> drawSoftPillow(w, h, stroke)
            "teddy" -> drawMiniTeddy(w, h, stroke)
            "pajamas" -> drawNightShirt(w, h, stroke)
            "blanket" -> drawCozyBlanket(w, h, stroke)
            "slippers" -> drawCozySlippers(w, h, stroke)

            "tv_remote" -> drawTvRemote(w, h, stroke)
            "ball" -> drawColorBall(w, h, stroke)
            "storybook" -> drawStorybook(w, h, stroke)
            "cushion" -> drawSofaCushion(w, h, stroke)
            "toy_car" -> drawToyCar(w, h, stroke)

            // === CARGA DEL TREN DE LOS NÚMEROS (MESOAMÉRICA) ===
            "corn" -> drawGoldenCorn(w, h, stroke)
            "clay_pot" -> drawClayPot(w, h, stroke)
            "jade_stone" -> drawJadeGem(w, h, stroke)
            "cacao" -> drawCacaoPod(w, h, stroke)
            "sunflower" -> drawCempasuchilFlower(w, h, stroke)

            else -> drawSoftPillow(w, h, stroke)
        }
    }
}

private fun DrawScope.drawGlossyBubble(center: Offset, radius: Float, stroke: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color(0xFF80D8FF).copy(alpha = 0.55f),
                Color(0xFFF48FB1).copy(alpha = 0.70f)
            ),
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = CartoonOutline.copy(alpha = 0.75f),
        radius = radius,
        center = center,
        style = Stroke(stroke)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(center.x - radius * 0.55f, center.y - radius * 0.58f),
        size = Size(radius * 0.45f, radius * 0.28f)
    )
}

private fun DrawScope.drawSparkleStar(center: Offset, radius: Float, color: Color, stroke: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
    drawPath(path, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))
}

// ============================================================================
// OBJETOS DEL BAÑO (REDISEÑADOS COMPLETAMENTE CON DETALLE ARTÍSTICO)
// ============================================================================

private fun DrawScope.drawToothbrush(w: Float, h: Float, stroke: Float) {
    // Sombra suave inferior
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(w * 0.14f, h * 0.82f),
        size = Size(w * 0.72f, h * 0.09f)
    )

    // 1. Cabezal de cerdas tupidas con divisiones y relieve (en ángulo diagonal)
    rotate(degrees = -36f, pivot = Offset(w * 0.50f, h * 0.50f)) {
        // Base de las cerdas (blanco azulado)
        val bristlesX = w * 0.54f
        val bristlesY = h * 0.25f
        val bristlesW = w * 0.30f
        val bristlesH = h * 0.17f

        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE1F5FE))),
            topLeft = Offset(bristlesX, bristlesY),
            size = Size(bristlesW, bristlesH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Líneas verticales que simulan mechones de cerdas reales
        for (i in 1..4) {
            val bx = bristlesX + (bristlesW * i / 5f)
            drawLine(
                color = Color(0xFF81D4FA),
                start = Offset(bx, bristlesY + bristlesH * 0.15f),
                end = Offset(bx, bristlesY + bristlesH * 0.90f),
                strokeWidth = stroke * 0.55f,
                cap = StrokeCap.Round
            )
        }
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(bristlesX, bristlesY),
            size = Size(bristlesW, bristlesH),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(stroke * 0.9f)
        )

        // 2. Copete ondulado de pasta dental tricolor (menta, blanco y rosa) sobre las cerdas
        val pastePath = Path().apply {
            moveTo(bristlesX + bristlesW * 0.05f, bristlesY + bristlesH * 0.12f)
            cubicTo(
                bristlesX + bristlesW * 0.15f, bristlesY - bristlesH * 0.85f,
                bristlesX + bristlesW * 0.55f, bristlesY - bristlesH * 0.75f,
                bristlesX + bristlesW * 0.75f, bristlesY - bristlesH * 0.30f
            )
            cubicTo(
                bristlesX + bristlesW * 0.92f, bristlesY - bristlesH * 0.10f,
                bristlesX + bristlesW * 1.02f, bristlesY - bristlesH * 0.55f,
                bristlesX + bristlesW * 0.95f, bristlesY + bristlesH * 0.12f
            )
            close()
        }
        drawPath(
            path = pastePath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF4DD0E1), Color.White, Color(0xFFF48FB1)),
                startX = bristlesX,
                endX = bristlesX + bristlesW
            )
        )
        drawPath(
            path = pastePath,
            color = CartoonOutline,
            style = Stroke(stroke * 0.85f, join = StrokeJoin.Round)
        )

        // 3. Mango ergonómico curvo del cepillo con goma antideslizante
        val handlePath = Path().apply {
            moveTo(w * 0.10f, h * 0.44f)
            cubicTo(w * 0.25f, h * 0.40f, w * 0.42f, h * 0.44f, w * 0.54f, h * 0.41f)
            lineTo(w * 0.86f, h * 0.41f)
            cubicTo(w * 0.90f, h * 0.41f, w * 0.90f, h * 0.50f, w * 0.85f, h * 0.50f)
            lineTo(w * 0.52f, h * 0.50f)
            cubicTo(w * 0.40f, h * 0.55f, w * 0.24f, h * 0.56f, w * 0.10f, h * 0.53f)
            cubicTo(w * 0.05f, h * 0.52f, w * 0.05f, h * 0.45f, w * 0.10f, h * 0.44f)
            close()
        }
        drawPath(
            path = handlePath,
            brush = Brush.verticalGradient(listOf(Color(0xFF29B6F6), Color(0xFF0288D1)))
        )

        // Inserto de goma blanca en el mango
        drawRoundRect(
            color = Color(0xFFE1F5FE),
            topLeft = Offset(w * 0.16f, h * 0.455f),
            size = Size(w * 0.22f, h * 0.055f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawPath(
            path = handlePath,
            color = CartoonOutline,
            style = Stroke(stroke, join = StrokeJoin.Round)
        )
    }

    // Burbujas de espuma y destellos de limpieza alrededor
    drawGlossyBubble(Offset(w * 0.30f, h * 0.24f), w * 0.075f, stroke * 0.55f)
    drawGlossyBubble(Offset(w * 0.44f, h * 0.16f), w * 0.050f, stroke * 0.50f)
    drawSparkleStar(Offset(w * 0.78f, h * 0.62f), w * 0.075f, Color(0xFFFFEE58), stroke * 0.55f)
}

private fun DrawScope.drawSoapBar(w: Float, h: Float, stroke: Float) {
    // Jabonera / base azul celeste debajo del jabón
    drawOval(
        color = Color(0xFF81D4FA),
        topLeft = Offset(w * 0.10f, h * 0.48f),
        size = Size(w * 0.80f, h * 0.36f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.10f, h * 0.48f),
        size = Size(w * 0.80f, h * 0.36f),
        style = Stroke(stroke)
    )

    // Base 3D inferior de la pastilla de jabón (sombra rosa intenso)
    drawRoundRect(
        color = Color(0xFFEC407A),
        topLeft = Offset(w * 0.16f, h * 0.38f),
        size = Size(w * 0.68f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.16f, h * 0.38f),
        size = Size(w * 0.68f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.20f, w * 0.20f),
        style = Stroke(stroke)
    )

    // Cara superior de la pastilla de jabón rosada
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1))),
        topLeft = Offset(w * 0.16f, h * 0.31f),
        size = Size(w * 0.68f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.16f, h * 0.31f),
        size = Size(w * 0.68f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.20f, w * 0.20f),
        style = Stroke(stroke)
    )

    // Medallón grabado en el centro del jabón
    drawRoundRect(
        color = Color(0xFFFCE4EC),
        topLeft = Offset(w * 0.28f, h * 0.39f),
        size = Size(w * 0.44f, h * 0.20f),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
    )
    drawRoundRect(
        color = Color(0xFFEC407A),
        topLeft = Offset(w * 0.28f, h * 0.39f),
        size = Size(w * 0.44f, h * 0.20f),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
        style = Stroke(stroke * 0.65f)
    )

    // Espuma blanca cremosa y racimo de burbujas brillantes
    drawCircle(color = Color.White, radius = w * 0.08f, center = Offset(w * 0.24f, h * 0.66f))
    drawCircle(color = Color.White, radius = w * 0.06f, center = Offset(w * 0.33f, h * 0.69f))
    drawGlossyBubble(Offset(w * 0.74f, h * 0.24f), w * 0.11f, stroke * 0.65f)
    drawGlossyBubble(Offset(w * 0.56f, h * 0.16f), w * 0.075f, stroke * 0.55f)
    drawGlossyBubble(Offset(w * 0.26f, h * 0.22f), w * 0.085f, stroke * 0.60f)
    drawGlossyBubble(Offset(w * 0.84f, h * 0.40f), w * 0.065f, stroke * 0.50f)
}

private fun DrawScope.drawCleanTowel(w: Float, h: Float, stroke: Float) {
    // 1. Barra del toallero de madera y soportes metálicos
    drawRoundRect(
        color = Color(0xFF8D6E63),
        topLeft = Offset(w * 0.10f, h * 0.16f),
        size = Size(w * 0.80f, h * 0.07f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.10f, h * 0.16f),
        size = Size(w * 0.80f, h * 0.07f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(stroke * 0.85f)
    )
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.055f, center = Offset(w * 0.12f, h * 0.195f))
    drawCircle(color = CartoonOutline, radius = w * 0.055f, center = Offset(w * 0.12f, h * 0.195f), style = Stroke(stroke * 0.75f))
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.055f, center = Offset(w * 0.88f, h * 0.195f))
    drawCircle(color = CartoonOutline, radius = w * 0.055f, center = Offset(w * 0.88f, h * 0.195f), style = Stroke(stroke * 0.75f))

    // 2. Capa trasera de la toalla colgando (más oscura para dar profundidad 3D)
    drawRoundRect(
        color = Color(0xFF26A69A),
        topLeft = Offset(w * 0.26f, h * 0.22f),
        size = Size(w * 0.52f, h * 0.62f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.26f, h * 0.22f),
        size = Size(w * 0.52f, h * 0.62f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(stroke)
    )

    // 3. Capa frontal de la toalla doblada sobre la barra
    val towelFront = Path().apply {
        moveTo(w * 0.22f, h * 0.14f)
        lineTo(w * 0.74f, h * 0.14f)
        quadraticTo(w * 0.78f, h * 0.14f, w * 0.78f, h * 0.20f)
        lineTo(w * 0.74f, h * 0.76f)
        lineTo(w * 0.22f, h * 0.76f)
        lineTo(w * 0.20f, h * 0.20f)
        quadraticTo(w * 0.20f, h * 0.14f, w * 0.22f, h * 0.14f)
        close()
    }
    drawPath(
        path = towelFront,
        brush = Brush.verticalGradient(listOf(Color(0xFFB2DFDB), Color(0xFF80CBC4), Color(0xFF4DB6AC)))
    )

    // Franjas decorativas bordadas (estilo textil mexicano)
    drawRect(
        color = Color(0xFFFF7043),
        topLeft = Offset(w * 0.215f, h * 0.58f),
        size = Size(w * 0.53f, h * 0.055f)
    )
    drawRect(
        color = Color(0xFFFFEE58),
        topLeft = Offset(w * 0.218f, h * 0.635f),
        size = Size(w * 0.525f, h * 0.045f)
    )

    // Pliegues suaves de tela
    drawLine(
        color = Color(0xFF00897B).copy(alpha = 0.45f),
        start = Offset(w * 0.38f, h * 0.24f),
        end = Offset(w * 0.36f, h * 0.54f),
        strokeWidth = stroke * 0.7f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = 0.55f),
        start = Offset(w * 0.28f, h * 0.22f),
        end = Offset(w * 0.28f, h * 0.54f),
        strokeWidth = stroke * 0.8f,
        cap = StrokeCap.Round
    )

    drawPath(towelFront, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // 4. Flecos / borlas blancas en el borde inferior de la toalla
    for (i in 0..6) {
        val fx = w * (0.24f + i * 0.08f)
        drawLine(
            color = Color(0xFFFFFDE7),
            start = Offset(fx, h * 0.76f),
            end = Offset(fx, h * 0.85f),
            strokeWidth = stroke * 1.1f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = CartoonOutline,
            start = Offset(fx, h * 0.76f),
            end = Offset(fx, h * 0.85f),
            strokeWidth = stroke * 0.45f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawRubberDuck(w: Float, h: Float, stroke: Float) {
    // Ondas de agua azul debajo del patito
    drawOval(
        color = Color(0xFF81D4FA),
        topLeft = Offset(w * 0.14f, h * 0.72f),
        size = Size(w * 0.72f, h * 0.16f)
    )
    // Cuerpo del patito
    val bodyPath = Path().apply {
        moveTo(w * 0.24f, h * 0.58f)
        cubicTo(w * 0.18f, h * 0.78f, w * 0.42f, h * 0.82f, w * 0.68f, h * 0.78f)
        cubicTo(w * 0.84f, h * 0.74f, w * 0.88f, h * 0.52f, w * 0.78f, h * 0.44f)
        lineTo(w * 0.66f, h * 0.50f)
        close()
    }
    drawPath(bodyPath, brush = Brush.verticalGradient(listOf(Color(0xFFFFEE58), Color(0xFFFBC02D))))
    drawPath(bodyPath, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Cabeza redonda
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFFF176), Color(0xFFFBC02D))),
        radius = w * 0.20f,
        center = Offset(w * 0.40f, h * 0.38f)
    )
    drawCircle(
        color = CartoonOutline,
        radius = w * 0.20f,
        center = Offset(w * 0.40f, h * 0.38f),
        style = Stroke(stroke)
    )

    // Piquito naranja
    val beak = Path().apply {
        moveTo(w * 0.22f, h * 0.35f)
        lineTo(w * 0.08f, h * 0.40f)
        lineTo(w * 0.23f, h * 0.45f)
        close()
    }
    drawPath(beak, color = Color(0xFFFF7043))
    drawPath(beak, color = CartoonOutline, style = Stroke(stroke * 0.85f, join = StrokeJoin.Round))

    // Alita
    drawOval(
        color = Color(0xFFFDD835),
        topLeft = Offset(w * 0.42f, h * 0.55f),
        size = Size(w * 0.26f, h * 0.15f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.42f, h * 0.55f),
        size = Size(w * 0.26f, h * 0.15f),
        style = Stroke(stroke * 0.8f)
    )

    // Ojo brillante
    drawCircle(color = CartoonOutline, radius = w * 0.035f, center = Offset(w * 0.34f, h * 0.35f))
    drawCircle(color = Color.White, radius = w * 0.014f, center = Offset(w * 0.33f, h * 0.34f))
    drawGlossyBubble(Offset(w * 0.74f, h * 0.26f), w * 0.08f, stroke * 0.55f)
}

private fun DrawScope.drawShampooBottle(w: Float, h: Float, stroke: Float) {
    // Dispensador superior
    drawRoundRect(
        color = Color(0xFFFFF59D),
        topLeft = Offset(w * 0.42f, h * 0.14f),
        size = Size(w * 0.16f, h * 0.16f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.42f, h * 0.14f),
        size = Size(w * 0.16f, h * 0.16f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(stroke * 0.85f)
    )
    // Botella redondeada
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFCE93D8), Color(0xFFAB47BC))),
        topLeft = Offset(w * 0.26f, h * 0.28f),
        size = Size(w * 0.48f, h * 0.58f),
        cornerRadius = CornerRadius(26f, 26f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.26f, h * 0.28f),
        size = Size(w * 0.48f, h * 0.58f),
        cornerRadius = CornerRadius(26f, 26f),
        style = Stroke(stroke)
    )
    // Etiqueta con gota
    drawOval(
        color = Color(0xFFFFFDE7),
        topLeft = Offset(w * 0.33f, h * 0.42f),
        size = Size(w * 0.34f, h * 0.30f)
    )
    drawCircle(color = Color(0xFF4FC3F7), radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.57f))
    drawGlossyBubble(Offset(w * 0.76f, h * 0.24f), w * 0.08f, stroke * 0.55f)
}

// ============================================================================
// OBJETOS DE LA RECÁMARA (REDISEÑADOS COMPLETAMENTE)
// ============================================================================

private fun DrawScope.drawSoftPillow(w: Float, h: Float, stroke: Float) {
    // Sombra bajo la almohada
    drawOval(
        color = Color.Black.copy(alpha = 0.14f),
        topLeft = Offset(w * 0.12f, h * 0.72f),
        size = Size(w * 0.76f, h * 0.12f)
    )

    // Silueta acolchada con puntas (orejitas de almohada real, no un simple rectángulo)
    val pillowPath = Path().apply {
        moveTo(w * 0.12f, h * 0.24f)
        cubicTo(w * 0.32f, h * 0.30f, w * 0.68f, h * 0.30f, w * 0.88f, h * 0.24f)
        cubicTo(w * 0.94f, h * 0.22f, w * 0.95f, h * 0.28f, w * 0.90f, h * 0.34f)
        cubicTo(w * 0.84f, h * 0.44f, w * 0.84f, h * 0.58f, w * 0.90f, h * 0.68f)
        cubicTo(w * 0.95f, h * 0.74f, w * 0.94f, h * 0.80f, w * 0.88f, h * 0.78f)
        cubicTo(w * 0.68f, h * 0.72f, w * 0.32f, h * 0.72f, w * 0.12f, h * 0.78f)
        cubicTo(w * 0.06f, h * 0.80f, w * 0.05f, h * 0.74f, w * 0.10f, h * 0.68f)
        cubicTo(w * 0.16f, h * 0.58f, w * 0.16f, h * 0.44f, w * 0.10f, h * 0.34f)
        cubicTo(w * 0.05f, h * 0.28f, w * 0.06f, h * 0.22f, w * 0.12f, h * 0.24f)
        close()
    }
    drawPath(
        path = pillowPath,
        brush = Brush.verticalGradient(listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFFB39DDB)))
    )

    // Costura interior dorada y pliegues de las esquinas
    drawRoundRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(w * 0.22f, h * 0.33f),
        size = Size(w * 0.56f, h * 0.36f),
        cornerRadius = CornerRadius(20f, 20f),
        style = Stroke(stroke * 0.6f)
    )

    // Luna creciente bordada en el centro
    drawCircle(color = Color(0xFFFFD54F), radius = w * 0.11f, center = Offset(w * 0.48f, h * 0.51f))
    drawCircle(color = Color(0xFFD1C4E9), radius = w * 0.085f, center = Offset(w * 0.53f, h * 0.48f))
    drawSparkleStar(Offset(w * 0.62f, h * 0.44f), w * 0.05f, Color(0xFFFFEE58), stroke * 0.45f)

    drawPath(pillowPath, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))
}

private fun DrawScope.drawMiniTeddy(w: Float, h: Float, stroke: Float) {
    // Sombra
    drawOval(
        color = Color.Black.copy(alpha = 0.16f),
        topLeft = Offset(w * 0.20f, h * 0.84f),
        size = Size(w * 0.60f, h * 0.10f)
    )

    // Orejas redondas
    listOf(w * 0.28f, w * 0.72f).forEach { ex ->
        drawCircle(color = Color(0xFFA1887F), radius = w * 0.13f, center = Offset(ex, h * 0.22f))
        drawCircle(color = CartoonOutline, radius = w * 0.13f, center = Offset(ex, h * 0.22f), style = Stroke(stroke))
        drawCircle(color = Color(0xFFF8BBD0), radius = w * 0.065f, center = Offset(ex, h * 0.22f))
    }

    // Brazos abiertos
    drawOval(color = Color(0xFFA1887F), topLeft = Offset(w * 0.14f, h * 0.48f), size = Size(w * 0.20f, h * 0.16f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.14f, h * 0.48f), size = Size(w * 0.20f, h * 0.16f), style = Stroke(stroke * 0.9f))
    drawOval(color = Color(0xFFA1887F), topLeft = Offset(w * 0.66f, h * 0.48f), size = Size(w * 0.20f, h * 0.16f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.66f, h * 0.48f), size = Size(w * 0.20f, h * 0.16f), style = Stroke(stroke * 0.9f))

    // Cuerpo rechoncho
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFBCAAA4), Color(0xFF8D6E63))),
        topLeft = Offset(w * 0.26f, h * 0.46f),
        size = Size(w * 0.48f, h * 0.42f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.26f, h * 0.46f),
        size = Size(w * 0.48f, h * 0.42f),
        style = Stroke(stroke)
    )

    // Pancita crema con costura
    drawOval(
        color = Color(0xFFFFECB3),
        topLeft = Offset(w * 0.35f, h * 0.54f),
        size = Size(w * 0.30f, h * 0.26f)
    )

    // Patitas inferiores con huellitas rosadas
    listOf(w * 0.32f, w * 0.68f).forEach { px ->
        drawCircle(color = Color(0xFF8D6E63), radius = w * 0.11f, center = Offset(px, h * 0.80f))
        drawCircle(color = CartoonOutline, radius = w * 0.11f, center = Offset(px, h * 0.80f), style = Stroke(stroke * 0.9f))
        drawCircle(color = Color(0xFFF8BBD0), radius = w * 0.055f, center = Offset(px, h * 0.81f))
    }

    // Cabeza
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFBCAAA4), Color(0xFF8D6E63))),
        radius = w * 0.23f,
        center = Offset(w * 0.50f, h * 0.35f)
    )
    drawCircle(color = CartoonOutline, radius = w * 0.23f, center = Offset(w * 0.50f, h * 0.35f), style = Stroke(stroke))

    // Hocico y nariz
    drawOval(color = Color(0xFFFFECB3), topLeft = Offset(w * 0.39f, h * 0.35f), size = Size(w * 0.22f, h * 0.14f))
    drawCircle(color = CartoonOutline, radius = w * 0.035f, center = Offset(w * 0.50f, h * 0.39f))

    // Ojos tiernos y mejillas
    drawCircle(color = CartoonOutline, radius = w * 0.032f, center = Offset(w * 0.41f, h * 0.30f))
    drawCircle(color = CartoonOutline, radius = w * 0.032f, center = Offset(w * 0.59f, h * 0.30f))
    drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(w * 0.40f, h * 0.29f))
    drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(w * 0.58f, h * 0.29f))
    drawCircle(color = Color(0xFFF48FB1).copy(alpha = 0.65f), radius = w * 0.035f, center = Offset(w * 0.35f, h * 0.36f))
    drawCircle(color = Color(0xFFF48FB1).copy(alpha = 0.65f), radius = w * 0.035f, center = Offset(w * 0.65f, h * 0.36f))

    // Moño rojo elegante en el cuello
    val bowLeft = Path().apply {
        moveTo(w * 0.50f, h * 0.52f)
        lineTo(w * 0.38f, h * 0.47f)
        lineTo(w * 0.38f, h * 0.57f)
        close()
    }
    val bowRight = Path().apply {
        moveTo(w * 0.50f, h * 0.52f)
        lineTo(w * 0.62f, h * 0.47f)
        lineTo(w * 0.62f, h * 0.57f)
        close()
    }
    drawPath(bowLeft, color = Color(0xFFEF5350))
    drawPath(bowLeft, color = CartoonOutline, style = Stroke(stroke * 0.7f, join = StrokeJoin.Round))
    drawPath(bowRight, color = Color(0xFFEF5350))
    drawPath(bowRight, color = CartoonOutline, style = Stroke(stroke * 0.7f, join = StrokeJoin.Round))
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.035f, center = Offset(w * 0.50f, h * 0.52f))
}

private fun DrawScope.drawNightShirt(w: Float, h: Float, stroke: Float) {
    // Camisa de pijama con mangas abiertas, puños amarillos y cuello
    val shirtPath = Path().apply {
        moveTo(w * 0.34f, h * 0.18f)
        lineTo(w * 0.66f, h * 0.18f)
        lineTo(w * 0.90f, h * 0.38f)
        lineTo(w * 0.78f, h * 0.52f)
        lineTo(w * 0.68f, h * 0.42f)
        lineTo(w * 0.68f, h * 0.84f)
        lineTo(w * 0.32f, h * 0.84f)
        lineTo(w * 0.32f, h * 0.42f)
        lineTo(w * 0.22f, h * 0.52f)
        lineTo(w * 0.10f, h * 0.38f)
        close()
    }
    drawPath(
        path = shirtPath,
        brush = Brush.verticalGradient(listOf(Color(0xFF90CAF9), Color(0xFF42A5F5)))
    )

    // Cuello en V amarillo
    val collar = Path().apply {
        moveTo(w * 0.36f, h * 0.18f)
        lineTo(w * 0.50f, h * 0.32f)
        lineTo(w * 0.64f, h * 0.18f)
        close()
    }
    drawPath(collar, color = Color(0xFFFFF59D))
    drawPath(collar, color = CartoonOutline, style = Stroke(stroke * 0.8f, join = StrokeJoin.Round))

    // Bolsillo con luna y botoncitos
    drawRoundRect(
        color = Color(0xFF1E88E5),
        topLeft = Offset(w * 0.53f, h * 0.44f),
        size = Size(w * 0.11f, h * 0.12f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawCircle(color = Color(0xFFFFEE58), radius = w * 0.035f, center = Offset(w * 0.585f, h * 0.50f))
    listOf(0.42f, 0.56f, 0.70f).forEach { by ->
        drawCircle(color = Color(0xFFFFF59D), radius = w * 0.025f, center = Offset(w * 0.50f, h * by))
    }

    drawPath(shirtPath, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))
}

private fun DrawScope.drawCozyBlanket(w: Float, h: Float, stroke: Float) {
    // Cobija acolchada doblada en 3 capas
    val colors = listOf(Color(0xFFFFAB91), Color(0xFFFFCC80), Color(0xFFF48FB1))
    colors.forEachIndexed { idx, col ->
        val topY = h * (0.24f + idx * 0.17f)
        drawRoundRect(
            color = col,
            topLeft = Offset(w * 0.16f, topY),
            size = Size(w * 0.68f, h * 0.22f),
            cornerRadius = CornerRadius(22f, 22f)
        )
        drawRoundRect(
            color = CartoonOutline,
            topLeft = Offset(w * 0.16f, topY),
            size = Size(w * 0.68f, h * 0.22f),
            cornerRadius = CornerRadius(22f, 22f),
            style = Stroke(stroke)
        )
    }
}

private fun DrawScope.drawCozySlippers(w: Float, h: Float, stroke: Float) {
    // Par de pantuflas esponjosas con orejitas
    listOf(w * 0.32f, w * 0.68f).forEach { cx ->
        // Orejitas
        drawOval(color = Color(0xFFF8BBD0), topLeft = Offset(cx - w * 0.11f, h * 0.22f), size = Size(w * 0.08f, h * 0.18f))
        drawOval(color = CartoonOutline, topLeft = Offset(cx - w * 0.11f, h * 0.22f), size = Size(w * 0.08f, h * 0.18f), style = Stroke(stroke * 0.75f))
        drawOval(color = Color(0xFFF8BBD0), topLeft = Offset(cx + w * 0.03f, h * 0.22f), size = Size(w * 0.08f, h * 0.18f))
        drawOval(color = CartoonOutline, topLeft = Offset(cx + w * 0.03f, h * 0.22f), size = Size(w * 0.08f, h * 0.18f), style = Stroke(stroke * 0.75f))

        // Cuerpo de la pantufla
        drawOval(
            color = Color(0xFFF48FB1),
            topLeft = Offset(cx - w * 0.15f, h * 0.36f),
            size = Size(w * 0.30f, h * 0.46f)
        )
        drawOval(
            color = CartoonOutline,
            topLeft = Offset(cx - w * 0.15f, h * 0.36f),
            size = Size(w * 0.30f, h * 0.46f),
            style = Stroke(stroke)
        )
        // Naricita
        drawCircle(color = CartoonOutline, radius = w * 0.02f, center = Offset(cx, h * 0.62f))
    }
}

// ============================================================================
// OBJETOS DE LA SALA (REDISEÑADOS COMPLETAMENTE)
// ============================================================================

private fun DrawScope.drawTvRemote(w: Float, h: Float, stroke: Float) {
    // Ondas infrarrojas saliendo de la punta
    drawArc(
        color = Color(0xFFEF5350),
        startAngle = 220f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = Offset(w * 0.36f, h * 0.04f),
        size = Size(w * 0.28f, h * 0.14f),
        style = Stroke(stroke * 0.75f, cap = StrokeCap.Round)
    )

    // Lateral 3D
    drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(w * 0.30f, h * 0.16f),
        size = Size(w * 0.42f, h * 0.72f),
        cornerRadius = CornerRadius(22f, 22f)
    )
    // Carcasa frontal
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF78909C), Color(0xFF455A64))),
        topLeft = Offset(w * 0.28f, h * 0.14f),
        size = Size(w * 0.42f, h * 0.72f),
        cornerRadius = CornerRadius(22f, 22f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.28f, h * 0.14f),
        size = Size(w * 0.42f, h * 0.72f),
        cornerRadius = CornerRadius(22f, 22f),
        style = Stroke(stroke)
    )

    // Botón grande rojo de encendido
    drawCircle(color = Color(0xFFEF5350), radius = w * 0.06f, center = Offset(w * 0.49f, h * 0.26f))
    drawCircle(color = CartoonOutline, radius = w * 0.06f, center = Offset(w * 0.49f, h * 0.26f), style = Stroke(stroke * 0.65f))

    // Cruz direccional central (D-Pad)
    drawCircle(color = Color(0xFF263238), radius = w * 0.11f, center = Offset(w * 0.49f, h * 0.46f))
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.045f, center = Offset(w * 0.49f, h * 0.46f))

    // Botones de colores inferiores
    drawCircle(color = Color(0xFF66BB6A), radius = w * 0.04f, center = Offset(w * 0.40f, h * 0.66f))
    drawCircle(color = Color(0xFF29B6F6), radius = w * 0.04f, center = Offset(w * 0.58f, h * 0.66f))
    drawCircle(color = Color(0xFFAB47BC), radius = w * 0.04f, center = Offset(w * 0.40f, h * 0.76f))
    drawCircle(color = Color(0xFFFF7043), radius = w * 0.04f, center = Offset(w * 0.58f, h * 0.76f))
}

private fun DrawScope.drawColorBall(w: Float, h: Float, stroke: Float) {
    val center = Offset(w * 0.5f, h * 0.50f)
    val r = w * 0.36f

    // Sombra en el piso
    drawOval(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(w * 0.20f, h * 0.80f),
        size = Size(w * 0.60f, h * 0.10f)
    )

    // Gajos multicolores de la pelota de playa
    drawCircle(color = Color(0xFFFFEE58), radius = r, center = center)
    drawArc(
        color = Color(0xFFEF5350),
        startAngle = -50f,
        sweepAngle = 110f,
        useCenter = true,
        topLeft = Offset(center.x - r, center.y - r),
        size = Size(r * 2, r * 2)
    )
    drawArc(
        color = Color(0xFF29B6F6),
        startAngle = 70f,
        sweepAngle = 110f,
        useCenter = true,
        topLeft = Offset(center.x - r, center.y - r),
        size = Size(r * 2, r * 2)
    )
    drawArc(
        color = Color(0xFF66BB6A),
        startAngle = 180f,
        sweepAngle = 70f,
        useCenter = true,
        topLeft = Offset(center.x - r, center.y - r),
        size = Size(r * 2, r * 2)
    )

    // Círculo polar blanco en la parte superior de la pelota
    val poleCenter = Offset(center.x - r * 0.18f, center.y - r * 0.22f)
    drawCircle(color = Color.White, radius = r * 0.24f, center = poleCenter)
    drawCircle(color = CartoonOutline, radius = r * 0.24f, center = poleCenter, style = Stroke(stroke * 0.8f))

    // Brillo curvo especular
    drawArc(
        color = Color.White.copy(alpha = 0.65f),
        startAngle = 200f,
        sweepAngle = 50f,
        useCenter = false,
        topLeft = Offset(center.x - r * 0.78f, center.y - r * 0.78f),
        size = Size(r * 1.56f, r * 1.56f),
        style = Stroke(stroke * 1.1f, cap = StrokeCap.Round)
    )

    drawCircle(color = CartoonOutline, radius = r, center = center, style = Stroke(stroke))
}

private fun DrawScope.drawStorybook(w: Float, h: Float, stroke: Float) {
    // Libro de cuentos ilustrado con lomo dorado y estrella
    drawRoundRect(
        color = Color(0xFFE53935),
        topLeft = Offset(w * 0.20f, h * 0.16f),
        size = Size(w * 0.60f, h * 0.68f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = Color(0xFFFFCA28),
        topLeft = Offset(w * 0.20f, h * 0.16f),
        size = Size(w * 0.12f, h * 0.68f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.20f, h * 0.16f),
        size = Size(w * 0.60f, h * 0.68f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(stroke)
    )
    drawSparkleStar(Offset(w * 0.54f, h * 0.46f), w * 0.14f, Color(0xFFFFEE58), stroke * 0.75f)
}

private fun DrawScope.drawSofaCushion(w: Float, h: Float, stroke: Float) {
    // Cojín bordado en rombo con borlas en las esquinas
    val path = Path().apply {
        moveTo(w * 0.50f, h * 0.14f)
        lineTo(w * 0.86f, h * 0.50f)
        lineTo(w * 0.50f, h * 0.86f)
        lineTo(w * 0.14f, h * 0.50f)
        close()
    }
    drawPath(path, brush = Brush.radialGradient(listOf(Color(0xFFFFCC80), Color(0xFFFF7043))))
    drawPath(path, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))
    drawCircle(color = Color(0xFFFFEE58), radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.50f))
    drawCircle(color = CartoonOutline, radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.50f), style = Stroke(stroke * 0.7f))
}

private fun DrawScope.drawToyCar(w: Float, h: Float, stroke: Float) {
    // Carrito de juguete colorido
    val body = Path().apply {
        moveTo(w * 0.12f, h * 0.66f)
        lineTo(w * 0.12f, h * 0.48f)
        lineTo(w * 0.26f, h * 0.46f)
        lineTo(w * 0.36f, h * 0.28f)
        lineTo(w * 0.68f, h * 0.28f)
        lineTo(w * 0.78f, h * 0.46f)
        lineTo(w * 0.88f, h * 0.48f)
        lineTo(w * 0.88f, h * 0.66f)
        close()
    }
    drawPath(body, color = Color(0xFF42A5F5))
    drawPath(body, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))
    // Ventana
    drawRoundRect(
        color = Color(0xFFE1F5FE),
        topLeft = Offset(w * 0.39f, h * 0.32f),
        size = Size(w * 0.26f, h * 0.14f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Ruedas
    listOf(w * 0.30f, w * 0.70f).forEach { wx ->
        drawCircle(color = Color(0xFF37474F), radius = w * 0.11f, center = Offset(wx, h * 0.68f))
        drawCircle(color = CartoonOutline, radius = w * 0.11f, center = Offset(wx, h * 0.68f), style = Stroke(stroke * 0.8f))
        drawCircle(color = Color(0xFFFFCA28), radius = w * 0.05f, center = Offset(wx, h * 0.68f))
    }
}

// ============================================================================
// CARGA DEL TREN MESOAMERICANO (REDISEÑADOS)
// ============================================================================

private fun DrawScope.drawGoldenCorn(w: Float, h: Float, stroke: Float) {
    // Hojas de maíz verdes (totomoxtle) abrazando la mazorca
    val leftLeaf = Path().apply {
        moveTo(w * 0.34f, h * 0.86f)
        cubicTo(w * 0.06f, h * 0.66f, w * 0.10f, h * 0.38f, w * 0.24f, h * 0.32f)
        cubicTo(w * 0.22f, h * 0.54f, w * 0.32f, h * 0.74f, w * 0.42f, h * 0.84f)
        close()
    }
    drawPath(leftLeaf, color = Color(0xFF43A047))
    drawPath(leftLeaf, color = CartoonOutline, style = Stroke(stroke * 0.85f, join = StrokeJoin.Round))

    val rightLeaf = Path().apply {
        moveTo(w * 0.66f, h * 0.86f)
        cubicTo(w * 0.94f, h * 0.66f, w * 0.90f, h * 0.38f, w * 0.76f, h * 0.32f)
        cubicTo(w * 0.78f, h * 0.54f, w * 0.68f, h * 0.74f, w * 0.58f, h * 0.84f)
        close()
    }
    drawPath(rightLeaf, color = Color(0xFF2E7D32))
    drawPath(rightLeaf, color = CartoonOutline, style = Stroke(stroke * 0.85f, join = StrokeJoin.Round))

    // Cuerpo de la mazorca (elote dorado con volumen)
    val cobPath = Path().apply {
        moveTo(w * 0.35f, h * 0.82f)
        lineTo(w * 0.65f, h * 0.82f)
        cubicTo(w * 0.72f, h * 0.54f, w * 0.68f, h * 0.20f, w * 0.50f, h * 0.14f)
        cubicTo(w * 0.32f, h * 0.20f, w * 0.28f, h * 0.54f, w * 0.35f, h * 0.82f)
        close()
    }
    drawPath(
        path = cobPath,
        brush = Brush.horizontalGradient(listOf(Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA000)))
    )
    drawPath(cobPath, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Filas de granos de maíz redondeados
    for (row in 1..4) {
        val y = h * (0.22f + row * 0.115f)
        drawLine(
            color = Color(0xFFF57F17),
            start = Offset(w * 0.35f, y),
            end = Offset(w * 0.65f, y),
            strokeWidth = stroke * 0.65f,
            cap = StrokeCap.Round
        )
    }
    listOf(w * 0.44f, w * 0.56f).forEach { gx ->
        drawLine(
            color = Color(0xFFF57F17),
            start = Offset(gx, h * 0.20f),
            end = Offset(gx, h * 0.78f),
            strokeWidth = stroke * 0.60f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawClayPot(w: Float, h: Float, stroke: Float) {
    // Asas laterales de la vasija
    drawOval(color = Color(0xFFBF360C), topLeft = Offset(w * 0.10f, h * 0.40f), size = Size(w * 0.22f, h * 0.26f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.10f, h * 0.40f), size = Size(w * 0.22f, h * 0.26f), style = Stroke(stroke * 0.85f))
    drawOval(color = Color(0xFFBF360C), topLeft = Offset(w * 0.68f, h * 0.40f), size = Size(w * 0.22f, h * 0.26f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.68f, h * 0.40f), size = Size(w * 0.22f, h * 0.26f), style = Stroke(stroke * 0.85f))

    // Cuerpo globular de barro (olla terracota con gradiente)
    val body = Path().apply {
        moveTo(w * 0.34f, h * 0.32f)
        cubicTo(w * 0.12f, h * 0.48f, w * 0.16f, h * 0.82f, w * 0.38f, h * 0.86f)
        lineTo(w * 0.62f, h * 0.86f)
        cubicTo(w * 0.84f, h * 0.82f, w * 0.88f, h * 0.48f, w * 0.66f, h * 0.32f)
        close()
    }
    drawPath(body, brush = Brush.radialGradient(listOf(Color(0xFFFF7043), Color(0xFFD84315), Color(0xFFBF360C))))
    drawPath(body, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Cuello y borde ancho de la vasija
    drawRoundRect(
        color = Color(0xFFFF8A65),
        topLeft = Offset(w * 0.28f, h * 0.22f),
        size = Size(w * 0.44f, h * 0.12f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.28f, h * 0.22f),
        size = Size(w * 0.44f, h * 0.12f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(stroke)
    )

    // Cenefa decorativa prehispánica en la panza
    drawRoundRect(
        color = Color(0xFFFFE082),
        topLeft = Offset(w * 0.25f, h * 0.52f),
        size = Size(w * 0.50f, h * 0.10f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.25f, h * 0.52f),
        size = Size(w * 0.50f, h * 0.10f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(stroke * 0.7f)
    )
}

private fun DrawScope.drawJadeGem(w: Float, h: Float, stroke: Float) {
    val cx = w * 0.5f
    val cy = h * 0.52f
    val r = w * 0.36f

    val outerHex = Path().apply {
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 30).toDouble())
            val x = cx + (cos(angle) * r).toFloat()
            val y = cy + (sin(angle) * r).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(outerHex, brush = Brush.linearGradient(listOf(Color(0xFF64FFDA), Color(0xFF00BFA5), Color(0xFF00796B))))
    drawPath(outerHex, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    val innerHex = Path().apply {
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 30).toDouble())
            val x = cx + (cos(angle) * r * 0.55f).toFloat()
            val y = cy + (sin(angle) * r * 0.55f).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(innerHex, color = Color(0xFFA7FFEB))
    drawPath(innerHex, color = Color(0xFF004D40), style = Stroke(stroke * 0.65f, join = StrokeJoin.Round))

    for (i in 0 until 6) {
        val angle = Math.toRadians((i * 60 - 30).toDouble())
        val x1 = cx + (cos(angle) * r * 0.55f).toFloat()
        val y1 = cy + (sin(angle) * r * 0.55f).toFloat()
        val x2 = cx + (cos(angle) * r).toFloat()
        val y2 = cy + (sin(angle) * r).toFloat()
        drawLine(color = Color(0xFF004D40), start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = stroke * 0.65f)
    }

    drawSparkleStar(Offset(cx - r * 0.25f, cy - r * 0.25f), w * 0.08f, Color.White, stroke * 0.5f)
}

private fun DrawScope.drawCacaoPod(w: Float, h: Float, stroke: Float) {
    // Mazorca de cacao mesoamericano
    drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFB74D), Color(0xFF8D6E63), Color(0xFF5D4037))),
        topLeft = Offset(w * 0.24f, h * 0.16f),
        size = Size(w * 0.52f, h * 0.68f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.24f, h * 0.16f),
        size = Size(w * 0.52f, h * 0.68f),
        style = Stroke(stroke)
    )
    drawLine(color = CartoonOutline, start = Offset(w * 0.50f, h * 0.18f), end = Offset(w * 0.50f, h * 0.82f), strokeWidth = stroke * 0.65f)
}

private fun DrawScope.drawCempasuchilFlower(w: Float, h: Float, stroke: Float) {
    val cx = w * 0.50f
    val cy = h * 0.50f
    for (i in 0 until 10) {
        val rad = Math.toRadians((i * 36).toDouble())
        val px = cx + (cos(rad) * w * 0.20f).toFloat()
        val py = cy + (sin(rad) * h * 0.20f).toFloat()
        drawCircle(color = Color(0xFFFF9800), radius = w * 0.13f, center = Offset(px, py))
        drawCircle(color = CartoonOutline, radius = w * 0.13f, center = Offset(px, py), style = Stroke(stroke * 0.7f))
    }
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.15f, center = Offset(cx, cy))
    drawCircle(color = CartoonOutline, radius = w * 0.15f, center = Offset(cx, cy), style = Stroke(stroke * 0.8f))
}

// ============================================================================
// ALIMENTOS Y UTENSILIOS DE LA COCINA (REDISEÑADOS CON VOLUMEN 3D)
// ============================================================================

private fun DrawScope.drawMeatSteak(w: Float, h: Float, stroke: Float) {
    // Espesor 3D inferior del corte de carne
    drawOval(
        color = Color(0xFFB71C1C),
        topLeft = Offset(w * 0.12f, h * 0.28f),
        size = Size(w * 0.76f, h * 0.54f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.12f, h * 0.28f),
        size = Size(w * 0.76f, h * 0.54f),
        style = Stroke(stroke)
    )
    // Borde de grasa crema superior
    drawOval(
        color = Color(0xFFFFCDD2),
        topLeft = Offset(w * 0.12f, h * 0.20f),
        size = Size(w * 0.76f, h * 0.54f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.12f, h * 0.20f),
        size = Size(w * 0.76f, h * 0.54f),
        style = Stroke(stroke)
    )
    // Centro rojo jugoso
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828))),
        topLeft = Offset(w * 0.18f, h * 0.26f),
        size = Size(w * 0.64f, h * 0.42f)
    )
    // Huesito blanco y marmoleado
    drawCircle(color = Color.White, radius = w * 0.095f, center = Offset(w * 0.36f, h * 0.47f))
    drawCircle(color = CartoonOutline, radius = w * 0.095f, center = Offset(w * 0.36f, h * 0.47f), style = Stroke(stroke * 0.75f))
    drawLine(
        color = Color(0xFFFFCDD2),
        start = Offset(w * 0.50f, h * 0.38f),
        end = Offset(w * 0.68f, h * 0.54f),
        strokeWidth = stroke * 0.85f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFFCDD2),
        start = Offset(w * 0.62f, h * 0.36f),
        end = Offset(w * 0.54f, h * 0.54f),
        strokeWidth = stroke * 0.75f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawChickenLeg(w: Float, h: Float, stroke: Float) {
    // Huesito con capuchón blanco
    drawRoundRect(
        color = Color(0xFFFFF9C4),
        topLeft = Offset(w * 0.54f, h * 0.50f),
        size = Size(w * 0.30f, h * 0.14f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawCircle(color = Color.White, radius = w * 0.085f, center = Offset(w * 0.82f, h * 0.52f))
    drawCircle(color = Color.White, radius = w * 0.085f, center = Offset(w * 0.82f, h * 0.62f))
    drawCircle(color = CartoonOutline, radius = w * 0.085f, center = Offset(w * 0.82f, h * 0.52f), style = Stroke(stroke * 0.7f))
    drawCircle(color = CartoonOutline, radius = w * 0.085f, center = Offset(w * 0.82f, h * 0.62f), style = Stroke(stroke * 0.7f))

    // Muslo rostizado dorado con brillo
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFFFB74D), Color(0xFFF57C00), Color(0xFFE65100))),
        topLeft = Offset(w * 0.12f, h * 0.24f),
        size = Size(w * 0.56f, h * 0.48f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.12f, h * 0.24f),
        size = Size(w * 0.56f, h * 0.48f),
        style = Stroke(stroke)
    )
    drawArc(
        color = Color.White.copy(alpha = 0.55f),
        startAngle = 200f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(w * 0.20f, h * 0.30f),
        size = Size(w * 0.40f, h * 0.32f),
        style = Stroke(stroke * 0.9f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMilkCarton(w: Float, h: Float, stroke: Float) {
    val left = w * 0.24f
    val top = h * 0.30f
    val cw = w * 0.50f
    val ch = h * 0.56f

    // Techo triangular
    val roof = Path().apply {
        moveTo(left, top)
        lineTo(w * 0.49f, h * 0.12f)
        lineTo(left + cw, top)
        close()
    }
    drawPath(roof, color = Color(0xFF29B6F6))
    drawPath(roof, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Taparrosca roja en el techo
    drawOval(color = Color(0xFFEF5350), topLeft = Offset(w * 0.54f, h * 0.17f), size = Size(w * 0.12f, h * 0.08f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.54f, h * 0.17f), size = Size(w * 0.12f, h * 0.08f), style = Stroke(stroke * 0.65f))

    // Cuerpo del envase
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(left, top),
        size = Size(cw, ch),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))),
        topLeft = Offset(left + stroke * 0.5f, top + ch * 0.26f),
        size = Size(cw - stroke, ch * 0.40f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(left, top),
        size = Size(cw, ch),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(stroke)
    )

    // Gota blanca de leche
    val drop = Path().apply {
        moveTo(w * 0.49f, top + ch * 0.33f)
        cubicTo(w * 0.38f, top + ch * 0.50f, w * 0.41f, top + ch * 0.60f, w * 0.49f, top + ch * 0.60f)
        cubicTo(w * 0.57f, top + ch * 0.60f, w * 0.60f, top + ch * 0.50f, w * 0.49f, top + ch * 0.33f)
        close()
    }
    drawPath(drop, color = Color.White)
}

private fun DrawScope.drawCheeseWedge(w: Float, h: Float, stroke: Float) {
    // Cara superior inclinada del queso
    val topPlane = Path().apply {
        moveTo(w * 0.14f, h * 0.48f)
        lineTo(w * 0.52f, h * 0.18f)
        lineTo(w * 0.86f, h * 0.36f)
        lineTo(w * 0.86f, h * 0.48f)
        close()
    }
    drawPath(topPlane, color = Color(0xFFFFEE58))
    drawPath(topPlane, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Cara frontal gruesa del queso
    val frontPlane = Path().apply {
        moveTo(w * 0.14f, h * 0.48f)
        lineTo(w * 0.86f, h * 0.36f)
        lineTo(w * 0.86f, h * 0.76f)
        lineTo(w * 0.14f, h * 0.76f)
        close()
    }
    drawPath(frontPlane, brush = Brush.verticalGradient(listOf(Color(0xFFFFCA28), Color(0xFFFFB300))))
    drawPath(frontPlane, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Agujeros del queso con sombra interna
    drawCircle(color = Color(0xFFF57F17), radius = w * 0.075f, center = Offset(w * 0.42f, h * 0.60f))
    drawCircle(color = Color(0xFFF57F17), radius = w * 0.055f, center = Offset(w * 0.66f, h * 0.52f))
    drawCircle(color = Color(0xFFF57F17), radius = w * 0.045f, center = Offset(w * 0.70f, h * 0.66f))
}

private fun DrawScope.drawFreshFish(w: Float, h: Float, stroke: Float) {
    // Aleta dorsal superior
    val dorsal = Path().apply {
        moveTo(w * 0.36f, h * 0.32f)
        quadraticTo(w * 0.48f, h * 0.16f, w * 0.58f, h * 0.32f)
        close()
    }
    drawPath(dorsal, color = Color(0xFF0288D1))
    drawPath(dorsal, color = CartoonOutline, style = Stroke(stroke * 0.8f, join = StrokeJoin.Round))

    // Cola bifurcada
    val tail = Path().apply {
        moveTo(w * 0.68f, h * 0.50f)
        lineTo(w * 0.90f, h * 0.28f)
        lineTo(w * 0.82f, h * 0.50f)
        lineTo(w * 0.90f, h * 0.72f)
        close()
    }
    drawPath(tail, color = Color(0xFF29B6F6))
    drawPath(tail, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Cuerpo del pez con gradiente plateado/azul
    drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFF29B6F6), Color(0xFF81D4FA), Color(0xFFE1F5FE))),
        topLeft = Offset(w * 0.10f, h * 0.30f),
        size = Size(w * 0.62f, h * 0.40f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.10f, h * 0.30f),
        size = Size(w * 0.62f, h * 0.40f),
        style = Stroke(stroke)
    )

    // Branquia y ojo expresivo
    drawArc(
        color = CartoonOutline,
        startAngle = -60f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(w * 0.24f, h * 0.38f),
        size = Size(w * 0.12f, h * 0.24f),
        style = Stroke(stroke * 0.75f, cap = StrokeCap.Round)
    )
    drawCircle(color = Color.White, radius = w * 0.055f, center = Offset(w * 0.23f, h * 0.45f))
    drawCircle(color = CartoonOutline, radius = w * 0.03f, center = Offset(w * 0.22f, h * 0.45f))
}

private fun DrawScope.drawWatermelonSlice(w: Float, h: Float, stroke: Float) {
    drawArc(
        color = Color(0xFF2E7D32),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.12f, h * 0.16f),
        size = Size(w * 0.76f, h * 0.68f)
    )
    drawArc(
        color = Color(0xFFC8E6C9),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.16f, h * 0.20f),
        size = Size(w * 0.68f, h * 0.60f)
    )
    drawArc(
        color = Color(0xFFE53935),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.20f, h * 0.24f),
        size = Size(w * 0.60f, h * 0.52f)
    )
    drawArc(
        color = CartoonOutline,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.12f, h * 0.16f),
        size = Size(w * 0.76f, h * 0.68f),
        style = Stroke(stroke)
    )
    drawCircle(color = CartoonOutline, radius = stroke * 0.6f, center = Offset(w * 0.38f, h * 0.60f))
    drawCircle(color = CartoonOutline, radius = stroke * 0.6f, center = Offset(w * 0.50f, h * 0.66f))
    drawCircle(color = CartoonOutline, radius = stroke * 0.6f, center = Offset(w * 0.62f, h * 0.60f))
}

private fun DrawScope.drawOrchardApple(w: Float, h: Float, stroke: Float) {
    // Tallo y hojita verde
    drawLine(
        color = CartoonOutline,
        start = Offset(w * 0.50f, h * 0.28f),
        end = Offset(w * 0.52f, h * 0.14f),
        strokeWidth = stroke * 1.1f,
        cap = StrokeCap.Round
    )
    drawOval(color = Color(0xFF66BB6A), topLeft = Offset(w * 0.52f, h * 0.14f), size = Size(w * 0.20f, h * 0.11f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.52f, h * 0.14f), size = Size(w * 0.20f, h * 0.11f), style = Stroke(stroke * 0.7f))

    // Manzana roja brillante
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFF5252), Color(0xFFD32F2F))),
        radius = w * 0.32f,
        center = Offset(w * 0.50f, h * 0.56f)
    )
    drawCircle(
        color = CartoonOutline,
        radius = w * 0.32f,
        center = Offset(w * 0.50f, h * 0.56f),
        style = Stroke(stroke)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.70f),
        topLeft = Offset(w * 0.30f, h * 0.38f),
        size = Size(w * 0.12f, h * 0.18f)
    )
}

private fun DrawScope.drawFreshEggs(w: Float, h: Float, stroke: Float) {
    // Dos huevos frescos de granja
    drawOval(color = Color(0xFFFFECB3), topLeft = Offset(w * 0.18f, h * 0.24f), size = Size(w * 0.36f, h * 0.50f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.18f, h * 0.24f), size = Size(w * 0.36f, h * 0.50f), style = Stroke(stroke))
    drawOval(color = Color(0xFFFFF8E1), topLeft = Offset(w * 0.44f, h * 0.30f), size = Size(w * 0.38f, h * 0.50f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.44f, h * 0.30f), size = Size(w * 0.38f, h * 0.50f), style = Stroke(stroke))
}

private fun DrawScope.drawWaterGlass(w: Float, h: Float, stroke: Float) {
    // Popote / pajilla roja con blanco
    drawLine(
        color = Color(0xFFEF5350),
        start = Offset(w * 0.58f, h * 0.10f),
        end = Offset(w * 0.46f, h * 0.76f),
        strokeWidth = stroke * 1.4f,
        cap = StrokeCap.Round
    )

    val glassPath = Path().apply {
        moveTo(w * 0.25f, h * 0.22f)
        lineTo(w * 0.75f, h * 0.22f)
        lineTo(w * 0.66f, h * 0.84f)
        lineTo(w * 0.34f, h * 0.84f)
        close()
    }
    drawPath(glassPath, color = Color(0xFFE1F5FE).copy(alpha = 0.90f))

    val waterPath = Path().apply {
        moveTo(w * 0.28f, h * 0.40f)
        lineTo(w * 0.72f, h * 0.40f)
        lineTo(w * 0.65f, h * 0.82f)
        lineTo(w * 0.35f, h * 0.82f)
        close()
    }
    drawPath(waterPath, brush = Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))))

    // Cubitos de hielo flotando
    drawRoundRect(
        color = Color.White.copy(alpha = 0.80f),
        topLeft = Offset(w * 0.36f, h * 0.44f),
        size = Size(w * 0.12f, h * 0.12f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawPath(glassPath, color = CartoonOutline, style = Stroke(stroke, join = StrokeJoin.Round))

    // Elipse superior del borde del vaso
    drawOval(
        color = Color.White,
        topLeft = Offset(w * 0.25f, h * 0.18f),
        size = Size(w * 0.50f, h * 0.08f)
    )
    drawOval(
        color = CartoonOutline,
        topLeft = Offset(w * 0.25f, h * 0.18f),
        size = Size(w * 0.50f, h * 0.08f),
        style = Stroke(stroke * 0.85f)
    )

    drawLine(
        color = Color.White,
        start = Offset(w * 0.35f, h * 0.30f),
        end = Offset(w * 0.39f, h * 0.76f),
        strokeWidth = stroke * 0.9f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCeramicPlate(w: Float, h: Float, stroke: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color = Color.White, radius = w * 0.38f, center = center)
    drawCircle(color = CartoonOutline, radius = w * 0.38f, center = center, style = Stroke(stroke))
    drawCircle(color = Color(0xFFFF7043), radius = w * 0.31f, center = center, style = Stroke(stroke * 0.75f))
    drawCircle(color = Color(0xFF29B6F6), radius = w * 0.27f, center = center, style = Stroke(stroke * 0.5f))
    drawCircle(color = Color(0xFFF5F5F5), radius = w * 0.22f, center = center)
    drawArc(
        color = Color.White,
        startAngle = 210f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(center.x - w * 0.16f, center.y - h * 0.16f),
        size = Size(w * 0.32f, h * 0.32f),
        style = Stroke(stroke * 0.8f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawWarmMug(w: Float, h: Float, stroke: Float) {
    // Vaporcito saliendo de la taza
    drawArc(
        color = Color.White.copy(alpha = 0.85f),
        startAngle = 120f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.38f, h * 0.08f),
        size = Size(w * 0.14f, h * 0.14f),
        style = Stroke(stroke * 0.75f, cap = StrokeCap.Round)
    )

    // Asa de la taza
    drawArc(
        color = Color(0xFFFF7043),
        startAngle = -60f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(w * 0.54f, h * 0.36f),
        size = Size(w * 0.28f, h * 0.34f),
        style = Stroke(stroke * 2.2f, cap = StrokeCap.Round)
    )
    drawArc(
        color = CartoonOutline,
        startAngle = -60f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(w * 0.54f, h * 0.36f),
        size = Size(w * 0.28f, h * 0.34f),
        style = Stroke(stroke, cap = StrokeCap.Round)
    )

    // Cuerpo de la taza
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFF8A65), Color(0xFFF4511E))),
        topLeft = Offset(w * 0.20f, h * 0.28f),
        size = Size(w * 0.48f, h * 0.54f),
        cornerRadius = CornerRadius(20f, 20f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.20f, h * 0.28f),
        size = Size(w * 0.48f, h * 0.54f),
        cornerRadius = CornerRadius(20f, 20f),
        style = Stroke(stroke)
    )

    // Boca superior con chocolate caliente adentro
    drawOval(color = Color(0xFFFFF8E1), topLeft = Offset(w * 0.20f, h * 0.24f), size = Size(w * 0.48f, h * 0.11f))
    drawOval(color = Color(0xFF5D4037), topLeft = Offset(w * 0.24f, h * 0.26f), size = Size(w * 0.40f, h * 0.07f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.20f, h * 0.24f), size = Size(w * 0.48f, h * 0.11f), style = Stroke(stroke * 0.85f))

    // Estrella decorativa en el frente de la taza
    drawSparkleStar(Offset(w * 0.44f, h * 0.56f), w * 0.09f, Color(0xFFFFEE58), stroke * 0.55f)
}

private fun DrawScope.drawCookingPan(w: Float, h: Float, stroke: Float) {
    // Mango de la sartén
    drawLine(
        color = CartoonOutline,
        start = Offset(w * 0.62f, h * 0.50f),
        end = Offset(w * 0.90f, h * 0.30f),
        strokeWidth = stroke * 2.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFF7043),
        start = Offset(w * 0.64f, h * 0.49f),
        end = Offset(w * 0.88f, h * 0.31f),
        strokeWidth = stroke * 1.3f,
        cap = StrokeCap.Round
    )

    val panCenter = Offset(w * 0.42f, h * 0.54f)
    drawCircle(color = Color(0xFF455A64), radius = w * 0.31f, center = panCenter)
    drawCircle(color = CartoonOutline, radius = w * 0.31f, center = panCenter, style = Stroke(stroke))
    drawCircle(color = Color(0xFF263238), radius = w * 0.24f, center = panCenter)

    // Huevo frito adentro con yema brillante
    drawCircle(color = Color.White, radius = w * 0.14f, center = panCenter)
    drawCircle(color = Color(0xFFFFB300), radius = w * 0.065f, center = panCenter)
    drawCircle(color = Color.White, radius = w * 0.02f, center = Offset(panCenter.x - w * 0.02f, panCenter.y - h * 0.02f))
}

private fun DrawScope.drawSoupBowl(w: Float, h: Float, stroke: Float) {
    drawRoundRect(
        color = Color(0xFFFFB300),
        topLeft = Offset(w * 0.34f, h * 0.72f),
        size = Size(w * 0.32f, h * 0.10f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.34f, h * 0.72f),
        size = Size(w * 0.32f, h * 0.10f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(stroke * 0.8f)
    )
    drawArc(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFA000))),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.16f, h * 0.18f),
        size = Size(w * 0.68f, h * 0.60f)
    )
    drawArc(
        color = CartoonOutline,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.16f, h * 0.18f),
        size = Size(w * 0.68f, h * 0.60f),
        style = Stroke(stroke)
    )
    // Boca superior del tazón
    drawOval(color = Color(0xFFFFF8E1), topLeft = Offset(w * 0.16f, h * 0.43f), size = Size(w * 0.68f, h * 0.10f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.16f, h * 0.43f), size = Size(w * 0.68f, h * 0.10f), style = Stroke(stroke * 0.8f))
}

private fun DrawScope.drawForkAndSpoon(w: Float, h: Float, stroke: Float) {
    // Cuchara a la izquierda
    drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(w * 0.34f, h * 0.38f),
        end = Offset(w * 0.34f, h * 0.84f),
        strokeWidth = stroke * 1.9f,
        cap = StrokeCap.Round
    )
    drawOval(color = Color(0xFFECEFF1), topLeft = Offset(w * 0.23f, h * 0.16f), size = Size(w * 0.22f, h * 0.26f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.23f, h * 0.16f), size = Size(w * 0.22f, h * 0.26f), style = Stroke(stroke * 0.9f))

    // Tenedor a la derecha con dientes definidos
    drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(w * 0.66f, h * 0.38f),
        end = Offset(w * 0.66f, h * 0.84f),
        strokeWidth = stroke * 1.9f,
        cap = StrokeCap.Round
    )
    drawRoundRect(
        color = Color(0xFFECEFF1),
        topLeft = Offset(w * 0.56f, h * 0.18f),
        size = Size(w * 0.20f, h * 0.22f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.56f, h * 0.18f),
        size = Size(w * 0.20f, h * 0.22f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(stroke * 0.9f)
    )
    listOf(0.62f, 0.70f).forEach { tx ->
        drawLine(
            color = CartoonOutline,
            start = Offset(w * tx, h * 0.18f),
            end = Offset(w * tx, h * 0.29f),
            strokeWidth = stroke * 0.55f
        )
    }
}

private fun DrawScope.drawRedTeapot(w: Float, h: Float, stroke: Float) {
    // Tetera roja clásica (como la de la cocina en bg_house_rooms.jpg)
    // Pico
    val spout = Path().apply {
        moveTo(w * 0.28f, h * 0.56f)
        lineTo(w * 0.10f, h * 0.38f)
        lineTo(w * 0.18f, h * 0.34f)
        lineTo(w * 0.32f, h * 0.46f)
        close()
    }
    drawPath(spout, color = Color(0xFFE53935))
    drawPath(spout, color = CartoonOutline, style = Stroke(stroke * 0.85f, join = StrokeJoin.Round))

    // Asa
    drawOval(color = Color(0xFFE53935), topLeft = Offset(w * 0.62f, h * 0.38f), size = Size(w * 0.24f, h * 0.30f), style = Stroke(stroke * 1.5f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.62f, h * 0.38f), size = Size(w * 0.24f, h * 0.30f), style = Stroke(stroke * 0.8f))

    // Cuerpo redondo
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828))),
        topLeft = Offset(w * 0.24f, h * 0.32f),
        size = Size(w * 0.50f, h * 0.50f)
    )
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.24f, h * 0.32f), size = Size(w * 0.50f, h * 0.50f), style = Stroke(stroke))

    // Tapa y perilla
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.055f, center = Offset(w * 0.49f, h * 0.27f))
    drawCircle(color = CartoonOutline, radius = w * 0.055f, center = Offset(w * 0.49f, h * 0.27f), style = Stroke(stroke * 0.75f))
}

private fun DrawScope.drawCookingPot(w: Float, h: Float, stroke: Float) {
    // Asas laterales
    drawRoundRect(color = CartoonOutline, topLeft = Offset(w * 0.10f, h * 0.44f), size = Size(w * 0.80f, h * 0.10f), cornerRadius = CornerRadius(10f, 10f))
    // Cuerpo de la olla
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFF7043), Color(0xFFD84315))),
        topLeft = Offset(w * 0.20f, h * 0.36f),
        size = Size(w * 0.60f, h * 0.46f),
        cornerRadius = CornerRadius(18f, 18f)
    )
    drawRoundRect(
        color = CartoonOutline,
        topLeft = Offset(w * 0.20f, h * 0.36f),
        size = Size(w * 0.60f, h * 0.46f),
        cornerRadius = CornerRadius(18f, 18f),
        style = Stroke(stroke)
    )
    // Tapa
    drawOval(color = Color(0xFFFF8A65), topLeft = Offset(w * 0.18f, h * 0.28f), size = Size(w * 0.64f, h * 0.14f))
    drawOval(color = CartoonOutline, topLeft = Offset(w * 0.18f, h * 0.28f), size = Size(w * 0.64f, h * 0.14f), style = Stroke(stroke))
    drawCircle(color = Color(0xFFFFCA28), radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.24f))
    drawCircle(color = CartoonOutline, radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.24f), style = Stroke(stroke * 0.75f))
}

/**
 * MASCOTA MEXICA DE CAL-MÉ-CAC: AJOLOTE ILUSTRADO (Con penacho dorado, jade, plumas de quetzal y estrella)
 */
@Composable
fun CalmecacAxolotlMascot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x66FFF59D),
                        Color(0x22FFD54F),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.50f,
                center = Offset(size.width * 0.5f, size.height * 0.52f)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.mascot_axolotl),
            contentDescription = "Mascota Ajolote de Cal-mé-cac",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
