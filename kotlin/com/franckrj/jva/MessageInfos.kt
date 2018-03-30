package com.franckrj.jva

import android.text.Spanned

open class MessageInfos(open val author: String, open val date: String, open val content: String)

class MutableMessageInfos(override var author: String = "", override var date: String = "", override var content: String = ""): MessageInfos(author, date, content)

data class MessageInfosShowable(val author: String, val date: String, val formatedContent: Spanned)
