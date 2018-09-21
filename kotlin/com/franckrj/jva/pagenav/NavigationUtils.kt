package com.franckrj.jva.pagenav

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.franckrj.jva.R

object NavigationUtils {
    fun initContentViewPagerNavigation(inThisActivity: AppCompatActivity, contentViewPager: ViewPager, contentNavigation: PageNavigationHelper, contentViewModel: NavigableViewModel) {
        contentViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //rien
            }

            override fun onPageSelected(position: Int) {
                contentViewModel.setCurrentPageNumber(position + 1)
            }

            override fun onPageScrollStateChanged(state: Int) {
                //rien
            }
        })

        contentViewModel.getLastPageNumber().observe(inThisActivity, Observer { newLastPageNumber ->
            if (newLastPageNumber != null) {
                contentNavigation.setNumberOfPages(newLastPageNumber)
            }
        })

        contentViewModel.getCurrentPageNumber().observe(inThisActivity, Observer { newCurrentPageNumber ->
            if (newCurrentPageNumber != null && (newCurrentPageNumber - 1) != contentNavigation.getCurrentItemIndex()) {
                contentNavigation.setCurrentItemIndex(newCurrentPageNumber - 1)
            }
        })
    }

    fun initPageNavHeaderAdapterNavigation(inThisFrag: Fragment, pageNavHeaderAdapter: PageNavigationHeaderAdapter, contentViewModel: NavigableViewModel, contentPageViewModel: NavigablePageViewModel) {
        contentPageViewModel.getCurrentPageNumber().observe(inThisFrag, Observer { newCurrentPageNumber ->
            if (newCurrentPageNumber != null) {
                pageNavHeaderAdapter.currentPageNumber = newCurrentPageNumber
                pageNavHeaderAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
            }
        })

        contentViewModel.getLastPageNumber().observe(inThisFrag, Observer { newLastPageNumber ->
            if (newLastPageNumber != null) {
                pageNavHeaderAdapter.lastPageNumber = newLastPageNumber
                pageNavHeaderAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
            }
        })

        pageNavHeaderAdapter.pageNavigationButtonClickedListener = { idOfButton ->
            when (idOfButton) {
                R.id.firstpage_button_header_row -> contentViewModel.setCurrentPageNumber(1)
                R.id.previouspage_button_header_row -> contentViewModel.setCurrentPageNumber((contentViewModel.getCurrentPageNumber().value ?: 2) - 1)
                R.id.nextpage_button_header_row -> contentViewModel.setCurrentPageNumber((contentViewModel.getCurrentPageNumber().value ?: 1) + 1)
                R.id.lastpage_button_header_row -> contentViewModel.setCurrentPageNumber(contentViewModel.getLastPageNumber().value ?: 1)
            }
        }
    }
}
