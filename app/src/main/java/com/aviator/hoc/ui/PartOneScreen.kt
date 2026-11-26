package com.aviator.hoc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aviator.hoc.core.formatClock
import com.aviator.hoc.data.GenerateResult
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.Stats
import com.aviator.hoc.ui.components.FlowingBar
import com.aviator.hoc.ui.theme.Mint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Part 1 smoke-test screen: proves the theme, the bar animation, Room and the signal logic
 * all work end-to-end. Part 2 replaces it with the real Home screen.
 */
@Composable
fun PartOneScreen(repository: SignalRepository, stats: Stats) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }

    // demo loop: 0 -> 1 every 8 s so you can watch the bar flow
    var progress by remember { mutableFloatStateOf(0f) }
    var seconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0..8) {
                progress = i / 8f
                seconds = (8 - i).toFloat()
                delay(1000)
            }
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text("Hello, Lalshan", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Next signal is coming up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // hero card
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = Mint.Card,
                border = BorderStroke(1.dp, Mint.Border),
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(10.dp).background(Mint.Lime, RoundedCornerShape(3.dp)))
                        Text("Bet now", style = MaterialTheme.typography.titleMedium, color = Mint.Lime)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Theme + animation check",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatClock(seconds.toLong()),
                        style = MaterialTheme.typography.displayLarge,
                        color = Mint.Lime,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowingBar(progress = progress)
                }
            }

            // stats row (live from Room)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Total", stats.total.toString(), Mint.Text, Modifier.weight(1f))
                StatTile("Today", stats.today.toString(), Mint.Text, Modifier.weight(1f))
                StatTile("Queue", stats.pending.toString(), Mint.Amber, Modifier.weight(1f))
                StatTile("Win %", "${stats.winRate}%", Mint.Lime, Modifier.weight(1f))
            }

            Button(
                onClick = {
                    scope.launch {
                        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        // 2.02x at "now" lands 2 min 2 s from now
                        message = when (val r = repository.generate("2.02x $now")) {
                            is GenerateResult.Success -> "Sample signal saved (#${r.id})"
                            is GenerateResult.Error -> r.message
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Mint.Lime, contentColor = Mint.Black),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Add sample signal", style = MaterialTheme.typography.labelLarge)
            }

            if (message.isNotEmpty()) {
                Text(message, style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Mint.Card,
        border = BorderStroke(1.dp, Mint.Border),
    ) {
        Column(
            Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = valueColor)
        }
    }
}
