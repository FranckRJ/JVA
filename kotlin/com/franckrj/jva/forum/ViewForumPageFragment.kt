package com.franckrj.jva.forum

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.ViewNavigablePageFragment
import com.franckrj.jva.topic.ViewTopicActivity
import com.franckrj.jva.utils.LoadableValue

class ViewForumPageFragment : ViewNavigablePageFragment() {
    private lateinit var topicListAdapter: ForumPageAdapter
    private lateinit var forumPageViewModel: ForumPageViewModel
    private lateinit var forumViewModel: ForumViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView: View = inflater.inflate(R.layout.fragment_viewforumpage, container, false)

        initListViewAndRefreshLayout(mainView.findViewById(R.id.topic_list_viewforumpage), mainView.findViewById(R.id.topiclist_refresh_viewforumpage),
                                     R.dimen.topicListPadding, R.dimen.topicCardSpacing)

        return mainView
    }

    override fun createActivityDependentObjectsAndViewModels() {
        topicListAdapter = ForumPageAdapter(requireActivity())
        forumPageViewModel = ViewModelProviders.of(this).get(ForumPageViewModel::class.java)
        forumViewModel = ViewModelProviders.of(requireActivity()).get(ForumViewModel::class.java)

        contentListAdapter = topicListAdapter
        contentPageViewModel = forumPageViewModel

        topicListAdapter.showLastPageButton = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contentListView.layoutManager = LinearLayoutManager(requireActivity())
        contentListView.adapter = topicListAdapter

        NavigationUtils.initPageNavHeaderAdapterNavigation(this, topicListAdapter, forumViewModel, forumPageViewModel)

        forumPageViewModel.getListOfTopicsShowable().observe(this, Observer { listOfTopicsShowable ->
            contentListRefreshLayout.isRefreshing = (listOfTopicsShowable?.status == LoadableValue.STATUS_LOADING)
            if (listOfTopicsShowable != null && (listOfTopicsShowable.value.isNotEmpty() || topicListAdapter.listOfTopicsShowable.isNotEmpty())) {
                topicListAdapter.listOfTopicsShowable = listOfTopicsShowable.value
                topicListAdapter.notifyDataSetChanged()
            }
        })

        topicListAdapter.onItemClickedListener = { position, isLongClick ->
            if (position != null) {
                val topicInfos: TopicInfos? = forumPageViewModel.getListOfTopicsInfos()?.getOrNull(position)

                if (topicInfos != null) {
                    val viewTopicIntent = Intent(requireActivity(), ViewTopicActivity::class.java)

                    viewTopicIntent.putExtra(ViewTopicActivity.EXTRA_TOPIC_URL, topicInfos.topicUrl)
                    if (isLongClick) {
                        viewTopicIntent.putExtra(ViewTopicActivity.PAGE_TO_GO, 1 + (topicInfos.numberOfReplys / 20))
                    }

                    startActivity(viewTopicIntent)
                }
            }
        }
    }

    override fun setIsActiveFragment(newIsActive: Boolean) {
        super.setIsActiveFragment(newIsActive)

        if (isActive) {
            forumViewModel.setNewSourceForPageInfos(forumPageViewModel.getInfosForForumPage())
            forumPageViewModel.getForumPageInfosIfNeeded(forumViewModel.forumUrl)
        }
    }
}
