package com.franckrj.jva

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        System.setProperty("http.keepAlive", "true")
        WebService.init()
    }
}
