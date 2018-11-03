package com.franckrj.jva.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView spécial ayant une Scrollbar qui change de taille de manière douce.
 */
class SmoothScrollbarRecyclerView : RecyclerView {
    /* L'initialisation à 0 fonctionne car les fonctions sont toujours appelées dans l'ordre range > offset > extent,
     * donc les valeurs ne sont jamais utilisées avant d'être réellement initialisées. */
    private var lastAverageSizeOfOneItemComputed: Double = 0.0
    private var lastRangeComputed: Int = 0
    private var lastOffsetComputed: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * Permet de savoir si la Scrollbar du RecyclerView est défilé au maximum vers le haut.
     *
     * @return      Vrai si la Scrollbar du RecyclerView est tout en haut, faux sinon.
     */
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

    /**
     * Permet de savoir si la Scrollbar doit être affichée (s'il est possible de scroller, que le contenu
     * du RecyclerView est plus grand que la zone affichable).
     *
     * @return      Vrai si la Scrollbar doit être affichée, faux sinon.
     */
    private fun scrollbarNeedToBeShown(linearLm: LinearLayoutManager): Boolean {
        val curAdapter: Adapter<RecyclerView.ViewHolder>? = adapter
        val firstItemPosition: Int = linearLm.findFirstVisibleItemPosition()

        return if (curAdapter == null) {
            false
        } else {
            (firstItemPosition != NO_POSITION && (linearLm.findFirstCompletelyVisibleItemPosition() > 0 || linearLm.findLastCompletelyVisibleItemPosition() < (curAdapter.itemCount - 1)))
        }
    }

    /**
     * Permet de connaitre la taille moyennes des Item affichés à l'écran. Le 1er et dernier Item affichés
     * ne comptent que comme un pourcentage de zone occupée à l'écran.
     * IE: Si le 1er Item n'occupe que 10% de l'écran, son coefficient dans la moyenne sera de 0.1.
     *
     * @return      La taille moyenne d'un Item.
     */
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

    /**
     * Retourne une fraction correspondant au pourcentage de l'Item visible à l'écran.
     *
     * @return      La fraction de l'Item visible à l'écran.
     */
    private fun getFractionOfItemVisible(item: View?): Double {
        return if (item == null) {
            0.0
        } else {
            val tmpFractionOfItemVisible: Double = (minOf(getRecyclerViewInsideBottom(), getViewOutsideBottom(item)) - maxOf(getRecyclerViewInsideTop(), getViewOutsideTop(item))) / getViewOutsideHeight(item).toDouble()
            tmpFractionOfItemVisible.coerceIn(0.0, 1.0)
        }
    }

    /**
     * Retourne une fraction correspondant au pourcentage de la partie supérieur de l'Item non visible à l'écran.
     *
     * @return      La fraction de la partie supérieur de l'Item non visible à l'écran.
     */
    private fun getFractionOfItemTopNotVisible(item: View?): Double {
        return if (item == null) {
            0.0
        } else {
            val tmpFractionOfItemTopNotVisible: Double = maxOf(getRecyclerViewInsideTop() - getViewOutsideTop(item), 0) / getViewOutsideHeight(item).toDouble()
            tmpFractionOfItemTopNotVisible.coerceIn(0.0, 1.0)
        }
    }

    /**
     * Retourne une fraction correspondant au pourcentage de la partie inférieur de l'Item non visible à l'écran.
     *
     * @return      La fraction de la partie inférieur de l'Item non visible à l'écran.
     */
    private fun getFractionOfItemBottomNotVisible(item: View?): Double {
        return if (item == null) {
            0.0
        } else {
            val tmpFractionOfItemBottomNotVisible: Double = maxOf(getViewOutsideBottom(item) - getRecyclerViewInsideBottom(), 0) / getViewOutsideHeight(item).toDouble()
            tmpFractionOfItemBottomNotVisible.coerceIn(0.0, 1.0)
        }
    }

    /**
     * Retourne la position en Y du point le plus haut de l'Item en comptant la marge.
     *
     * @return      La position en Y du point le plus haut de l'Item en comptant la marge.
     */
    private fun getViewOutsideTop(view: View?): Int {
        return if (view == null) {
            0
        } else {
            val layoutParam = view.layoutParams as RecyclerView.LayoutParams
            (view.top - layoutParam.topMargin)
        }
    }

    /**
     * Retourne la position en Y du point le plus bas de l'Item en comptant la marge.
     *
     * @return      La position en Y du point le plus bas de l'Item en comptant la marge.
     */
    private fun getViewOutsideBottom(view: View): Int {
        val layoutParam = view.layoutParams as RecyclerView.LayoutParams
        return view.bottom + layoutParam.bottomMargin
    }

    /**
     * Retourne la hauteur de l'Item en comptant les marges.
     *
     * @return      La hauteur de l'Item en comptant les marges.
     */
    private fun getViewOutsideHeight(view: View?): Int {
        return if (view == null) {
            0
        } else {
            (getViewOutsideBottom(view) - getViewOutsideTop(view))
        }
    }

    /**
     * Retourne la position en Y du point le plus haut du RecyclerView sans compter le padding.
     *
     * @return      La position en Y du point le plus haut du RecyclerView sans compter le padding.
     */
    private fun getRecyclerViewInsideTop(): Int {
        return paddingTop
    }

    /**
     * Retourne la position en Y du point le plus bas du RecyclerView sans compter le padding.
     *
     * @return      La position en Y du point le plus bas du RecyclerView sans compter le padding.
     */
    private fun getRecyclerViewInsideBottom(): Int {
        return height - paddingBottom
    }
}
