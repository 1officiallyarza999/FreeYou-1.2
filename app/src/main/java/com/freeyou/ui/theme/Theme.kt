package com.freeyou.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

object AppColors {
    // Base Backgrounds (OLED Black & Deep Liquid Glass tones)
    val Background = Color(0xFF000000)
    val Ink = Color(0xFF030303)
    val DeepBlack = Color(0xFF000000)
    val SurfaceDark = Color(0xFF0A0A0E)
    
    // Glass Cards & Surfaces
    val CardSurface = Color(0x99101014)
    val CardElevated = Color(0xCC15151C)
    val GlassCardBg = Color(0x33101018)
    
    // Borders (Liquid Glass reflections)
    val BorderGlass = Color(0x1AFFFFFF)
    val BorderGlassBright = Color(0x33FFFFFF)
    val BorderSubtle = Color(0x0DFFFFFF)
    
    // --- Core Accent Palettes (Black Liquid Glass) ---
    
    // Cyan (Futuristic & Sharp)
    val Cyan = Color(0xFF00E5FF)
    val CyanGlow = Color(0x6600E5FF)
    val CyanGlass = Color(0x1A00E5FF)
    val CyanSoft = Color(0xFF67E8F9)
    
    // Electric Blue (Depth & Technology)
    val BlueElectric = Color(0xFF0047FF)
    val BlueGlow = Color(0x660047FF)
    val BlueGlass = Color(0x1A0047FF)
    val BlueSoft = Color(0xFF60A5FA)
    
    // Purple (Premium & Human)
    val Purple = Color(0xFF8B5CF6)
    val PurpleGlow = Color(0x668B5CF6)
    val PurpleGlass = Color(0x1A8B5CF6)
    val PurpleSoft = Color(0xFFA78BFA)
    
    // Emerald / Green (Success & Growth)
    val Emerald = Color(0xFF10B981)
    val EmeraldGlow = Color(0x6610B981)
    val EmeraldGlass = Color(0x1A10B981)
    val EmeraldSoft = Color(0xFF34D399)

    // Amber / Warning
    val Amber = Color(0xFFFFB020)
    val AmberGlow = Color(0x66FFB020)
    val AmberGlass = Color(0x1AFFB020)

    // Rose / Alert
    val Rose = Color(0xFFF43F5E)
    val RoseGlow = Color(0x66F43F5E)
    val RoseGlass = Color(0x1AF43F5E)
    
    // Text Typography
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA1A1AA)
    val TextTertiary = Color(0xFF71717A)
    val TextMuted = Color(0xFF52525B)
    
    // Legacy Aliases (to prevent compilation errors)
    val Violet = Purple
    val VioletSoft = PurpleSoft
    val Lime = Emerald
}

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Cyan,
    onPrimary = Color.Black,
    secondary = AppColors.Purple,
    onSecondary = Color.White,
    tertiary = AppColors.BlueElectric,
    background = AppColors.Background,
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
