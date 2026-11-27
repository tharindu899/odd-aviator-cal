package com.aviator.hoc.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class MintPalette(
    val isDark: Boolean,
    val bg: Color,
    val card: Color,
    val card2: Color,
    val border: Color,
    val border2: Color,
    val text: Color,
    val muted: Color,
    val lime: Color,
    val lime2: Color,
    val limeChip: Color,
    val limeBorder: Color,
    val amber: Color,
    val amber2: Color,
    val pink: Color,
    val pinkSoft: Color,
    val pinkChip: Color,
    val track: Color,
    /** text / icons drawn on top of the lime or pink fills */
    val onAccent: Color,
)

/** "D · Neon Mint (OLED)" – night */
val DarkPalette = MintPalette(
    isDark = true,
    bg = Color(0xFF000000),
    card = Color(0xFF0B0B0B),
    card2 = Color(0xFF101010),
    border = Color(0xFF1F1F1F),
    border2 = Color(0xFF262626),
    text = Color(0xFFF2F5EE),
    muted = Color(0xFF9AA093),
    lime = Color(0xFFB6FF5C),
    lime2 = Color(0xFF8FD43A),
    limeChip = Color(0xFF141A0C),
    limeBorder = Color(0xFF2B3A12),
    amber = Color(0xFFFFC53D),
    amber2 = Color(0xFFD69E1C),
    pink = Color(0xFFFF6B8A),
    pinkSoft = Color(0xFFFF9DB2),
    pinkChip = Color(0xFF1C0D12),
    track = Color(0xFF1A1A1A),
    onAccent = Color(0xFF000000),
)

/** Day: soft off-white, deeper green accent so text stays readable */
val LightPalette = MintPalette(
    isDark = false,
    bg = Color(0xFFF4F7EF),
    card = Color(0xFFFFFFFF),
    card2 = Color(0xFFF0F4E8),
    border = Color(0xFFE2E7D8),
    border2 = Color(0xFFD1D8C4),
    text = Color(0xFF151A0E),
    muted = Color(0xFF636B58),
    lime = Color(0xFF3F8F00),
    lime2 = Color(0xFF63B31C),
    limeChip = Color(0xFFE6F4D2),
    limeBorder = Color(0xFFB5D98A),
    amber = Color(0xFFB87400),
    amber2 = Color(0xFFE0A21A),
    pink = Color(0xFFD6335A),
    pinkSoft = Color(0xFFC2274E),
    pinkChip = Color(0xFFFCE8EE),
    track = Color(0xFFE4E9DA),
    onAccent = Color(0xFFFFFFFF),
)

/**
 * Colour accessors used everywhere in the UI. They read a Compose state, so switching the
 * palette recomposes (and animates, where animateColorAsState is used) with no call-site changes.
 */
object Mint {
    var palette by mutableStateOf(DarkPalette)

    val Bg: Color get() = palette.bg
    /** text / icon colour for content on top of Lime / Pink fills (black at night, white by day) */
    val Black: Color get() = palette.onAccent
    val Card: Color get() = palette.card
    val Card2: Color get() = palette.card2
    val Border: Color get() = palette.border
    val Border2: Color get() = palette.border2

    val Text: Color get() = palette.text
    val Muted: Color get() = palette.muted

    val Lime: Color get() = palette.lime
    val Lime2: Color get() = palette.lime2
    val LimeChip: Color get() = palette.limeChip
    val LimeBorder: Color get() = palette.limeBorder

    val Amber: Color get() = palette.amber
    val Amber2: Color get() = palette.amber2

    val Pink: Color get() = palette.pink
    val PinkSoft: Color get() = palette.pinkSoft
    val PinkChip: Color get() = palette.pinkChip
    val Track: Color get() = palette.track
}
