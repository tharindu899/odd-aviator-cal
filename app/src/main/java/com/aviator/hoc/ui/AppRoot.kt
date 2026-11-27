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
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.ui.components.BottomNav
import com.aviator.hoc.ui.components.Tab
import com.aviator.hoc.ui.home.HomeScreen
import com.aviator.hoc.ui.home.HomeViewModel
import com.aviator.hoc.ui.home.MarkStrip
import com.aviator.hoc.ui.placeholder.ComingSoonScreen
import com.aviator.hoc.ui.theme.Mint

@Composable
fun AppRoot(viewModel: HomeViewModel, repository: SignalRepository) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                        userName = "Lalshan",
                        onGenerate = { tabIndex = Tab.Generate.ordinal },
                        onSettings = { /* Part 5 */ },
                        bottomPadding = bottomSpace,
                    )
                    Tab.Generate -> ComingSoonScreen("Generate", part = 3, repository = repository)
                    Tab.Results -> ComingSoonScreen("Results", part = 4)
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
                )
                BottomNav(selected = tab, onSelect = { tabIndex = it.ordinal })
            }
        }
    }
}
