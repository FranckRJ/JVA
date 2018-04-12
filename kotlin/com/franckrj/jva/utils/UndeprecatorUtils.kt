package com.franckrj.jva.utils

import android.os.Build
import android.text.Html
import android.text.Spanned

object UndeprecatorUtils {
    fun fromHtml(source: String, imageGetter: Html.ImageGetter? = null, tagHandler: Html.TagHandler? = null): Spanned {
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(source, imageGetter, tagHandler)
        }
    }
}
