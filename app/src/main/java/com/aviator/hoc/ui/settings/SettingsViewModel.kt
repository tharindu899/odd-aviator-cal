package com.aviator.hoc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviator.hoc.data.AppSettings
import com.aviator.hoc.data.SettingsStore
import com.aviator.hoc.util.SignalAlerts
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val store: SettingsStore,
    private val alerts: SignalAlerts,
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _saved = MutableStateFlow(false)
    /** true for ~1.6 s after any change – drives the "Saved" flash */
    val saved: StateFlow<Boolean> = _saved.asStateFlow()
    private var savedJob: Job? = null

    private fun flash() {
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            _saved.value = true
            delay(1600)
            _saved.value = false
        }
    }

    private fun change(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            store.update(transform)
            flash()
        }
    }

    fun setAutoMark(on: Boolean) = change { it.copy(autoMarkMissed = on) }
    fun setCountdown(on: Boolean) = change { it.copy(showCountdown = on) }
    fun setHaptics(on: Boolean) = change { it.copy(haptics = on) }
    fun setSound(on: Boolean) {
        change { it.copy(sound = on) }
        if (on) alerts.preview()
    }
    fun setName(name: String) = change { it.copy(userName = name) }
    fun reset() {
        viewModelScope.launch {
            store.reset()
            flash()
        }
    }
}
