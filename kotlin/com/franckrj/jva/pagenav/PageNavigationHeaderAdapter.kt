package com.franckrj.jva.pagenav

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.franckrj.jva.R

abstract class PageNavigationHeaderAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        @JvmStatic protected val TYPE_HEADER: Int = 0
        @JvmStatic protected val NUMBER_OF_HEADERS: Int = 1

        const val HEADER_POSITION: Int = 0
    }

    private val waitingText: String = context.getString(R.string.waitingText)
    var currentPageNumber: Int = -1
    var lastPageNumber: Int = -1
    var showAllPageInfos: Boolean = false
    var showLastPageButton: Boolean = true
    var pageNavigationButtonClickedListener: OnPageNavigationButtonClickedListener? = null

    protected val internalPageNavigationButtonClickedListener = View.OnClickListener { view ->
        pageNavigationButtonClickedListener?.onPageNavigationButtonClicked(view.id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            return HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_navigation_header, parent, false))
        } else {
            throw RuntimeException("Type non supporté.")
        }
    }

    override fun getItemViewType(position: Int): Int  = TYPE_HEADER

    override fun getItemCount(): Int = NUMBER_OF_HEADERS

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bindView(currentPageNumber, lastPageNumber, showAllPageInfos, showLastPageButton)
        }
    }

    protected inner class HeaderViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val firstPageButton: Button = mainView.findViewById(R.id.firstpage_button_header_row)
        private val previousPageButton: Button = mainView.findViewById(R.id.previouspage_button_header_row)
        private val currentPageButton: Button = mainView.findViewById(R.id.currentpage_button_header_row)
        private val nextPageButton: Button = mainView.findViewById(R.id.nextpage_button_header_row)
        private val lastPageButton: Button = mainView.findViewById(R.id.lastpage_button_header_row)

        init {
            firstPageButton.setOnClickListener(internalPageNavigationButtonClickedListener)
            previousPageButton.setOnClickListener(internalPageNavigationButtonClickedListener)
            currentPageButton.setOnClickListener(internalPageNavigationButtonClickedListener)
            nextPageButton.setOnClickListener(internalPageNavigationButtonClickedListener)
            lastPageButton.setOnClickListener(internalPageNavigationButtonClickedListener)
        }

        fun bindView(currentPageNumber: Int, lastPageNumber: Int, showAllPageInfos: Boolean, showLastPageButton: Boolean) {
            if (currentPageNumber >= 0) {
                currentPageButton.text = currentPageNumber.toString()

                if (showAllPageInfos) {
                    if (currentPageNumber > 1) {
                        firstPageButton.visibility = View.VISIBLE
                        previousPageButton.visibility = View.VISIBLE
                    } else {
                        firstPageButton.visibility = View.INVISIBLE
                        previousPageButton.visibility = View.INVISIBLE
                    }

                    if (lastPageNumber > currentPageNumber) {
                        if (showLastPageButton) {
                            lastPageButton.text = lastPageNumber.toString()
                            lastPageButton.visibility = View.VISIBLE
                        } else {
                            lastPageButton.visibility = View.INVISIBLE
                        }

                        nextPageButton.visibility = View.VISIBLE
                    } else {
                        nextPageButton.visibility = View.INVISIBLE
                        lastPageButton.visibility = View.INVISIBLE
                    }
                } else {
                    firstPageButton.visibility = View.INVISIBLE
                    previousPageButton.visibility = View.INVISIBLE
                    nextPageButton.visibility = View.INVISIBLE
                    lastPageButton.visibility = View.INVISIBLE
                }
            } else {
                currentPageButton.text = waitingText
                firstPageButton.visibility = View.INVISIBLE
                previousPageButton.visibility = View.INVISIBLE
                nextPageButton.visibility = View.INVISIBLE
                lastPageButton.visibility = View.INVISIBLE
            }
        }
    }

    interface OnPageNavigationButtonClickedListener {
        fun onPageNavigationButtonClicked(idOfButton: Int)
    }
}
