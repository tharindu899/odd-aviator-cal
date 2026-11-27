package com.aviator.hoc.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aviator.hoc.domain.PlannedAlarm

/** Schedules one AlarmManager alarm per planned notification and cancels the stale ones. */
class AlarmScheduler(context: Context) {
    private val ctx = context.applicationContext
    private val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = ctx.getSharedPreferences("alarms", Context.MODE_PRIVATE)

    @Synchronized
    fun sync(plan: List<PlannedAlarm>) {
        val wanted = plan.map { it.id }.toSet()
        val old = prefs.getStringSet(KEY, emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }

        old.filter { it !in wanted }.forEach { cancel(it) }
        plan.forEach { schedule(it) }

        prefs.edit().putStringSet(KEY, wanted.map { it.toString() }.toSet()).apply()
    }

    private fun pending(id: Long, targetMs: Long, flags: Int): PendingIntent? {
        val i = Intent(ctx, SignalAlarmReceiver::class.java)
            .putExtra(SignalAlarmReceiver.EXTRA_ID, id)
            .putExtra(SignalAlarmReceiver.EXTRA_TARGET, targetMs)
        return PendingIntent.getBroadcast(ctx, id.toInt(), i, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun schedule(a: PlannedAlarm) {
        val pi = pending(a.id, a.targetMs, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        try {
            if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, a.triggerMs, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, a.triggerMs, pi)
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, a.triggerMs, pi)
        }
    }

    private fun cancel(id: Long) {
        pending(id, 0L, PendingIntent.FLAG_NO_CREATE)?.let {
            am.cancel(it)
            it.cancel()
        }
    }

    private companion object {
        const val KEY = "ids"
    }
}
