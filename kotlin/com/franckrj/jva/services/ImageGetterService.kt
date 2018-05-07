package com.franckrj.jva.services

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.text.Html
import com.franckrj.jva.R
import com.franckrj.jva.utils.GlideApp
import com.franckrj.jva.utils.GlideRequests
import com.franckrj.jva.utils.WrapperTarget

class ImageGetterService(contextToUse: Context, @DrawableRes private val downloadDrawableId: Int, @DrawableRes private val deletedDrawableId: Int) : Html.ImageGetter, Drawable.Callback {
    private val resources: Resources = contextToUse.resources
    private val deletedDrawable: Drawable = contextToUse.getDrawable(deletedDrawableId)
    private val packageName: String = contextToUse.packageName
    private val stickerSize: Int = resources.getDimensionPixelSize(R.dimen.stickerSize)
    private val miniNoelshackWidth: Int = resources.getDimensionPixelSize(R.dimen.miniNoelshackWidth)
    private val miniNoelshackHeight: Int = resources.getDimensionPixelSize(R.dimen.miniNoelshackHeight)
    private val glide: GlideRequests = GlideApp.with(contextToUse)
    private val listOfTargetForDrawables: ArrayList<WrapperTarget> = ArrayList()

    var listenerForInvalidateTextViewNeeded: OnInvalidateTextViewNeededListener? = null

    init {
        deletedDrawable.setBounds(0, 0, deletedDrawable.intrinsicWidth, deletedDrawable.intrinsicHeight)
    }

    /* TODO: Faire en sorte que tous les chargements de drawable passent par Glide ? */
    override fun getDrawable(source: String): Drawable {
        if (!source.startsWith("http://") && !source.startsWith("https://")) {
            val resId: Int = resources.getIdentifier(source.substring(0, source.lastIndexOf(".")), "drawable", packageName)

            try {
                val drawable: Drawable = resources.getDrawable(resId, null)

                if (source.startsWith("sticker")) {
                    drawable.setBounds(0, 0, stickerSize, stickerSize)
                } else {
                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                }

                return drawable
            } catch (_: Exception) {
                //rien
            }
        } else if (source.startsWith("http://image.noelshack.com/minis/") || source.startsWith("https://image.noelshack.com/minis/")) {
            val newDrawableTarget = WrapperTarget(source, miniNoelshackWidth, miniNoelshackHeight)
            newDrawableTarget.wrapperDrawable.callback = this
            listOfTargetForDrawables.add(newDrawableTarget)
            return newDrawableTarget.wrapperDrawable
        }

        return deletedDrawable
    }

    fun downloadDrawables() {
        for (thisTarget in listOfTargetForDrawables) {
            if (!thisTarget.loadHasStarted) {
                glide.load(thisTarget.sourceForDrawable)
                     .placeholder(downloadDrawableId)
                     .error(deletedDrawableId)
                     .override(miniNoelshackWidth, miniNoelshackHeight)
                     .into(thisTarget)
                thisTarget.loadHasStarted = true
            }
        }
    }

    fun clearDrawables() {
        for (thisTarget in listOfTargetForDrawables) {
            glide.clear(thisTarget)
        }
        listOfTargetForDrawables.clear()
    }

    fun clearOnlyDownloadedDrawables() {
        for (thisTarget in listOfTargetForDrawables) {
            if (thisTarget.loadHasStarted) {
                glide.clear(thisTarget)
            }
        }
        listOfTargetForDrawables.removeAll { it.loadHasStarted }
    }

    override fun unscheduleDrawable(who: Drawable?, what: Runnable?) {
        //rien
    }

    override fun invalidateDrawable(who: Drawable?) {
        listenerForInvalidateTextViewNeeded?.onInvalidateTextViewNeeded()
    }

    override fun scheduleDrawable(who: Drawable?, what: Runnable?, `when`: Long) {
        //rien
    }

    interface OnInvalidateTextViewNeededListener {
        fun onInvalidateTextViewNeeded()
    }
}
