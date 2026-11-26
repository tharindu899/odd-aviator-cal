package com.aviator.hoc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.aviator.hoc.ui.theme.Mint

/**
 * Progress bar from the approved design: smooth gradient that flows left -> right,
 * with a soft light sheen sweeping over the filled part. No dots, no blocky stripes.
 */
@Composable
fun FlowingBar(
    progress: Float,
    accent: Color = Mint.Lime,
    accent2: Color = Mint.Lime2,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFF1A1A1A),
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "barProgress",
    )
    val infinite = rememberInfiniteTransition(label = "barFlow")
    val flow by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "flow",
    )
    val sheen by infinite.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "sheen",
    )

    Canvas(modifier = modifier.fillMaxWidth().height(14.dp)) {
        val r = CornerRadius(size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = r)

        val w = size.width * animated
        if (w <= 0f) return@Canvas

        clipRect(left = 0f, top = 0f, right = w, bottom = size.height) {
            // gradient is 2x the bar wide and mirrored, so shifting it makes light flow left -> right
            val span = size.width
            val shift = flow * span * 2f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(accent2, accent, Color.White, accent, accent2),
                    startX = -span * 2f + shift,
                    endX = shift,
                    tileMode = TileMode.Mirror,
                ),
                size = Size(w, size.height),
                cornerRadius = r,
            )
            // sheen
            val sw = w * 0.3f
            val sx = sheen * w
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent),
                    startX = sx,
                    endX = sx + sw,
                ),
                topLeft = Offset(sx, 0f),
                size = Size(sw, size.height),
            )
        }
    }
}
