package com.aviator.hoc

import android.app.Application
import com.aviator.hoc.data.SettingsStore
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.db.AppDatabase

/** Tiny manual DI container – enough for this app, no Hilt needed. */
class AviatorApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: SignalRepository
        private set
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.create(this)
        repository = SignalRepository(database.signalDao())
        settings = SettingsStore(this)
    }
}
