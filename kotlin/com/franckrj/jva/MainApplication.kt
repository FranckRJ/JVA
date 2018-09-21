package com.franckrj.jva

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig

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

        initializeEmojiCompat()
    }
}
