package com.aviator.hoc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val MintScheme = darkColorScheme(
    primary = Mint.Lime,
    onPrimary = Mint.Black,
    primaryContainer = Mint.LimeChip,
    onPrimaryContainer = Mint.Lime,
    secondary = Mint.Amber,
    onSecondary = Mint.Black,
    tertiary = Mint.Pink,
    onTertiary = Mint.Black,
    background = Mint.Black,
    onBackground = Mint.Text,
    surface = Mint.Card,
    onSurface = Mint.Text,
    surfaceVariant = Mint.Card2,
    onSurfaceVariant = Mint.Muted,
    outline = Mint.Border2,
    outlineVariant = Mint.Border,
    error = Mint.Pink,
    onError = Mint.Black,
)

private val AviatorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AviatorTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val typography = remember { aviatorTypography(loadDisplayFont(context)) }
    MaterialTheme(
        colorScheme = MintScheme,
        typography = typography,
        shapes = AviatorShapes,
        content = content,
    )
}
