package com.franckrj.jva.base

import android.os.Build
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.franckrj.jva.R
import com.franckrj.jva.utils.MovableToolbar
import com.franckrj.jva.utils.UndeprecatorUtils
import com.franckrj.jva.utils.Utils

abstract class BaseActivity : AppCompatActivity(), MovableToolbar {
    private val statusbarHeight: Int by lazy { Utils.getStatusbarHeight(this) }
    private val navbarHeight: Int by lazy { Utils.getNavbarHeight(this) }

    private var toolbarCard: CardView? = null
    private var titleTextToolbar: TextView? = null
    private var subtitleTextToolbar: TextView? = null
    private var defaultToolbarCardElevation: Float = 0f
    private var aboveToolbarCardElevation: Float = 0f

    protected fun initSysbars(statusbarBackground: View, navbarBackground: View) {
        val navbarIsInApp: Boolean = Utils.getNavbarIsInApp(this)

        if (navbarIsInApp) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if (Build.VERSION.SDK_INT >= 26) {
            window.decorView.systemUiVisibility = ((if (!isInMultiWindowMode) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0) or
                                                   (if (navbarIsInApp) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0))
            navbarBackground.visibility = View.GONE

            if (!isInMultiWindowMode) {
                statusbarBackground.layoutParams.height = statusbarHeight
                statusbarBackground.setBackgroundColor(UndeprecatorUtils.getColor(this, R.color.sysBarLightColor))
            } else {
                statusbarBackground.visibility = View.GONE
            }
        } else {
            statusbarBackground.layoutParams.height = statusbarHeight
            statusbarBackground.setBackgroundColor(UndeprecatorUtils.getColor(this, R.color.sysBarDarkColor))

            if (navbarIsInApp) {
                navbarBackground.layoutParams.height = navbarHeight
                navbarBackground.setBackgroundColor(UndeprecatorUtils.getColor(this, R.color.sysBarDarkColor))
            } else {
                navbarBackground.visibility = View.GONE
            }
        }
    }

    protected fun initCollapsibleToolbar(scrollableContent: View, appbarLayout: AppBarLayout, toolbarLayout: FrameLayout,
                                         newToolbarCard: CardView, newTitleTextToolbar: TextView, newSubtitleTextToolbar: TextView? = null) {
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        defaultToolbarCardElevation = resources.getDimensionPixelSize(R.dimen.defaultToolbarCardElevation).toFloat()
        aboveToolbarCardElevation = resources.getDimensionPixelSize(R.dimen.aboveToolbarCardElevation).toFloat()

        toolbarCard = newToolbarCard
        titleTextToolbar = newTitleTextToolbar
        subtitleTextToolbar = newSubtitleTextToolbar

        toolbarLayout.setPaddingRelative(defaultToolbarMargin, defaultToolbarMargin + statusbarHeight, defaultToolbarMargin, defaultToolbarMargin)
        toolbarCard?.translationZ = defaultToolbarCardElevation
        scrollableContent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollableContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                scrollableContent.translationY = -(appbarLayout.height.toFloat())
                scrollableContent.layoutParams.height = scrollableContent.height + appbarLayout.height
                scrollableContent.requestLayout()
            }
        })
    }

    protected fun setToolbarTitle(newTitle: String) {
        titleTextToolbar?.text = newTitle
    }

    protected fun setToolbarSubTitle(newSubtitle: String) {
        subtitleTextToolbar?.text = newSubtitle

        if (newSubtitle.isEmpty()) {
            subtitleTextToolbar?.visibility = View.GONE
        } else {
            subtitleTextToolbar?.visibility = View.VISIBLE
        }
    }

    override fun toolbarMoved(toolbarIsOnTop: Boolean) {
        val currentToolbarCard: CardView? = toolbarCard
        if (currentToolbarCard != null) {
            if (currentToolbarCard.translationZ == aboveToolbarCardElevation && toolbarIsOnTop) {
                currentToolbarCard.animate().translationZ(defaultToolbarCardElevation).setDuration(200).start()
            } else if (currentToolbarCard.translationZ == defaultToolbarCardElevation && !toolbarIsOnTop) {
                currentToolbarCard.translationZ = aboveToolbarCardElevation
            }
        }
    }
}
