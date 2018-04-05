package com.franckrj.jva

import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TopicAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER: Int = 0
        private const val TYPE_ITEM: Int = 1
    }

    val spannableFactoryToUse: CopylessSpannableFactory = CopylessSpannableFactory.instance
    var listOfHeaders: List<HeaderInfos> = ArrayList()
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
                HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_topic_header, parent, false), spannableFactoryToUse)
            }
            TYPE_ITEM -> {
                MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_message, parent, false), internalAuthorClickedListener, internalDateClickedListener, spannableFactoryToUse)
            }
            else -> {
                throw RuntimeException("Type non supporté.")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < listOfHeaders.size) TYPE_HEADER else TYPE_ITEM
    }

    override fun getItemCount(): Int = listOfHeaders.size + listOfMessagesShowable.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bindView(listOfHeaders[position])
        } else if (holder is MessageViewHolder) {
            holder.bindView(listOfMessagesShowable[position - listOfHeaders.size], position)
        }
    }

    private class MessageViewHolder(mainView: View,
                                    authorTextViewClickedListener: View.OnClickListener,
                                    dateTextViewClickedListener: View.OnClickListener,
                                    spannableFactoryToUse: Spannable.Factory) : RecyclerView.ViewHolder(mainView) {
        private val authorTextView: TextView = mainView.findViewById(R.id.author_text_message_row)
        private val dateTextView: TextView = mainView.findViewById(R.id.date_text_message_row)
        private val contentTextView: TextView = mainView.findViewById(R.id.content_text_message_row)

        init {
            authorTextView.setOnClickListener(authorTextViewClickedListener)
            dateTextView.setOnClickListener(dateTextViewClickedListener)

            authorTextView.setSpannableFactory(spannableFactoryToUse)
            dateTextView.setSpannableFactory(spannableFactoryToUse)
            contentTextView.setSpannableFactory(spannableFactoryToUse)
        }

        fun bindView(message: MessageInfosShowable, position: Int) {
            authorTextView.setText(message.author, TextView.BufferType.SPANNABLE)
            dateTextView.setText(message.date, TextView.BufferType.SPANNABLE)
            contentTextView.setText(message.formatedContent, TextView.BufferType.SPANNABLE)

            authorTextView.tag = position
            dateTextView.tag = position
        }
    }

    private class HeaderViewHolder(mainView: View, spannableFactoryToUse: Spannable.Factory) : RecyclerView.ViewHolder(mainView) {
        private val headerTextView: TextView = mainView as TextView

        init {
            headerTextView.setSpannableFactory(spannableFactoryToUse)
        }

        fun bindView(infosForHeader: HeaderInfos) {
            headerTextView.setText(infosForHeader.content, TextView.BufferType.SPANNABLE)
            headerTextView.setCompoundDrawablesRelativeWithIntrinsicBounds((if (infosForHeader.hasAnArrow) R.drawable.ic_arrow_back else 0), 0, 0, 0)
        }
    }

    class HeaderInfos(val content: Spannable, val hasAnArrow: Boolean)

    interface OnItemClickedListener {
        fun onItemClicked(position: Int)
    }
}
