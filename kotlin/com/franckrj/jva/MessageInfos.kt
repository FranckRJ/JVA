package com.franckrj.jva

import android.text.Spannable

open class MessageInfos(open val author: String,
                        open val date: String,
                        open val content: String)

class MutableMessageInfos(override var author: String = "",
                          override var date: String = "",
                          override var content: String = ""): MessageInfos(author, date, content)

data class MessageInfosShowable(val author: Spannable,
                                val date: Spannable,
                                val formatedContent: Spannable)
