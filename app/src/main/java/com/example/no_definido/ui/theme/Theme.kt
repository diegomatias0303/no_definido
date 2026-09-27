package com.example.no_definido.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KidsColorScheme = lightColorScheme(
    primary = KidsPrimary,
    onPrimary = KidsOnPrimary,
    primaryContainer = KidsPrimaryContainer,
    onPrimaryContainer = KidsOnPrimaryContainer,
    secondary = KidsSecondary,
    secondaryContainer = KidsSecondaryContainer,
    tertiary = KidsTertiary,
    tertiaryContainer = KidsTertiaryContainer,
    background = KidsBackground,
    surface = KidsSurface
)

@Composable
fun No_definidoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KidsColorScheme,
        typography = Typography,
        content = content
    )
}
