package com.franckrj.jva.forum

import android.graphics.drawable.Drawable
import android.text.Spannable

enum class TopicType { SINGLE_PAGE, MULTIPLE_PAGE }

open class TopicInfos(open val title: String,
                      open val author: String,
                      open val dateOfLastReply: String,
                      open val numberOfReplys: Int,
                      open val typeOfTopic: TopicType)

class MutableTopicInfos(override var title: String = "",
                        override var author: String = "",
                        override var dateOfLastReply: String = "",
                        override var numberOfReplys: Int = -1,
                        override var typeOfTopic: TopicType = TopicType.SINGLE_PAGE): TopicInfos(title, author, dateOfLastReply, numberOfReplys, typeOfTopic)

data class TopicInfosShowable(val titleAndNumberOfReplys: Spannable,
                              val author: Spannable,
                              val dateOfLastReply: Spannable,
                              val topicIcon: Drawable)
