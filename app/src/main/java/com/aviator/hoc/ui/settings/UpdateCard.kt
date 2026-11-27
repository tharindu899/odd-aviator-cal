package com.aviator.hoc.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.FlowingBar
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import com.aviator.hoc.update.UpdateState
import java.util.Locale

@Composable
fun UpdateCard(
    state: UpdateState,
    currentVersion: String,
    onCheck: () -> Unit,
    onInstall: () -> Unit,
) {
    val available = state is UpdateState.Available || state is UpdateState.Ready ||
        state is UpdateState.NeedsPermission || state is UpdateState.Downloading

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (available) Mint.LimeChip else Mint.Card2,
        border = BorderStroke(1.dp, if (available) Mint.LimeBorder else Mint.Border),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(AppIcons.Download, null, tint = if (available) Mint.Lime else Mint.Muted, modifier = Modifier.size(22.dp))
                Column(Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = titleOf(state),
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "updTitle",
                    ) { Text(it, style = MaterialTheme.typography.titleMedium, color = Mint.Text) }
                    Text("Installed: v$currentVersion", style = MaterialTheme.typography.bodySmall, color = Mint.Muted)
                }
            }

            when (state) {
                is UpdateState.Available -> {
                    if (state.info.notes.isNotBlank()) {
                        Text(state.info.notes, style = MaterialTheme.typography.bodySmall, color = Mint.Muted)
                    }
                    ActionButton(
                        "Download & install" + sizeSuffix(state.info.sizeBytes), filled = true, onClick = onInstall,
                    )
                }
                is UpdateState.Downloading -> {
                    FlowingBar(progress = state.progress, accent = Mint.Lime, accent2 = Mint.Lime2)
                    Text(
                        "Downloading… ${(state.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall, color = Mint.Muted,
                    )
                }
                is UpdateState.Ready -> ActionButton("Install now", filled = true, onClick = onInstall)
                is UpdateState.NeedsPermission -> {
                    Text(
                        "Allow “Install unknown apps” for Aviator HOC in the screen that just opened, then come back and tap Install.",
                        style = MaterialTheme.typography.bodySmall, color = Mint.Amber,
                    )
                    ActionButton("Install", filled = true, onClick = onInstall)
                }
                is UpdateState.Error -> {
                    Text(state.message, style = MaterialTheme.typography.bodySmall, color = Mint.PinkSoft)
                    ActionButton("Try again", filled = false, onClick = onCheck)
                }
                UpdateState.Checking -> Unit
                UpdateState.UpToDate, UpdateState.Idle ->
                    ActionButton("Check for updates", filled = false, onClick = onCheck)
            }
        }
    }
}

private fun titleOf(s: UpdateState): String = when (s) {
    UpdateState.Idle -> "App updates"
    UpdateState.Checking -> "Checking…"
    UpdateState.UpToDate -> "You're up to date"
    is UpdateState.Available -> "Update available · ${s.info.name}"
    is UpdateState.Downloading -> "Downloading update"
    is UpdateState.Ready -> "Update ready"
    is UpdateState.NeedsPermission -> "Permission needed"
    is UpdateState.Error -> "Couldn't check"
}

private fun sizeSuffix(bytes: Long): String =
    if (bytes > 0) String.format(Locale.ROOT, " (%.1f MB)", bytes / 1_048_576.0) else ""

@Composable
private fun ActionButton(label: String, filled: Boolean, onClick: () -> Unit) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (filled) Mint.Lime else Mint.Card,
        border = if (filled) null else BorderStroke(1.dp, Mint.Border2),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
            Text(
                label, style = MaterialTheme.typography.labelLarge,
                color = if (filled) Mint.Black else Mint.Text,
            )
        }
    }
}
