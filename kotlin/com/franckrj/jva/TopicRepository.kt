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

    fun updateAllTopicInfos(linkOfTopicPage: String, topicInfosLiveData: MutableLiveData<TopicInfos?>) {
        TopicGetter(serviceForWeb, parserForTopic, linkOfTopicPage, topicInfosLiveData).execute()
    }
}

private class TopicGetter(private val webServiceToUse: WebService, private val topicParserToUse: TopicParser, private val linkOfTopicPage: String,
                          private val topicInfosLiveData: MutableLiveData<TopicInfos?>) : AsyncTask<Void, Void, TopicInfos?>() {
    override fun doInBackground(vararg voids: Void): TopicInfos? {
        val sourceOfWebPage: String?
        val webInfos: WebService.WebInfos = WebService.WebInfos()
        webInfos.followRedirects = false

        sourceOfWebPage = webServiceToUse.sendRequest(linkOfTopicPage, "GET", "", "", webInfos)

        return if (sourceOfWebPage == null) {
            null
        } else {
            val tmpTopicInfos = MutableTopicInfos()
            tmpTopicInfos.namesForForumAndTopic = topicParserToUse.getForumAndTopicNameFromPageSource(sourceOfWebPage)
            tmpTopicInfos.listOfMessages = topicParserToUse.getListOfMessagesFromPageSource(sourceOfWebPage)
            tmpTopicInfos
        }
    }

    override fun onPostExecute(infosForTopic: TopicInfos?) {
        topicInfosLiveData.value = infosForTopic
    }
}
