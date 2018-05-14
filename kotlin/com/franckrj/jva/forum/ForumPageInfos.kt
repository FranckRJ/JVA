package com.franckrj.jva.forum

open class ForumPageInfos(open val forumName: String,
                          open val listOfTopics: List<TopicInfos>)

class MutableForumPageInfos(override var forumName: String = "",
                            override var listOfTopics: List<TopicInfos> = ArrayList()) : ForumPageInfos(forumName, listOfTopics)
