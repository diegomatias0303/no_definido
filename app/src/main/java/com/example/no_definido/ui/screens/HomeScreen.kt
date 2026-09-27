package com.example.no_definido.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.no_definido.ui.components.PlaceholderCard

@Composable
fun HomeScreen(
    onOpenLivingRoom: () -> Unit,
    onStartActivity: () -> Unit,
    onExploreAreas: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏠 Mi Hogar Interactivo",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Aprende a reconocer y cuidar las cosas de tu casa",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceholderCard(
                title = "🛋️ Sala de la Casa",
                subtitle = "Explora la sala interactiva",
                icon = Icons.Default.Chair,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onOpenLivingRoom,
                modifier = Modifier
                    .width(220.dp)
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            PlaceholderCard(
                title = "🧩 Juego de Objetos",
                subtitle = "¿Dónde va cada cosa?",
                icon = Icons.Default.PlayArrow,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onStartActivity,
                modifier = Modifier
                    .width(220.dp)
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            PlaceholderCard(
                title = "🏠 Habitaciones",
                subtitle = "Conoce las áreas",
                icon = Icons.Default.Home,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onExploreAreas,
                modifier = Modifier
                    .width(220.dp)
                    .height(180.dp)
            )
        }
    }
}
