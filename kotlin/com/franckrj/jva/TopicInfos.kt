package com.franckrj.jva

data class ForumAndTopicName(val forumName: String, val topicName: String)

open class TopicInfos(open val namesForForumAndTopic: ForumAndTopicName,
                      open val listOfMessages: List<MessageInfos>)

class MutableTopicInfos(override var namesForForumAndTopic: ForumAndTopicName = ForumAndTopicName("", ""),
                        override var listOfMessages: List<MessageInfos> = ArrayList()) : TopicInfos(namesForForumAndTopic, listOfMessages)
