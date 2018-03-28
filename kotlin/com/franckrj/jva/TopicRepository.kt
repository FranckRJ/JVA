package com.franckrj.jva

import android.arch.lifecycle.MutableLiveData

class TopicRepository {
    fun getListOfMessages(): MutableLiveData<ArrayList<MessageInfos>> {
        val listOfMessagesReturned: MutableLiveData<ArrayList<MessageInfos>> = MutableLiveData()
        val listOfMessages: ArrayList<MessageInfos> = ArrayList()

        listOfMessages.add(MessageInfos("auteur-1", "date-1", "message-1"))
        listOfMessages.add(MessageInfos("auteur-1651651", "date-1651651", "message-1651651"))
        listOfMessages.add(MessageInfos("auteur-4125", "date-4125", "message-4125"))
        listOfMessages.add(MessageInfos("auteur-0000000000000000000000000000000000000000000000000", "date-000000000000000000000000000000000000000000000000000000000000000", "message-0"))
        listOfMessages.add(MessageInfos("auteur-888888", "date-888888", "message-888888"))
        listOfMessages.add(MessageInfos("auteur-235", "date-235", "message-235"))
        listOfMessages.add(MessageInfos("auteur-6", "date-6", "message-6"))
        listOfMessages.add(MessageInfos("auteur-1", "date-1111111111111111111111111111111111111111111111111111111111111111111111111111111", "message-1"))
        listOfMessages.add(MessageInfos("auteur-1651651", "date-1651651", "message-1651651"))
        listOfMessages.add(MessageInfos("auteur-4125", "date-4125", "message-4125"))
        listOfMessages.add(MessageInfos("auteur-000000000000000000000000000000000000000000000000000", "date-0", "message-0"))
        listOfMessages.add(MessageInfos("auteur-888888", "date-888888", "message-888888"))
        listOfMessages.add(MessageInfos("auteur-235", "date-235", "message-235"))
        listOfMessages.add(MessageInfos("auteur-6", "date-6", "message-6"))

        listOfMessagesReturned.value = listOfMessages

        return listOfMessagesReturned
    }
}
