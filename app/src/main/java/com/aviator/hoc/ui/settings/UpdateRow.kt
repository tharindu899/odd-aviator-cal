package com.aviator.hoc.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import com.aviator.hoc.update.UpdateState

/** Compact update entry on the Settings page. The real flow (notes, download, install) lives in UpdateDialog. */
@Composable
fun UpdateRow(
    state: UpdateState,
    currentVersion: String,
    onCheck: () -> Unit,
    onOpen: () -> Unit,
) {
    val hasUpdate = state is UpdateState.Available || state is UpdateState.Ready ||
        state is UpdateState.NeedsPermission || state is UpdateState.Downloading

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (hasUpdate) Mint.LimeChip else Mint.Card2,
        border = BorderStroke(1.dp, if (hasUpdate) Mint.LimeBorder else Mint.Border),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(AppIcons.Download, null, tint = if (hasUpdate) Mint.Lime else Mint.Muted, modifier = Modifier.size(22.dp))
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = titleOf(state),
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "updTitle",
                ) { Text(it, style = MaterialTheme.typography.titleMedium, color = Mint.Text) }
                Text(
                    if (state is UpdateState.Error) state.message else "Installed: v$currentVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state is UpdateState.Error) Mint.PinkSoft else Mint.Muted,
                )
            }
            if (state != UpdateState.Checking) {
                PressableSurface(
                    onClick = if (hasUpdate) onOpen else onCheck,
                    shape = RoundedCornerShape(14.dp),
                    color = if (hasUpdate) Mint.Lime else Mint.Card,
                    border = if (hasUpdate) null else BorderStroke(1.dp, Mint.Border2),
                ) {
                    Text(
                        if (hasUpdate) "View" else "Check",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (hasUpdate) Mint.Black else Mint.Text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

private fun titleOf(s: UpdateState): String = when (s) {
    UpdateState.Idle -> "App updates"
    UpdateState.Checking -> "Checking…"
    UpdateState.UpToDate -> "You're up to date"
    is UpdateState.Available -> "Update available · ${s.info.name}"
    is UpdateState.Downloading -> "Downloading update…"
    is UpdateState.Ready -> "Update ready to install"
    is UpdateState.NeedsPermission -> "Permission needed"
    is UpdateState.Error -> "Couldn't check"
}
