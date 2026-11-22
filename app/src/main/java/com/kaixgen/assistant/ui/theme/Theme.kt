package com.kaixgen.assistant.ui.theme

import android.os.Build
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
    val subtleShadow: Color,
    val secondaryBackground: Color = KaiCharcoalBg,
    val elevatedSurface: Color = KaiElevatedCharcoal,
    val glassSurface: Color = KaiGlassOverlay,
    val glassBorder: Color = KaiGlassBorder,
    val mutedText: Color = KaiTextMutedDark,
    val divider: Color = KaiDividerDark
)

private val LightExtendedColors = KaiExtendedColors(
    brandDeep = KaiBlueDeep,
    cyanAccent = KaiOrangeSecondary,
    cyanGlow = KaiCyanGlow,
    micContainer = KaiOrangeTintLight,
    lockContainer = KaiGreenTint,
    success = KaiGreenSuccess,
    warning = KaiWarningOrange,
    warningContainer = KaiWarningTint,
    cardSurface = KaiCardLight,
    cardBorder = KaiOutlineLight,
    subtleShadow = Color(0x140B0D10),
    secondaryBackground = KaiSurfaceVariantLight,
    elevatedSurface = KaiCardLight,
    glassSurface = Color(0xE6FFFFFF),
    glassBorder = KaiOutlineLight,
    mutedText = Color(0xFF737B87),
    divider = KaiOutlineLight
)

private val DarkExtendedColors = KaiExtendedColors(
    brandDeep = KaiTextPrimaryDark,
    cyanAccent = KaiOrangeSecondary,
    cyanGlow = KaiOrangePrimary,
    micContainer = KaiOrangeTintDark,
    lockContainer = KaiGreenTintDark,
    success = KaiGreenSuccess,
    warning = KaiWarningOrange,
    warningContainer = KaiWarningTintDark,
    cardSurface = KaiSurfaceCharcoal,
    cardBorder = KaiDividerDark,
    subtleShadow = Color(0x55000000),
    secondaryBackground = KaiCharcoalBg,
    elevatedSurface = KaiElevatedCharcoal,
    glassSurface = KaiGlassOverlay,
    glassBorder = KaiGlassBorder,
    mutedText = KaiTextMutedDark,
    divider = KaiDividerDark
)

val LocalKaiColors = staticCompositionLocalOf { DarkExtendedColors }

private val KaiLightColorScheme = lightColorScheme(
    primary = KaiOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = KaiOrangeTintLight,
    onPrimaryContainer = KaiBlueDeep,
    secondary = KaiOrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1EB),
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
    primary = KaiOrangePrimary,
    onPrimary = Color(0xFF0B0D10),
    primaryContainer = KaiOrangeTintDark,
    onPrimaryContainer = Color(0xFFFFD8C8),
    secondary = KaiOrangeSecondary,
    onSecondary = Color(0xFF0B0D10),
    secondaryContainer = KaiElevatedCharcoal,
    onSecondaryContainer = KaiTextPrimaryDark,
    tertiary = KaiGreenSuccess,
    onTertiary = Color(0xFF052E1B),
    tertiaryContainer = KaiGreenTintDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = KaiObsidianBg,
    onBackground = KaiTextPrimaryDark,
    surface = KaiSurfaceCharcoal,
    onSurface = KaiTextPrimaryDark,
    surfaceVariant = KaiElevatedCharcoal,
    onSurfaceVariant = KaiTextSecondaryDark,
    outline = KaiDividerDark,
    error = KaiDangerRed,
    onError = Color(0xFF2B0808),
    errorContainer = KaiDangerTintDark,
    onErrorContainer = Color(0xFFFFCACA)
)

object KaiTheme {
    val extendedColors: KaiExtendedColors
        @Composable get() = LocalKaiColors.current
}

@Composable
fun KAIxGenTheme(
    darkTheme: Boolean = true,
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
