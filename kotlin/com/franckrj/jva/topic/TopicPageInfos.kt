package com.franckrj.jva.topic

data class ForumAndTopicName(val forumName: String, val topicName: String)

open class TopicPageInfos(open val namesForForumAndTopic: ForumAndTopicName,
                          open val lastPageNumber: Int,
                          open val listOfMessages: List<MessageInfos>,
                          open val listOfMessagesShowable: List<MessageInfosShowable>)

class MutableTopicPageInfos(override var namesForForumAndTopic: ForumAndTopicName = ForumAndTopicName("", ""),
                            override var lastPageNumber: Int = -1,
                            override var listOfMessages: List<MessageInfos> = ArrayList(),
                            override var listOfMessagesShowable: List<MessageInfosShowable> = ArrayList()) : TopicPageInfos(namesForForumAndTopic, lastPageNumber, listOfMessages, listOfMessagesShowable)
