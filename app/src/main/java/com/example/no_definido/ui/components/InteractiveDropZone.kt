package com.example.no_definido.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DropZoneState {
    DEFAULT,
    HIGHLIGHTED,
    CORRECT,
    INCORRECT
}

@Composable
fun InteractiveDropZone(
    areaName: String,
    areaState: DropZoneState = DropZoneState.DEFAULT,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetBgColor by animateColorAsState(
        targetValue = when (areaState) {
            DropZoneState.DEFAULT -> accentColor.copy(alpha = 0.15f)
            DropZoneState.HIGHLIGHTED -> accentColor.copy(alpha = 0.35f)
            DropZoneState.CORRECT -> Color(0xFF4CAF50).copy(alpha = 0.25f)
            DropZoneState.INCORRECT -> Color(0xFFE53935).copy(alpha = 0.25f)
        },
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when (areaState) {
            DropZoneState.DEFAULT -> accentColor
            DropZoneState.HIGHLIGHTED -> accentColor
            DropZoneState.CORRECT -> Color(0xFF2E7D32)
            DropZoneState.INCORRECT -> Color(0xFFC62828)
        },
        label = "borderColor"
    )

    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = targetBgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = when (areaState) {
                    DropZoneState.CORRECT -> Icons.Default.CheckCircle
                    DropZoneState.INCORRECT -> Icons.Default.Warning
                    else -> Icons.Default.Home
                }

                Icon(
                    imageVector = icon,
                    contentDescription = areaName,
                    tint = borderColor,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = areaName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
