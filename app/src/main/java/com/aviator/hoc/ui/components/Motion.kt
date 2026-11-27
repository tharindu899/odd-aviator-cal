package com.aviator.hoc.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

/** false once a tab has been shown before: its content then appears instantly (tab switches stay smooth). */
val LocalPlayEntrance = compositionLocalOf { true }

/** Staggered entrance: fades + slides up after [delayMs]. Use inside a Column. */
@Composable
fun ColumnScope.RiseIn(
    delayMs: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val play = LocalPlayEntrance.current
    var shown by remember { mutableStateOf(!play) }
    LaunchedEffect(Unit) {
        if (!shown) {
            delay(delayMs.toLong())
            shown = true
        }
    }
    AnimatedVisibility(
        visible = shown,
        modifier = modifier,
        enter = fadeIn(tween(550)) + slideInVertically(tween(550)) { it / 5 },
    ) { content() }
}

/** Surface that squishes (springy) while pressed. */
@Composable
fun PressableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape,
    color: Color,
    border: BorderStroke? = null,
    pressedScale: Float = 0.92f,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press",
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        shape = shape,
        color = color,
        border = border,
        interactionSource = source,
        content = content,
    )
}

/** Big clock text where only the digit that changes rolls (slides + fades). */
@Composable
fun AnimatedDigits(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        text.forEachIndexed { index, ch ->
            // key by position so each slot animates on its own
            androidx.compose.runtime.key(index) {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = {
                        (slideInVertically(tween(220)) { h -> -h / 3 } + fadeIn(tween(220))) togetherWith
                            (slideOutVertically(tween(220)) { h -> h / 3 } + fadeOut(tween(160)))
                    },
                    label = "digit",
                ) { c ->
                    Text(text = c.toString(), style = style, color = color)
                }
            }
        }
    }
}
