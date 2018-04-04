package com.franckrj.jva

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel

class TopicViewModel : ViewModel() {
    private val tagHandler: TagHandlerService = TagHandlerService.instance
    private val topicRepo: TopicRepository = TopicRepository.instance
    private val topicParser: TopicParser = TopicParser.instance
    private val mutableListOfMessages: MutableLiveData<ArrayList<MessageInfos>> = MutableLiveData()
    private val mutableListOfMessagesShowable: MediatorLiveData<ArrayList<MessageInfosShowable>> by lazy {
        val tmp: MediatorLiveData<ArrayList<MessageInfosShowable>> = MediatorLiveData()
        tmp.addSource(mutableListOfMessages, { messagesList ->
            if (messagesList != null) {
                tmp.value = ArrayList(messagesList.map { messageInfos ->
                    MessageInfosShowable(messageInfos.author, messageInfos.date, UndeprecatorUtils.fromHtml(topicParser.formatMessageToPrettyMessage(messageInfos.content), null, tagHandler))
                })
            } else {
                tmp.value = ArrayList()
            }
        })
        tmp }

    val listOfMessages: LiveData<List<MessageInfos>> = Transformations.map(mutableListOfMessages, {it})
    val listOfMessagesShowable: LiveData<List<MessageInfosShowable>> = Transformations.map(mutableListOfMessagesShowable, {it})

    fun updateListOfMessages(linkOfTopicPage: String) {
        topicRepo.updateListOfMessages(topicParser.formatThisUrlToClassicJvcUrl(linkOfTopicPage), mutableListOfMessages)
    }
}
