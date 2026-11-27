package com.aviator.hoc.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviator.hoc.core.hmsOfEpoch
import com.aviator.hoc.domain.HomeUiState
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.components.RiseIn
import com.aviator.hoc.ui.theme.Mint
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    state: HomeUiState,
    userName: String,
    showCountdown: Boolean,
    onGenerate: () -> Unit,
    onSettings: () -> Unit,
    updateAvailable: Boolean = false,
    bottomPadding: Dp,
) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RiseIn(0) { Header(userName, updateAvailable, onSettings) }
        RiseIn(80) { HeroCard(state, showCountdown) }
        RiseIn(160) { StatsRow(state) }
        RiseIn(240, Modifier.weight(1f)) {
            UpcomingList(
                signals = state.upcoming,
                onGenerate = onGenerate,
                showCountdown = showCountdown,
                bottomPadding = bottomPadding,
            )
        }
    }
}

@Composable
private fun Header(userName: String, updateAvailable: Boolean, onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Hello, $userName", style = MaterialTheme.typography.headlineMedium, color = Mint.Text)
            Text("Next signal is coming up", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
        }
        LiveClock()
        Spacer(Modifier.width(10.dp))
        Box(contentAlignment = Alignment.TopEnd) {
            PressableSurface(
                onClick = onSettings,
                shape = CircleShape,
                color = Mint.Card,
                border = BorderStroke(1.dp, Mint.Border2),
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    AppIcons.Menu, contentDescription = "Settings", tint = Mint.Text,
                    modifier = Modifier.padding(12.dp),
                )
            }
            if (updateAvailable) {
                // "update available" dot
                Box(
                    Modifier.size(12.dp).background(Mint.Lime, CircleShape)
                        .border(2.dp, Mint.Bg, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun StatsRow(state: HomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile("Total", state.stats.total.toString(), Mint.Text, Modifier.weight(1f))
        StatTile("Today", state.stats.today.toString(), Mint.Text, Modifier.weight(1f))
        StatTile("Queue", state.upcoming.size.toString(), Mint.Amber, Modifier.weight(1f))
        StatTile("Win %", "${state.stats.winRate}%", Mint.Lime, Modifier.weight(1f))
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
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = valueColor)
        }
    }
}

/**
 * Current device time with seconds (HH:mm:ss), ticking once per second.
 * Has its own ticker so only this small pill recomposes every second.
 */
@Composable
private fun LiveClock(modifier: Modifier = Modifier) {
    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            val t = System.currentTimeMillis()
            value = t
            delay(1000 - (t % 1000) + 5) // aligned to the next second boundary
        }
    }
    val hms = hmsOfEpoch(now)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Mint.Card,
        border = BorderStroke(1.dp, Mint.Border2),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(AppIcons.Clock, contentDescription = null, tint = Mint.Lime, modifier = Modifier.size(14.dp))
            Text(
                text = hms.toString(), // 24h HH:mm:ss, same clock the signals use
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFeatureSettings = "tnum",
                ),
                color = Mint.Text,
            )
        }
    }
}
