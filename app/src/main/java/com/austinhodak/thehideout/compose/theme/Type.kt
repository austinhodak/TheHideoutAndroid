package com.austinhodak.thehideout.compose.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.austinhodak.thehideout.R

val Bender = FontFamily(
    Font(R.font.bender, FontWeight.Normal),
    Font(R.font.bender_it, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.bender_bold, FontWeight.Medium),
    Font(R.font.bender_bold_it, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.bender_bold, FontWeight.Bold),
    Font(R.font.bender_bold_it, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.bender_black, FontWeight.Black),
    Font(R.font.bender_black_it, FontWeight.Black, FontStyle.Italic),
    Font(R.font.bender_light, FontWeight.Light),
    Font(R.font.bender_light_it, FontWeight.Light, FontStyle.Italic)
)



// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = Bender,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    h6 = TextStyle(
        fontFamily = Bender,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    h5 = TextStyle(
        fontFamily = Bender,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = Bender,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = Bender,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    caption = TextStyle(
        fontFamily = Bender,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
)