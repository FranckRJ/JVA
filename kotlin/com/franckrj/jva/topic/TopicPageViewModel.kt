package com.franckrj.jva.topic

import android.app.Application
import android.text.SpannableString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigablePageViewModel
import com.franckrj.jva.services.ImageGetterService
import com.franckrj.jva.services.TagHandlerService
import com.franckrj.jva.utils.BetterQuoteSpan
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.UndeprecatorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicPageViewModel(app: Application) : NavigablePageViewModel(app) {
    private val topicPageRepo: TopicPageRepository = TopicPageRepository.instance
    private val topicPageParser: TopicPageParser = TopicPageParser.instance
    private val imageGetter: ImageGetterService = ImageGetterService(app.applicationContext, R.drawable.ic_image_download, R.drawable.ic_image_deleted)
    private val tagHandler: TagHandlerService = TagHandlerService.instance
    private val settingsForMessages: TopicPageParser.MessageSettings
    private var currentTaskForMessagesFormat: Job? = null

    private val infosForTopicPage: MutableLiveData<LoadableValue<TopicPageInfos?>?> = MutableLiveData()
    private val listOfMessagesShowable: MediatorLiveData<LoadableValue<List<MessageInfosShowable>>> = MediatorLiveData()
    private val invalidateTextViewNeeded: MutableLiveData<Boolean> = MutableLiveData()

    init {
        val settingsForBetterQuotes = BetterQuoteSpan.BetterQuoteSettings(UndeprecatorUtils.getColor(app, R.color.quoteBackgroundColor),
                                                                          UndeprecatorUtils.getColor(app, R.color.colorAccent),
                                                                          app.resources.getDimensionPixelSize(R.dimen.quoteStripeSize),
                                                                          app.resources.getDimensionPixelSize(R.dimen.quoteGapSize))

        settingsForMessages = TopicPageParser.MessageSettings(settingsForBetterQuotes, imageGetter, tagHandler, 2)

        listOfMessagesShowable.addSource(infosForTopicPage) { newInfosForTopicPage ->
            /* Effacement de la liste des messages lors d'une erreur ou d'un début de chargement.
             * Comportement voulu ? */
            if (newInfosForTopicPage != null) {
                when {
                    newInfosForTopicPage.value != null && newInfosForTopicPage.status == LoadableValue.STATUS_LOADED -> {
                        if (currentTaskForMessagesFormat != null) {
                            cancelCurrentFormatMessagesTask()
                        }
                        currentTaskForMessagesFormat = formatMessagesToShowableMessages(newInfosForTopicPage.value.listOfMessages)
                    }
                    newInfosForTopicPage.status == LoadableValue.STATUS_LOADING -> setListOfMessagesShowableValue(LoadableValue.loading(ArrayList()))
                    else -> setListOfMessagesShowableValue(LoadableValue.error(ArrayList()))
                }
            }
        }

        imageGetter.invalidateTextViewNeededListener = {
            invalidateTextViewNeeded.value = true
        }
    }

    private fun formatMessagesToShowableMessages(listOfBaseMessages: List<MessageInfos>): Job = GlobalScope.launch {
        val newListOfShowableMessages: List<MessageInfosShowable> = listOfBaseMessages.map { messageInfos ->
            MessageInfosShowable(messageInfos.avatarUrl,
                    SpannableString(messageInfos.author),
                    SpannableString(messageInfos.date),
                    topicPageParser.createMessageContentShowable(messageInfos, settingsForMessages))
        }
        withContext(Dispatchers.Main) {
            if (isActive) {
                setListOfMessagesShowableValue(LoadableValue.loaded(newListOfShowableMessages))
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
        currentTaskForMessagesFormat?.cancel()
        currentTaskForMessagesFormat = null
    }

    override fun onCleared() {
        cancelGetContentPageInfos()
        cancelCurrentFormatMessagesTask()
        imageGetter.clearDrawables()
    }

    override fun cancelGetContentPageInfos() {
        topicPageRepo.cancelRequestForThisLiveData(infosForTopicPage)
    }

    override fun clearListOfContentShowable() {
        setListOfMessagesShowableValue(LoadableValue.loaded(ArrayList()))
    }

    override fun clearInfosForContentPage() {
        infosForTopicPage.value = null
    }

    fun getInfosForTopicPage() : LiveData<LoadableValue<TopicPageInfos?>?> = infosForTopicPage

    fun getListOfMessagesShowable(): LiveData<LoadableValue<List<MessageInfosShowable>>?> = listOfMessagesShowable

    fun getInvalidateTextViewNeeded(): LiveData<Boolean?> = invalidateTextViewNeeded

    /* Ne récupère les informations que si aucun message n'est actuellement affiché ni en cours de chargement. */
    fun getTopicPageInfosIfNeeded(formatedTopicUrl: String, allowFetchFromCache: Boolean) {
        val realListOfMessagesShowable: LoadableValue<List<MessageInfosShowable>>? = listOfMessagesShowable.value
        if (realListOfMessagesShowable == null || (realListOfMessagesShowable.value.isEmpty() && realListOfMessagesShowable.status != LoadableValue.STATUS_LOADING)) {
            topicPageRepo.updateTopicPageInfos(topicPageParser.setPageNumberForThisTopicUrl(formatedTopicUrl, pageNumber.value ?: 0), infosForTopicPage, allowFetchFromCache)
        }
    }
}
