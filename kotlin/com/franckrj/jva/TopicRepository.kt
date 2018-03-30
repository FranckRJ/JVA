package com.franckrj.jva

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask

/* TODO: Stocker les messages dans une BDD pour pouvoir les récup' quand le process est kill (manque de RAM etc). */
class TopicRepository private constructor() {
    companion object {
        val instance: TopicRepository by lazy { TopicRepository() }
    }

    private val serviceForWeb: WebService = WebService.instance
    private val parserForTopic: TopicParser = TopicParser.instance

    fun updateListOfMessages(linkOfTopicPage: String, listOfMessagesLiveData: MutableLiveData<ArrayList<MessageInfos>>) {
        TopicGetter(serviceForWeb, parserForTopic, linkOfTopicPage, listOfMessagesLiveData).execute()
    }
}

private class TopicGetter(private val webServiceToUse: WebService, private val topicParserToUse: TopicParser, private val linkOfTopicPage: String,
                          private val listOfMessagesLiveData: MutableLiveData<ArrayList<MessageInfos>>) : AsyncTask<Void, Void, ArrayList<MessageInfos>>() {
    override fun doInBackground(vararg voids: Void): ArrayList<MessageInfos> {
        val sourceOfWebPage: String?
        val webInfos: WebService.WebInfos = WebService.WebInfos()
        webInfos.followRedirects = false

        sourceOfWebPage = webServiceToUse.sendRequest(linkOfTopicPage, "GET", "", "", webInfos)

        return if (sourceOfWebPage == null) {
            ArrayList()
        } else {
            topicParserToUse.getListOfMessagesFromPageSource(sourceOfWebPage)
        }
    }

    override fun onPostExecute(listOfMessages: ArrayList<MessageInfos>) {
        listOfMessagesLiveData.value = listOfMessages
    }
}
