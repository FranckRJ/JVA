package com.franckrj.jva.topic

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.franckrj.jva.services.CopylessSpannableFactory
import com.franckrj.jva.utils.GlideApp
import com.franckrj.jva.R

class TopicAdapter(private val context: Context,
                   private val sizeOfAvatars: Int,
                   sizeOfAvatarRoundedCorners: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER: Int = 0
        private const val TYPE_ITEM: Int = 1
        private const val NUMBER_OF_HEADERS: Int = 1

        const val HEADER_POSITION: Int = 0
    }

    private val spannableFactory: CopylessSpannableFactory = CopylessSpannableFactory.instance
    private val avatarRoundedCorners = RoundedCorners(sizeOfAvatarRoundedCorners)
    private val transitionOption = DrawableTransitionOptions.withCrossFade()
    private val waitingText: String = context.getString(R.string.waitingText)
    var currentPageNumber: Int = -1
    var lastPageNumber: Int = -1
    var listOfMessagesShowable: List<MessageInfosShowable> = ArrayList()
    var authorClickedListener: OnItemClickedListener? = null
    var dateClickedListener: OnItemClickedListener? = null

    private val internalAuthorClickedListener = View.OnClickListener { view ->
        authorClickedListener?.onItemClicked(view.tag as Int)
    }

    private val internalDateClickedListener = View.OnClickListener { view ->
        dateClickedListener?.onItemClicked(view.tag as Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_topic_header, parent, false))
            }
            TYPE_ITEM -> {
                MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_message_ll, parent, false))
            }
            else -> {
                throw RuntimeException("Type non supporté.")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < NUMBER_OF_HEADERS) TYPE_HEADER else TYPE_ITEM
    }

    override fun getItemCount(): Int = NUMBER_OF_HEADERS + listOfMessagesShowable.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bindView(currentPageNumber, lastPageNumber)
        } else if (holder is MessageViewHolder) {
            holder.bindView(listOfMessagesShowable[position - NUMBER_OF_HEADERS], position - NUMBER_OF_HEADERS)
        }
    }

    private inner class MessageViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val avatarImageView: ImageView = mainView.findViewById(R.id.avatar_image_message_row)
        private val authorTextView: TextView = mainView.findViewById(R.id.author_text_message_row)
        private val dateTextView: TextView = mainView.findViewById(R.id.date_text_message_row)
        private val contentTextView: TextView = mainView.findViewById(R.id.content_text_message_row)

        init {
            authorTextView.setOnClickListener(internalAuthorClickedListener)
            dateTextView.setOnClickListener(internalDateClickedListener)

            authorTextView.setSpannableFactory(spannableFactory)
            dateTextView.setSpannableFactory(spannableFactory)
            contentTextView.setSpannableFactory(spannableFactory)
        }

        fun bindView(message: MessageInfosShowable, position: Int) {
            GlideApp.with(context)
                    .load(message.avatarLink)
                    .override(sizeOfAvatars, sizeOfAvatars)
                    .transform(avatarRoundedCorners)
                    .transition(transitionOption)
                    .into(avatarImageView)

            authorTextView.setText(message.author, TextView.BufferType.SPANNABLE)
            dateTextView.setText(message.date, TextView.BufferType.SPANNABLE)
            contentTextView.setText(message.formatedContent, TextView.BufferType.SPANNABLE)

            authorTextView.tag = position
            dateTextView.tag = position
        }
    }

    private inner class HeaderViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val firstPageButton: TextView = mainView.findViewById(R.id.firstpage_button_header_row)
        private val previousPageButton: TextView = mainView.findViewById(R.id.previouspage_button_header_row)
        private val currentPageButton: TextView = mainView.findViewById(R.id.currentpage_button_header_row)
        private val nextPageButton: TextView = mainView.findViewById(R.id.nextpage_button_header_row)
        private val lastPageButton: TextView = mainView.findViewById(R.id.lastpage_button_header_row)

        fun bindView(currentPageNumber: Int, lastPageNumber: Int) {
            if (currentPageNumber >= 0) {
                currentPageButton.text = currentPageNumber.toString()

                if (currentPageNumber > 1) {
                    firstPageButton.visibility = View.VISIBLE
                    previousPageButton.visibility = View.VISIBLE
                } else {
                    firstPageButton.visibility = View.INVISIBLE
                    previousPageButton.visibility = View.INVISIBLE
                }

                if (lastPageNumber > currentPageNumber) {
                    lastPageButton.text = lastPageNumber.toString()
                    nextPageButton.visibility = View.VISIBLE
                    lastPageButton.visibility = View.VISIBLE
                } else {
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

    interface OnItemClickedListener {
        fun onItemClicked(position: Int)
    }
}
