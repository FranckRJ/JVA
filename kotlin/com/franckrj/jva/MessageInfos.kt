package com.franckrj.jva

data class MessageInfos(var author: String = "", var date: String = "", var content: String = "")

data class MessageInfosShowable(val author: String, val date: String, val formatedContent: String)
