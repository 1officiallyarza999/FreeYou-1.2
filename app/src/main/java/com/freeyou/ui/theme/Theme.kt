package com.freeyou.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

object AppColors {
    val Ink = Color(0xFF050508)
    val DeepBlack = Color(0xFF030305)
    val SurfaceDark = Color(0xFF0A0A12)
    val CardSurface = Color(0xFF10101C)
    val CardElevated = Color(0xFF161626)
    val GlassCardBg = Color(0xB2121222)
    val BorderGlass = Color(0x28FFFFFF)
    val BorderGlassBright = Color(0x45FFFFFF)
    val BorderSubtle = Color(0xFF1C1C2C)

    val Violet = Color(0xFF7C3AED)
    val VioletSoft = Color(0xFFA78BFA)
    val Amber = Color(0xFFFFB020)
    val AmberGlow = Color(0xFFFF9100)
    val Rose = Color(0xFFF43F5E)
    val Cyan = Color(0xFF00E5FF)
    val Lime = Color(0xFF10B981)
    val BlueElectric = Color(0xFF3B82F6)

    val TextPrimary = Color(0xFFF9FAFB)
    val TextSecondary = Color(0xFF9CA3AF)
    val TextTertiary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF4B5563)
}

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Violet,
    onPrimary = Color.White,
    secondary = AppColors.Amber,
    onSecondary = Color.Black,
    tertiary = AppColors.Rose,
    background = AppColors.Ink,
    surface = AppColors.SurfaceDark,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary
)

@Composable
fun FreeYouTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}
