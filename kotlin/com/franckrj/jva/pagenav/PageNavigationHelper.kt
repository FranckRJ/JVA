package com.franckrj.jva.pagenav

import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

class PageNavigationHelper(private val navigationViewPager: ViewPager,
                           fragmentBuilder: () -> ViewNavigablePageFragment,
                           fragManager: FragmentManager) {
    private val navigationViewPagerAdapter = NavigationViewPagerAdapter(fragmentBuilder, fragManager)

    private val pageChangeOnPagerListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            //rien
        }

        override fun onPageSelected(position: Int) {
            navigationViewPagerAdapter.getFragment(position)?.setIsActiveFragment(true)
            callFunOnNearbyFrag(position) { setIsActiveFragment(false) }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                callFunOnNearbyFrag(getCurrentItemIndex()) { clearContent() }
            }
        }
    }

    init {
        navigationViewPager.adapter = navigationViewPagerAdapter
        navigationViewPager.addOnPageChangeListener(pageChangeOnPagerListener)
    }

    private fun callFunOnNearbyFrag(position: Int, funToCall: ViewNavigablePageFragment.() -> Unit) {
        if (position > 0) {
            navigationViewPagerAdapter.getFragment(position - 1)?.funToCall()
        }
        if (position < navigationViewPagerAdapter.numberOfPages - 1) {
            navigationViewPagerAdapter.getFragment(position + 1)?.funToCall()
        }
    }

    fun getCurrentItemIndex() : Int = navigationViewPager.currentItem

    fun setCurrentItemIndex(position: Int) {
        val newPosition = position.coerceIn(0, (navigationViewPagerAdapter.numberOfPages - 1))

        if (newPosition == getCurrentItemIndex()) {
            navigationViewPagerAdapter.getFragment(newPosition)?.setIsActiveFragment(true)
        } else {
            navigationViewPager.currentItem = newPosition
        }
    }

    fun setNumberOfPages(newNumberOfPages: Int) {
        navigationViewPagerAdapter.numberOfPages = newNumberOfPages
        navigationViewPagerAdapter.notifyDataSetChanged()
    }

    private inner class NavigationViewPagerAdapter(private val fragmentBuilder: () -> ViewNavigablePageFragment,
                                                   fragManager: FragmentManager) : FragmentStatePagerAdapter(fragManager) {
        private var referenceMap = SparseArray<ViewNavigablePageFragment>()
        var numberOfPages: Int = 1
            set(newNumberOfPages) {
                field = if (newNumberOfPages < 1) {
                    1
                } else {
                    newNumberOfPages
                }
            }

        fun getFragment(key: Int): ViewNavigablePageFragment? {
            return referenceMap.get(key)
        }

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            (item as ViewNavigablePageFragment).setIsActiveFragment(false)
            referenceMap.remove(position)
            super.destroyItem(container, position, item)
        }

        override fun getItem(position: Int): Fragment {
            val argForFrag = Bundle()
            val newViewNavigablePageFrag = fragmentBuilder()

            argForFrag.putInt(ViewNavigablePageFragment.ARG_PAGE_NUMBER, position + 1)
            argForFrag.putBoolean(ViewNavigablePageFragment.ARG_IS_ACTIVE, (position == getCurrentItemIndex()))
            newViewNavigablePageFrag.arguments = argForFrag

            return newViewNavigablePageFrag
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as ViewNavigablePageFragment
            referenceMap.put(position, fragment)
            return fragment
        }

        override fun getCount(): Int {
            return numberOfPages
        }
    }
}
