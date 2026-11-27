package com.aviator.hoc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val app = application as AviatorApp

        // Android 13+: ask for notification permission (once on first launch, and again when the user turns it on)
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val askPermission = {
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

        setContent {
            AviatorTheme {
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
                AppRoot(
                    viewModel = vm,
                    generateViewModel = gen,
                    resultsViewModel = results,
                    settingsViewModel = settingsVm,
                    onRequestNotificationPermission = askPermission,
                )
            }
        }
    }
}
