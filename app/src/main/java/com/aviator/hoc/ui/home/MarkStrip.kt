package com.aviator.hoc.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.aviator.hoc.domain.MarkInfo
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import java.util.Locale

private class LastMark { var value: MarkInfo? = null }

/** WIN / LOSS bar: springs up 25 s before the window closes, leaves 5 s after. */
@Composable
fun MarkStrip(
    mark: MarkInfo?,
    onWin: (Long) -> Unit,
    onLoss: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // keep the last content so the exit animation still has something to draw
    val last = remember { LastMark() }
    if (mark != null) last.value = mark

    AnimatedVisibility(
        visible = mark != null,
        modifier = modifier,
        enter = slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) { it } +
            fadeIn(tween(200)) + scaleIn(initialScale = 0.92f),
        exit = slideOutVertically(tween(220)) { it } + fadeOut(tween(180)),
    ) {
        last.value?.let { m -> MarkContent(m, onWin, onLoss) }
    }
}

@Composable
private fun MarkContent(m: MarkInfo, onWin: (Long) -> Unit, onLoss: (Long) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Mint.LimeChip,
        border = BorderStroke(1.dp, Mint.Lime),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(m.signal.resultTime.toString(), style = MaterialTheme.typography.titleLarge, color = Mint.Text)
                Text(
                    "${String.format(Locale.ROOT, "%.2f", m.signal.odd)}x · ${m.secondsLeft}s left",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mint.Amber,
                )
            }
            PressableSurface(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onWin(m.signal.id) },
                shape = RoundedCornerShape(16.dp),
                color = Mint.Lime,
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcons.Check, contentDescription = null, tint = Mint.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("WIN", style = MaterialTheme.typography.labelLarge, color = Mint.Black)
                }
            }
            PressableSurface(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLoss(m.signal.id) },
                shape = RoundedCornerShape(16.dp),
                color = Mint.Card,
                border = BorderStroke(1.dp, Mint.Pink),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcons.Close, contentDescription = null, tint = Mint.PinkSoft, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("LOSS", style = MaterialTheme.typography.labelLarge, color = Mint.PinkSoft)
                }
            }
        }
    }
}
