package com.franckrj.jva

import android.os.Build
import android.text.Html
import android.text.Spanned

object UndeprecatorUtils {
    fun fromHtml(source: String): Spanned {
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(source)
        }
    }
}
