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
    onSecondary = KidsOnSecondary,
    secondaryContainer = KidsSecondaryContainer,
    onSecondaryContainer = KidsOnSecondaryContainer,
    tertiary = KidsTertiary,
    onTertiary = KidsOnTertiary,
    tertiaryContainer = KidsTertiaryContainer,
    onTertiaryContainer = KidsOnTertiaryContainer,
    error = KidsError,
    onError = KidsOnError,
    errorContainer = KidsErrorContainer,
    onErrorContainer = KidsOnErrorContainer,
    background = KidsBackground,
    onBackground = KidsOnBackground,
    surface = KidsSurface,
    onSurface = KidsOnSurface,
    surfaceVariant = KidsSurfaceVariant,
    onSurfaceVariant = KidsOnSurfaceVariant
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
