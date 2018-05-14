package com.franckrj.jva.topic

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.franckrj.jva.pagenav.NavigableViewModel
import com.franckrj.jva.utils.LoadableValue

class TopicViewModel(app: Application) : NavigableViewModel(app) {
    private val topicPageParser: TopicPageParser = TopicPageParser.instance

    private var infosForTopicPage: LiveData<LoadableValue<TopicPageInfos?>?>? = null
    private val forumAndTopicName: MediatorLiveData<ForumAndTopicName> = MediatorLiveData()
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
        /* Dans cet ordre bien précisément car currentPageNumber ne peut pas être supérieur à lastPageNumber.
         * Le atLeast(1) pour éviter des possibles bugs. */
        lastPageNumber.value = topicPageParser.getPageNumberOfThisTopicUrl(topicUrl).coerceAtLeast(1)
        currentPageNumber.value = lastPageNumber.value
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
                /* La fonction pour récupérer le numéro de la dernière page retourne -1 quand c'est la page courante. */
                lastPageNumber.value = if (lastInfosForTopicPage.value.lastPageNumber < 1) {
                    (currentPageNumber.value ?: 1)
                } else {
                    lastInfosForTopicPage.value.lastPageNumber
                }
            }
        })
    }

    override fun onCleared() {
        removeCurrentSourceForPageInfos()
    }

    fun getForumAndTopicName(): LiveData<ForumAndTopicName?> = forumAndTopicName
}
