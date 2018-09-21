package com.franckrj.jva.forum

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigablePageViewModel
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.UndeprecatorUtils
import com.franckrj.jva.utils.Utils

class ForumPageViewModel(app: Application) : NavigablePageViewModel(app) {
    private val forumPageRepo: ForumPageRepository = ForumPageRepository.instance
    private val forumPageParser: ForumPageParser = ForumPageParser.instance
    private val settingsForTopics = ForumPageParser.TopicSettings(Utils.colorToString(UndeprecatorUtils.getColor(app, R.color.colorAccent)))
    private var currentTaskForTopicsFormat: FormatTopicsToShowableTopics? = null
    private val listOfTopicIconForType: List<Drawable> = listOf(app.getDrawable(R.drawable.icon_topic_dossier1),
                                                                app.getDrawable(R.drawable.icon_topic_dossier2),
                                                                app.getDrawable(R.drawable.icon_topic_lock_light),
                                                                app.getDrawable(R.drawable.icon_topic_marque_on),
                                                                app.getDrawable(R.drawable.icon_topic_marque_off),
                                                                app.getDrawable(R.drawable.icon_topic_ghost),
                                                                app.getDrawable(R.drawable.icon_topic_resolu))

    private val infosForForumPage: MutableLiveData<LoadableValue<ForumPageInfos?>?> = MutableLiveData()
    private val listOfTopicsShowable: MediatorLiveData<LoadableValue<List<TopicInfosShowable>>> = MediatorLiveData()

    init {
        listOfTopicsShowable.addSource(infosForForumPage, { newInfosForForumPage ->
            /* Effacement de la liste des topics lors d'une erreur ou d'un début de chargement.
             * Comportement voulu ? */
            if (newInfosForForumPage != null) {
                when {
                    newInfosForForumPage.value != null && newInfosForForumPage.status == LoadableValue.STATUS_LOADED -> {
                        if (currentTaskForTopicsFormat != null) {
                            cancelCurrentFormatTopicsTask()
                        }
                        currentTaskForTopicsFormat = FormatTopicsToShowableTopics(newInfosForForumPage.value.listOfTopics)
                        currentTaskForTopicsFormat?.execute()
                    }
                    newInfosForForumPage.status == LoadableValue.STATUS_LOADING -> listOfTopicsShowable.value = LoadableValue.loading(ArrayList())
                    else -> listOfTopicsShowable.value = LoadableValue.error(ArrayList())
                }
            }
        })
    }

    private fun cancelCurrentFormatTopicsTask() {
        currentTaskForTopicsFormat?.cancel(true)
        currentTaskForTopicsFormat = null
    }

    override fun onCleared() {
        cancelGetContentPageInfos()
        cancelCurrentFormatTopicsTask()
    }

    override fun cancelGetContentPageInfos() {
        forumPageRepo.cancelRequestForThisLiveData(infosForForumPage)
    }

    override fun clearListOfContentShowable() {
        listOfTopicsShowable.value = LoadableValue.loaded(ArrayList())
    }

    override fun clearInfosForContentPage() {
        infosForForumPage.value = null
    }

    fun getInfosForForumPage() : LiveData<LoadableValue<ForumPageInfos?>?> = infosForForumPage

    fun getListOfTopicsShowable(): LiveData<LoadableValue<List<TopicInfosShowable>>?> = listOfTopicsShowable

    fun getListOfTopicsInfos(): List<TopicInfos>? = infosForForumPage.value?.value?.listOfTopics

    /* Ne récupère les informations que si aucun topic n'est actuellement affiché ni en cours de chargement. */
    fun getForumPageInfosIfNeeded(formatedForumUrl: String) {
        val realListOfTopicsShowable: LoadableValue<List<TopicInfosShowable>>? = listOfTopicsShowable.value
        if (realListOfTopicsShowable == null || (realListOfTopicsShowable.value.isEmpty() && realListOfTopicsShowable.status != LoadableValue.STATUS_LOADING)) {
            forumPageRepo.updateForumPageInfos(forumPageParser.setPageNumberForThisForumUrl(formatedForumUrl, pageNumber.value ?: 0), infosForForumPage)
        }
    }

    /* Ne devrait pas leak, normalement. */
    @SuppressLint("StaticFieldLeak")
    private inner class FormatTopicsToShowableTopics(private var listOfBaseTopics: List<TopicInfos>) : AsyncTask<Void, Void, List<TopicInfosShowable>>() {
        override fun doInBackground(vararg voids: Void): List<TopicInfosShowable> {
            return listOfBaseTopics.map { topicInfos ->
                TopicInfosShowable(forumPageParser.createTopicTitleShowable(topicInfos, settingsForTopics),
                                   forumPageParser.createTopicAuthorShowable(topicInfos),
                                   forumPageParser.createTopicDateOfLastReplyShowable(topicInfos),
                                   listOfTopicIconForType[topicInfos.typeOfTopic.index])
            }
        }

        override fun onPostExecute(newListOfShowableTopics: List<TopicInfosShowable>) {
            if (!isCancelled) {
                listOfTopicsShowable.value = LoadableValue.loaded(newListOfShowableTopics)
            }
        }
    }
}
