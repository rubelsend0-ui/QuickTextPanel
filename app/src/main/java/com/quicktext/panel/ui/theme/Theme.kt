package com.quicktext.panel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quicktext.panel.data.repository.ThemeMode

val ElectricBlue = Color(0xFF2F7BFF)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0B2A66),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF00B8FF),
    onSecondary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFEAF0FF),
    surface = Color(0xFF0A0E17),
    onSurface = Color(0xFFEAF0FF),
    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF9FB0D0),
    outline = Color(0xFF26324A),
    error = Color(0xFFFF5470),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1558E6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B4D),
    secondary = Color(0xFF0088CC),
    background = Color(0xFFF5F8FF),
    onBackground = Color(0xFF0A1020),
    surface = Color.White,
    onSurface = Color(0xFF0A1020),
    surfaceVariant = Color(0xFFE6EEFF),
    onSurfaceVariant = Color(0xFF44526E),
    outline = Color(0xFFC3D0EA),
    error = Color(0xFFD62B4B),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun QuickTextTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}
