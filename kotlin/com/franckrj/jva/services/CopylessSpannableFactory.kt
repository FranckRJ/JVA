package com.franckrj.jva.services

import android.text.Spannable

/**
 * SpannableFactory ayant pour but de créer des Spannable sans copier leur contenu dans un soucis d'optimisation.
 */
class CopylessSpannableFactory private constructor() : Spannable.Factory() {
    companion object {
        val instance: CopylessSpannableFactory by lazy { CopylessSpannableFactory() }
    }

    override fun newSpannable(source: CharSequence?): Spannable {
        return source as? Spannable ?: super.newSpannable(source)
    }
}
