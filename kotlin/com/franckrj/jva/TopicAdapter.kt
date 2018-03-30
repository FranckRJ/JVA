package com.franckrj.jva

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TopicAdapter : RecyclerView.Adapter<TopicAdapter.MessageViewHolder>() {
    var listOfMessagesShowable: List<MessageInfosShowable> = ArrayList()
    var authorClickedListener: OnItemClickedListener? = null
    var dateClickedListener: OnItemClickedListener? = null

    private val internalAuthorClickedListener = View.OnClickListener { view ->
        authorClickedListener?.onItemClicked(view.tag as Int)
    }

    private val internalDateClickedListener = View.OnClickListener { view ->
        dateClickedListener?.onItemClicked(view.tag as Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_message, parent, false), internalAuthorClickedListener, internalDateClickedListener)
    }

    override fun getItemCount(): Int = listOfMessagesShowable.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bindView(listOfMessagesShowable[position], position)
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

    interface OnItemClickedListener {
        fun onItemClicked(position: Int)
    }
}
