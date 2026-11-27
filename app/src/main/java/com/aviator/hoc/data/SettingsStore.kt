package com.aviator.hoc.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

data class AppSettings(
    /** pending signals become "missed" 5 s after the window closes */
    val autoMarkMissed: Boolean = true,
    /** live countdown timers on Home */
    val showCountdown: Boolean = true,
    /** beep when the window opens / closes */
    val sound: Boolean = false,
    /** vibrate when the window opens / closes */
    val haptics: Boolean = true,
    /** system notification ~1 min before a signal (works with the app closed) */
    val notify: Boolean = true,
    /** "system" | "light" | "dark" */
    val themeMode: String = THEME_SYSTEM,
    /** shown in "Hello, <name>" */
    val userName: String = DEFAULT_NAME,
) {
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val DEFAULT_NAME = "Lalshan"
        const val MAX_NAME = 24
    }
}

/** Trim, collapse whitespace, cap length; blank falls back to the default name. */
fun cleanUserName(raw: String): String {
    val t = raw.trim().replace(Regex("""\s+"""), " ").take(AppSettings.MAX_NAME).trim()
    return t.ifEmpty { AppSettings.DEFAULT_NAME }
}

class SettingsStore(private val context: Context) {
    private object Keys {
        val autoMark = booleanPreferencesKey("autoMarkMissed")
        val countdown = booleanPreferencesKey("showCountdown")
        val sound = booleanPreferencesKey("sound")
        val haptics = booleanPreferencesKey("haptics")
        val notify = booleanPreferencesKey("notify")
        val userName = stringPreferencesKey("userName")
        val theme = stringPreferencesKey("themeMode")
    }

    private fun Preferences.toSettings(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            autoMarkMissed = this[Keys.autoMark] ?: d.autoMarkMissed,
            showCountdown = this[Keys.countdown] ?: d.showCountdown,
            sound = this[Keys.sound] ?: d.sound,
            haptics = this[Keys.haptics] ?: d.haptics,
            notify = this[Keys.notify] ?: d.notify,
            userName = this[Keys.userName] ?: d.userName,
            themeMode = this[Keys.theme] ?: d.themeMode,
        )
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { p ->
            val next = transform(p.toSettings())
            p[Keys.autoMark] = next.autoMarkMissed
            p[Keys.countdown] = next.showCountdown
            p[Keys.sound] = next.sound
            p[Keys.haptics] = next.haptics
            p[Keys.notify] = next.notify
            p[Keys.theme] = next.themeMode
            p[Keys.userName] = cleanUserName(next.userName)
        }
    }

    suspend fun reset() = update { AppSettings() }
}
