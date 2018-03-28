package com.franckrj.jva

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TopicAdapter : RecyclerView.Adapter<TopicAdapter.MessageViewHolder>() {
    var listOfMessagesShowable: ArrayList<MessageInfosShowable> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_message, parent, false))
    }

    override fun getItemCount(): Int = listOfMessagesShowable.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bindView(listOfMessagesShowable[position])
    }

    class MessageViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val authorTextView: TextView = mainView.findViewById(R.id.author_text_message_row)
        private val dateTextView: TextView = mainView.findViewById(R.id.date_text_message_row)
        private val contentTextView: TextView = mainView.findViewById(R.id.content_text_message_row)

        fun bindView(message: MessageInfosShowable) {
            authorTextView.text = message.author
            dateTextView.text = message.date
            contentTextView.text = message.formatedContent
        }
    }
}
