package com.franckrj.jva.topic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import com.franckrj.jva.utils.LoadableValue

class TopicViewModel : ViewModel() {
    private val topicPageParser: TopicPageParser = TopicPageParser.instance

    private val forumAndTopicName: MediatorLiveData<ForumAndTopicName> = MediatorLiveData()
    private val lastPageNumber: MediatorLiveData<Int> = MediatorLiveData()
    private var infosForTopicPage: LiveData<LoadableValue<TopicPageInfos?>?>? = null

    var topicUrl: String = ""
        set(newTopicUrl) {
            field = topicPageParser.formatThisUrlToClassicJvcUrl(newTopicUrl)
            lastPageNumber.value = topicPageParser.getPageNumberOfThisTopicUrl(topicUrl)
        }

    private fun removeCurrentSourceForPageInfos() {
        val currentInfosForTopicPage: LiveData<LoadableValue<TopicPageInfos?>?>? = infosForTopicPage
        if (currentInfosForTopicPage != null) {
            forumAndTopicName.removeSource(currentInfosForTopicPage)
            lastPageNumber.removeSource(currentInfosForTopicPage)
            infosForTopicPage = null
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

    fun getLastPageNumber(): LiveData<Int> = lastPageNumber
}
