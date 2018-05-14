package com.franckrj.jva.pagenav

import android.support.v4.app.Fragment

abstract class ViewNavigablePageFragment : Fragment() {
    companion object {
        const val ARG_PAGE_NUMBER: String = "ARG_PAGE_NUMBER"
    }

    abstract fun setIsActiveFragment(newIsActive: Boolean)
    abstract fun clearContent()
}
