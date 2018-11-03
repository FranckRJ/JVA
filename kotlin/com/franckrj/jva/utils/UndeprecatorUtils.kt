package com.franckrj.jva.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

/**
 * Namespace (en gros) contenant des fonctions appelant leur nouvelles ou anciennes version selon l'API de l'appareil.
 */
object UndeprecatorUtils {
    /**
     * Appelle [Html.fromHtml].
     */
    fun fromHtml(source: String, imageGetter: Html.ImageGetter? = null, tagHandler: Html.TagHandler? = null): Spanned {
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(source, imageGetter, tagHandler)
        }
    }

    /**
     * Appelle [Context.getColor].
     */
    @ColorInt
    fun getColor(context: Context, @ColorRes colorId: Int): Int {
        return if (Build.VERSION.SDK_INT >= 23) {
            context.getColor(colorId)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(colorId)
        }
    }
}
