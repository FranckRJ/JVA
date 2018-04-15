package com.franckrj.jva.topic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.SpannableString
import com.franckrj.jva.services.ImageGetterService
import com.franckrj.jva.services.TagHandlerService
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.UndeprecatorUtils
import com.franckrj.jva.utils.Utils

class TopicViewModel : ViewModel() {
    private val imageGetter: ImageGetterService = ImageGetterService.instance
    private val tagHandler: TagHandlerService = TagHandlerService.instance
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
                listOfMessagesShowable.value = newInfosForTopicPage.value.listOfMessages.map { messageInfos ->
                    MessageInfosShowable(messageInfos.avatarLink,
                            SpannableString(messageInfos.author),
                            SpannableString(messageInfos.date),
                            SpannableString(Utils.applyEmojiCompatIfPossible(UndeprecatorUtils.fromHtml(topicParser.formatMessageToPrettyMessage(messageInfos.content, messageInfos.containSpoilTag), imageGetter, tagHandler))))
                }
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
