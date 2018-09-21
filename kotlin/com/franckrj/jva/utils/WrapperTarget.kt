package com.franckrj.jva.utils

import android.graphics.drawable.Drawable
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.request.target.SimpleTarget
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.graphics.drawable.DrawableWrapper
import com.bumptech.glide.request.transition.Transition

@SuppressLint("RestrictedApi")
internal class WrapperTarget(val sourceForDrawable: String,
                             private val width: Int,
                             private val height: Int) : SimpleTarget<Drawable>(width, height) {
    companion object {
        private val nullObject = ColorDrawable(Color.TRANSPARENT)
    }

    val wrapperDrawable = DrawableWrapper(null)
    var loadHasStarted: Boolean = false

    init {
        setDrawable(null)
        wrapperDrawable.setBounds(0, 0, width, height)
    }

    private fun setDrawable(newDrawable: Drawable?) {
        var drawable = newDrawable
        if (drawable == null) {
            drawable = nullObject
        }
        drawable.setBounds(0, 0, width, height)
        wrapperDrawable.wrappedDrawable = drawable
        wrapperDrawable.invalidateSelf()
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        setDrawable(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        setDrawable(errorDrawable)
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        setDrawable(resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        setDrawable(placeholder)
    }
}
