package com.franckrj.jva.utils

import android.graphics.drawable.Drawable
import android.graphics.drawable.ColorDrawable
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.graphics.drawable.DrawableWrapper
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

/**
 * Target pour Glide servant de Wrapper entre différents Drawable.
 */
@SuppressLint("RestrictedApi")
class WrapperTarget(val sourceForDrawable: String,
                             private val width: Int,
                             private val height: Int) : Target<Drawable> {
    companion object {
        private val nullObject = ColorDrawable(Color.TRANSPARENT)
    }

    private var curRequest: Request? = null
    val wrapperDrawable = DrawableWrapper(null)
    var loadHasStarted: Boolean = false

    init {
        setDrawable(null)
        wrapperDrawable.setBounds(0, 0, width, height)
    }

    /**
     * Change le Drawable du Wrapper.
     *
     * @param   newDrawable     Nouveau Drawable utilisé par le Wrapper, si null le Drawable sera transparent.
     */
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

    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(width, height)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
        /* Rien à faire, le callback n'est pas retenu. */
    }

    override fun getRequest(): Request? {
        return curRequest
    }

    override fun setRequest(request: Request?) {
        curRequest = request
    }

    override fun onStart() {
        /* Rien à faire. */
    }

    override fun onStop() {
        /* Rien à faire. */
    }

    override fun onDestroy() {
        /* Rien à faire. */
    }
}
