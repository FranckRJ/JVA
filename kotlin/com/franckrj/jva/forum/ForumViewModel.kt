package com.franckrj.jva.forum

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.franckrj.jva.pagenav.NavigableViewModel
import com.franckrj.jva.utils.LoadableValue

class ForumViewModel(app: Application) : NavigableViewModel(app) {
    private val forumPageParser: ForumPageParser = ForumPageParser.instance

    private var infosForForumPage: LiveData<LoadableValue<ForumPageInfos?>?>? = null
    private val forumName: MediatorLiveData<String> = MediatorLiveData()
    var forumUrl: String = ""
        private set

    init {
        lastPageNumber.value = 100
    }

    private fun removeCurrentSourceForPageInfos() {
        val currentInfosForForumPage: LiveData<LoadableValue<ForumPageInfos?>?>? = infosForForumPage
        if (currentInfosForForumPage != null) {
            forumName.removeSource(currentInfosForForumPage)
            infosForForumPage = null
        }
    }

    fun setUrlForForum(newForumUrl: String) {
        forumUrl = forumPageParser.formatThisUrlToClassicJvcUrl(newForumUrl)
        currentPageNumber.value = forumPageParser.getPageNumberOfThisForumUrl(forumUrl).coerceIn(1, lastPageNumber.value)
    }

    fun setNewSourceForPageInfos(newInfosForForumPage: LiveData<LoadableValue<ForumPageInfos?>?>) {
        removeCurrentSourceForPageInfos()
        infosForForumPage = newInfosForForumPage

        forumName.addSource(newInfosForForumPage, { lastInfosForForumPage ->
            if (lastInfosForForumPage?.value != null && lastInfosForForumPage.status == LoadableValue.STATUS_LOADED &&
                    forumName.value != lastInfosForForumPage.value.forumName) {
                forumName.value = lastInfosForForumPage.value.forumName
            }
        })
    }

    override fun onCleared() {
        removeCurrentSourceForPageInfos()
    }

    fun getForumName(): LiveData<String?> = forumName
}
