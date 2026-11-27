package com.aviator.hoc.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aviator.hoc.ui.components.BottomNav
import com.aviator.hoc.ui.components.LocalPlayEntrance
import com.aviator.hoc.ui.components.Tab
import com.aviator.hoc.ui.generate.GenerateScreen
import com.aviator.hoc.ui.generate.GenerateViewModel
import com.aviator.hoc.ui.home.HomeScreen
import com.aviator.hoc.ui.home.HomeViewModel
import com.aviator.hoc.ui.home.MarkStrip
import com.aviator.hoc.ui.results.ResultsScreen
import com.aviator.hoc.ui.results.ResultsViewModel
import com.aviator.hoc.ui.settings.SettingsSheet
import com.aviator.hoc.ui.settings.SettingsViewModel
import com.aviator.hoc.update.UpdateState
import com.aviator.hoc.update.UpdateViewModel

@Composable
fun AppRoot(
    viewModel: HomeViewModel,
    generateViewModel: GenerateViewModel,
    resultsViewModel: ResultsViewModel,
    settingsViewModel: SettingsViewModel,
    updateViewModel: UpdateViewModel,
    onRequestNotificationPermission: () -> Unit = {},
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val saved by settingsViewModel.saved.collectAsStateWithLifecycle()
    val update by updateViewModel.state.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    // bit mask of tabs already shown once: they skip the entrance animation next time
    var visited by rememberSaveable { mutableStateOf(0) }
    val tab = Tab.entries[tabIndex]
    LaunchedEffect(tab) { visited = visited or (1 shl tab.ordinal) }

    // room at the bottom of every screen for nav (70dp) + strip (~72dp) + margins
    val bottomSpace = 170.dp

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Crossfade(targetState = tab, animationSpec = tween(160), label = "tab") { t ->
                val firstVisit = remember { (visited and (1 shl t.ordinal)) == 0 }
                CompositionLocalProvider(LocalPlayEntrance provides firstVisit) {
                    when (t) {
                        Tab.Home -> HomeRoute(
                            viewModel = viewModel,
                            userName = settings.userName,
                            showCountdown = settings.showCountdown,
                            onGenerate = { tabIndex = Tab.Generate.ordinal },
                            onSettings = { showSettings = true },
                            updateAvailable = update is UpdateState.Available || update is UpdateState.Ready,
                            bottomPadding = bottomSpace,
                        )
                        Tab.Generate -> GenerateRoute(generateViewModel, bottomSpace)
                        Tab.Results -> ResultsRoute(resultsViewModel, bottomSpace)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MarkOverlay(viewModel, settings.haptics)
                BottomNav(selected = tab, onSelect = { tabIndex = it.ordinal })
            }

            if (showSettings) {
                SettingsSheet(
                    settings = settings,
                    saved = saved,
                    onDismiss = { showSettings = false },
                    onAutoMark = settingsViewModel::setAutoMark,
                    onCountdown = settingsViewModel::setCountdown,
                    onTheme = settingsViewModel::setTheme,
                    onNotify = { on ->
                        settingsViewModel.setNotify(on)
                        if (on) onRequestNotificationPermission()
                    },
                    onSound = settingsViewModel::setSound,
                    onHaptics = settingsViewModel::setHaptics,
                    onName = settingsViewModel::setName,
                    onReset = settingsViewModel::reset,
                    update = update,
                    appVersion = updateViewModel.currentVersion,
                    onCheckUpdate = { updateViewModel.check() },
                    onInstallUpdate = updateViewModel::install,
                )
            }
        }
    }
}

// Each route collects its own state, so the once-per-second Home ticker only recomposes Home
// (and the WIN/LOSS overlay) instead of the whole app – this is what keeps tab switches smooth.

@Composable
private fun HomeRoute(
    viewModel: HomeViewModel,
    userName: String,
    showCountdown: Boolean,
    onGenerate: () -> Unit,
    onSettings: () -> Unit,
    updateAvailable: Boolean,
    bottomPadding: Dp,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        userName = userName,
        showCountdown = showCountdown,
        onGenerate = onGenerate,
        onSettings = onSettings,
        updateAvailable = updateAvailable,
        bottomPadding = bottomPadding,
    )
}

@Composable
private fun GenerateRoute(vm: GenerateViewModel, bottomPadding: Dp) {
    val state by vm.state.collectAsStateWithLifecycle()
    GenerateScreen(
        state = state,
        onInput = vm::onInput,
        onPasted = vm::onPasted,
        onClipboardEmpty = vm::onClipboardEmpty,
        onSubmit = vm::submit,
        bottomPadding = bottomPadding,
    )
}

@Composable
private fun ResultsRoute(vm: ResultsViewModel, bottomPadding: Dp) {
    val state by vm.state.collectAsStateWithLifecycle()
    ResultsScreen(vm, state, bottomPadding)
}

@Composable
private fun MarkOverlay(viewModel: HomeViewModel, haptics: Boolean) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MarkStrip(
        mark = state.mark,
        onWin = { viewModel.markWin(it) },
        onLoss = { viewModel.markLoss(it) },
        haptics = haptics,
    )
}
