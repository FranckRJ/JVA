package com.franckrj.jva

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.SpannableString

class TopicViewModel : ViewModel() {
    private val tagHandler: TagHandlerService = TagHandlerService.instance
    private val topicRepo: TopicRepository = TopicRepository.instance
    private val topicParser: TopicParser = TopicParser.instance

    private val infosForTopic: MutableLiveData<TopicInfos?> = MutableLiveData()
    private val forumAndTopicName: MediatorLiveData<ForumAndTopicName> = MediatorLiveData()
    private val listOfMessagesShowable: MediatorLiveData<List<MessageInfosShowable>> = MediatorLiveData()

    init {
        forumAndTopicName.addSource(infosForTopic, { newInfosForTopic ->
            if (newInfosForTopic != null && forumAndTopicName.value != newInfosForTopic.namesForForumAndTopic) {
                forumAndTopicName.value = newInfosForTopic.namesForForumAndTopic
            }
        })

        listOfMessagesShowable.addSource(infosForTopic, { newInfosForTopic ->
            if (newInfosForTopic != null) {
                listOfMessagesShowable.value = newInfosForTopic.listOfMessages.map { messageInfos ->
                    MessageInfosShowable(SpannableString(messageInfos.author),
                                         SpannableString(messageInfos.date),
                                         SpannableString(UndeprecatorUtils.fromHtml(topicParser.formatMessageToPrettyMessage(messageInfos.content), null, tagHandler)))
                }
            } else {
                listOfMessagesShowable.value = ArrayList()
            }
        })
    }

    fun getForumAndTopicName(): LiveData<ForumAndTopicName> = forumAndTopicName

    fun getListOfMessagesShowable(): LiveData<List<MessageInfosShowable>> = listOfMessagesShowable

    fun updateAllTopicInfos(linkOfTopicPage: String) {
        topicRepo.updateAllTopicInfos(topicParser.formatThisUrlToClassicJvcUrl(linkOfTopicPage), infosForTopic)
    }
}
