package com.grupo4.smarthome

import android.app.Application
import com.grupo4.smarthome.core.di.AppContainer

class SmartHomeApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
