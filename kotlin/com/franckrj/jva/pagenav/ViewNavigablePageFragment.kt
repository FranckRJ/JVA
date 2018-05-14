package com.franckrj.jva.pagenav

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.DimenRes
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import com.franckrj.jva.R
import com.franckrj.jva.utils.MovableToolbar
import com.franckrj.jva.utils.SmoothScrollbarRecyclerView

abstract class ViewNavigablePageFragment : Fragment() {
    companion object {
        const val ARG_PAGE_NUMBER: String = "ARG_PAGE_NUMBER"
        const val ARG_IS_ACTIVE: String = "ARG_IS_ACTIVE"

        private const val SAVE_IS_ACTIVE: String = "SAVE_IS_ACTIVE"
    }

    protected lateinit var contentListView: SmoothScrollbarRecyclerView
    protected lateinit var contentListRefreshLayout: SwipeRefreshLayout
    protected lateinit var contentPageViewModel: NavigablePageViewModel
    protected var isActive: Boolean = false

    fun initListViewAndRefreshLayout(newContentListView: SmoothScrollbarRecyclerView, newContentListRefreshLayout: SwipeRefreshLayout,
                                     @DimenRes listViewPaddingResId: Int, @DimenRes listItemSpacingResId: Int) {
        val idOfNavBarHeight: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarHeight: Int = if (idOfNavBarHeight > 0) resources.getDimensionPixelSize(idOfNavBarHeight) else 0
        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val toolbarHeight: Int = resources.getDimensionPixelSize(R.dimen.toolbarHeight)
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        val defaultListViewPadding: Int = resources.getDimensionPixelSize(listViewPaddingResId)
        val listItemSpacing: Int = resources.getDimensionPixelSize(listItemSpacingResId)
        val refreshSpinnerTopMargin: Int = resources.getDimensionPixelSize(R.dimen.refreshSpinnerTopMargin)
        val realToolbarHeight: Int = toolbarHeight + (defaultToolbarMargin * 2)

        var navBarIsInApp = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= 24) {
            if (requireActivity().isInMultiWindowMode) {
                navBarIsInApp = false
            }
        }

        contentListView = newContentListView
        contentListRefreshLayout = newContentListRefreshLayout

        contentListRefreshLayout.isEnabled = false
        contentListRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        contentListRefreshLayout.setProgressViewOffset(false, statusBarHeight + defaultToolbarMargin, refreshSpinnerTopMargin + realToolbarHeight + statusBarHeight)
        contentListView.setPaddingRelative(defaultListViewPadding,
                                           realToolbarHeight + statusBarHeight,
                                           defaultListViewPadding,
                                           defaultListViewPadding - listItemSpacing + if (navBarIsInApp) navBarHeight else 0)
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        createActivityDependentObjectsAndViewModels()

        val currentActivity: Activity = requireActivity()
        if (currentActivity is MovableToolbar) {
            contentListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    currentActivity.toolbarMoved(contentListView.isScrolledAtTop())
                }
            })
        }

        contentPageViewModel.init(arguments?.getInt(ARG_PAGE_NUMBER) ?: 1)

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(SAVE_IS_ACTIVE)) {
                setIsActiveFragment(true)
            }
        } else {
            if (arguments?.getBoolean(ARG_IS_ACTIVE) == true) {
                setIsActiveFragment(true)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVE_IS_ACTIVE, isActive)
    }

    @CallSuper
    open fun setIsActiveFragment(newIsActive: Boolean) {
        isActive = newIsActive
    }

    protected abstract fun createActivityDependentObjectsAndViewModels()
    abstract fun clearContent()
}
