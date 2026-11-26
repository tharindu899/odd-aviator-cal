@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.aviator.hoc.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val SORA_ASSET = "fonts/sora.ttf"
private const val TNUM = "tnum"

/**
 * Sora (variable font) is optional: CI / scripts/get-font.sh drop it into assets/fonts/sora.ttf.
 * If the file is missing or broken the app silently falls back to the system sans-serif,
 * so the build and the app never fail because of a font.
 */
fun loadDisplayFont(context: Context): FontFamily {
    return try {
        val present = context.assets.list("fonts")?.contains("sora.ttf") == true
        if (!present) return FontFamily.SansSerif
        fun f(w: FontWeight) = Font(
            path = SORA_ASSET,
            assetManager = context.assets,
            weight = w,
            variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)),
        )
        FontFamily(f(FontWeight.Medium), f(FontWeight.SemiBold), f(FontWeight.Bold), f(FontWeight.ExtraBold))
    } catch (e: Exception) {
        FontFamily.SansSerif
    }
}

fun aviatorTypography(display: FontFamily) = Typography(
    // giant countdown
    displayLarge = TextStyle(
        fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 92.sp,
        lineHeight = 88.sp, letterSpacing = (-5.5).sp, fontFeatureSettings = TNUM,
    ),
    displayMedium = TextStyle(
        fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp,
        lineHeight = 52.sp, letterSpacing = (-2.5).sp, fontFeatureSettings = TNUM,
    ),
    headlineMedium = TextStyle(
        fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
        lineHeight = 30.sp, letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 17.sp,
        lineHeight = 22.sp, fontFeatureSettings = TNUM,
    ),
    titleMedium = TextStyle(
        fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp,
    ),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
)
