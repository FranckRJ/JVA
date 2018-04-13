package com.franckrj.jva.utils

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

class SmoothScrollbarRecyclerView : RecyclerView {
    /* Fonctionne car les fonctions sont toujours appelées dans l'ordre range > offset > extent. */
    private var lastAverageSizeOfOneItemComputed: Double = 0.0
    private var lastRangeComputed: Int = 0
    private var lastOffsetComputed: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun computeVerticalScrollRange(): Int {
        val linearLm: LinearLayoutManager? = layoutManager as? LinearLayoutManager

        return if (linearLm != null) {
            if (scrollbarNeedToBeShown(linearLm)) {
                lastAverageSizeOfOneItemComputed = computeAverageSizeOfOneItem(linearLm)
                lastRangeComputed = (lastAverageSizeOfOneItemComputed * adapter.itemCount).toInt()
                lastRangeComputed
            } else {
                0
            }
        } else {
            super.computeVerticalScrollRange()
        }
    }

    override fun computeVerticalScrollOffset(): Int {
        val linearLm: LinearLayoutManager? = layoutManager as? LinearLayoutManager

        return if (linearLm != null) {
            if (scrollbarNeedToBeShown(linearLm)) {
                val firstItemPosition: Int = linearLm.findFirstVisibleItemPosition()
                val firstItem: View = linearLm.findViewByPosition(firstItemPosition)

                lastOffsetComputed = ((lastAverageSizeOfOneItemComputed * firstItemPosition.toDouble()) + (getFractionOfItemTopNotVisible(firstItem) * lastAverageSizeOfOneItemComputed)).toInt()
                lastOffsetComputed
            } else {
                0
            }
        } else {
            super.computeVerticalScrollOffset()
        }
    }

    override fun computeVerticalScrollExtent(): Int {
        val linearLm: LinearLayoutManager? = layoutManager as? LinearLayoutManager

        return if (linearLm != null) {
            if (scrollbarNeedToBeShown(linearLm)) {
                val lastItemPosition: Int = linearLm.findLastVisibleItemPosition()
                val lastItem: View = linearLm.findViewByPosition(lastItemPosition)

                lastRangeComputed - lastOffsetComputed - ((lastAverageSizeOfOneItemComputed * (adapter.itemCount - 1 - lastItemPosition)) + (getFractionOfItemBottomNotVisible(lastItem) * lastAverageSizeOfOneItemComputed)).toInt()
            } else {
                0
            }
        } else {
            super.computeVerticalScrollExtent()
        }
    }

    private fun scrollbarNeedToBeShown(linearLm: LinearLayoutManager): Boolean {
        val firstItemPosition: Int = linearLm.findFirstVisibleItemPosition()

        return (firstItemPosition != NO_POSITION && (linearLm.findFirstCompletelyVisibleItemPosition() > 0 || linearLm.findLastCompletelyVisibleItemPosition() < (adapter.itemCount - 1)))
    }

    private fun computeAverageSizeOfOneItem(linearLm: LinearLayoutManager): Double {
        var proportionnalSizeOfVisiblesItems = 0
        var numberOfItemsComputed: Double
        val firstItemPosition: Int = linearLm.findFirstVisibleItemPosition()
        val firstItem: View = linearLm.findViewByPosition(firstItemPosition)
        val lastItemPosition: Int = linearLm.findLastVisibleItemPosition()

        if (firstItemPosition != lastItemPosition) {
            val lastItem: View = linearLm.findViewByPosition(lastItemPosition)
            val fractionOfScreenOccupedByFirstItem: Double = getFractionOfScreenOccupedByItem(firstItem)
            val fractionOfScreenOccupedByLastItem: Double = getFractionOfScreenOccupedByItem(lastItem)

            proportionnalSizeOfVisiblesItems += (getViewOutsideHeight(firstItem) * fractionOfScreenOccupedByFirstItem).toInt()
            proportionnalSizeOfVisiblesItems += (getViewOutsideHeight(lastItem) * fractionOfScreenOccupedByLastItem).toInt()
            numberOfItemsComputed = fractionOfScreenOccupedByFirstItem + fractionOfScreenOccupedByLastItem

            for (posIndex: Int in (firstItemPosition + 1)..(lastItemPosition - 1)) {
                proportionnalSizeOfVisiblesItems += getViewOutsideHeight(linearLm.findViewByPosition(posIndex))
                numberOfItemsComputed += 1.0
            }
        } else {
            /* Un seul objet est visible, donc on suppose que tous les objets ont sa taille. */
            proportionnalSizeOfVisiblesItems = getViewOutsideHeight(firstItem)
            numberOfItemsComputed = 1.0
        }

        return (proportionnalSizeOfVisiblesItems / numberOfItemsComputed)
    }

    private fun getFractionOfScreenOccupedByItem(item: View): Double {
        val tmpFractionOfScreenOccupedByItem: Double = (minOf(getRecyclerViewInsideBottom(), getViewOutsideBottom(item)) - maxOf(getRecyclerViewInsideTop(), getViewOutsideTop(item))) / getRecyclerViewInsideBottom().toDouble()
        return when {
            tmpFractionOfScreenOccupedByItem < 0 -> 0.0
            tmpFractionOfScreenOccupedByItem > 1 -> 1.0
            else -> tmpFractionOfScreenOccupedByItem
        }
    }

    private fun getFractionOfItemTopNotVisible(item: View): Double {
        val tmpFractionOfItemTopNotVisible: Double = maxOf(getRecyclerViewInsideTop() - getViewOutsideTop(item), 0) / getViewOutsideHeight(item).toDouble()
        return when {
            tmpFractionOfItemTopNotVisible < 0 -> 0.0
            tmpFractionOfItemTopNotVisible > 1 -> 1.0
            else -> tmpFractionOfItemTopNotVisible
        }
    }

    private fun getFractionOfItemBottomNotVisible(item: View): Double {
        val tmpFractionOfItemBottomNotVisible: Double = maxOf(getViewOutsideBottom(item) - getRecyclerViewInsideBottom(), 0) / getViewOutsideHeight(item).toDouble()
        return when {
            tmpFractionOfItemBottomNotVisible < 0 -> 0.0
            tmpFractionOfItemBottomNotVisible > 1 -> 1.0
            else -> tmpFractionOfItemBottomNotVisible
        }
    }

    private fun getViewOutsideTop(view: View): Int {
        val layoutParam = view.layoutParams as RecyclerView.LayoutParams
        return view.top - layoutParam.topMargin
    }

    private fun getViewOutsideBottom(view: View): Int {
        val layoutParam = view.layoutParams as RecyclerView.LayoutParams
        return view.bottom + layoutParam.bottomMargin
    }

    private fun getViewOutsideHeight(view: View): Int {
        return getViewOutsideBottom(view) - getViewOutsideTop(view)
    }

    private fun getRecyclerViewInsideTop(): Int {
        return paddingTop
    }

    private fun getRecyclerViewInsideBottom(): Int {
        return height - paddingBottom
    }
}