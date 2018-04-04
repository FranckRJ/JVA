package com.franckrj.jva

open class TopicInfos(open val topicName: String,
                      open val listOfMessages: List<MessageInfos>)

class MutableTopicInfos(override var topicName: String = "",
                        override var listOfMessages: List<MessageInfos> = ArrayList()) : TopicInfos(topicName, listOfMessages)
