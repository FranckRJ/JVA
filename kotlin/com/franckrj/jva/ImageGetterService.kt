package com.franckrj.jva

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.text.Html

class ImageGetterService private constructor(private val resources: Resources,
                                             private val deletedDrawable: Drawable,
                                             private val packageName: String) : Html.ImageGetter {
    companion object {
        lateinit var instance: ImageGetterService
            private set

        fun init(contextToUse: Context, @DrawableRes deletedDrawableId: Int) {
            instance = ImageGetterService(contextToUse.resources, contextToUse.getDrawable(deletedDrawableId), contextToUse.packageName)
        }
    }

    init {
        deletedDrawable.setBounds(0, 0, deletedDrawable.intrinsicWidth, deletedDrawable.intrinsicHeight)
    }

    override fun getDrawable(source: String): Drawable {
        val resId: Int = resources.getIdentifier(source.substring(0, source.lastIndexOf(".")), "drawable", packageName)

        return try {
            val drawable: Drawable = resources.getDrawable(resId, null)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable
        } catch (e: Exception) {
            deletedDrawable
        }
    }
}
