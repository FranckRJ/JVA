package com.franckrj.jva.topic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.SparseArray
import android.view.ViewGroup

class TopicNavigationHelper(private val topicViewPager: ViewPager,
                            fragManager: FragmentManager) {
    private val topicViewPagerAdapter = TopicViewPagerAdapter(fragManager)

    private val pageChangeOnPagerListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            //rien
        }

        override fun onPageSelected(position: Int) {
            topicViewPagerAdapter.getFragment(position)?.setIsActiveFragment(true)
            callFunOnNearbyFrag(position, { setIsActiveFragment(false) })
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                callFunOnNearbyFrag(getCurrentItemIndex(), { clearMessages() })
            }
        }
    }

    init {
        topicViewPager.adapter = topicViewPagerAdapter
        topicViewPager.addOnPageChangeListener(pageChangeOnPagerListener)
    }

    private fun callFunOnNearbyFrag(position: Int, funToCall: ViewTopicPageFragment.() -> Any) {
        if (position > 0) {
            topicViewPagerAdapter.getFragment(position - 1)?.funToCall()
        }
        if (position < topicViewPagerAdapter.numberOfPages - 1) {
            topicViewPagerAdapter.getFragment(position + 1)?.funToCall()
        }
    }

    fun getCurrentItemIndex() : Int = topicViewPager.currentItem

    fun setCurrentItemIndex(position: Int) {
        val newPosition = (if (position < 0) 0 else position)

        if (newPosition == getCurrentItemIndex()) {
            topicViewPagerAdapter.getFragment(newPosition)?.setIsActiveFragment(true)
        } else {
            topicViewPager.currentItem = newPosition
        }
    }

    fun setNumberOfPages(newNumberOfPages: Int) {
        topicViewPagerAdapter.numberOfPages = newNumberOfPages
        topicViewPagerAdapter.notifyDataSetChanged()
    }

    inner class TopicViewPagerAdapter(fragManager: FragmentManager) : FragmentStatePagerAdapter(fragManager) {
        private var referenceMap = SparseArray<ViewTopicPageFragment>()
        var numberOfPages: Int = 1
            set(newNumberOfPages) {
                field = if (newNumberOfPages < 1) {
                    1
                } else {
                    newNumberOfPages
                }
            }

        fun getFragment(key: Int): ViewTopicPageFragment? {
            return referenceMap.get(key)
        }

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            (item as ViewTopicPageFragment).setIsActiveFragment(false)
            referenceMap.remove(position)
            super.destroyItem(container, position, item)
        }

        override fun getItem(position: Int): Fragment {
            val argForFrag = Bundle()
            val newViewTopicPageFrag = ViewTopicPageFragment()

            if (position == getCurrentItemIndex()) {
                argForFrag.putBoolean(ViewTopicPageFragment.ARG_IS_ACTIVE_FRAG, true)
            }

            argForFrag.putInt(ViewTopicPageFragment.ARG_PAGE_NUMBER, position + 1)
            newViewTopicPageFrag.arguments = argForFrag

            return newViewTopicPageFrag
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as ViewTopicPageFragment
            referenceMap.put(position, fragment)
            return fragment
        }

        override fun getCount(): Int {
            return numberOfPages
        }
    }
}
