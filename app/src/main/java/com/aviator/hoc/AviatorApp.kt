package com.aviator.hoc

import android.app.Application
import com.aviator.hoc.data.SettingsStore
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.db.AppDatabase
import com.aviator.hoc.util.SignalAlerts

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
    }
}
