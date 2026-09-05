package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Adaptive helpers yang sinkron dengan BistroMateTheme (dark vs light)
// Semua screen yang sebelumnya hardcode Bento* kini pakai fungsi ini agar light mode tidak jelek

@Composable
fun adaptiveBackground(): Color = MaterialTheme.colorScheme.background

@Composable
fun adaptiveSurface(): Color = MaterialTheme.colorScheme.surface

@Composable
fun adaptiveCard(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun adaptiveCardElevated(): Color {
    val scheme = MaterialTheme.colorScheme
    // gunakan luminance untuk deteksi light/dark agar tidak brittle terhadap accent
    val isLight = scheme.background.luminance() > 0.5f
    return if (isLight) LightSurfaceElevated else BentoCardElevated
}

@Composable
fun adaptiveBorder(): Color = MaterialTheme.colorScheme.outline

@Composable
fun adaptiveBorderSubtle(): Color {
    val scheme = MaterialTheme.colorScheme
    val isLight = scheme.background.luminance() > 0.5f
    return if (isLight) LightBorder else BentoBorderSubtle
}

@Composable
fun adaptiveTextPrimary(): Color = MaterialTheme.colorScheme.onBackground

@Composable
fun adaptiveTextSecondary(): Color = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun adaptiveTextMuted(): Color {
    return MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
}
