package com.franckrj.jva

import android.app.Application
import android.support.text.emoji.EmojiCompat
import android.support.text.emoji.FontRequestEmojiCompatConfig
import android.support.v4.provider.FontRequest
import com.franckrj.jva.services.ImageGetterService

class MainApplication : Application() {
    private fun initializeEmojiCompat() {
        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
        val config = FontRequestEmojiCompatConfig(this, fontRequest).setReplaceAll(true)
        EmojiCompat.init(config)
    }

    override fun onCreate() {
        super.onCreate()

        ImageGetterService.init(this, R.drawable.ic_image_deleted)
        initializeEmojiCompat()
    }
}
