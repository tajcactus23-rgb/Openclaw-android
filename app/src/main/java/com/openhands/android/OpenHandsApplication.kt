package com.openhands.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenHandsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}