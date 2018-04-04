package com.franckrj.jva

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class TopicViewModel : ViewModel() {
    private val tagHandler: TagHandlerService = TagHandlerService.instance
    private val topicRepo: TopicRepository = TopicRepository.instance
    private val topicParser: TopicParser = TopicParser.instance

    private val infosForTopic: MutableLiveData<TopicInfos> = MutableLiveData()
    private val topicName: MediatorLiveData<String> = MediatorLiveData()
    private val listOfMessagesShowable: MediatorLiveData<List<MessageInfosShowable>> = MediatorLiveData()

    init {
        topicName.addSource(infosForTopic, { newInfosForTopic ->
            /* Pas de .isNullOrEmpty() pour activer le smartcast. */
            if (newInfosForTopic?.topicName != null && newInfosForTopic.topicName.isNotEmpty() && topicName.value != newInfosForTopic.topicName) {
                topicName.value = newInfosForTopic.topicName
            }
        })

        listOfMessagesShowable.addSource(infosForTopic, { newInfosForTopic ->
            if (newInfosForTopic?.listOfMessages != null) {
                listOfMessagesShowable.value = ArrayList(newInfosForTopic.listOfMessages.map { messageInfos ->
                    MessageInfosShowable(messageInfos.author, messageInfos.date, UndeprecatorUtils.fromHtml(topicParser.formatMessageToPrettyMessage(messageInfos.content), null, tagHandler))
                })
            } else {
                listOfMessagesShowable.value = ArrayList()
            }
        })
    }

    fun getTopicName(): LiveData<String> = topicName

    fun getListOfMessagesShowable(): LiveData<List<MessageInfosShowable>> = listOfMessagesShowable

    fun updateAllTopicInfos(linkOfTopicPage: String) {
        topicRepo.updateAllTopicInfos(topicParser.formatThisUrlToClassicJvcUrl(linkOfTopicPage), infosForTopic)
    }
}
