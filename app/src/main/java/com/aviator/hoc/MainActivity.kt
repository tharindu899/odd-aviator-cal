package com.aviator.hoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.ui.PartOneScreen
import com.aviator.hoc.ui.theme.AviatorTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val repository = (application as AviatorApp).repository

        setContent {
            AviatorTheme {
                val statsFlow = remember { repository.signals.map { SignalRepository.statsOf(it) } }
                val stats by statsFlow.collectAsStateWithLifecycle(
                    initialValue = SignalRepository.statsOf(emptyList())
                )
                PartOneScreen(repository = repository, stats = stats)
            }
        }
    }
}
