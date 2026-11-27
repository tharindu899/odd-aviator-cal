package com.aviator.hoc.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aviator.hoc.core.WindowStatus
import com.aviator.hoc.core.formatCountdown
import com.aviator.hoc.core.windowOpensInSeconds
import com.aviator.hoc.domain.LiveSignal
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import java.util.Locale
import kotlin.math.abs

@Composable
fun UpcomingList(
    signals: List<LiveSignal>,
    onGenerate: () -> Unit,
    showCountdown: Boolean = true,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Upcoming", style = MaterialTheme.typography.titleMedium, color = Mint.Text)
            Spacer(Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(10.dp), color = Mint.LimeChip, border = BorderStroke(1.dp, Mint.LimeBorder)) {
                Text(
                    "${signals.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mint.Lime,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }

        if (signals.isEmpty()) {
            EmptyUpcoming(onGenerate)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                items(signals, key = { it.id }) { s ->
                    UpcomingRow(s, showCountdown, Modifier.animateItem())
                }
            }
        }
    }
}

private fun countdownText(s: LiveSignal, showCountdown: Boolean): String = if (!showCountdown) {
    when (s.status) {
        WindowStatus.WAITING -> "Waiting"
        WindowStatus.ACTIVE -> "Open"
        WindowStatus.GRACE -> "Closing"
        WindowStatus.CLOSED -> "Closed"
    }
} else when (s.status) {
    WindowStatus.WAITING -> "-" + formatCountdown(windowOpensInSeconds(s.countdown))
    WindowStatus.ACTIVE -> formatCountdown(s.countdown)
    WindowStatus.GRACE -> "+" + formatCountdown(abs(s.countdown))
    WindowStatus.CLOSED -> "--:--"
}

@Composable
private fun UpcomingRow(s: LiveSignal, showCountdown: Boolean, modifier: Modifier = Modifier) {
    val betNow = s.status == WindowStatus.ACTIVE || s.status == WindowStatus.GRACE
    val tone = if (betNow) Mint.Lime else Mint.Amber
    val pulse = rememberInfiniteTransition(label = "rowPulse")
    val a by pulse.animateFloat(
        initialValue = 1f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "rowAlpha",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (betNow) Mint.LimeChip else Mint.Card,
        border = BorderStroke(1.dp, if (betNow) Mint.LimeBorder else Mint.Border),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(s.resultTime.toString(), style = MaterialTheme.typography.titleLarge, color = Mint.Text)
                Text(
                    "${String.format(Locale.ROOT, "%.2f", s.odd)}x · ${s.times.open} → ${s.times.close}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mint.Muted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(50), color = Mint.LimeChip.copy(alpha = if (betNow) 0f else 1f)) {
                    Text(
                        countdownText(s, showCountdown),
                        style = MaterialTheme.typography.titleMedium,
                        color = tone,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                if (betNow) {
                    Text(
                        "BET NOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mint.Lime,
                        modifier = Modifier.alpha(a),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyUpcoming(onGenerate: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Mint.Card,
        border = BorderStroke(1.dp, Mint.Border),
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("No upcoming signals", style = MaterialTheme.typography.titleMedium, color = Mint.Text)
            Text("Add your first one to start the timer.", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
            Spacer(Modifier.height(10.dp))
            PressableSurface(
                onClick = onGenerate,
                shape = RoundedCornerShape(24.dp),
                color = Mint.Lime,
            ) {
                Text(
                    "Generate signal",
                    style = MaterialTheme.typography.labelLarge,
                    color = Mint.Black,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 13.dp),
                )
            }
        }
    }
}
