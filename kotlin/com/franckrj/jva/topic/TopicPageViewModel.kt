package com.franckrj.jva.topic

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.text.SpannableString
import com.franckrj.jva.R
import com.franckrj.jva.services.ImageGetterService
import com.franckrj.jva.services.TagHandlerService
import com.franckrj.jva.utils.BetterQuoteSpan
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.UndeprecatorUtils

class TopicPageViewModel(app: Application) : AndroidViewModel(app) {
    private val topicPageRepo: TopicPageRepository = TopicPageRepository.instance
    private val topicPageParser: TopicPageParser = TopicPageParser.instance
    private val imageGetter: ImageGetterService = ImageGetterService.instance
    private val tagHandler: TagHandlerService = TagHandlerService.instance
    private val settingsForMessages: TopicPageParser.MessageSettings
    private var currentTaskForMessagesFormat: FormatMessagesToShowableMessages? = null

    private val infosForTopicPage: MutableLiveData<LoadableValue<TopicPageInfos?>?> = MutableLiveData()
    private val currentPageNumber: MutableLiveData<Int> = MutableLiveData()
    private val listOfMessagesShowable: MediatorLiveData<LoadableValue<List<MessageInfosShowable>>> = MediatorLiveData()

    init {
        val settingsForBetterQuotes = BetterQuoteSpan.BetterQuoteSettings(UndeprecatorUtils.getColor(app, R.color.quoteBackgroundColor),
                                                                          UndeprecatorUtils.getColor(app, R.color.colorAccent),
                                                                          app.resources.getDimensionPixelSize(R.dimen.quoteStripeSize),
                                                                          app.resources.getDimensionPixelSize(R.dimen.quoteGapSize))

        settingsForMessages = TopicPageParser.MessageSettings(settingsForBetterQuotes, imageGetter, tagHandler, 2)

        listOfMessagesShowable.addSource(infosForTopicPage, { newInfosForTopicPage ->
            /* Effacement de la liste des messages lors d'une erreur ou d'un début de chargement.
             * Comportement voulu ? */
            when {
                newInfosForTopicPage?.value != null && newInfosForTopicPage.status == LoadableValue.STATUS_LOADED -> {
                    if (currentTaskForMessagesFormat != null) {
                        cancelCurrentFormatMessagesTask()
                    }
                    currentTaskForMessagesFormat = FormatMessagesToShowableMessages(newInfosForTopicPage.value.listOfMessages,
                            topicPageParser,
                            settingsForMessages,
                            listOfMessagesShowable)
                    currentTaskForMessagesFormat?.execute()
                }
                newInfosForTopicPage?.status == LoadableValue.STATUS_LOADING -> listOfMessagesShowable.value = LoadableValue.loading(ArrayList())
                else -> listOfMessagesShowable.value = LoadableValue.error(ArrayList())
            }
        })
    }

    private fun cancelCurrentFormatMessagesTask() {
        currentTaskForMessagesFormat?.cancel(true)
        currentTaskForMessagesFormat = null
    }

    override fun onCleared() {
        cancelCurrentFormatMessagesTask()
    }

    fun init(pageNumberUsed: Int) {
        currentPageNumber.value = pageNumberUsed
    }

    fun getInfosForTopicPage() : LiveData<LoadableValue<TopicPageInfos?>?> = infosForTopicPage

    fun getCurrentPageNumber(): LiveData<Int> = currentPageNumber

    fun getListOfMessagesShowable(): LiveData<LoadableValue<List<MessageInfosShowable>>> = listOfMessagesShowable

    fun updateTopicPageInfos(formatedTopicUrl: String) {
        topicPageRepo.updateTopicPageInfos(topicPageParser.setPageNumberForThisTopicUrl(formatedTopicUrl, currentPageNumber.value ?: 0), infosForTopicPage)
    }

    private class FormatMessagesToShowableMessages(private var listOfBaseMessages: List<MessageInfos>,
                                                   private var parserForTopic: TopicPageParser,
                                                   private var settingsForMessages: TopicPageParser.MessageSettings,
                                                   private var listOfShowableMessagesToUpdate: MutableLiveData<LoadableValue<List<MessageInfosShowable>>>) : AsyncTask<Void, Void, List<MessageInfosShowable>>() {
        override fun doInBackground(vararg voids: Void): List<MessageInfosShowable> {
            return listOfBaseMessages.map { messageInfos ->
                MessageInfosShowable(messageInfos.avatarUrl,
                        SpannableString(messageInfos.author),
                        SpannableString(messageInfos.date),
                        parserForTopic.createMessageContentShowable(messageInfos, settingsForMessages))
            }
        }

        override fun onPostExecute(newListOfShowableMessages: List<MessageInfosShowable>) {
            if (!isCancelled) {
                listOfShowableMessagesToUpdate.value = LoadableValue.loaded(newListOfShowableMessages)
            }
        }
    }
}