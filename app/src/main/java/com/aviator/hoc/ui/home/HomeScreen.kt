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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aviator.hoc.domain.HomeUiState
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.components.RiseIn
import com.aviator.hoc.ui.theme.Mint

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
