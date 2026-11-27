package com.aviator.hoc.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aviator.hoc.core.MARK_SECONDS_MAX
import com.aviator.hoc.core.MARK_SECONDS_MIN
import com.aviator.hoc.data.AppSettings
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import com.aviator.hoc.update.UpdateState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Full-screen settings page. Slides in from the right (see AppRoot), back arrow / system back closes it. */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    saved: Boolean,
    onBack: () -> Unit,
    onAutoMark: (Boolean) -> Unit,
    onMissedAfter: (Int) -> Unit,
    onCountdown: (Boolean) -> Unit,
    onTheme: (String) -> Unit,
    onNotify: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onName: (String) -> Unit,
    onReset: () -> Unit,
    update: UpdateState,
    appVersion: String,
    onCheckUpdate: () -> Unit,
    onOpenUpdate: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Surface(color = Mint.Bg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PressableSurface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Mint.Card,
                    border = BorderStroke(1.dp, Mint.Border2),
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(AppIcons.Back, contentDescription = "Back", tint = Mint.Text, modifier = Modifier.padding(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Mint.Text)
                    Text("Everything stays on this phone", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
                }
                SavedFlash(saved)
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 4.dp, bottom = 120.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UpdateRow(update, appVersion, onCheckUpdate, onOpenUpdate)

                NameField(settings.userName, onName)

                ThemePicker(settings.themeMode, onTheme)

                SettingRow(
                    icon = AppIcons.Clock,
                    title = "Auto-mark missed",
                    subtitle = "Pending signals become Missed ${settings.missedAfterSeconds} s after the window closes",
                    checked = settings.autoMarkMissed,
                    onChange = onAutoMark,
                )
                MissedDelayPicker(settings.missedAfterSeconds, onMissedAfter)
                SettingRow(
                    icon = AppIcons.Timer,
                    title = "Live countdown",
                    subtitle = "Show countdown timers on Home",
                    checked = settings.showCountdown,
                    onChange = onCountdown,
                )
                SettingRow(
                    icon = AppIcons.Alert,
                    title = "Signal notifications",
                    subtitle = "Notify with sound 1 minute before a signal, even when the app is closed",
                    checked = settings.notify,
                    onChange = onNotify,
                )
                SettingRow(
                    icon = AppIcons.Bolt,
                    title = "In-app beeps",
                    subtitle = "Beep inside the app when the window opens and when WIN / LOSS appears",
                    checked = settings.sound,
                    onChange = onSound,
                )
                SettingRow(
                    icon = AppIcons.Activity,
                    title = "Vibration",
                    subtitle = "Buzz at the same moments",
                    checked = settings.haptics,
                    onChange = onHaptics,
                )

                ResetRow(onReset)
            }
        }
    }
}

@Composable
private fun SavedFlash(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + scaleIn(spring(Spring.DampingRatioMediumBouncy), initialScale = 0.7f),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.9f),
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Mint.LimeChip,
            border = BorderStroke(1.dp, Mint.LimeBorder),
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(AppIcons.Check, null, tint = Mint.Lime, modifier = Modifier.size(14.dp))
                Text("Saved", style = MaterialTheme.typography.labelSmall, color = Mint.Lime)
            }
        }
    }
}

@Composable
private fun NameField(saved: String, onCommit: (String) -> Unit) {
    var text by remember(saved) { mutableStateOf(saved) }
    val focus = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    val border by animateColorAsState(if (focused) Mint.Lime else Mint.Border, tween(200), label = "nameBorder")

    fun commit() {
        if (text.trim() != saved) onCommit(text)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Your name", style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            BasicTextField(
                value = text,
                onValueChange = { text = it.take(AppSettings.MAX_NAME) },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(color = Mint.Text),
                cursorBrush = SolidColor(Mint.Lime),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commit(); focus.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (focused && !it.isFocused) commit()
                        focused = it.isFocused
                    },
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val borderColor by animateColorAsState(if (checked) Mint.LimeBorder else Mint.Border, tween(250), label = "rowBorder")
    val iconTint by animateColorAsState(if (checked) Mint.Lime else Mint.Muted, tween(250), label = "rowIcon")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChange(!checked)
            },
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Mint.Text)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mint.Muted)
            }
            MintSwitch(checked)
        }
    }
}

/** Pill switch: thumb springs across, track colour cross-fades. */
@Composable
private fun MintSwitch(checked: Boolean) {
    val track by animateColorAsState(if (checked) Mint.Lime else Mint.Border2, tween(250), label = "track")
    val thumb by animateColorAsState(if (checked) Mint.Black else Mint.Muted, tween(250), label = "thumb")
    val x by animateDpAsState(
        if (checked) 22.dp else 2.dp,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "thumbX",
    )
    Box(
        Modifier.width(48.dp).height(28.dp).background(track, CircleShape),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.offset(x = x).size(24.dp).background(thumb, CircleShape))
    }
}

@Composable
private fun ResetRow(onReset: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }
    // confirmation expires on its own
    LaunchedEffect(confirming) {
        if (confirming) {
            delay(4000)
            confirming = false
        }
    }
    val border by animateColorAsState(if (confirming) Mint.Pink else Mint.Border, tween(200), label = "resetBorder")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Reset settings", style = MaterialTheme.typography.titleMedium, color = Mint.Text)
                Text("Your signals and results are kept", style = MaterialTheme.typography.bodySmall, color = Mint.Muted)
            }
            PressableSurface(
                onClick = {
                    if (confirming) {
                        confirming = false
                        onReset()
                    } else {
                        confirming = true
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = if (confirming) Mint.Pink else Mint.Card,
                border = BorderStroke(1.dp, if (confirming) Mint.Pink else Mint.Border2),
            ) {
                AnimatedContent(
                    targetState = confirming,
                    transitionSpec = { (fadeIn(tween(150)) + slideInVertically { it / 2 }) togetherWith (fadeOut(tween(100)) + slideOutVertically { -it / 2 }) },
                    label = "resetLabel",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                ) { c ->
                    Text(
                        if (c) "Confirm" else "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (c) Mint.Black else Mint.PinkSoft,
                    )
                }
            }
        }
    }
}

/** Pick how many seconds after the window closes a pending signal is auto-marked Missed (also how long WIN / LOSS stay). */
@Composable
private fun MissedDelayPicker(seconds: Int, onChange: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val presets = listOf(5, 10, 15, 30)

    @Composable
    fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
        PressableSurface(
            onClick = { if (enabled) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() } },
            shape = CircleShape,
            color = Mint.Card,
            border = BorderStroke(1.dp, Mint.Border2),
            modifier = Modifier.size(44.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled) Mint.Text else Mint.Muted.copy(alpha = 0.4f),
                )
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, Mint.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Missed delay (seconds after close)", style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StepButton("−", seconds > MARK_SECONDS_MIN) { onChange(seconds - 1) }
                Text("$seconds s", style = MaterialTheme.typography.headlineMedium, color = Mint.Lime)
                StepButton("+", seconds < MARK_SECONDS_MAX) { onChange(seconds + 1) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { v ->
                    val on = v == seconds
                    val bg by animateColorAsState(if (on) Mint.Lime else Mint.Card, tween(250), label = "delayBg")
                    val fg by animateColorAsState(if (on) Mint.Black else Mint.Muted, tween(250), label = "delayFg")
                    PressableSurface(
                        onClick = { onChange(v) },
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = BorderStroke(1.dp, if (on) Mint.Lime else Mint.Border2),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text("${v}s", style = MaterialTheme.typography.labelLarge, color = fg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePicker(mode: String, onChange: (String) -> Unit) {
    val options = listOf(
        AppSettings.THEME_SYSTEM to "Auto",
        AppSettings.THEME_LIGHT to "Day",
        AppSettings.THEME_DARK to "Night",
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, Mint.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Theme", style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (value, label) ->
                    val on = value == mode
                    val bg by animateColorAsState(if (on) Mint.Lime else Mint.Card, tween(250), label = "themeBg")
                    val fg by animateColorAsState(if (on) Mint.Black else Mint.Muted, tween(250), label = "themeFg")
                    PressableSurface(
                        onClick = { onChange(value) },
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = BorderStroke(1.dp, if (on) Mint.Lime else Mint.Border2),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                            Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
                        }
                    }
                }
            }
        }
    }
}
