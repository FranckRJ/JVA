package com.franckrj.jva.forum

import android.graphics.drawable.Drawable
import android.text.Spannable
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Collections

enum class TopicType(val index: Int) {
    SINGLE_PAGE(0),
    MULTIPLE_PAGE(1),
    LOCKED(2),
    PINNED_OPENED(3),
    PINNED_LOCKED(4),
    DELETED(5),
    SOLVED(6)
}

data class MutableTopicInfos(var title: String = "",
                             var author: String = "",
                             var dateOfLastReply: String = "",
                             var numberOfReplys: Int = -1,
                             var typeOfTopic: TopicType = TopicType.SINGLE_PAGE,
                             var topicUrl: String = "")

data class TopicInfos(val title: String,
                      val author: String,
                      val dateOfLastReply: String,
                      val numberOfReplys: Int,
                      val typeOfTopic: TopicType,
                      val topicUrl: String) {
    constructor(copy : MutableTopicInfos) : this(copy.title, copy.author, copy.dateOfLastReply, copy.numberOfReplys, copy.typeOfTopic, copy.topicUrl)
}

data class TopicInfosShowable(val titleAndNumberOfReplys: Spannable,
                              val author: Spannable,
                              val dateOfLastReply: Spannable,
                              val topicIcon: Drawable)

object TopicInfosConverter {
    private var gson = Gson()

    @TypeConverter
    @JvmStatic
    fun stringToTopicInfosList(jsonTopicInfosList: String?): List<TopicInfos> {
        return if (jsonTopicInfosList == null) {
            Collections.emptyList()
        } else {
            val listType: Type = object : TypeToken<List<TopicInfos>>() {}.type

            gson.fromJson(jsonTopicInfosList, listType)
        }
    }

    @TypeConverter
    @JvmStatic
    fun topicInfosListToString(topicInfosList: List<TopicInfos>): String {
        return gson.toJson(topicInfosList)
    }
}
