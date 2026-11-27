package com.aviator.hoc.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.aviator.hoc.domain.AlertEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Beeps (synthesised sine tones, same pitches as the web app) and vibration.
 * Everything is best-effort: any failure is swallowed so an alert can never crash the app.
 */
class SignalAlerts(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }.getOrNull()

    fun play(event: AlertEvent, sound: Boolean, haptics: Boolean) {
        scope.launch {
            when (event) {
                // window opened: one beep, one buzz
                AlertEvent.WINDOW_OPEN -> {
                    if (haptics) vibrate(longArrayOf(0, 90))
                    if (sound) beep(880, 160)
                }
                // WIN / LOSS appeared: two lower beeps, double buzz
                AlertEvent.MARK_OPEN -> {
                    if (haptics) vibrate(longArrayOf(0, 60, 80, 60))
                    if (sound) {
                        beep(520, 130)
                        delay(190)
                        beep(520, 130)
                    }
                }
            }
        }
    }

    /** Short beep shown when the user switches sound on in Settings. */
    fun preview() {
        scope.launch { beep(880, 160) }
    }

    private fun vibrate(pattern: LongArray) {
        runCatching {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            v.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    private suspend fun beep(freqHz: Int, durationMs: Int) {
        runCatching {
            val rate = 22_050
            val n = rate * durationMs / 1000
            val fade = (rate * 0.012).toInt().coerceAtLeast(1) // 12 ms in/out avoids clicks
            val pcm = ShortArray(n) { i ->
                val env = minOf(1f, i / fade.toFloat(), (n - 1 - i) / fade.toFloat())
                (sin(2.0 * PI * freqHz * i / rate) * 0.35 * env * Short.MAX_VALUE).toInt().toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * 2)
                .build()
            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                delay(durationMs + 40L)
            } finally {
                track.release()
            }
        }
    }
}
