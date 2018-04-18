package com.franckrj.jva.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.text.Layout
import android.text.style.LineBackgroundSpan
import android.text.style.LeadingMarginSpan

class BetterQuoteSpan(private val settingsForQuote: BetterQuoteSettings) : LeadingMarginSpan, LineBackgroundSpan {

    override fun getLeadingMargin(first: Boolean): Int {
        return (settingsForQuote.stripeWidth + settingsForQuote.gap)
    }

    override fun drawLeadingMargin(thisCanvas: Canvas, thisPaint: Paint, pos: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int, first: Boolean, thisLayout: Layout) {
        val oldPaintStyle: Paint.Style = thisPaint.style
        val oldPaintColor: Int = thisPaint.color

        thisPaint.style = Paint.Style.FILL
        thisPaint.color = settingsForQuote.stripeColor

        thisCanvas.drawRect(pos.toFloat(), top.toFloat(), (pos + dir * settingsForQuote.stripeWidth).toFloat(), bottom.toFloat(), thisPaint)

        thisPaint.style = oldPaintStyle
        thisPaint.color = oldPaintColor
    }

    override fun drawBackground(thisCanvas: Canvas, thisPaint: Paint, left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
                                text: CharSequence, start: Int, end: Int, lnum: Int) {
        val oldPaintColor: Int = thisPaint.color

        thisPaint.color = settingsForQuote.backgroundColor

        thisCanvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), thisPaint)

        thisPaint.color = oldPaintColor
    }

    class BetterQuoteSettings(@ColorInt val backgroundColor: Int,
                              @ColorInt val stripeColor: Int,
                              val stripeWidth: Int,
                              val gap: Int)
}
