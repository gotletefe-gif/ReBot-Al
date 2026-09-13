package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode(val title: String, val subtitle: String) {
    SYSTEM("Sistem Teması (Otomatik)", "Cihazın açık/koyu modunu otomatik algılar"),
    LIGHT("Açık Mod", "Ferah ve parlak gündüz görünümü"),
    DARK("Koyu Mod", "Göz yormayan şık gece teması")
}

private val DarkColorScheme =
    darkColorScheme(
        primary = CrimsonLight,
        onPrimary = Color.White,
        primaryContainer = CrimsonDark,
        onPrimaryContainer = Color.White,
        secondary = AmberSecondary,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF4A3400),
        onSecondaryContainer = AmberLight,
        tertiary = LiveGreen,
        onTertiary = Color.Black,
        background = ObsidianBackground,
        onBackground = TextPrimary,
        surface = ObsidianSurface,
        onSurface = TextPrimary,
        surfaceVariant = ObsidianSurfaceVariant,
        onSurfaceVariant = TextSecondary,
        outline = ObsidianCardBorder,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = CrimsonPrimary,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFD8D8),
        onPrimaryContainer = CrimsonDark,
        secondary = Color(0xFFD97706),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF92400E),
        tertiary = Color(0xFF059669),
        onTertiary = Color.White,
        background = LightBg,
        onBackground = Color(0xFF0F172A),
        surface = LightSurface,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFE2E8F0),
    )

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            isDark -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


