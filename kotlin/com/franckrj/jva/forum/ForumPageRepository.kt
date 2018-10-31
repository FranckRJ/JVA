package com.franckrj.jva.forum

import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.base.AbsAsyncValueSetter
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

    private inner class ForumGetter(private val urlOfForumPage: String, private val forumPageInfosLiveData: MutableLiveData<LoadableValue<ForumPageInfos?>?>) :
            AbsAsyncValueSetter<ForumPageInfos>(forumPageInfosLiveData) {
        override fun doInBackground(): ForumPageInfos? {
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

        override fun onPostExecute(result: ForumPageInfos?, isStillActive: Boolean) {
            if (isStillActive) {
                if (result == null) {
                    forumPageInfosLiveData.value = LoadableValue.error(null)
                } else {
                    forumPageInfosLiveData.value = LoadableValue.loaded(result)
                }
                removeRequestForThisLiveData(forumPageInfosLiveData)
            }
        }

        override fun onCancelled() {
            forumPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
