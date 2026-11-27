package com.aviator.hoc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private fun schemeOf(p: MintPalette) = if (p.isDark) {
    darkColorScheme(
        primary = p.lime, onPrimary = p.onAccent,
        primaryContainer = p.limeChip, onPrimaryContainer = p.lime,
        secondary = p.amber, onSecondary = p.onAccent,
        tertiary = p.pink, onTertiary = p.onAccent,
        background = p.bg, onBackground = p.text,
        surface = p.card, onSurface = p.text,
        surfaceVariant = p.card2, onSurfaceVariant = p.muted,
        outline = p.border2, outlineVariant = p.border,
        error = p.pink, onError = p.onAccent,
    )
} else {
    lightColorScheme(
        primary = p.lime, onPrimary = p.onAccent,
        primaryContainer = p.limeChip, onPrimaryContainer = p.lime,
        secondary = p.amber, onSecondary = p.onAccent,
        tertiary = p.pink, onTertiary = p.onAccent,
        background = p.bg, onBackground = p.text,
        surface = p.card, onSurface = p.text,
        surfaceVariant = p.card2, onSurfaceVariant = p.muted,
        outline = p.border2, outlineVariant = p.border,
        error = p.pink, onError = p.onAccent,
    )
}

private val AviatorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AviatorTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val typography = remember { aviatorTypography(loadDisplayFont(context)) }
    val palette = if (dark) DarkPalette else LightPalette
    // set before any child reads it, so the first frame already has the right colours
    if (Mint.palette !== palette) Mint.palette = palette
    MaterialTheme(
        colorScheme = remember(dark) { schemeOf(palette) },
        typography = typography,
        shapes = AviatorShapes,
        content = content,
    )
}
