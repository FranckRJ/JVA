package com.franckrj.jva.topic

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.franckrj.jva.services.WebService
import com.franckrj.jva.utils.LoadableValue

/* TODO: Stocker les messages dans une BDD pour pouvoir les récup' quand le process est kill (manque de RAM etc). */
class TopicPageRepository private constructor() {
    companion object {
        val instance: TopicPageRepository by lazy { TopicPageRepository() }
    }

    private val serviceForWeb: WebService = WebService.instance
    private val parserForTopicPage: TopicPageParser = TopicPageParser.instance
    private val mapOfTopicGetterInstances: HashMap<MutableLiveData<LoadableValue<TopicPageInfos?>?>, TopicGetter> = HashMap()

    fun cancelRequestForThisLiveData(liveDataLinkedToRequest: MutableLiveData<LoadableValue<TopicPageInfos?>?>) {
        val requestInstance: TopicGetter? = mapOfTopicGetterInstances[liveDataLinkedToRequest]

        if (requestInstance != null) {
            requestInstance.cancel(false)
            serviceForWeb.cancelRequest(requestInstance.hashCode())
            mapOfTopicGetterInstances.remove(liveDataLinkedToRequest)
        }
    }

    fun updateTopicPageInfos(urlOfTopicPage: String, topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>) {
        val newTopicGetterInstance = TopicGetter(urlOfTopicPage, topicPageInfosLiveData)
        cancelRequestForThisLiveData(topicPageInfosLiveData)
        mapOfTopicGetterInstances[topicPageInfosLiveData] = newTopicGetterInstance
        topicPageInfosLiveData.value = LoadableValue.loading(topicPageInfosLiveData.value?.value)
        newTopicGetterInstance.execute()
    }

    /* Ça ne devrait pas poser de problème normalement car
     * cette AsyncTask n'a aucune référence vers un contexte. */
    @SuppressLint("StaticFieldLeak")
    private inner class TopicGetter(private val urlOfTopicPage: String, private val topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>) : AsyncTask<Void, Void, TopicPageInfos?>() {
        override fun doInBackground(vararg voids: Void): TopicPageInfos? {
            val sourceOfWebPage:String? = serviceForWeb.getPage(urlOfTopicPage, hashCode())

            return if (sourceOfWebPage == null) {
                null
            } else {
                val tmpTopicPageInfos = MutableTopicPageInfos()

                tmpTopicPageInfos.namesForForumAndTopic = parserForTopicPage.getForumAndTopicNameFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.lastPageNumber = parserForTopicPage.getLastPageNumberFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.listOfMessages = parserForTopicPage.getListOfMessagesFromPageSource(sourceOfWebPage)

                tmpTopicPageInfos
            }
        }

        override fun onPostExecute(infosForTopicPage: TopicPageInfos?) {
            if (!isCancelled) {
                if (infosForTopicPage == null) {
                    topicPageInfosLiveData.value = LoadableValue.error(null)
                } else {
                    topicPageInfosLiveData.value = LoadableValue.loaded(infosForTopicPage)
                }
                mapOfTopicGetterInstances.remove(topicPageInfosLiveData)
            }
        }

        override fun onCancelled(result: TopicPageInfos?) {
            topicPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
