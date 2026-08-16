package com.pulsepoint.app

import android.app.Application
import com.pulsepoint.app.core.di.AppContainer

class PulsePointApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
