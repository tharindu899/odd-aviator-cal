package com.aviator.hoc.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.aviator.hoc.core.Hms
import com.aviator.hoc.core.canBet
import com.aviator.hoc.core.formatClock
import com.aviator.hoc.domain.HomeUiState
import com.aviator.hoc.ui.components.AnimatedDigits
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.FlowingBar
import com.aviator.hoc.ui.theme.Mint
import java.util.Locale

@Composable
fun HeroCard(state: HomeUiState, showCountdown: Boolean = true, modifier: Modifier = Modifier) {
    val cur = state.current
    if (cur == null) {
        EmptyHero(modifier)
        return
    }

    val live = cur.status.canBet
    val accent by animateColorAsState(if (live) Mint.Lime else Mint.Amber, tween(500), label = "accent")
    val accent2 by animateColorAsState(if (live) Mint.Lime2 else Mint.Amber2, tween(500), label = "accent2")
    val borderColor by animateColorAsState(
        (if (live) Mint.Lime else Mint.Amber).copy(alpha = 0.35f), tween(500), label = "border",
    )

    // countdown hidden in Settings -> show the signal time itself instead
    val clock = if (showCountdown) formatClock(state.readoutSeconds) else cur.resultTime.toString()
    val digitStyle = if (clock.length > 5) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = Mint.Card,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingSquare(accent)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (live) "Bet now" else "Waiting",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
                Spacer(Modifier.weight(1f))
                if (state.nearby > 0) {
                    Chip(text = "${state.nearby + 1}×", color = Mint.Muted, icon = true)
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    text = String.format(Locale.ROOT, "%.2fx", cur.odd),
                    style = MaterialTheme.typography.titleMedium,
                    color = Mint.Muted,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    !showCountdown -> "Signal at"
                    live -> "Window ends in"
                    else -> "Window opens in"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Mint.Muted,
            )
            AnimatedDigits(text = clock, style = digitStyle, color = accent)

            Spacer(Modifier.height(8.dp))
            FlowingBar(progress = state.progress, accent = accent, accent2 = accent2)

            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TimeLabel("Open", cur.times.open, bold = false)
                TimeLabel("Signal", cur.times.signal, bold = true)
                TimeLabel("Close", cur.times.close, bold = false)
            }
        }
    }
}

@Composable
private fun TimeLabel(label: String, time: Hms, bold: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
        Text(
            text = time.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = if (bold) Mint.Text else Mint.Muted,
        )
    }
}

@Composable
private fun PulsingSquare(color: androidx.compose.ui.graphics.Color) {
    val t = rememberInfiniteTransition(label = "pulse")
    val a by t.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulseAlpha",
    )
    Box(Modifier.size(10.dp).alpha(a).background(color, RoundedCornerShape(3.dp)))
}

@Composable
private fun Chip(text: String, color: androidx.compose.ui.graphics.Color, icon: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, Mint.Border2),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon) Icon(AppIcons.Layers, contentDescription = "Several signals", tint = color, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
private fun EmptyHero(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = Mint.Card,
        border = BorderStroke(1.dp, Mint.Border),
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(AppIcons.Timer, contentDescription = null, tint = Mint.Lime, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text("No active signal", style = MaterialTheme.typography.titleLarge, color = Mint.Text)
            Text(
                "Open Generate and add an odd with its time.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mint.Muted,
            )
        }
    }
}
