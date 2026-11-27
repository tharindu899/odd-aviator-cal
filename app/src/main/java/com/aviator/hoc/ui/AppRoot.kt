package com.aviator.hoc.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aviator.hoc.ui.components.BottomNav
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

@Composable
fun AppRoot(
    viewModel: HomeViewModel,
    generateViewModel: GenerateViewModel,
    resultsViewModel: ResultsViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val saved by settingsViewModel.saved.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val genState by generateViewModel.state.collectAsStateWithLifecycle()
    val resultsState by resultsViewModel.state.collectAsStateWithLifecycle()
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    val tab = Tab.entries[tabIndex]

    // room at the bottom of every screen for nav (70dp) + strip (~72dp) + margins
    val bottomSpace = 170.dp

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Crossfade(targetState = tab, animationSpec = tween(250), label = "tab") { t ->
                when (t) {
                    Tab.Home -> HomeScreen(
                        state = state,
                        userName = settings.userName,
                        showCountdown = settings.showCountdown,
                        onGenerate = { tabIndex = Tab.Generate.ordinal },
                        onSettings = { showSettings = true },
                        bottomPadding = bottomSpace,
                    )
                    Tab.Generate -> GenerateScreen(
                        state = genState,
                        onInput = generateViewModel::onInput,
                        onPasted = generateViewModel::onPasted,
                        onClipboardEmpty = generateViewModel::onClipboardEmpty,
                        onSubmit = generateViewModel::submit,
                        bottomPadding = bottomSpace,
                    )
                    Tab.Results -> ResultsScreen(resultsViewModel, resultsState, bottomSpace)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MarkStrip(
                    mark = state.mark,
                    onWin = { viewModel.markWin(it) },
                    onLoss = { viewModel.markLoss(it) },
                    haptics = settings.haptics,
                )
                BottomNav(selected = tab, onSelect = { tabIndex = it.ordinal })
            }

            if (showSettings) {
                SettingsSheet(
                    settings = settings,
                    saved = saved,
                    onDismiss = { showSettings = false },
                    onAutoMark = settingsViewModel::setAutoMark,
                    onCountdown = settingsViewModel::setCountdown,
                    onSound = settingsViewModel::setSound,
                    onHaptics = settingsViewModel::setHaptics,
                    onName = settingsViewModel::setName,
                    onReset = settingsViewModel::reset,
                )
            }
        }
    }
}
