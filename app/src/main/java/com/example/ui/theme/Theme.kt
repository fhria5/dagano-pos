package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun bistroDarkScheme(accent: com.example.domain.AccentTheme) = darkColorScheme(
    primary = accent.primary,
    onPrimary = Color(0xFF0F1115),
    primaryContainer = accent.dark,
    onPrimaryContainer = accent.light,
    secondary = accent.light,
    onSecondary = Color(0xFF0F1115),
    background = BentoBackground,
    onBackground = BentoTextPrimary,
    surface = BentoSurface,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoCard,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorder,
    error = StatusVoid,
    onError = Color.White
)

private fun bistroLightScheme(accent: com.example.domain.AccentTheme) = lightColorScheme(
    primary = accent.primary,
    onPrimary = Color.White,
    primaryContainer = when (accent) {
        com.example.domain.AccentTheme.EMERALD -> Color(0xFFD1FAE5)
        com.example.domain.AccentTheme.AMBER -> Color(0xFFFDE68A)
        com.example.domain.AccentTheme.BLUE -> Color(0xFFDBEAFE)
        com.example.domain.AccentTheme.PURPLE -> Color(0xFFEDE9FE)
        com.example.domain.AccentTheme.ROSE -> Color(0xFFFFE4E6)
    },
    onPrimaryContainer = when (accent) {
        com.example.domain.AccentTheme.EMERALD -> Color(0xFF064E3B)
        com.example.domain.AccentTheme.AMBER -> Color(0xFF92400E)
        com.example.domain.AccentTheme.BLUE -> Color(0xFF1E40AF)
        com.example.domain.AccentTheme.PURPLE -> Color(0xFF5B21B6)
        com.example.domain.AccentTheme.ROSE -> Color(0xFF881337)
    },
    secondary = accent.dark,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = StatusVoid,
    onError = Color.White
)

@Composable
fun BistroMateTheme(
    darkTheme: Boolean = true,
    accent: com.example.domain.AccentTheme = com.example.domain.AccentTheme.EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) bistroDarkScheme(accent) else bistroLightScheme(accent)
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
