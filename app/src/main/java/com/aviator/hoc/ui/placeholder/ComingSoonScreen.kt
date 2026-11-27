package com.aviator.hoc.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aviator.hoc.data.GenerateResult
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * Temporary screen for tabs that arrive in later parts.
 * [repository] != null adds an "Add sample signal" button so Home can be tested right now.
 */
@Composable
fun ComingSoonScreen(title: String, part: Int, repository: SignalRepository? = null) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Mint.Text)
        Text("Coming in Part $part", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)

        if (repository != null) {
            Spacer(Modifier.height(28.dp))
            PressableSurface(
                onClick = {
                    scope.launch {
                        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        // 2.02x at "now" lands 2 min 2 s from now -> visible on Home straight away
                        message = when (val r = repository.generate("2.02x $now")) {
                            is GenerateResult.Success -> "Sample signal saved. Check Home!"
                            is GenerateResult.Error -> r.message
                        }
                    }
                },
                shape = RoundedCornerShape(26.dp),
                color = Mint.Lime,
            ) {
                Text(
                    "Add sample signal",
                    style = MaterialTheme.typography.labelLarge,
                    color = Mint.Black,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 15.dp),
                )
            }
            if (message.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
            }
        }
    }
}
