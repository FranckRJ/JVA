package com.franckrj.jva.topic

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.text.SpannableString
import com.franckrj.jva.services.WebService
import com.franckrj.jva.utils.LoadableValue

/* TODO: Stocker les messages dans une BDD pour pouvoir les récup' quand le process est kill (manque de RAM etc). */
class TopicRepository private constructor() {
    companion object {
        val instance: TopicRepository by lazy { TopicRepository() }
    }

    private val serviceForWeb: WebService = WebService.instance
    private val parserForTopic: TopicParser = TopicParser.instance

    fun updateAllTopicPageInfos(linkOfTopicPage: String, topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>, settingsForMessages: TopicParser.MessageSettings) {
        topicPageInfosLiveData.value = LoadableValue.loading(topicPageInfosLiveData.value?.value)
        TopicGetter(linkOfTopicPage, topicPageInfosLiveData, settingsForMessages).execute()
    }

    /* Ça ne devrait pas poser de problème normalement car
     * cette AsyncTask n'a aucune référence vers un contexte. */
    @SuppressLint("StaticFieldLeak")
    private inner class TopicGetter(private val linkOfTopicPage: String,
                                    private val topicPageInfosLiveData: MutableLiveData<LoadableValue<TopicPageInfos?>?>,
                                    private val settingsForMessages: TopicParser.MessageSettings) : AsyncTask<Void, Void, TopicPageInfos?>() {
        override fun doInBackground(vararg voids: Void): TopicPageInfos? {
            val sourceOfWebPage:String? = serviceForWeb.getPage(linkOfTopicPage)

            return if (sourceOfWebPage == null) {
                null
            } else {
                val tmpTopicPageInfos = MutableTopicPageInfos()

                tmpTopicPageInfos.namesForForumAndTopic = parserForTopic.getForumAndTopicNameFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.lastPageNumber = parserForTopic.getLastPageNumberFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.listOfMessages = parserForTopic.getListOfMessagesFromPageSource(sourceOfWebPage)
                tmpTopicPageInfos.listOfMessagesShowable = tmpTopicPageInfos.listOfMessages.map { messageInfos ->
                    MessageInfosShowable(messageInfos.avatarLink,
                                         SpannableString(messageInfos.author),
                                         SpannableString(messageInfos.date),
                                         parserForTopic.createMessageContentShowable(messageInfos, settingsForMessages))
                }

                tmpTopicPageInfos
            }
        }

        override fun onPostExecute(infosForTopicPage: TopicPageInfos?) {
            if (infosForTopicPage == null) {
                topicPageInfosLiveData.value = LoadableValue.error(null)
            } else {
                topicPageInfosLiveData.value = LoadableValue.loaded(infosForTopicPage)
            }
        }
    }
}
