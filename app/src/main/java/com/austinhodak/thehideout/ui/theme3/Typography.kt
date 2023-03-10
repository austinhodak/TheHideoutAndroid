package com.austinhodak.thehideout.ui.theme3

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.austinhodak.thehideout.R

@OptIn(ExperimentalTextApi::class)
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@OptIn(ExperimentalTextApi::class)
val rubik = FontFamily(
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.Bold),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.Black),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.Medium),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.SemiBold),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.ExtraBold),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.Light),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.Thin),
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Rubik", true), fontProvider = provider, weight = FontWeight.ExtraLight),
)

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
    displayLarge = TextStyle(
        fontFamily = Bender,
        fontSize = 57.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Bender,
        fontSize = 45.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Bender,
        fontSize = 36.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Bender,
        fontSize = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Bender,
        fontSize = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Bender,
        fontSize = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Bender,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Bender,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = TextStyle(
        fontFamily = Bender,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontFamily = Bender,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Bender,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Bender,
        fontSize = 12.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Bender,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    ),
    labelMedium = TextStyle(
        fontFamily = Bender,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = TextStyle(
        fontFamily = Bender,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )
)