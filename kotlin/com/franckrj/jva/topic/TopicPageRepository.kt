package com.franckrj.jva.topic

import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.base.AbsAsyncValueSetter
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

    private inner class TopicGetter(private val urlOfTopicPage: String, private val topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>) :
            AbsAsyncValueSetter<TopicPageInfos>(topicPageInfosLiveData) {
        override fun doInBackground(): TopicPageInfos? {
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

        override fun onPostExecute(result: TopicPageInfos?, isStillActive: Boolean) {
            if (isStillActive) {
                if (result == null) {
                    topicPageInfosLiveData.value = LoadableValue.error(null)
                } else {
                    topicPageInfosLiveData.value = LoadableValue.loaded(result)
                }
                removeRequestForThisLiveData(topicPageInfosLiveData)
            }
        }

        override fun onCancelled() {
            topicPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
