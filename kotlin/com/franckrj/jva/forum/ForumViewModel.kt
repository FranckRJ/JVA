package com.franckrj.jva.forum

import android.app.Application
import android.view.ViewTreeObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.franckrj.jva.pagenav.NavigableViewModel
import com.franckrj.jva.utils.LoadableValue
import com.google.android.material.appbar.AppBarLayout

class ForumViewModel(app: Application) : NavigableViewModel(app) {
    private val forumPageParser: ForumPageParser = ForumPageParser.instance

    private var infosForForumPage: LiveData<LoadableValue<ForumPageInfos?>?>? = null
    private val forumName: MediatorLiveData<String?> = MediatorLiveData()
    private val frameScrollOffset: MutableLiveData<Int?> = MutableLiveData()
    var frameOutsideScreenHeight: Int = 0
        private set
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

    fun initCollapsibleToolbarInfos(appBarLayout: AppBarLayout, viewPager: ViewPager) {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            frameScrollOffset.value = offset
        })
        appBarLayout.viewTreeObserver.addOnGlobalLayoutListener ( object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                frameOutsideScreenHeight = appBarLayout.height
                viewPager.layoutParams.height = viewPager.height + frameOutsideScreenHeight
                viewPager.requestLayout()
            }
        })
    }

    fun setUrlForForum(newForumUrl: String) {
        forumUrl = forumPageParser.formatThisUrlToClassicJvcUrl(newForumUrl)
        currentPageNumber.value = forumPageParser.getPageNumberOfThisForumUrl(forumUrl).coerceIn(1, lastPageNumber.value)
    }

    fun setNewSourceForPageInfos(newInfosForForumPage: LiveData<LoadableValue<ForumPageInfos?>?>) {
        removeCurrentSourceForPageInfos()
        infosForForumPage = newInfosForForumPage

        forumName.addSource(newInfosForForumPage) { lastInfosForForumPage ->
            if (lastInfosForForumPage?.value != null && lastInfosForForumPage.status == LoadableValue.STATUS_LOADED &&
                    forumName.value != lastInfosForForumPage.value.forumName) {
                forumName.value = lastInfosForForumPage.value.forumName
            }
        }
    }

    override fun onCleared() {
        removeCurrentSourceForPageInfos()
    }

    fun getForumName(): LiveData<String?> = forumName

    fun getFrameScrollOffset() : LiveData<Int?> = frameScrollOffset
}
