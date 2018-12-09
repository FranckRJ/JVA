package com.franckrj.jva.topic

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class ForumAndTopicName(val forumName: String, val topicName: String)

data class MutableTopicPageInfos(var topicLink: String = "",
                                 var namesForForumAndTopic: ForumAndTopicName = ForumAndTopicName("", ""),
                                 var lastPageNumber: Int = -1,
                                 var listOfMessages: List<MessageInfos> = ArrayList())

@Entity
@TypeConverters(ForumAndTopicNameConverter::class, MessageInfosConverter::class)
data class TopicPageInfos(@PrimaryKey val topicLink: String,
                          val namesForForumAndTopic: ForumAndTopicName,
                          val lastPageNumber: Int,
                          val listOfMessages: List<MessageInfos>) {
    constructor(copy: MutableTopicPageInfos) : this(copy.topicLink, copy.namesForForumAndTopic, copy.lastPageNumber, copy.listOfMessages)
}

@Dao
interface TopicPageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTopicPages(topicPage: TopicPageInfos)

    @Delete
    fun deleteTopicPages(vararg topicPage: TopicPageInfos)

    @Query("DELETE FROM topicpageinfos")
    fun deleteAllTopicPages()

    @Query("SELECT * FROM topicpageinfos WHERE topiclink = :topicLinkToSearch")
    fun findByLink(topicLinkToSearch: String): TopicPageInfos?
}

object ForumAndTopicNameConverter {
    private var gson = Gson()

    @TypeConverter
    @JvmStatic
    fun stringToForumAndTopicName(jsonForumAndTopicName: String?): ForumAndTopicName {
        return if (jsonForumAndTopicName == null) {
            ForumAndTopicName("", "")
        } else {
            val listType: Type = object : TypeToken<ForumAndTopicName>() {}.type

            gson.fromJson(jsonForumAndTopicName, listType)
        }
    }

    @TypeConverter
    @JvmStatic
    fun forumAndTopicNameToString(forumAndTopicName: ForumAndTopicName): String {
        return gson.toJson(forumAndTopicName)
    }
}
