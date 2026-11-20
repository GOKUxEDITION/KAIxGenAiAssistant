package com.kaixgen.assistant.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class KaiExtendedColors(
    val brandDeep: Color,
    val cyanAccent: Color,
    val cyanGlow: Color,
    val micContainer: Color,
    val lockContainer: Color,
    val success: Color,
    val warning: Color,
    val warningContainer: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val subtleShadow: Color
)

private val LightExtendedColors = KaiExtendedColors(
    brandDeep = KaiBlueDeep,
    cyanAccent = KaiCyanAccent,
    cyanGlow = KaiCyanGlow,
    micContainer = KaiSkyTint,
    lockContainer = KaiGreenTint,
    success = KaiGreenSuccess,
    warning = KaiWarningOrange,
    warningContainer = KaiWarningTint,
    cardSurface = KaiCardLight,
    cardBorder = Color(0xFFEEF2F7),
    subtleShadow = Color(0x140E3572)
)

private val DarkExtendedColors = KaiExtendedColors(
    brandDeep = Color(0xFFD6E6FF),
    cyanAccent = KaiCyanAccentDark,
    cyanGlow = Color(0xFF0EA5E9),
    micContainer = KaiSkyTintDark,
    lockContainer = KaiGreenTintDark,
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    warningContainer = Color(0xFF33270F),
    cardSurface = KaiCardDark,
    cardBorder = KaiOutlineDark,
    subtleShadow = Color(0x40000000)
)

val LocalKaiColors = staticCompositionLocalOf { LightExtendedColors }

private val KaiLightColorScheme = lightColorScheme(
    primary = KaiBluePrimary,
    onPrimary = Color.White,
    primaryContainer = KaiSkyTint,
    onPrimaryContainer = KaiBlueDeep,
    secondary = KaiCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F4FF),
    onSecondaryContainer = KaiBlueDeep,
    tertiary = KaiGreenSuccess,
    onTertiary = Color.White,
    tertiaryContainer = KaiGreenTint,
    onTertiaryContainer = Color(0xFF0D4E21),
    background = KaiSurfaceLight,
    onBackground = KaiTextPrimaryLight,
    surface = KaiCardLight,
    onSurface = KaiTextPrimaryLight,
    surfaceVariant = KaiSurfaceVariantLight,
    onSurfaceVariant = KaiTextSecondaryLight,
    outline = KaiOutlineLight,
    error = KaiDangerRed,
    onError = Color.White,
    errorContainer = KaiDangerTint,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val KaiDarkColorScheme = darkColorScheme(
    primary = KaiBluePrimaryDark,
    onPrimary = Color(0xFF061B3A),
    primaryContainer = KaiSkyTintDark,
    onPrimaryContainer = Color(0xFFD6E6FF),
    secondary = KaiCyanAccentDark,
    onSecondary = Color(0xFF042235),
    secondaryContainer = Color(0xFF103452),
    onSecondaryContainer = Color(0xFFC5EBFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF052E1B),
    tertiaryContainer = KaiGreenTintDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = KaiSurfaceDark,
    onBackground = KaiTextPrimaryDark,
    surface = KaiCardDark,
    onSurface = KaiTextPrimaryDark,
    surfaceVariant = KaiSurfaceVariantDark,
    onSurfaceVariant = KaiTextSecondaryDark,
    outline = KaiOutlineDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF451414),
    onErrorContainer = Color(0xFFFECACA)
)

object KaiTheme {
    val extendedColors: KaiExtendedColors
        @Composable get() = LocalKaiColors.current
}

@Composable
fun KAIxGenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> KaiDarkColorScheme
        else -> KaiLightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalKaiColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KaiTypography,
            shapes = KaiShapes,
            content = content
        )
    }
}
