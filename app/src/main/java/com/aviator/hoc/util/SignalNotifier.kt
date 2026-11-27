package com.aviator.hoc.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aviator.hoc.MainActivity
import com.aviator.hoc.R
import com.aviator.hoc.core.hmsOfEpoch
import com.aviator.hoc.core.windowTimes
import com.aviator.hoc.data.db.SignalEntity
import java.util.Locale

object SignalNotifier {
    private const val CHANNEL = "signal_alerts_v1"

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL) != null) return
        val ch = NotificationChannel(CHANNEL, "Signal alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "A sound alert about a minute before a signal"
            enableVibration(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
        nm.createNotificationChannel(ch)
    }

    fun show(ctx: Context, s: SignalEntity) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        ensureChannel(ctx)
        val signal = hmsOfEpoch(s.targetMs)
        val times = windowTimes(signal)
        val odd = String.format(Locale.ROOT, "%.2fx", s.odd)

        val open = PendingIntent.getActivity(
            ctx,
            s.id.toInt(),
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_bolt)
            .setContentTitle("Signal in 1 min · $odd")
            .setContentText("Window opens ${times.open} · signal $signal")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Window opens ${times.open}\nSignal $signal\nWindow closes ${times.close}"),
            )
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(0xFFB6FF5C.toInt())
            .setAutoCancel(true)
            .setTimeoutAfter(3 * 60_000L)
            .setContentIntent(open)
            .build()

        runCatching { NotificationManagerCompat.from(ctx).notify(s.id.toInt(), n) }
    }
}
