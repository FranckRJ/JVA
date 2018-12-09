package com.franckrj.jva.topic

import android.text.Spannable
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Collections

data class MutableMessageInfos(var avatarUrl: String = "",
                               var author: String = "",
                               var date: String = "",
                               var content: String = "",
                               var containSpoilTag: Boolean = false)

data class MessageInfos(val avatarUrl: String,
                        val author: String,
                        val date: String,
                        val content: String,
                        val containSpoilTag: Boolean) {
    constructor(copy: MutableMessageInfos) : this(copy.avatarUrl, copy.author, copy.date, copy.content, copy.containSpoilTag)
}

data class MessageInfosShowable(val avatarUrl: String,
                                val author: Spannable,
                                val date: Spannable,
                                val formatedContent: Spannable)

object MessageInfosConverter {
    private var gson = Gson()

    @TypeConverter
    @JvmStatic
    fun stringToMessageInfosList(jsonMessageInfosList: String?): List<MessageInfos> {
        return if (jsonMessageInfosList == null) {
            Collections.emptyList()
        } else {
            val listType: Type = object : TypeToken<List<MessageInfos>>() {}.type

            gson.fromJson(jsonMessageInfosList, listType)
        }
    }

    @TypeConverter
    @JvmStatic
    fun messageInfosListToString(messageInfosList: List<MessageInfos>): String {
        return gson.toJson(messageInfosList)
    }
}
