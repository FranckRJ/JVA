package com.franckrj.jva.forum

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.PageNavigationHeaderAdapter
import com.franckrj.jva.services.CopylessSpannableFactory

class ForumPageAdapter(context: Context) : PageNavigationHeaderAdapter(context) {
    companion object {
        @JvmStatic private val TYPE_ITEM: Int = (TYPE_HEADER + 1)
    }

    private val spannableFactory: CopylessSpannableFactory = CopylessSpannableFactory.instance
    var listOfTopicsShowable: List<TopicInfosShowable> = ArrayList()
    var onItemClickedListener: ((Int?) -> Any)? = null

    private val internalItemClickedListener = View.OnClickListener { view ->
        onItemClickedListener?.invoke(view.tag as? Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_topic, parent, false))
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= NUMBER_OF_HEADERS) TYPE_ITEM else super.getItemViewType(position)
    }

    override fun getItemCount(): Int = super.getItemCount() + listOfTopicsShowable.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageViewHolder) {
            holder.bindView(listOfTopicsShowable[position - NUMBER_OF_HEADERS], position - NUMBER_OF_HEADERS)
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    private inner class MessageViewHolder(private val mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val titleAndNumberOfReplysTextView: TextView = mainView.findViewById(R.id.titleandnumberofreplys_text_text_row)
        private val authorTextView: TextView = mainView.findViewById(R.id.author_text_topic_row)
        private val dateOfLastReplyTextView: TextView = mainView.findViewById(R.id.dateoflastreply_text_topic_row)
        private val topicIconImageView: ImageView = mainView.findViewById(R.id.icon_image_topic_row)

        init {
            titleAndNumberOfReplysTextView.setSpannableFactory(spannableFactory)
            authorTextView.setSpannableFactory(spannableFactory)
            dateOfLastReplyTextView.setSpannableFactory(spannableFactory)

            mainView.setOnClickListener(internalItemClickedListener)
        }

        fun bindView(topic: TopicInfosShowable, position: Int) {
            titleAndNumberOfReplysTextView.setText(topic.titleAndNumberOfReplys, TextView.BufferType.SPANNABLE)
            authorTextView.setText(topic.author, TextView.BufferType.SPANNABLE)
            dateOfLastReplyTextView.setText(topic.dateOfLastReply, TextView.BufferType.SPANNABLE)
            topicIconImageView.setImageDrawable(topic.topicIcon)

            mainView.tag = position
        }
    }
}
