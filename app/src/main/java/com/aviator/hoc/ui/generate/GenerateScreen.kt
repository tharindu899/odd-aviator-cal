package com.aviator.hoc.ui.generate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aviator.hoc.core.Hms
import com.aviator.hoc.domain.GeneratePreview
import com.aviator.hoc.ui.components.AnimatedDigits
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint

private data class Example(val text: String, val label: String)

private val EXAMPLES = listOf(
    Example("2.02x 21:31:22", "Standard"),
    Example("150x 23:34:00", "High"),
    Example("1,437.20x 06:31:56", "Very high"),
)

@Composable
fun GenerateScreen(
    state: GenerateUiState,
    onInput: (String) -> Unit,
    onPasted: (String) -> Unit,
    onClipboardEmpty: () -> Unit,
    onSubmit: () -> Unit,
    bottomPadding: Dp,
) {
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val focus = LocalFocusManager.current

    LaunchedEffect(state.success) {
        if (state.success) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val submit = {
        focus.clearFocus()
        onSubmit()
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text("Add signal", style = MaterialTheme.typography.headlineMedium, color = Mint.Text)
            Text("Paste the odd and its time", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
        }

        // ---------- input row ----------
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInput,
                modifier = Modifier.weight(1f),
                enabled = !state.generating,
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                placeholder = { Text("2.02x 21:31:22", color = Mint.Muted, style = MaterialTheme.typography.titleLarge) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (state.input.isNotBlank()) submit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Mint.Text,
                    unfocusedTextColor = Mint.Text,
                    disabledTextColor = Mint.Muted,
                    focusedContainerColor = Mint.Card,
                    unfocusedContainerColor = Mint.Card,
                    disabledContainerColor = Mint.Card,
                    focusedBorderColor = Mint.Lime,
                    unfocusedBorderColor = Mint.Border2,
                    disabledBorderColor = Mint.Border,
                    cursorColor = Mint.Lime,
                ),
            )
            PressableSurface(
                onClick = {
                    val text = clipboard.getText()?.text
                    if (text.isNullOrBlank()) onClipboardEmpty() else onPasted(text)
                },
                enabled = !state.generating,
                shape = RoundedCornerShape(20.dp),
                color = Mint.LimeChip,
                border = BorderStroke(1.dp, Mint.LimeBorder),
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(AppIcons.Clipboard, contentDescription = "Paste from clipboard", tint = Mint.Lime, modifier = Modifier.size(24.dp))
                }
            }
        }

        // ---------- banners ----------
        Banner(visible = state.error != null, text = state.error.orEmpty(), isError = true)
        Banner(visible = state.success, text = "Added to queue", isError = false)

        // ---------- examples ----------
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EXAMPLES, key = { it.text }) { ex ->
                PressableSurface(
                    onClick = { onInput(ex.text) },
                    shape = RoundedCornerShape(14.dp),
                    color = Mint.Card,
                    border = BorderStroke(1.dp, Mint.Border2),
                    pressedScale = 0.94f,
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(ex.text, style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), color = Mint.Lime)
                        Text(ex.label, style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
                    }
                }
            }
        }

        // ---------- live preview ----------
        PreviewCard(state.preview)

        // ---------- submit ----------
        val canSubmit = state.input.isNotBlank() && !state.generating
        PressableSurface(
            onClick = submit,
            enabled = canSubmit,
            shape = RoundedCornerShape(26.dp),
            color = Mint.Lime,
            pressedScale = 0.96f,
            modifier = Modifier.fillMaxWidth().height(56.dp).alpha(if (canSubmit || state.generating) 1f else 0.4f),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.generating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp,
                        color = Mint.Black, trackColor = Color.Transparent,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text("Calculating…", style = MaterialTheme.typography.labelLarge, color = Mint.Black)
                } else {
                    Icon(AppIcons.Play, contentDescription = null, tint = Mint.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(10.dp))
                    Text("Add to queue", style = MaterialTheme.typography.labelLarge, color = Mint.Black)
                }
            }
        }
    }
}

@Composable
private fun Banner(visible: Boolean, text: String, isError: Boolean) {
    // keep the last message so it doesn't go blank while the banner is animating out
    val last = androidx.compose.runtime.remember { arrayOf("") }
    if (text.isNotEmpty()) last[0] = text
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(tween(250)) + fadeIn(tween(250)),
        exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
    ) {
        val tone = if (isError) Mint.PinkSoft else Mint.Lime
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (isError) Mint.PinkChip else Mint.LimeChip,
            border = BorderStroke(1.dp, if (isError) Mint.Pink.copy(alpha = 0.5f) else Mint.LimeBorder),
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    if (isError) AppIcons.Alert else AppIcons.Check,
                    contentDescription = null, tint = tone, modifier = Modifier.size(18.dp),
                )
                Text(last[0], style = MaterialTheme.typography.bodyMedium, color = tone)
            }
        }
    }
}

@Composable
private fun PreviewCard(preview: GeneratePreview?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = Mint.Card,
        border = BorderStroke(1.dp, if (preview != null) Mint.LimeBorder else Mint.Border),
    ) {
        AnimatedContent(
            targetState = preview != null,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "preview",
        ) { has ->
            if (has && preview != null) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "SIGNAL TIME" + if (preview.nextDay) "  +1d" else "",
                        style = MaterialTheme.typography.labelSmall, color = Mint.Lime,
                    )
                    AnimatedDigits(
                        text = preview.resultTime.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Mint.Lime,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeTile("Open", preview.window.open, Modifier.weight(1f))
                        TimeTile("Close", preview.window.close, Modifier.weight(1f))
                    }
                    if (preview.alreadyPassed) {
                        Text(
                            "This time has already passed. It will be marked missed right away.",
                            style = MaterialTheme.typography.bodySmall, color = Mint.Amber,
                        )
                    }
                }
            } else {
                Text(
                    "Format: odd x HH:MM:SS. The signal time and its 45 s window appear here as you type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mint.Muted,
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun TimeTile(label: String, time: Hms, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Mint.Card2, border = BorderStroke(1.dp, Mint.Border)) {
        Column(Modifier.padding(vertical = 10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Text(time.toString(), style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), color = Mint.Text)
        }
    }
}
