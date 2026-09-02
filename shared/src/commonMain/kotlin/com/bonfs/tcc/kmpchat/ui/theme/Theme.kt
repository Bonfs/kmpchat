package com.bonfs.tcc.kmpchat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MidnightCobaltColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightTextPrimary,
    primaryContainer = MidnightBubbleOutgoing,
    onPrimaryContainer = MidnightTextPrimary,
    secondary = MidnightSecondary,
    onSecondary = MidnightBackground,
    secondaryContainer = MidnightBorder,
    onSecondaryContainer = MidnightSecondary,
    background = MidnightBackground,
    onBackground = MidnightTextPrimary,
    surface = MidnightSurface,
    onSurface = MidnightTextPrimary,
    surfaceVariant = MidnightBorder,
    onSurfaceVariant = MidnightTextSecondary,
    outline = MidnightBorder,
    error = ErrorRed,
    onError = MidnightTextPrimary
)

@Composable
fun KmpChatTheme(
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = MidnightCobaltColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
