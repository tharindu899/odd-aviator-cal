package com.aviator.hoc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviator.hoc.data.SettingsStore
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.db.SignalStatus
import com.aviator.hoc.domain.HomeUiState
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

    init {
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
