package com.aviator.hoc.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviator.hoc.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateViewModel(context: Context) : ViewModel() {
    private val updater = Updater(context.applicationContext, BuildConfig.UPDATE_REPO)
    private val prefs = context.applicationContext.getSharedPreferences("updates", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val _dialog = MutableStateFlow(false)
    /** the update popup is open */
    val dialogVisible: StateFlow<Boolean> = _dialog.asStateFlow()

    val currentVersion: String = BuildConfig.VERSION_NAME
    private var downloaded: File? = null
    private var job: Job? = null

    init {
        // check every time the app starts; a newer build pops the update dialog up
        check(silent = true)
    }

    fun showDialog() { _dialog.value = true }

    fun dismissDialog() {
        _dialog.value = false
        if (_state.value is UpdateState.Error) _state.value = UpdateState.Idle
    }

    fun cancelDownload() {
        val s = _state.value as? UpdateState.Downloading ?: return
        job?.cancel()
        _state.value = UpdateState.Available(s.info)
    }

    fun check(silent: Boolean = false) {
        if (_state.value is UpdateState.Checking || _state.value is UpdateState.Downloading) return
        job = viewModelScope.launch {
            if (!silent) _state.value = UpdateState.Checking
            try {
                val latest = updater.fetchLatest()
                prefs.edit().putLong(KEY_LAST, System.currentTimeMillis()).apply()
                if (latest != null && isNewer(latest.versionCode, BuildConfig.VERSION_CODE)) _dialog.value = true
                _state.value = when {
                    latest != null && isNewer(latest.versionCode, BuildConfig.VERSION_CODE) -> UpdateState.Available(latest)
                    silent -> UpdateState.Idle
                    else -> UpdateState.UpToDate
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = if (silent) UpdateState.Idle else UpdateState.Error(friendly(e))
            }
        }
    }

    /** Download (if needed) and open the installer. */
    fun install() {
        val info = when (val s = _state.value) {
            is UpdateState.Available -> s.info
            is UpdateState.Ready -> s.info
            is UpdateState.NeedsPermission -> s.info
            else -> return
        }
        _dialog.value = true
        if (!updater.canInstall()) {
            _state.value = UpdateState.NeedsPermission(info)
            updater.openInstallPermissionSettings()
            return
        }
        val file = downloaded?.takeIf { it.exists() && it.name == "update-${info.versionCode}.apk" }
        if (file != null) {
            updater.install(file)
            _state.value = UpdateState.Ready(info)
            return
        }
        job = viewModelScope.launch {
            _state.value = UpdateState.Downloading(info, 0f)
            try {
                val f = updater.download(info) { p -> _state.value = UpdateState.Downloading(info, p) }
                downloaded = f
                _state.value = UpdateState.Ready(info)
                updater.install(f)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UpdateState.Error(friendly(e))
            }
        }
    }

    private fun friendly(e: Exception): String = when (e) {
        is java.net.UnknownHostException, is java.net.SocketTimeoutException -> "No internet connection"
        else -> e.message ?: "Update failed"
    }

    private companion object {
        const val KEY_LAST = "lastCheck"
    }
}
