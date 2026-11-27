package com.aviator.hoc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.graphics.Color
import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aviator.hoc.data.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aviator.hoc.ui.AppRoot
import com.aviator.hoc.ui.generate.GenerateViewModel
import com.aviator.hoc.ui.home.HomeViewModel
import com.aviator.hoc.ui.results.ResultsViewModel
import com.aviator.hoc.ui.settings.SettingsViewModel
import com.aviator.hoc.ui.theme.AviatorTheme
import com.aviator.hoc.update.UpdateViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AviatorApp

        // Android 13+: ask for notification permission (once on first launch, and again when the user turns it on)
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val askPermission: () -> Unit = {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val prefs = getSharedPreferences("onboarding", MODE_PRIVATE)
        if (!prefs.getBoolean("asked_notifications", false)) {
            prefs.edit().putBoolean("asked_notifications", true).apply()
            askPermission()
        }

        // read once up front so the very first frame already has the saved theme (no flash)
        val initial = runBlocking { app.settings.settings.first() }

        setContent {
            val cfg by app.settings.settings.collectAsStateWithLifecycle(initial)
            val systemDark = isSystemInDarkTheme()
            val dark = when (cfg.themeMode) {
                AppSettings.THEME_LIGHT -> false
                AppSettings.THEME_DARK -> true
                else -> systemDark
            }
            DisposableEffect(dark) {
                this@MainActivity.enableEdgeToEdge(
                    statusBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                    navigationBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                )
                onDispose { }
            }
            AviatorTheme(dark = dark) {
                val vm: HomeViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { HomeViewModel(app.repository, app.settings, app.alerts) }
                    },
                )
                val gen: GenerateViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { GenerateViewModel(app.repository) }
                    },
                )
                val results: ResultsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { ResultsViewModel(app.repository) }
                    },
                )
                val settingsVm: SettingsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { SettingsViewModel(app.settings, app.alerts) }
                    },
                )
                val updateVm: UpdateViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { UpdateViewModel(app) }
                    },
                )
                AppRoot(
                    viewModel = vm,
                    generateViewModel = gen,
                    resultsViewModel = results,
                    settingsViewModel = settingsVm,
                    updateViewModel = updateVm,
                    onRequestNotificationPermission = askPermission,
                )
            }
        }
    }
}
