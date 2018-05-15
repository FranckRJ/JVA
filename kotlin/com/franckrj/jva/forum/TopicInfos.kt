package com.franckrj.jva.forum

import android.graphics.drawable.Drawable
import android.text.Spannable

enum class TopicType(val index: Int) {
    SINGLE_PAGE(0),
    MULTIPLE_PAGE(1),
    LOCKED(2),
    PINNED_OPENED(3),
    PINNED_LOCKED(4),
    DELETED(5),
    SOLVED(6)
}

open class TopicInfos(open val title: String,
                      open val author: String,
                      open val dateOfLastReply: String,
                      open val numberOfReplys: Int,
                      open val typeOfTopic: TopicType,
                      open val topicUrl: String)

class MutableTopicInfos(override var title: String = "",
                        override var author: String = "",
                        override var dateOfLastReply: String = "",
                        override var numberOfReplys: Int = -1,
                        override var typeOfTopic: TopicType = TopicType.SINGLE_PAGE,
                        override var topicUrl: String = ""): TopicInfos(title, author, dateOfLastReply, numberOfReplys, typeOfTopic, topicUrl)

data class TopicInfosShowable(val titleAndNumberOfReplys: Spannable,
                              val author: Spannable,
                              val dateOfLastReply: Spannable,
                              val topicIcon: Drawable)
