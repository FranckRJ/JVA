package com.franckrj.jva

import android.text.Spannable

class CopylessSpannableFactory private constructor() : Spannable.Factory() {
    companion object {
        val instance: CopylessSpannableFactory by lazy { CopylessSpannableFactory() }
    }

    override fun newSpannable(source: CharSequence?): Spannable {
        return source as Spannable
    }
}
