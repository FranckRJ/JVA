package com.franckrj.jva.topic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.franckrj.jva.utils.LoadableValue

class TopicViewModel : ViewModel() {
    private val topicPageParser: TopicPageParser = TopicPageParser.instance

    private var infosForTopicPage: LiveData<LoadableValue<TopicPageInfos?>?>? = null
    private val forumAndTopicName: MediatorLiveData<ForumAndTopicName> = MediatorLiveData()
    private val lastPageNumber: MediatorLiveData<Int> = MediatorLiveData()
    private val currentPageNumber: MutableLiveData<Int> = MutableLiveData()
    var topicUrl: String = ""
        private set

    private fun removeCurrentSourceForPageInfos() {
        val currentInfosForTopicPage: LiveData<LoadableValue<TopicPageInfos?>?>? = infosForTopicPage
        if (currentInfosForTopicPage != null) {
            forumAndTopicName.removeSource(currentInfosForTopicPage)
            lastPageNumber.removeSource(currentInfosForTopicPage)
            infosForTopicPage = null
        }
    }

    fun setUrlForTopic(newTopicUrl: String) {
        topicUrl = topicPageParser.formatThisUrlToClassicJvcUrl(newTopicUrl)
        /* Dans cet ordre bien précisément car currentPageNumber ne doit jamais être supérieur à lastPageNumber. */
        lastPageNumber.value = topicPageParser.getPageNumberOfThisTopicUrl(topicUrl)
        currentPageNumber.value = lastPageNumber.value
    }

    fun setCurrentPageNumber(newCurrentPageNumber: Int) {
        if (currentPageNumber.value != newCurrentPageNumber) {
            currentPageNumber.value = newCurrentPageNumber
        }
    }

    fun setNewSourceForPageInfos(newInfosForTopicPage: LiveData<LoadableValue<TopicPageInfos?>?>) {
        removeCurrentSourceForPageInfos()
        infosForTopicPage = newInfosForTopicPage

        forumAndTopicName.addSource(newInfosForTopicPage, { lastInfosForTopicPage ->
            if (lastInfosForTopicPage?.value != null && lastInfosForTopicPage.status == LoadableValue.STATUS_LOADED &&
                    forumAndTopicName.value != lastInfosForTopicPage.value.namesForForumAndTopic) {
                forumAndTopicName.value = lastInfosForTopicPage.value.namesForForumAndTopic
            }
        })

        lastPageNumber.addSource(newInfosForTopicPage, { lastInfosForTopicPage ->
            if (lastInfosForTopicPage?.value != null && lastInfosForTopicPage.status == LoadableValue.STATUS_LOADED &&
                    lastPageNumber.value != lastInfosForTopicPage.value.lastPageNumber) {
                lastPageNumber.value = lastInfosForTopicPage.value.lastPageNumber
            }
        })
    }

    override fun onCleared() {
        removeCurrentSourceForPageInfos()
    }

    fun getForumAndTopicName(): LiveData<ForumAndTopicName?> = forumAndTopicName

    fun getLastPageNumber(): LiveData<Int?> = lastPageNumber

    fun getCurrentPageNumber(): LiveData<Int?> = currentPageNumber
}
