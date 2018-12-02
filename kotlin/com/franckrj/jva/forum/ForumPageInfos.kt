package com.franckrj.jva.forum

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverters

data class MutableForumPageInfos(var forumLink: String = "",
                                 var forumName: String = "",
                                 var listOfTopics: List<TopicInfos> = ArrayList())

@Entity
@TypeConverters(TopicInfosConverter::class)
data class ForumPageInfos(@PrimaryKey val forumLink: String,
                          val forumName: String,
                          val listOfTopics: List<TopicInfos>) {
    constructor(copy: MutableForumPageInfos) : this(copy.forumLink, copy.forumName, copy.listOfTopics)
}

@Dao
interface ForumPageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertForumPages(forumPage: ForumPageInfos)

    @Delete
    fun deleteForumPages(vararg forumPage: ForumPageInfos)

    @Query("DELETE FROM forumpageinfos")
    fun deleteAllForumPages()

    @Query("SELECT * FROM forumpageinfos WHERE forumlink = :forumLinkToSearch")
    fun findByLink(forumLinkToSearch: String): ForumPageInfos?
}
