package com.franckrj.jva.topic

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.text.SpannableString
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigablePageViewModel
import com.franckrj.jva.services.ImageGetterService
import com.franckrj.jva.services.TagHandlerService
import com.franckrj.jva.utils.BetterQuoteSpan
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.UndeprecatorUtils

class TopicPageViewModel(app: Application) : NavigablePageViewModel(app) {
    private val topicPageRepo: TopicPageRepository = TopicPageRepository.instance
    private val topicPageParser: TopicPageParser = TopicPageParser.instance
    private val imageGetter: ImageGetterService = ImageGetterService(app.applicationContext, R.drawable.ic_image_download, R.drawable.ic_image_deleted)
    private val tagHandler: TagHandlerService = TagHandlerService.instance
    private val settingsForMessages: TopicPageParser.MessageSettings
    private var currentTaskForMessagesFormat: FormatMessagesToShowableMessages? = null

    private val infosForTopicPage: MutableLiveData<LoadableValue<TopicPageInfos?>?> = MutableLiveData()
    private val listOfMessagesShowable: MediatorLiveData<LoadableValue<List<MessageInfosShowable>>> = MediatorLiveData()
    private val invalidateTextViewNeeded: MutableLiveData<Boolean> = MutableLiveData()

    init {
        val settingsForBetterQuotes = BetterQuoteSpan.BetterQuoteSettings(UndeprecatorUtils.getColor(app, R.color.quoteBackgroundColor),
                                                                          UndeprecatorUtils.getColor(app, R.color.colorAccent),
                                                                          app.resources.getDimensionPixelSize(R.dimen.quoteStripeSize),
                                                                          app.resources.getDimensionPixelSize(R.dimen.quoteGapSize))

        settingsForMessages = TopicPageParser.MessageSettings(settingsForBetterQuotes, imageGetter, tagHandler, 2)

        listOfMessagesShowable.addSource(infosForTopicPage, { newInfosForTopicPage ->
            /* Effacement de la liste des messages lors d'une erreur ou d'un début de chargement.
             * Comportement voulu ? */
            if (newInfosForTopicPage != null) {
                when {
                    newInfosForTopicPage.value != null && newInfosForTopicPage.status == LoadableValue.STATUS_LOADED -> {
                        if (currentTaskForMessagesFormat != null) {
                            cancelCurrentFormatMessagesTask()
                        }
                        currentTaskForMessagesFormat = FormatMessagesToShowableMessages(newInfosForTopicPage.value.listOfMessages)
                        currentTaskForMessagesFormat?.execute()
                    }
                    newInfosForTopicPage.status == LoadableValue.STATUS_LOADING -> setListOfMessagesShowableValue(LoadableValue.loading(ArrayList()))
                    else -> setListOfMessagesShowableValue(LoadableValue.error(ArrayList()))
                }
            }
        })

        imageGetter.listenerForInvalidateTextViewNeeded = object : ImageGetterService.OnInvalidateTextViewNeededListener {
            override fun onInvalidateTextViewNeeded() {
                invalidateTextViewNeeded.value = true
            }
        }
    }

    private fun setListOfMessagesShowableValue(newValue: LoadableValue<List<MessageInfosShowable>>) {
        listOfMessagesShowable.value = newValue
        if (newValue.value.isEmpty()) {
            imageGetter.clearDrawables()
        } else {
            imageGetter.clearOnlyDownloadedDrawables()
            imageGetter.downloadDrawables()
        }
    }

    private fun cancelCurrentFormatMessagesTask() {
        currentTaskForMessagesFormat?.cancel(true)
        currentTaskForMessagesFormat = null
    }

    override fun onCleared() {
        cancelGetTopicPageInfos()
        cancelCurrentFormatMessagesTask()
        imageGetter.clearDrawables()
    }

    fun cancelGetTopicPageInfos() {
        topicPageRepo.cancelRequestForThisLiveData(infosForTopicPage)
    }

    fun clearListOfMessagesShowable() {
        setListOfMessagesShowableValue(LoadableValue.loaded(ArrayList()))
    }

    fun clearInfosForTopicPage() {
        infosForTopicPage.value = null
    }

    fun getInfosForTopicPage() : LiveData<LoadableValue<TopicPageInfos?>?> = infosForTopicPage

    fun getListOfMessagesShowable(): LiveData<LoadableValue<List<MessageInfosShowable>>?> = listOfMessagesShowable

    fun getInvalidateTextViewNeeded(): LiveData<Boolean?> = invalidateTextViewNeeded

    /* Ne récupère les informations que si aucun message n'est actuellement affiché ni en cours de chargement. */
    fun getTopicPageInfosIfNeeded(formatedTopicUrl: String) {
        val realListOfMessagesShowable: LoadableValue<List<MessageInfosShowable>>? = listOfMessagesShowable.value
        if (realListOfMessagesShowable == null || (realListOfMessagesShowable.value.isEmpty() && realListOfMessagesShowable.status != LoadableValue.STATUS_LOADING)) {
            topicPageRepo.updateTopicPageInfos(topicPageParser.setPageNumberForThisTopicUrl(formatedTopicUrl, pageNumber.value ?: 0), infosForTopicPage)
        }
    }

    /* Ne devrait pas leak, normalement. */
    @SuppressLint("StaticFieldLeak")
    private inner class FormatMessagesToShowableMessages(private var listOfBaseMessages: List<MessageInfos>) : AsyncTask<Void, Void, List<MessageInfosShowable>>() {
        override fun doInBackground(vararg voids: Void): List<MessageInfosShowable> {
            return listOfBaseMessages.map { messageInfos ->
                MessageInfosShowable(messageInfos.avatarUrl,
                                     SpannableString(messageInfos.author),
                                     SpannableString(messageInfos.date),
                                     topicPageParser.createMessageContentShowable(messageInfos, settingsForMessages))
            }
        }

        override fun onPostExecute(newListOfShowableMessages: List<MessageInfosShowable>) {
            if (!isCancelled) {
                setListOfMessagesShowableValue(LoadableValue.loaded(newListOfShowableMessages))
            }
        }
    }
}
