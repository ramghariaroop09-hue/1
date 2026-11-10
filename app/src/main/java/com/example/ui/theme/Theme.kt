package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF03141B),
    primaryContainer = CyberSurfaceElevated,
    onPrimaryContainer = CyberCyan,
    secondary = CyberViolet,
    onSecondary = Color.White,
    secondaryContainer = CyberSurfaceDark,
    onSecondaryContainer = CyberViolet,
    tertiary = CyberBlue,
    onTertiary = Color.White,
    background = CyberBgDark,
    onBackground = CyberTextPrimary,
    surface = CyberSurfaceDark,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceElevated,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberSurfaceHighlight,
    error = CyberDanger,
    onError = Color.White,
    errorContainer = CyberDangerBg,
    onErrorContainer = CyberDanger
)

@Composable
fun CyberShieldTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
