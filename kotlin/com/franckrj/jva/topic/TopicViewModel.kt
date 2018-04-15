package com.franckrj.jva.topic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.franckrj.jva.utils.LoadableValue

class TopicViewModel : ViewModel() {
    private val topicRepo: TopicRepository = TopicRepository.instance
    private val topicParser: TopicParser = TopicParser.instance

    private val infosForTopicPage: MutableLiveData<LoadableValue<TopicPageInfos?>?> = MutableLiveData()
    private val infosForTopicLoadingStatus: MediatorLiveData<Int> = MediatorLiveData()
    private val forumAndTopicName: MediatorLiveData<ForumAndTopicName> = MediatorLiveData()
    private val currentPageNumber: MutableLiveData<Int> = MutableLiveData()
    private val lastPageNumber: MediatorLiveData<Int> = MediatorLiveData()
    private val listOfMessagesShowable: MediatorLiveData<List<MessageInfosShowable>> = MediatorLiveData()

    init {
        infosForTopicLoadingStatus.addSource(infosForTopicPage, { newInfosForTopicPage ->
            if (newInfosForTopicPage != null) {
                infosForTopicLoadingStatus.value = newInfosForTopicPage.status
            }
        })

        forumAndTopicName.addSource(infosForTopicPage, { newInfosForTopicPage ->
            if (newInfosForTopicPage?.value != null && newInfosForTopicPage.status == LoadableValue.STATUS_LOADED &&
                    forumAndTopicName.value != newInfosForTopicPage.value.namesForForumAndTopic) {
                forumAndTopicName.value = newInfosForTopicPage.value.namesForForumAndTopic
            }
        })

        lastPageNumber.addSource(infosForTopicPage, { newInfosForTopicPage ->
            if (newInfosForTopicPage?.value != null && newInfosForTopicPage.status == LoadableValue.STATUS_LOADED &&
                    lastPageNumber.value != newInfosForTopicPage.value.lastPageNumber) {
                lastPageNumber.value = newInfosForTopicPage.value.lastPageNumber
            }
        })

        listOfMessagesShowable.addSource(infosForTopicPage, { newInfosForTopicPage ->
            if (newInfosForTopicPage?.value != null && newInfosForTopicPage.status == LoadableValue.STATUS_LOADED) {
                listOfMessagesShowable.value = newInfosForTopicPage.value.listOfMessagesShowable
            } else {
                /* Effacement de la liste des messages lors d'une erreur ou d'un début de chargement.
                 * Comportement voulu ? */
                listOfMessagesShowable.value = ArrayList()
            }
        })
    }

    fun getInfosForTopicLoadingStatus(): LiveData<Int> = infosForTopicLoadingStatus

    fun getForumAndTopicName(): LiveData<ForumAndTopicName> = forumAndTopicName

    fun getCurrentPageNumber(): LiveData<Int> = currentPageNumber

    fun getLastPageNumber(): LiveData<Int> = lastPageNumber

    fun getListOfMessagesShowable(): LiveData<List<MessageInfosShowable>> = listOfMessagesShowable

    fun updateAllTopicPageInfos(linkOfTopicPage: String) {
        val formatedLinkOfTopicPage: String = topicParser.formatThisUrlToClassicJvcUrl(linkOfTopicPage)
        val newCurrentPageNumber: Int = topicParser.getPageNumberOfThisTopicUrl(formatedLinkOfTopicPage)

        if (newCurrentPageNumber != currentPageNumber.value) {
            currentPageNumber.value = newCurrentPageNumber
        }

        topicRepo.updateAllTopicPageInfos(formatedLinkOfTopicPage, infosForTopicPage)
    }
}
