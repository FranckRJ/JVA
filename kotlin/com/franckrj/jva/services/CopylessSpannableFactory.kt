package com.franckrj.jva.services

import android.text.Spannable

class CopylessSpannableFactory private constructor() : Spannable.Factory() {
    companion object {
        val instance: CopylessSpannableFactory by lazy { CopylessSpannableFactory() }
    }

    override fun newSpannable(source: CharSequence?): Spannable {
        return if (source is Spannable) {
            source
        } else {
            super.newSpannable(source)
        }
    }
}
