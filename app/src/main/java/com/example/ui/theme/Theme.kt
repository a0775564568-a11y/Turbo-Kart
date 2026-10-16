package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TurboDarkColorScheme = darkColorScheme(
    primary = TurboOrange,
    onPrimary = Color.White,
    primaryContainer = TurboPurple,
    secondary = TurboGold,
    onSecondary = Color.Black,
    tertiary = TurboCyan,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TurboKartTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TurboDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
