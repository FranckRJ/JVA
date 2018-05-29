package com.franckrj.jva.pagenav

import android.app.Activity
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.DimenRes
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import com.franckrj.jva.R
import com.franckrj.jva.utils.MovableToolbar
import com.franckrj.jva.utils.SmoothScrollbarRecyclerView
import com.franckrj.jva.utils.Utils

abstract class ViewNavigablePageFragment : Fragment() {
    companion object {
        const val ARG_PAGE_NUMBER: String = "ARG_PAGE_NUMBER"
        const val ARG_IS_ACTIVE: String = "ARG_IS_ACTIVE"

        private const val SAVE_IS_ACTIVE: String = "SAVE_IS_ACTIVE"
    }

    protected lateinit var contentListView: SmoothScrollbarRecyclerView
    protected lateinit var contentListRefreshLayout: SwipeRefreshLayout
    protected lateinit var contentListAdapter: PageNavigationHeaderAdapter
    protected lateinit var contentPageViewModel: NavigablePageViewModel
    protected var isActive: Boolean = false

    fun initListViewAndRefreshLayout(newContentListView: SmoothScrollbarRecyclerView, newContentListRefreshLayout: SwipeRefreshLayout,
                                     @DimenRes listViewPaddingResId: Int, @DimenRes listItemSpacingResId: Int) {
        val statusBarHeight: Int = Utils.getStatusbarHeight(requireActivity())
        val navBarHeight: Int = Utils.getNavbarHeight(requireActivity())
        val toolbarHeight: Int = resources.getDimensionPixelSize(R.dimen.toolbarHeight)
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        val defaultListViewPadding: Int = resources.getDimensionPixelSize(listViewPaddingResId)
        val listItemSpacing: Int = resources.getDimensionPixelSize(listItemSpacingResId)
        val refreshSpinnerTopMargin: Int = resources.getDimensionPixelSize(R.dimen.refreshSpinnerTopMargin)
        val realToolbarHeight: Int = toolbarHeight + (defaultToolbarMargin * 2)
        val navBarIsInApp: Boolean = Utils.getNavbarIsInApp(requireActivity())

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

        if (isActive) {
            contentListAdapter.showAllPageInfos = true
        } else {
            contentListAdapter.showAllPageInfos = false
            contentPageViewModel.clearInfosForContentPage()
            contentPageViewModel.cancelGetContentPageInfos()
        }

        contentListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
    }

    fun clearContent() {
        contentPageViewModel.clearListOfContentShowable()
    }

    protected abstract fun createActivityDependentObjectsAndViewModels()
}
