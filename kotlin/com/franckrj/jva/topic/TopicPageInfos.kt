package com.franckrj.jva.topic

data class ForumAndTopicName(val forumName: String, val topicName: String)

open class TopicPageInfos(open val namesForForumAndTopic: ForumAndTopicName,
                          open val lastPageNumber: Int,
                          open val listOfMessages: List<MessageInfos>)

class MutableTopicPageInfos(override var namesForForumAndTopic: ForumAndTopicName = ForumAndTopicName("", ""),
                            override var lastPageNumber: Int = -1,
                            override var listOfMessages: List<MessageInfos> = ArrayList()) : TopicPageInfos(namesForForumAndTopic, lastPageNumber, listOfMessages)
