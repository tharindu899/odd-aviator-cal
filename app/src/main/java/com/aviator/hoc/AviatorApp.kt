package com.aviator.hoc

import android.app.Application
import com.aviator.hoc.data.SettingsStore
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.db.AppDatabase
import com.aviator.hoc.domain.planAlarms
import com.aviator.hoc.util.AlarmScheduler
import com.aviator.hoc.util.SignalAlerts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Tiny manual DI container – enough for this app, no Hilt needed. */
class AviatorApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: SignalRepository
        private set
    lateinit var settings: SettingsStore
        private set

    lateinit var alerts: SignalAlerts
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.create(this)
        repository = SignalRepository(database.signalDao())
        settings = SettingsStore(this)
        alerts = SignalAlerts(this)

        // keep the "1 minute before" alarms in sync with the DB and the setting.
        // Also runs after a reboot, because the boot receiver starts this process.
        val scheduler = AlarmScheduler(this)
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            combine(repository.signals, settings.settings.map { it.notify }.distinctUntilChanged()) { list, on ->
                if (on) planAlarms(list, System.currentTimeMillis()) else emptyList()
            }.collect { scheduler.sync(it) }
        }
    }
}
