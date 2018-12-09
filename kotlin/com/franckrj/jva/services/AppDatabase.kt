package com.franckrj.jva.services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.franckrj.jva.forum.ForumPageDao
import com.franckrj.jva.forum.ForumPageInfos
import com.franckrj.jva.topic.TopicPageDao
import com.franckrj.jva.topic.TopicPageInfos

@Database(entities = [ForumPageInfos::class, TopicPageInfos::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        lateinit var instance: AppDatabase

        fun initDatabase(appContext: Context) {
            instance = Room.databaseBuilder(appContext,
                    AppDatabase::class.java, "jva-main-db").build()
        }
    }

    abstract fun forumPageDao(): ForumPageDao
    abstract fun topicPageDao(): TopicPageDao
}
