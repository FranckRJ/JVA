package com.franckrj.jva

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class TopicViewModel : ViewModel() {
    private val topicRepo: TopicRepository = TopicRepository()
    private val listOfMessages: MutableLiveData<ArrayList<MessageInfos>> by lazy { topicRepo.getListOfMessages() }
    private val listOfMessagesShowable: MediatorLiveData<ArrayList<MessageInfosShowable>> by lazy {
        val tmp: MediatorLiveData<ArrayList<MessageInfosShowable>> = MediatorLiveData()
        tmp.addSource(listOfMessages, { messagesList ->
            if (messagesList != null) {
                tmp.value = ArrayList(messagesList.map { messageInfos -> MessageInfosShowable(messageInfos.author, messageInfos.date, messageInfos.content) })
            } else {
                tmp.value = ArrayList()
            }
        })
        tmp }

    fun getListOfMessagesShowable(): LiveData<ArrayList<MessageInfosShowable>> = listOfMessagesShowable
}
