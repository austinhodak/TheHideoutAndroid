package com.austinhodak.thehideout.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.austinhodak.thehideout.compose.theme.DarkGrey
import com.austinhodak.thehideout.compose.theme.DarkPrimary
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.Red500
import com.austinhodak.thehideout.compose.theme.Red700
import com.austinhodak.thehideout.compose.theme.Shapes
import com.austinhodak.thehideout.ui.theme.Typography
import com.austinhodak.thehideout.compose.theme.White
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = DarkGrey,
    primaryVariant = Red400,
    secondary = Red400,
    onPrimary = White,
    surface = Color(0xFD1F1F1F),
    onSecondary = Color.Black
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

private val DarkPalette = darkColors(
    primary = Red400,
    primaryVariant = DarkPrimary
    //surface = Color(0xFE1F1F1F)
)

private val LightPalette = lightColors(
    primary = Red400,
    primaryVariant = Red700
)

@Composable
fun TheHideoutTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val systemUiController = rememberSystemUiController()

    val colors = if (darkTheme) {
        DarkPalette
    } else {
        LightPalette
    }

    systemUiController.setStatusBarColor(colors.primaryVariant)

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}