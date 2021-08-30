package com.austinhodak.thehideout.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = DarkGrey,
    primaryVariant = Red400,
    secondary = Red400,
    onPrimary = White
)

private val LightColorPalette = lightColors(
    primary = Red500,
    primaryVariant = Red700,
    secondary = Red400,
    background = Color(0xFFFAFAFA)
)

@Composable
fun HideoutTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        DarkColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}