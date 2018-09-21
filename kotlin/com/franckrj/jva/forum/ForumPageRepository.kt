package com.franckrj.jva.forum

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.base.AbsRepository
import com.franckrj.jva.services.WebService
import com.franckrj.jva.utils.LoadableValue

/* TODO: Stocker les topics dans une BDD pour pouvoir les récup' quand le process est kill (manque de RAM etc). */
class ForumPageRepository private constructor() : AbsRepository() {
    companion object {
        val instance: ForumPageRepository by lazy { ForumPageRepository() }
    }

    override val serviceForWeb: WebService = WebService.instance
    private val parserForForumPage: ForumPageParser = ForumPageParser.instance

    fun updateForumPageInfos(urlOfForumPage: String, forumPageInfosLiveData: MutableLiveData<LoadableValue<ForumPageInfos?>?>) {
        val newForumGetterInstance = ForumGetter(urlOfForumPage, forumPageInfosLiveData)
        addThisRequestForThisLiveData(newForumGetterInstance, forumPageInfosLiveData)
        forumPageInfosLiveData.value = LoadableValue.loading(forumPageInfosLiveData.value?.value)
        newForumGetterInstance.execute()
    }

    /* Ça ne devrait pas poser de problème normalement car
     * cette AsyncTask n'a aucune référence vers un contexte. */
    @SuppressLint("StaticFieldLeak")
    private inner class ForumGetter(private val urlOfForumPage: String, private val forumPageInfosLiveData: MutableLiveData<LoadableValue<ForumPageInfos?>?>) : AsyncTask<Void, Void, ForumPageInfos?>() {
        override fun doInBackground(vararg voids: Void): ForumPageInfos? {
            val sourceOfWebPage:String? = serviceForWeb.getPage(urlOfForumPage, hashCode())

            return if (sourceOfWebPage == null) {
                null
            } else {
                val tmpForumPageInfos = MutableForumPageInfos()

                tmpForumPageInfos.forumName = parserForForumPage.getForumNameFromPageSource(sourceOfWebPage)
                tmpForumPageInfos.listOfTopics = parserForForumPage.getListOfTopicsFromPageSource(sourceOfWebPage)

                tmpForumPageInfos
            }
        }

        override fun onPostExecute(infosForForumPage: ForumPageInfos?) {
            if (!isCancelled) {
                if (infosForForumPage == null) {
                    forumPageInfosLiveData.value = LoadableValue.error(null)
                } else {
                    forumPageInfosLiveData.value = LoadableValue.loaded(infosForForumPage)
                }
                removeRequestForThisLiveData(forumPageInfosLiveData)
            }
        }

        override fun onCancelled(result: ForumPageInfos?) {
            forumPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
