package com.aviator.hoc.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

data class AppSettings(
    /** pending signals become "missed" 5 s after the window closes */
    val autoMarkMissed: Boolean = true,
    /** live countdown chips */
    val showCountdown: Boolean = true,
    /** beep when the window opens / closes */
    val sound: Boolean = false,
    /** vibrate when the window opens */
    val haptics: Boolean = true,
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val autoMark = booleanPreferencesKey("autoMarkMissed")
        val countdown = booleanPreferencesKey("showCountdown")
        val sound = booleanPreferencesKey("sound")
        val haptics = booleanPreferencesKey("haptics")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        val d = AppSettings()
        AppSettings(
            autoMarkMissed = p[Keys.autoMark] ?: d.autoMarkMissed,
            showCountdown = p[Keys.countdown] ?: d.showCountdown,
            sound = p[Keys.sound] ?: d.sound,
            haptics = p[Keys.haptics] ?: d.haptics,
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { p ->
            val d = AppSettings()
            val cur = AppSettings(
                p[Keys.autoMark] ?: d.autoMarkMissed,
                p[Keys.countdown] ?: d.showCountdown,
                p[Keys.sound] ?: d.sound,
                p[Keys.haptics] ?: d.haptics,
            )
            val next = transform(cur)
            p[Keys.autoMark] = next.autoMarkMissed
            p[Keys.countdown] = next.showCountdown
            p[Keys.sound] = next.sound
            p[Keys.haptics] = next.haptics
        }
    }

    suspend fun reset() = update { AppSettings() }
}
