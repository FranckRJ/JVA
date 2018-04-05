package com.franckrj.jva

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        System.setProperty("http.keepAlive", "true")
        ImageGetterService.init(this, R.drawable.ic_image_deleted)
    }
}
