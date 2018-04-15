package com.franckrj.jva.topic

import android.text.Spannable

open class MessageInfos(open val avatarLink: String,
                        open val author: String,
                        open val date: String,
                        open val content: String,
                        open val containSpoilTag: Boolean)

class MutableMessageInfos(override var avatarLink: String = "",
                          override var author: String = "",
                          override var date: String = "",
                          override var content: String = "",
                          override var containSpoilTag: Boolean = false): MessageInfos(avatarLink, author, date, content, containSpoilTag)

data class MessageInfosShowable(val avatarLink: String,
                                val author: Spannable,
                                val date: Spannable,
                                val formatedContent: Spannable)
