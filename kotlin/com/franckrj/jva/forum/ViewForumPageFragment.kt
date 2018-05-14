package com.franckrj.jva.forum

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.PageNavigationHeaderAdapter
import com.franckrj.jva.pagenav.ViewNavigablePageFragment
import com.franckrj.jva.topic.ViewTopicActivity
import com.franckrj.jva.utils.LoadableValue

class ViewForumPageFragment : ViewNavigablePageFragment() {
    private lateinit var topicListAdapter: ForumPageAdapter
    private lateinit var forumViewModel: ForumViewModel
    private lateinit var forumPageViewModel: ForumPageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView: View = inflater.inflate(R.layout.fragment_viewforumpage, container, false)

        initListViewAndRefreshLayout(mainView.findViewById(R.id.topic_list_viewforumpage), mainView.findViewById(R.id.topiclist_refresh_viewforumpage),
                                     R.dimen.topicListPadding, R.dimen.topicCardSpacing)

        return mainView
    }

    override fun createActivityDependentObjectsAndViewModels() {
        topicListAdapter = ForumPageAdapter(requireActivity())
        forumViewModel = ViewModelProviders.of(requireActivity()).get(ForumViewModel::class.java)
        forumPageViewModel = ViewModelProviders.of(this).get(ForumPageViewModel::class.java)
        contentPageViewModel = forumPageViewModel

        topicListAdapter.showLastPageButton = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val messageListLayoutManager = LinearLayoutManager(requireActivity())
        contentListView.layoutManager = messageListLayoutManager
        contentListView.adapter = topicListAdapter

        forumPageViewModel.getListOfTopicsShowable().observe(this, Observer { listOfTopicsShowable ->
            contentListRefreshLayout.isRefreshing = (listOfTopicsShowable?.status == LoadableValue.STATUS_LOADING)
            if (listOfTopicsShowable != null && (listOfTopicsShowable.value.isNotEmpty() || topicListAdapter.listOfTopicsShowable.isNotEmpty())) {
                topicListAdapter.listOfTopicsShowable = listOfTopicsShowable.value
                topicListAdapter.notifyDataSetChanged()
            }
        })

        forumPageViewModel.getCurrentPageNumber().observe(this, Observer { newCurrentPageNumber ->
            if (newCurrentPageNumber != null) {
                topicListAdapter.currentPageNumber = newCurrentPageNumber
                topicListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
            }
        })

        forumViewModel.getLastPageNumber().observe(this, Observer { newLastPageNumber ->
            if (newLastPageNumber != null) {
                topicListAdapter.lastPageNumber = newLastPageNumber
                topicListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
            }
        })

        topicListAdapter.onItemClickedListener = { position ->
            if (position != null) {
                val topicInfos: TopicInfos? = forumPageViewModel.getListOfTopicsInfos()?.getOrNull(position)

                if (topicInfos != null) {
                    val viewTopicIntent = Intent(requireActivity(), ViewTopicActivity::class.java)

                    viewTopicIntent.putExtra(ViewTopicActivity.EXTRA_TOPIC_URL, topicInfos.topicUrl)

                    startActivity(viewTopicIntent)
                }
            }
        }

        topicListAdapter.pageNavigationButtonClickedListener = { idOfButton ->
            when (idOfButton) {
                R.id.firstpage_button_header_row -> forumViewModel.setCurrentPageNumber(1)
                R.id.previouspage_button_header_row -> forumViewModel.setCurrentPageNumber((forumViewModel.getCurrentPageNumber().value ?: 2) - 1)
                R.id.nextpage_button_header_row -> forumViewModel.setCurrentPageNumber((forumViewModel.getCurrentPageNumber().value ?: 1) + 1)
                R.id.lastpage_button_header_row -> forumViewModel.setCurrentPageNumber(forumViewModel.getLastPageNumber().value ?: 1)
            }
        }
    }

    override fun setIsActiveFragment(newIsActive: Boolean) {
        super.setIsActiveFragment(newIsActive)

        if (isActive) {
            forumViewModel.setNewSourceForPageInfos(forumPageViewModel.getInfosForForumPage())
            topicListAdapter.showAllPageInfos = true
            forumPageViewModel.getForumPageInfosIfNeeded(forumViewModel.forumUrl)
        } else {
            topicListAdapter.showAllPageInfos = false
            forumPageViewModel.clearInfosForForumPage()
            forumPageViewModel.cancelGetForumPageInfos()
        }

        topicListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
    }

    override fun clearContent() {
        forumPageViewModel.clearListOfTopicsShowable()
    }
}
