package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForgePurple,
    onPrimary = Color.White,
    primaryContainer = ForgeViolet,
    onPrimaryContainer = Color.White,
    secondary = ForgeMint,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF133E38),
    onSecondaryContainer = ForgeMint,
    tertiary = ForgeAmber,
    background = ForgeBg,
    onBackground = ForgeTextPrimary,
    surface = ForgePanel,
    onSurface = ForgeTextPrimary,
    surfaceVariant = ForgeCard,
    onSurfaceVariant = ForgeTextSecondary,
    outline = ForgeLine,
    outlineVariant = ForgeLineActive,
    error = ForgeRose,
    onError = Color(0xFF450A0A)
)

@Composable
fun BedrockForgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
