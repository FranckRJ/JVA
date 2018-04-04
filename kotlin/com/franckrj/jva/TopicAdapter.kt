package com.franckrj.jva

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TopicAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER: Int = 0
        private const val TYPE_ITEM: Int = 1
    }

    var listOfHeaders: List<String> = ArrayList()
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
                MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_message, parent, false), internalAuthorClickedListener, internalDateClickedListener)
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

    class MessageViewHolder(mainView: View, authorTextViewClickedListener: View.OnClickListener, dateTextViewClickedListener: View.OnClickListener) : RecyclerView.ViewHolder(mainView) {
        private val authorTextView: TextView = mainView.findViewById(R.id.author_text_message_row)
        private val dateTextView: TextView = mainView.findViewById(R.id.date_text_message_row)
        private val contentTextView: TextView = mainView.findViewById(R.id.content_text_message_row)

        init {
            authorTextView.setOnClickListener(authorTextViewClickedListener)
            dateTextView.setOnClickListener(dateTextViewClickedListener)
        }

        fun bindView(message: MessageInfosShowable, position: Int) {
            authorTextView.text = message.author
            dateTextView.text = message.date
            contentTextView.text = message.formatedContent

            authorTextView.tag = position
            dateTextView.tag = position
        }
    }

    class HeaderViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val headerTextView: TextView = mainView as TextView

        fun bindView(headerContent: String) {
            headerTextView.text = headerContent
        }
    }

    interface OnItemClickedListener {
        fun onItemClicked(position: Int)
    }
}
