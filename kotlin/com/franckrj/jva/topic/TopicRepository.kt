package com.franckrj.jva.topic

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.franckrj.jva.services.WebService

/* TODO: Stocker les messages dans une BDD pour pouvoir les récup' quand le process est kill (manque de RAM etc). */
class TopicRepository private constructor() {
    companion object {
        val instance: TopicRepository by lazy { TopicRepository() }
    }

    private val serviceForWeb: WebService = WebService.instance
    private val parserForTopic: TopicParser = TopicParser.instance

    fun updateAllTopicPageInfos(linkOfTopicPage: String, topicPageInfosLiveData: MutableLiveData<TopicPageInfos?>) {
        TopicGetter(serviceForWeb, parserForTopic, linkOfTopicPage, topicPageInfosLiveData).execute()
    }
}

private class TopicGetter(private val webServiceToUse: WebService, private val topicParserToUse: TopicParser, private val linkOfTopicPage: String,
                          private val topicPageInfosLiveData: MutableLiveData<TopicPageInfos?>) : AsyncTask<Void, Void, TopicPageInfos?>() {
    override fun doInBackground(vararg voids: Void): TopicPageInfos? {
        val sourceOfWebPage: String?
        val webInfos: WebService.WebInfos = WebService.WebInfos()
        webInfos.followRedirects = false

        sourceOfWebPage = webServiceToUse.sendRequest(linkOfTopicPage, "GET", "", "", webInfos)

        return if (sourceOfWebPage == null) {
            null
        } else {
            val tmpTopicPageInfos = MutableTopicPageInfos()
            tmpTopicPageInfos.namesForForumAndTopic = topicParserToUse.getForumAndTopicNameFromPageSource(sourceOfWebPage)
            tmpTopicPageInfos.listOfMessages = topicParserToUse.getListOfMessagesFromPageSource(sourceOfWebPage)
            tmpTopicPageInfos
        }
    }

    override fun onPostExecute(infosForTopicPage: TopicPageInfos?) {
        topicPageInfosLiveData.value = infosForTopicPage
    }
}
