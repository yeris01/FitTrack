package com.fittrack.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores basada en el nuevo diseño violeta
val FitPurple = Color(0xFF6B4DFF)
val FitPurpleDark = Color(0xFF4A34CC)
val FitPurpleLight = Color(0xFFE8E2FF)
val FitDark = Color(0xFF1D2635)
val FitOrange = Color(0xFFFF6D00) // Mantenemos el naranja para detalles específicos

private val DarkColors = darkColorScheme(
    primary = FitPurple,
    secondary = FitOrange,
    background = FitDark,
    surface = Color(0xFF2C2C2E),
    onPrimary = Color.White
)

private val LightColors = lightColorScheme(
    primary = FitPurple,
    secondary = FitOrange,
    background = Color(0xFFFDF7FF),
    surface = Color.White,
    onPrimary = Color.White,
    secondaryContainer = FitPurpleLight
)

@Composable
fun FitTrackTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
