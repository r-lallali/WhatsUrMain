package com.example.wum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ColorPack {
    BLUE,
    GREEN,
    PURPLE,
    SUNSET,
    CORAL,
    NIGHT
}

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun AppTheme(themeViewModel: ThemeViewModel, content: @Composable () -> Unit) {
    val darkTheme = when (themeViewModel.themeMode.value) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (themeViewModel.colorPack.value) {
        ColorPack.BLUE -> if (darkTheme) {
            darkColorScheme(
                primary = BlueDarkPrimary,
                background = BlueDarkBackground,
                onBackground = BlueDarkOnBackground,
                onPrimary = Color.White
            )
        } else {
            lightColorScheme(
                primary = BlueLightPrimary,
                background = BlueLightBackground,
                onBackground = BlueLightOnBackground
            )
        }

        ColorPack.GREEN -> if (darkTheme) {
            darkColorScheme(
                primary = GreenDarkPrimary,
                background = GreenDarkBackground,
                onBackground = GreenDarkOnBackground,
                onPrimary = Color.White
            )
        } else {
            lightColorScheme(
                primary = GreenLightPrimary,
                background = GreenLightBackground,
                onBackground = GreenLightOnBackground
            )
        }

        ColorPack.PURPLE -> if (darkTheme) {
            darkColorScheme(
                primary = PurpleDarkPrimary,
                background = PurpleDarkBackground,
                onBackground = PurpleDarkOnBackground,
                onPrimary = Color.White
            )
        } else {
            lightColorScheme(
                primary = PurpleLightPrimary,
                background = PurpleLightBackground,
                onBackground = PurpleLightOnBackground
            )
        }
        ColorPack.SUNSET -> if (darkTheme) {
            darkColorScheme(
                primary = SunsetDarkPrimary,
                background = SunsetDarkBackground,
                onBackground = SunsetDarkOnBackground,
                onPrimary = Color.White
            )
        } else {
            lightColorScheme(
                primary = SunsetLightPrimary,
                background = SunsetLightBackground,
                onBackground = SunsetLightOnBackground
            )
        }

        ColorPack.CORAL -> if (darkTheme) {
            darkColorScheme(
                primary = CoralDarkPrimary,
                background = CoralDarkBackground,
                onBackground = CoralDarkOnBackground,
                onPrimary = Color.White
            )
        } else {
            lightColorScheme(
                primary = CoralLightPrimary,
                background = CoralLightBackground,
                onBackground = CoralLightOnBackground
            )
        }
        ColorPack.NIGHT -> if (darkTheme) {
            darkColorScheme(
                primary = NightDarkPrimary,
                background = NightDarkBackground,
                onBackground = NightDarkOnBackground,
                onPrimary = Color.White
            )
        } else {
            lightColorScheme(
                primary = NightLightPrimary,
                background = NightLightBackground,
                onBackground = NightLightOnBackground
            )
        }
    }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
