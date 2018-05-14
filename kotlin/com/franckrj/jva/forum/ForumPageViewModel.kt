package com.franckrj.jva.forum

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.text.SpannableString
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigablePageViewModel
import com.franckrj.jva.utils.LoadableValue

class ForumPageViewModel(app: Application) : NavigablePageViewModel(app) {
    private var currentTaskForTopicsFormat: FormatTopicsToShowableTopics? = null

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
        cancelGetForumPageInfos()
        cancelCurrentFormatTopicsTask()
    }

    fun cancelGetForumPageInfos() {
        //TODO
    }

    fun clearListOfTopicsShowable() {
        listOfTopicsShowable.value = LoadableValue.loaded(ArrayList())
    }

    fun clearInfosForForumPage() {
        infosForForumPage.value = null
    }

    fun getInfosForForumPage() : LiveData<LoadableValue<ForumPageInfos?>?> = infosForForumPage

    fun getListOfTopicsShowable(): LiveData<LoadableValue<List<TopicInfosShowable>>?> = listOfTopicsShowable

    fun getListOfTopicsInfos(): List<TopicInfos>? = infosForForumPage.value?.value?.listOfTopics

    /* Ne récupère les informations que si aucun topic n'est actuellement affiché ni en cours de chargement. */
    fun getForumPageInfosIfNeeded(formatedTopicUrl: String) {
        val realListOfTopicsShowable: LoadableValue<List<TopicInfosShowable>>? = listOfTopicsShowable.value
        if (realListOfTopicsShowable == null || (realListOfTopicsShowable.value.isEmpty() && realListOfTopicsShowable.status != LoadableValue.STATUS_LOADING)) {
            infosForForumPage.value = LoadableValue.loaded(ForumPageInfos("test(android)", listOf(
                    TopicInfos("mdr mdr", "salutbonjour", "un jour précédent", 32, TopicType.SINGLE_PAGE, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("qsdfsqdfsqdf", "salutbonjour", "un jour précédent", 32, TopicType.MULTIPLE_PAGE, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("salut bonjour", "salutbonjour", "un jour précédent", 32, TopicType.PINNED_LOCKED, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("ok zut déjà pris lol", "salutbonjour", "un jour précédent", 32, TopicType.PINNED_OPENED, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("arf arfarf", "salutbonjour", "un jour précédent", 32, TopicType.DELETED, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("okhuui", "salutbonjour", "un jour précédent", 32, TopicType.LOCKED, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("rororo", "salutbonjour", "un jour précédent", 32, TopicType.SOLVED, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("poulet parmesanbt", "salutbonjour", "un jour précédent", 32, TopicType.SINGLE_PAGE, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("mdr mdr", "salutbonjour", "un jour précédent", 32, TopicType.MULTIPLE_PAGE, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("aoins", "salutbonjour", "un jour précédent", 32, TopicType.PINNED_LOCKED, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("ainsi font font font les petites marionettes heu ainsi font font font etc etc", "salutbonjour", "un jour précédent", 32, TopicType.PINNED_OPENED, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("encore du texte", "salutbonjour", "un jour précédent", 32, TopicType.DELETED, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("toujours du texte", "salutbonjour", "un jour précédent", 32, TopicType.LOCKED, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos(formatedTopicUrl, "salutbonjour", "un jour précédent", 32, TopicType.SOLVED, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm"),
                    TopicInfos("finallement du texte", "salutbonjour", "un jour précédent", 32, TopicType.SINGLE_PAGE, "http://www.jeuxvideo.com/forums/42-1000005-47929326-1-0-1-0-ok-google-blabla-android.htm"),
                    TopicInfos("à la fin\"><>&=ok", "salutbonjour", "un jour précédent", 32, TopicType.MULTIPLE_PAGE, "http://www.jeuxvideo.com/forums/42-1000005-54624544-1-0-1-0-pour-la-moderation-c-est-ici.htm")
            )))
        }
    }

    /* Ne devrait pas leak, normalement. */
    @SuppressLint("StaticFieldLeak")
    private inner class FormatTopicsToShowableTopics(private var listOfBaseTopics: List<TopicInfos>) : AsyncTask<Void, Void, List<TopicInfosShowable>>() {
        override fun doInBackground(vararg voids: Void): List<TopicInfosShowable> {
            return listOfBaseTopics.map { topicInfos ->
                TopicInfosShowable(SpannableString(topicInfos.title + " (" + topicInfos.numberOfReplys.toString() + ")"),
                                   SpannableString(topicInfos.author),
                                   SpannableString(topicInfos.dateOfLastReply),
                                   getApplication<Application>().getDrawable(R.drawable.smiley_1))
            }
        }

        override fun onPostExecute(newListOfShowableTopics: List<TopicInfosShowable>) {
            if (!isCancelled) {
                listOfTopicsShowable.value = LoadableValue.loaded(newListOfShowableTopics)
            }
        }
    }
}
