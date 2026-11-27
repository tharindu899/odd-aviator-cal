package com.aviator.hoc.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aviator.hoc.AviatorApp
import com.aviator.hoc.data.db.SignalStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Fires ~1 minute before a signal and posts the notification (if the signal is still pending). */
class SignalAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id < 0) return
        val app = context.applicationContext as AviatorApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = app.database.signalDao().getById(id)
                if (s != null && s.status == SignalStatus.PENDING && app.settings.settings.first().notify) {
                    SignalNotifier.show(app, s)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "signal_id"
        const val EXTRA_TARGET = "target_ms"
    }
}

/** Receiving the boot broadcast starts the app process, which re-schedules all alarms. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
