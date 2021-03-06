package com.franckrj.jva.topic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.franckrj.jva.services.CopylessSpannableFactory
import com.franckrj.jva.utils.GlideApp
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.PageNavigationHeaderAdapter
import com.franckrj.jva.utils.GlideRequests

class TopicPageAdapter(context: Context,
                       private val sizeOfAvatars: Int,
                       sizeOfAvatarRoundedCorners: Int) : PageNavigationHeaderAdapter(context) {
    companion object {
        private const val TYPE_ITEM: Int = (TYPE_HEADER + 1)
    }

    private val spannableFactory: CopylessSpannableFactory = CopylessSpannableFactory.instance
    private val avatarRoundedCorners = RoundedCorners(sizeOfAvatarRoundedCorners)
    private val transitionOption = DrawableTransitionOptions.withCrossFade()
    private val glide: GlideRequests = GlideApp.with(context)
    var listOfMessagesShowable: List<MessageInfosShowable> = ArrayList()
    var authorClickedListener: ((Int?) -> Unit)? = null
    var dateClickedListener: ((Int?) -> Unit)? = null

    private val internalAuthorClickedListener = View.OnClickListener { view ->
        authorClickedListener?.invoke(view.tag as? Int)
    }

    private val internalDateClickedListener = View.OnClickListener { view ->
        dateClickedListener?.invoke(view.tag as? Int)
    }

    fun invalidateTextViewOfThisViewHolder(holder: RecyclerView.ViewHolder) {
        if (holder is MessageViewHolder) {
            holder.invalidateTextViewWithImageSpan()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_message_ll, parent, false))
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= NUMBER_OF_HEADERS) TYPE_ITEM else super.getItemViewType(position)
    }

    override fun getItemCount(): Int = super.getItemCount() + listOfMessagesShowable.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageViewHolder) {
            holder.bindView(listOfMessagesShowable[position - NUMBER_OF_HEADERS], position - NUMBER_OF_HEADERS)
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    private inner class MessageViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val avatarImageView: ImageView = mainView.findViewById(R.id.avatar_image_message_row)
        private val authorTextView: TextView = mainView.findViewById(R.id.author_text_message_row)
        private val dateTextView: TextView = mainView.findViewById(R.id.date_text_message_row)
        private val contentTextView: AppCompatTextView = mainView.findViewById(R.id.content_text_message_row)

        init {
            authorTextView.setOnClickListener(internalAuthorClickedListener)
            dateTextView.setOnClickListener(internalDateClickedListener)

            authorTextView.setSpannableFactory(spannableFactory)
            dateTextView.setSpannableFactory(spannableFactory)
            contentTextView.setSpannableFactory(spannableFactory)
        }

        fun bindView(message: MessageInfosShowable, position: Int) {
            glide.load(message.avatarUrl)
                 .override(sizeOfAvatars, sizeOfAvatars)
                 .transform(avatarRoundedCorners)
                 .transition(transitionOption)
                 .into(avatarImageView)

            authorTextView.setText(message.author, TextView.BufferType.SPANNABLE)
            dateTextView.setText(message.date, TextView.BufferType.SPANNABLE)
            contentTextView.setTextFuture(PrecomputedTextCompat.getTextFuture(message.formatedContent, contentTextView.textMetricsParamsCompat, null))

            authorTextView.tag = position
            dateTextView.tag = position
        }

        fun invalidateTextViewWithImageSpan() {
            contentTextView.invalidate()
        }
    }
}
