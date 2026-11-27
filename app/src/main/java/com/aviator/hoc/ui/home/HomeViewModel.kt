package com.aviator.hoc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviator.hoc.data.SettingsStore
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.db.SignalStatus
import com.aviator.hoc.data.AppSettings
import com.aviator.hoc.domain.AlertTracker
import com.aviator.hoc.domain.HomeUiState
import com.aviator.hoc.util.SignalAlerts
import com.aviator.hoc.domain.buildHomeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SignalRepository,
    private val settings: SettingsStore,
    private val alerts: SignalAlerts,
) : ViewModel() {

    /** Emits the current time once per second, aligned to the second boundary. */
    private val ticker = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(now)
            delay(1000 - (now % 1000) + 5)
        }
    }

    val state: StateFlow<HomeUiState> =
        combine(repository.signals, ticker) { list, now -> buildHomeState(list, now) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Empty)

    /** Live settings for the UI (name, countdown toggle). */
    val appSettings: StateFlow<AppSettings> =
        settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    init {
        // beep / vibrate once when a window opens and when WIN/LOSS appears
        viewModelScope.launch {
            val tracker = AlertTracker()
            state.collect { st ->
                val events = tracker.onState(st)
                if (events.isEmpty()) return@collect
                val cfg = settings.settings.first()
                if (!cfg.sound && !cfg.haptics) return@collect
                events.forEach { alerts.play(it, cfg.sound, cfg.haptics) }
            }
        }
        // auto-mark: a pending signal becomes "missed" 5 s after its window closes
        viewModelScope.launch {
            while (true) {
                if (settings.settings.first().autoMarkMissed) repository.markExpiredAsMissed()
                delay(3_000)
            }
        }
    }

    fun markWin(id: Long) = viewModelScope.launch { repository.mark(id, SignalStatus.WIN) }
    fun markLoss(id: Long) = viewModelScope.launch { repository.mark(id, SignalStatus.LOSS) }
}
