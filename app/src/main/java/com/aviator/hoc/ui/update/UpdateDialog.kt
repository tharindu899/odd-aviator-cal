package com.aviator.hoc.ui.update

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.FlowingBar
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import com.aviator.hoc.update.UpdateState
import java.util.Locale

/**
 * The update popup: announces the new version, shows an animated progress orb while the APK
 * downloads, then a bouncing check mark when it is ready to install.
 */
@Composable
fun UpdateDialog(
    state: UpdateState,
    onInstall: () -> Unit,
    onCancel: () -> Unit,
    onClose: () -> Unit,
) {
    val downloading = state is UpdateState.Downloading
    Dialog(
        onDismissRequest = { if (!downloading) onClose() },
        properties = DialogProperties(
            dismissOnBackPress = !downloading,
            dismissOnClickOutside = !downloading,
            usePlatformDefaultWidth = false,
        ),
    ) {
        // pop-in: the dialog window has no enter animation of its own
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { shown = true }
        AnimatedVisibility(
            visible = shown,
            enter = fadeIn(tween(200)) +
                scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), initialScale = 0.8f) +
                slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 8 },
            exit = fadeOut(tween(120)) + slideOutVertically(tween(160)) { it / 10 },
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Mint.Card,
                border = BorderStroke(1.dp, Mint.LimeBorder),
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Orb(state)
                    AnimatedContent(
                        targetState = kindOf(state),
                        transitionSpec = { fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120)) },
                        label = "updBody",
                    ) { kind ->
                        Body(kind, state, onInstall, onCancel, onClose)
                    }
                }
            }
        }
    }
}

private enum class Kind { AVAILABLE, DOWNLOADING, READY, PERMISSION, ERROR, OTHER }

private fun kindOf(s: UpdateState) = when (s) {
    is UpdateState.Available -> Kind.AVAILABLE
    is UpdateState.Downloading -> Kind.DOWNLOADING
    is UpdateState.Ready -> Kind.READY
    is UpdateState.NeedsPermission -> Kind.PERMISSION
    is UpdateState.Error -> Kind.ERROR
    else -> Kind.OTHER
}

@Composable
private fun Body(
    kind: Kind,
    state: UpdateState,
    onInstall: () -> Unit,
    onCancel: () -> Unit,
    onClose: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (kind) {
            Kind.AVAILABLE -> {
                val info = (state as? UpdateState.Available)?.info
                Heading("Update available", info?.name.orEmpty())
                if (!info?.notes.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(16.dp), color = Mint.Card2, border = BorderStroke(1.dp, Mint.Border)) {
                        Text(
                            info!!.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Mint.Muted,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DialogButton("Later", filled = false, onClick = onClose, modifier = Modifier.weight(1f))
                    DialogButton(
                        "Update now" + sizeSuffix(info?.sizeBytes ?: 0L), filled = true,
                        onClick = onInstall, modifier = Modifier.weight(1.4f),
                    )
                }
            }
            Kind.DOWNLOADING -> {
                val d = state as? UpdateState.Downloading
                val p = d?.progress ?: 0f
                Heading("Downloading…", "Keep the app open")
                FlowingBar(progress = p, accent = Mint.Lime, accent2 = Mint.Lime2)
                val total = d?.info?.sizeBytes ?: 0L
                Text(
                    if (total > 0) String.format(Locale.ROOT, "%.1f of %.1f MB", p * total / 1_048_576.0, total / 1_048_576.0)
                    else "${(p * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall, color = Mint.Muted,
                )
                DialogButton("Cancel", filled = false, onClick = onCancel, modifier = Modifier.fillMaxWidth())
            }
            Kind.READY -> {
                Heading("Ready to install", "Confirm in the Android installer")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DialogButton("Close", filled = false, onClick = onClose, modifier = Modifier.weight(1f))
                    DialogButton("Install", filled = true, onClick = onInstall, modifier = Modifier.weight(1.4f))
                }
            }
            Kind.PERMISSION -> {
                Heading("One quick step", "Allow “Install unknown apps” for Aviator HOC, then come back and tap Install")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DialogButton("Close", filled = false, onClick = onClose, modifier = Modifier.weight(1f))
                    DialogButton("Install", filled = true, onClick = onInstall, modifier = Modifier.weight(1.4f))
                }
            }
            Kind.ERROR -> {
                Heading("Update failed", (state as? UpdateState.Error)?.message.orEmpty())
                DialogButton("Close", filled = false, onClick = onClose, modifier = Modifier.fillMaxWidth())
            }
            Kind.OTHER -> Unit
        }
    }
}

@Composable
private fun Heading(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.headlineMedium, color = Mint.Text, textAlign = TextAlign.Center)
    if (subtitle.isNotBlank()) {
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Mint.Muted, textAlign = TextAlign.Center)
    }
}

private fun sizeSuffix(bytes: Long): String =
    if (bytes > 0) String.format(Locale.ROOT, " · %.1f MB", bytes / 1_048_576.0) else ""

@Composable
private fun DialogButton(label: String, filled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (filled) Mint.Lime else Mint.Card2,
        border = if (filled) null else BorderStroke(1.dp, Mint.Border2),
        modifier = modifier,
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(
                label, style = MaterialTheme.typography.labelLarge, maxLines = 1,
                color = if (filled) Mint.Black else Mint.Text,
            )
        }
    }
}

/**
 * The animated centrepiece: a glowing orb. Idle = a lime arc orbiting the ring around a bobbing
 * download arrow. Downloading = the ring fills with the real progress and the % counts up in the
 * middle. Ready = the ring completes and a check mark bounces in.
 */
@Composable
private fun Orb(state: UpdateState) {
    val inf = rememberInfiniteTransition(label = "orb")
    val pulse by inf.animateFloat(
        0.9f, 1.08f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse",
    )
    val spin by inf.animateFloat(
        0f, 360f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "spin",
    )
    val bob by inf.animateFloat(
        -3f, 3f, infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob",
    )

    val downloading = state as? UpdateState.Downloading
    val ready = state is UpdateState.Ready
    val failed = state is UpdateState.Error
    val target = when {
        downloading != null -> downloading.progress
        ready -> 1f
        else -> 0f
    }
    val progress by animateFloatAsState(target, tween(350, easing = LinearOutSlowInEasing), label = "orbProgress")
    val determinate = downloading != null || ready

    val lime = if (failed) Mint.Pink else Mint.Lime
    val lime2 = if (failed) Mint.PinkSoft else Mint.Lime2
    val track = Mint.Border2

    Box(Modifier.size(136.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val glowR = size.minDimension / 2f * pulse
            drawCircle(
                brush = Brush.radialGradient(listOf(lime.copy(alpha = 0.30f), Color.Transparent), radius = glowR),
                radius = glowR,
            )
            val d = size.minDimension - 28.dp.toPx()
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))

            if (determinate) {
                rotate(-90f) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(lime2, lime, lime2)),
                        startAngle = 0f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
            } else {
                rotate(spin) {
                    drawArc(
                        color = lime,
                        startAngle = 0f,
                        sweepAngle = 95f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
            }
        }

        val center = when {
            ready -> 2
            failed -> 3
            downloading != null -> 1
            else -> 0
        }
        AnimatedContent(
            targetState = center,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(180)) + scaleIn(spring(Spring.DampingRatioMediumBouncy), initialScale = 0.4f),
                    initialContentExit = fadeOut(tween(120)),
                )
            },
            label = "orbCenter",
        ) { c ->
            when (c) {
                1 -> Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium, color = Mint.Text,
                )
                2 -> Icon(AppIcons.Check, null, tint = lime, modifier = Modifier.size(44.dp))
                3 -> Icon(AppIcons.Alert, null, tint = lime, modifier = Modifier.size(40.dp))
                else -> Icon(
                    AppIcons.Download, null, tint = lime,
                    modifier = Modifier.size(38.dp).offset(y = bob.dp),
                )
            }
        }
    }
}
