package com.aviator.hoc

import android.os.Bundle
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
import com.aviator.hoc.ui.theme.AviatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val app = application as AviatorApp

        setContent {
            AviatorTheme {
                val vm: HomeViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { HomeViewModel(app.repository, app.settings) }
                    },
                )
                val gen: GenerateViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { GenerateViewModel(app.repository) }
                    },
                )
                AppRoot(viewModel = vm, generateViewModel = gen)
            }
        }
    }
}
