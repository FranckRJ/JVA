package com.franckrj.jva.utils

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.support.text.emoji.EmojiCompat
import android.support.annotation.ColorInt

object Utils {
    fun colorToString(@ColorInt colorValue: Int): String {
        return String.format("#%06X", 0xFFFFFF and colorValue)
    }

    fun applyEmojiCompatIfPossible(baseMessage: CharSequence): CharSequence {
        return if (EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            EmojiCompat.get().process(baseMessage)
        } else {
            baseMessage
        }
    }

    fun getStatusbarHeight(fromThisActivity: Activity): Int {
        val idOfStatusBarHeight: Int = fromThisActivity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (idOfStatusBarHeight > 0) fromThisActivity.resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
    }

    fun getNavbarHeight(fromThisActivity: Activity): Int {
        val idOfNavBarHeight: Int = fromThisActivity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (idOfNavBarHeight > 0) fromThisActivity.resources.getDimensionPixelSize(idOfNavBarHeight) else 0
    }

    fun getNavbarIsInApp(fromThisActivity: Activity): Boolean {
        var navBarIsInApp: Boolean = fromThisActivity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= 24) {
            if (fromThisActivity.isInMultiWindowMode) {
                navBarIsInApp = false
            }
        }

        return navBarIsInApp
    }
}
