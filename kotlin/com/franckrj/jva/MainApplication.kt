package com.franckrj.jva

import android.app.Application
import com.franckrj.jva.services.ImageGetterService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ImageGetterService.init(this, R.drawable.ic_image_deleted)
    }
}
