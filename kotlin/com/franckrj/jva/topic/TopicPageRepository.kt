package com.franckrj.jva.topic

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.base.AbsRepository
import com.franckrj.jva.services.WebService
import com.franckrj.jva.utils.LoadableValue

/* TODO: Stocker les messages dans une BDD pour pouvoir les récup' quand le process est kill (manque de RAM etc). */
class TopicPageRepository private constructor() : AbsRepository() {
    companion object {
        val instance: TopicPageRepository by lazy { TopicPageRepository() }
    }

    override val serviceForWeb: WebService = WebService.instance
    private val parserForTopicPage: TopicPageParser = TopicPageParser.instance

    fun updateTopicPageInfos(urlOfTopicPage: String, topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>) {
        val newTopicGetterInstance = TopicGetter(urlOfTopicPage, topicPageInfosLiveData)
        addThisRequestForThisLiveData(newTopicGetterInstance, topicPageInfosLiveData)
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
                removeRequestForThisLiveData(topicPageInfosLiveData)
            }
        }

        override fun onCancelled(result: TopicPageInfos?) {
            topicPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
