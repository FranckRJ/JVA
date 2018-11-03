package com.franckrj.jva.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SmoothScrollbarRecyclerView : RecyclerView {
    /* Fonctionne car les fonctions sont toujours appelées dans l'ordre range > offset > extent. */
    private var lastAverageSizeOfOneItemComputed: Double = 0.0
    private var lastRangeComputed: Int = 0
    private var lastOffsetComputed: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun isScrolledAtTop(): Boolean {
        val linearLm: LinearLayoutManager? = layoutManager as? LinearLayoutManager

        return if (linearLm != null) {
            val firstItemPos = linearLm.findFirstVisibleItemPosition()

            when (firstItemPos) {
                NO_POSITION -> true
                0 -> (getViewOutsideTop(linearLm.findViewByPosition(firstItemPos)) == getRecyclerViewInsideTop())
                else -> false
            }
        } else {
            false
        }
    }

    override fun computeVerticalScrollRange(): Int {
        val linearLm: LinearLayoutManager? = layoutManager as? LinearLayoutManager

        return if (linearLm != null) {
            val curAdapter: Adapter<RecyclerView.ViewHolder>? = adapter

            if (curAdapter != null && scrollbarNeedToBeShown(linearLm)) {
                lastAverageSizeOfOneItemComputed = computeAverageSizeOfOneItem(linearLm)
                (lastAverageSizeOfOneItemComputed * curAdapter.itemCount).toInt().also { lastRangeComputed = it }
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
                val firstItem: View? = linearLm.findViewByPosition(firstItemPosition)

                ((firstItemPosition + getFractionOfItemTopNotVisible(firstItem)) * lastAverageSizeOfOneItemComputed).toInt().also { lastOffsetComputed = it }
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
            val curAdapter: Adapter<RecyclerView.ViewHolder>? = adapter

            if (curAdapter != null && scrollbarNeedToBeShown(linearLm)) {
                val lastItemPosition: Int = linearLm.findLastVisibleItemPosition()
                val lastItem: View? = linearLm.findViewByPosition(lastItemPosition)

                lastRangeComputed - lastOffsetComputed - (((curAdapter.itemCount - 1 - lastItemPosition) + getFractionOfItemBottomNotVisible(lastItem)) * lastAverageSizeOfOneItemComputed).toInt()
            } else {
                0
            }
        } else {
            super.computeVerticalScrollExtent()
        }
    }

    private fun scrollbarNeedToBeShown(linearLm: LinearLayoutManager): Boolean {
        val curAdapter: Adapter<RecyclerView.ViewHolder>? = adapter
        val firstItemPosition: Int = linearLm.findFirstVisibleItemPosition()

        return if (curAdapter == null) {
            false
        } else {
            (firstItemPosition != NO_POSITION && (linearLm.findFirstCompletelyVisibleItemPosition() > 0 || linearLm.findLastCompletelyVisibleItemPosition() < (curAdapter.itemCount - 1)))
        }
    }

    private fun computeAverageSizeOfOneItem(linearLm: LinearLayoutManager): Double {
        var sizeOfAllVisiblesItems = 0
        var numberOfItemsComputed: Double
        val firstItemPosition: Int = linearLm.findFirstVisibleItemPosition()
        val firstItem: View? = linearLm.findViewByPosition(firstItemPosition)
        val lastItemPosition: Int = linearLm.findLastVisibleItemPosition()

        if (firstItemPosition != lastItemPosition) {
            val lastItem: View? = linearLm.findViewByPosition(lastItemPosition)

            sizeOfAllVisiblesItems += getViewOutsideHeight(firstItem) + getViewOutsideHeight(lastItem)
            numberOfItemsComputed = getFractionOfItemVisible(firstItem) + getFractionOfItemVisible(lastItem)

            for (posIndex: Int in (firstItemPosition + 1)..(lastItemPosition - 1)) {
                sizeOfAllVisiblesItems += getViewOutsideHeight(linearLm.findViewByPosition(posIndex))
                numberOfItemsComputed += 1.0
            }
        } else {
            /* Un seul objet est visible, donc on suppose que tous les objets ont sa taille. */
            sizeOfAllVisiblesItems = getViewOutsideHeight(firstItem)
            numberOfItemsComputed = 1.0
        }

        return (sizeOfAllVisiblesItems / numberOfItemsComputed)
    }

    private fun getFractionOfItemVisible(item: View?): Double {
        return if (item == null) {
            0.0
        } else {
            val tmpFractionOfItemVisible: Double = (minOf(getRecyclerViewInsideBottom(), getViewOutsideBottom(item)) - maxOf(getRecyclerViewInsideTop(), getViewOutsideTop(item))) / getViewOutsideHeight(item).toDouble()
            tmpFractionOfItemVisible.coerceIn(0.0, 1.0)
        }
    }

    private fun getFractionOfItemTopNotVisible(item: View?): Double {
        return if (item == null) {
            0.0
        } else {
            val tmpFractionOfItemTopNotVisible: Double = maxOf(getRecyclerViewInsideTop() - getViewOutsideTop(item), 0) / getViewOutsideHeight(item).toDouble()
            tmpFractionOfItemTopNotVisible.coerceIn(0.0, 1.0)
        }
    }

    private fun getFractionOfItemBottomNotVisible(item: View?): Double {
        return if (item == null) {
            0.0
        } else {
            val tmpFractionOfItemBottomNotVisible: Double = maxOf(getViewOutsideBottom(item) - getRecyclerViewInsideBottom(), 0) / getViewOutsideHeight(item).toDouble()
            tmpFractionOfItemBottomNotVisible.coerceIn(0.0, 1.0)
        }
    }

    private fun getViewOutsideTop(view: View?): Int {
        return if (view == null) {
            0
        } else {
            val layoutParam = view.layoutParams as RecyclerView.LayoutParams
            (view.top - layoutParam.topMargin)
        }
    }

    private fun getViewOutsideBottom(view: View): Int {
        val layoutParam = view.layoutParams as RecyclerView.LayoutParams
        return view.bottom + layoutParam.bottomMargin
    }

    private fun getViewOutsideHeight(view: View?): Int {
        return if (view == null) {
            0
        } else {
            (getViewOutsideBottom(view) - getViewOutsideTop(view))
        }
    }

    private fun getRecyclerViewInsideTop(): Int {
        return paddingTop
    }

    private fun getRecyclerViewInsideBottom(): Int {
        return height - paddingBottom
    }
}
