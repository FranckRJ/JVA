package com.franckrj.jva.forum

import androidx.lifecycle.MutableLiveData
import com.franckrj.jva.base.AbsAsyncValueSetter
import com.franckrj.jva.base.AbsRepository
import com.franckrj.jva.services.AppDatabase
import com.franckrj.jva.services.WebService
import com.franckrj.jva.utils.LoadableValue

class ForumPageRepository private constructor() : AbsRepository() {
    companion object {
        val instance: ForumPageRepository by lazy { ForumPageRepository() }
    }

    override val serviceForWeb: WebService = WebService.instance
    private val parserForForumPage: ForumPageParser = ForumPageParser.instance
    private val database: AppDatabase = AppDatabase.instance

    fun updateForumPageInfos(urlOfForumPage: String, forumPageInfosLiveData: MutableLiveData<LoadableValue<ForumPageInfos?>?>, allowFetchFromCache: Boolean) {
        val newForumGetterInstance = ForumGetter(urlOfForumPage, forumPageInfosLiveData)
        addThisRequestForThisLiveData(newForumGetterInstance, forumPageInfosLiveData)
        forumPageInfosLiveData.value = LoadableValue.loading(forumPageInfosLiveData.value?.value)
        newForumGetterInstance.execute(allowFetchFromCache)
    }

    private inner class ForumGetter(private val urlOfForumPage: String, private val forumPageInfosLiveData: MutableLiveData<LoadableValue<ForumPageInfos?>?>) :
            AbsAsyncValueSetter<ForumPageInfos>(forumPageInfosLiveData) {
        override fun getValueToSetInBackground(tryToGetCachedValue: Boolean): ForumPageInfos? {
            if (tryToGetCachedValue) {
                val forumPageFromDatabase = database.forumPageDao().findByLink(urlOfForumPage)
                if (forumPageFromDatabase != null) {
                    return forumPageFromDatabase
                }
            }

            val sourceOfWebPage: String? = serviceForWeb.getPage(urlOfForumPage, hashCode())

            return if (sourceOfWebPage == null) {
                null
            } else {
                val tmpForumPageInfos = MutableForumPageInfos()

                tmpForumPageInfos.forumLink = urlOfForumPage
                tmpForumPageInfos.forumName = parserForForumPage.getForumNameFromPageSource(sourceOfWebPage)
                tmpForumPageInfos.listOfTopics = parserForForumPage.getListOfTopicsFromPageSource(sourceOfWebPage)

                ForumPageInfos(tmpForumPageInfos)
            }
        }

        override fun setTheValueGetted(result: ForumPageInfos?, isStillActive: Boolean) {
            if (isStillActive) {
                if (result == null) {
                    forumPageInfosLiveData.value = LoadableValue.error(null)
                } else {
                    forumPageInfosLiveData.value = LoadableValue.loaded(result)
                }
                removeRequestForThisLiveData(forumPageInfosLiveData)
            }
        }

        override fun afterValueGettedInBackground(result: ForumPageInfos?, isStillActive: Boolean) {
            if (isStillActive && result != null) {
                database.forumPageDao().insertForumPages(result)
            }
        }

        override fun onCancelled() {
            forumPageInfosLiveData.value = LoadableValue.error(null)
        }
    }
}
