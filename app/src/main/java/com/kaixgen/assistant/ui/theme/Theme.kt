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
    val secondaryBackground: Color = IosGroupedDarkBg,
    val elevatedSurface: Color = IosElevatedDark,
    val glassSurface: Color = IosGlassDark,
    val glassBorder: Color = IosGlassBorderDark,
    val mutedText: Color = KaiTextMutedDark,
    val divider: Color = IosSeparatorDark
)

private val LightExtendedColors = KaiExtendedColors(
    brandDeep = KaiBlueDeep,
    cyanAccent = IosBlueLight,
    cyanGlow = Color(0xFF5AC8FA),
    micContainer = IosBlueTintLight,
    lockContainer = KaiGreenTint,
    success = Color(0xFF34C759),
    warning = Color(0xFFFF9500),
    warningContainer = KaiWarningTint,
    cardSurface = KaiCardLight,
    cardBorder = Color(0xFFE5E5EA),
    subtleShadow = Color(0x12000000),
    secondaryBackground = KaiSurfaceLight,
    elevatedSurface = KaiCardLight,
    glassSurface = Color(0xE6FFFFFF),
    glassBorder = Color(0xFFE5E5EA),
    mutedText = Color(0xFF8E8E93),
    divider = Color(0xFFC6C6C8)
)

private val DarkExtendedColors = KaiExtendedColors(
    brandDeep = Color.White,
    cyanAccent = Color(0xFF64D2FF),
    cyanGlow = IosBlueDark,
    micContainer = IosBlueTintDark,
    lockContainer = KaiGreenTintDark,
    success = KaiGreenSuccess,
    warning = KaiWarningOrange,
    warningContainer = KaiWarningTintDark,
    cardSurface = IosSurfaceDark,
    cardBorder = IosSeparatorDark,
    subtleShadow = Color(0x66000000),
    secondaryBackground = IosGroupedDarkBg,
    elevatedSurface = IosElevatedDark,
    glassSurface = IosGlassDark,
    glassBorder = IosGlassBorderDark,
    mutedText = KaiTextMutedDark,
    divider = IosSeparatorDark
)

val LocalKaiColors = staticCompositionLocalOf { DarkExtendedColors }

private val KaiLightColorScheme = lightColorScheme(
    primary = IosBlueLight,
    onPrimary = Color.White,
    primaryContainer = IosBlueTintLight,
    onPrimaryContainer = KaiBlueDeep,
    secondary = IosIndigoLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAEAFD),
    onSecondaryContainer = Color(0xFF2E2C8A),
    tertiary = Color(0xFF34C759),
    onTertiary = Color.White,
    tertiaryContainer = KaiGreenTint,
    onTertiaryContainer = Color(0xFF125A26),
    background = KaiSurfaceLight,
    onBackground = KaiTextPrimaryLight,
    surface = KaiCardLight,
    onSurface = KaiTextPrimaryLight,
    surfaceVariant = KaiSurfaceVariantLight,
    onSurfaceVariant = KaiTextSecondaryLight,
    outline = KaiOutlineLight,
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = KaiDangerTint,
    onErrorContainer = Color(0xFF8A1812)
)

private val KaiDarkColorScheme = darkColorScheme(
    primary = IosBlueDark,
    onPrimary = Color.White,
    primaryContainer = IosBlueTintDark,
    onPrimaryContainer = Color(0xFFB5DAFF),
    secondary = IosIndigoDark,
    onSecondary = Color.White,
    secondaryContainer = IosElevatedDark,
    onSecondaryContainer = Color.White,
    tertiary = KaiGreenSuccess,
    onTertiary = Color(0xFF00240C),
    tertiaryContainer = KaiGreenTintDark,
    onTertiaryContainer = Color(0xFFA6F4BC),
    background = IosBlackBg,
    onBackground = KaiTextPrimaryDark,
    surface = IosSurfaceDark,
    onSurface = KaiTextPrimaryDark,
    surfaceVariant = IosElevatedDark,
    onSurfaceVariant = KaiTextSecondaryDark,
    outline = IosSeparatorDark,
    error = KaiDangerRed,
    onError = Color.White,
    errorContainer = KaiDangerTintDark,
    onErrorContainer = Color(0xFFFFC7C4)
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
