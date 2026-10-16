package com.automate.app

import android.app.Application
import com.automate.app.engine.Notifier

class AutoMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifier.createChannels(this)
    }
}
