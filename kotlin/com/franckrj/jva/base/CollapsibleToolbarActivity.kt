package com.franckrj.jva.base

import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import com.franckrj.jva.R
import com.franckrj.jva.utils.MovableToolbar

abstract class CollapsibleToolbarActivity : AppCompatActivity(), MovableToolbar {
    private lateinit var toolbarCard: CardView
    private lateinit var titleTextToolbar: TextView
    private lateinit var subtitleTextToolbar: TextView
    private var defaultToolbarCardElevation: Float = 0f
    private var aboveToolbarCardElevation: Float = 0f

    protected fun initToolbar(navigableViewPager: ViewPager, appbarLayout: AppBarLayout, toolbarLayout: FrameLayout,
                              newToolbarCard: CardView, newTitleTextToolbar: TextView, newSubtitleTextToolbar: TextView) {
        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        defaultToolbarCardElevation = resources.getDimensionPixelSize(R.dimen.defaultToolbarCardElevation).toFloat()
        aboveToolbarCardElevation = resources.getDimensionPixelSize(R.dimen.aboveToolbarCardElevation).toFloat()

        toolbarCard = newToolbarCard
        titleTextToolbar = newTitleTextToolbar
        subtitleTextToolbar = newSubtitleTextToolbar

        toolbarLayout.setPaddingRelative(defaultToolbarMargin, defaultToolbarMargin + statusBarHeight, defaultToolbarMargin, defaultToolbarMargin)
        toolbarCard.translationZ = defaultToolbarCardElevation
        navigableViewPager.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                navigableViewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)
                navigableViewPager.translationY = -(appbarLayout.height.toFloat())
                navigableViewPager.layoutParams.height = navigableViewPager.height + appbarLayout.height
            }
        })
    }

    protected fun setTitle(newTitle: String) {
        titleTextToolbar.text = newTitle
    }

    protected fun setSubTitle(newSubtitle: String) {
        subtitleTextToolbar.text = newSubtitle

        if (newSubtitle.isEmpty()) {
            subtitleTextToolbar.visibility = View.GONE
        } else {
            subtitleTextToolbar.visibility = View.VISIBLE
        }
    }

    override fun toolbarMoved(toolbarIsOnTop: Boolean) {
        if (toolbarCard.translationZ == aboveToolbarCardElevation && toolbarIsOnTop) {
            toolbarCard.animate().translationZ(defaultToolbarCardElevation).setDuration(200).start()
        } else if (toolbarCard.translationZ == defaultToolbarCardElevation && !toolbarIsOnTop) {
            toolbarCard.translationZ = aboveToolbarCardElevation
        }
    }
}
