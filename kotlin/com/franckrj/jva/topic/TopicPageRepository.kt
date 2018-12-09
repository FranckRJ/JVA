package com.franckrj.jva.topic

import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.base.AbsAsyncValueSetter
import com.franckrj.jva.base.AbsRepository
import com.franckrj.jva.services.AppDatabase
import com.franckrj.jva.services.WebService
import com.franckrj.jva.utils.LoadableValue

class TopicPageRepository private constructor() : AbsRepository() {
    companion object {
        val instance: TopicPageRepository by lazy { TopicPageRepository() }
    }

    override val serviceForWeb: WebService = WebService.instance
    private val parserForTopicPage: TopicPageParser = TopicPageParser.instance
    private val database: AppDatabase = AppDatabase.instance

    fun updateTopicPageInfos(urlOfTopicPage: String, topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>, allowFetchFromCache: Boolean) {
        val newTopicGetterInstance = TopicGetter(urlOfTopicPage, topicPageInfosLiveData)
        addThisRequestForThisLiveData(newTopicGetterInstance, topicPageInfosLiveData)
        topicPageInfosLiveData.value = LoadableValue.loading(topicPageInfosLiveData.value?.value)
        newTopicGetterInstance.execute(allowFetchFromCache)
    }

    private inner class TopicGetter(private val urlOfTopicPage: String, private val topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>) :
            AbsAsyncValueSetter<TopicPageInfos>(topicPageInfosLiveData) {
        override fun getValueToSetInBackground(tryToGetCachedValue: Boolean): TopicPageInfos? {
            if (tryToGetCachedValue) {
                val topicPageFromDatabase = database.topicPageDao().findByLink(urlOfTopicPage)
                if (topicPageFromDatabase != null) {
                    return topicPageFromDatabase
                }
            }

            val sourceOfWebPage:String? = serviceForWeb.getPage(urlOfTopicPage, hashCode())

            return if (sourceOfWebPage == null) {
                null
            } else {
                val tmpTopicPageInfos = MutableTopicPageInfos()

                tmpTopicPageInfos.topicLink = urlOfTopicPage
                tmpTopicPageInfos.namesForForumAndTopic = parserForTopicPage.getForumAndTopicNameFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.lastPageNumber = parserForTopicPage.getLastPageNumberFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.listOfMessages = parserForTopicPage.getListOfMessagesFromPageSource(sourceOfWebPage)

                TopicPageInfos(tmpTopicPageInfos)
            }
        }

        override fun setTheValueGetted(result: TopicPageInfos?, isStillActive: Boolean) {
            if (isStillActive) {
                if (result == null) {
                    topicPageInfosLiveData.value = LoadableValue.error(null)
                } else {
                    topicPageInfosLiveData.value = LoadableValue.loaded(result)
                }
                removeRequestForThisLiveData(topicPageInfosLiveData)
            }
        }

        override fun afterValueGettedInBackground(result: TopicPageInfos?, isStillActive: Boolean) {
            if (isStillActive && result != null) {
                database.topicPageDao().insertTopicPages(result)
            }
        }

        override fun onCancelled() {
            topicPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
