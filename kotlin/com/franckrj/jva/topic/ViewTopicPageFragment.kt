package com.franckrj.jva.topic

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.franckrj.jva.R
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.MovableToolbar
import com.franckrj.jva.utils.SmoothScrollbarRecyclerView

class ViewTopicPageFragment : Fragment() {
    companion object {
        const val ARG_PAGE_NUMBER: String = "ARG_PAGE_NUMBER"
        const val ARG_IS_ACTIVE_FRAG: String = "ARG_IS_ACTIVE_FRAG"
    }

    private lateinit var messageListRefreshLayout: SwipeRefreshLayout
    private lateinit var messageListView: SmoothScrollbarRecyclerView
    private lateinit var messageListAdapter: TopicPageAdapter
    private lateinit var topicViewModel: TopicViewModel
    private lateinit var topicPageViewModel: TopicPageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView: View = inflater.inflate(R.layout.fragment_viewtopicpage, container, false)

        val idOfNavBarHeight: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarHeight: Int = if (idOfNavBarHeight > 0) resources.getDimensionPixelSize(idOfNavBarHeight) else 0
        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val toolbarHeight: Int = resources.getDimensionPixelSize(R.dimen.toolbarHeight)
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        val defaultMessageListPadding: Int = resources.getDimensionPixelSize(R.dimen.messageListPadding)
        val messageCardSpacing: Int = resources.getDimensionPixelSize(R.dimen.messageCardSpacing)
        val refreshSpinnerTopMargin: Int = resources.getDimensionPixelSize(R.dimen.refreshSpinnerTopMargin)
        val realToolbarHeight: Int = toolbarHeight + (defaultToolbarMargin * 2)

        var navBarIsInApp = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= 24) {
            if (requireActivity().isInMultiWindowMode) {
                navBarIsInApp = false
            }
        }

        messageListRefreshLayout = mainView.findViewById(R.id.messagelist_refresh_viewtopicpage)
        messageListView = mainView.findViewById(R.id.message_list_viewtopicpage)

        messageListRefreshLayout.isEnabled = false
        messageListRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        messageListRefreshLayout.setProgressViewOffset(false, statusBarHeight + defaultToolbarMargin, refreshSpinnerTopMargin + realToolbarHeight + statusBarHeight)
        messageListView.setPaddingRelative(defaultMessageListPadding,
                                           realToolbarHeight + statusBarHeight,
                                           defaultMessageListPadding,
                                           defaultMessageListPadding - messageCardSpacing + if (navBarIsInApp) navBarHeight else 0)

        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        messageListAdapter = TopicPageAdapter(requireActivity(), resources.getDimensionPixelSize(R.dimen.avatarSize), resources.getDimensionPixelSize(R.dimen.defaultCardCornerRadius))
        topicViewModel = ViewModelProviders.of(requireActivity()).get(TopicViewModel::class.java)
        topicPageViewModel = ViewModelProviders.of(this).get(TopicPageViewModel::class.java)

        val messageListLayoutManager = LinearLayoutManager(requireActivity())
        messageListView.layoutManager = messageListLayoutManager
        messageListView.adapter = messageListAdapter

        messageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentActivity: Activity = requireActivity()
                if (currentActivity is MovableToolbar) {
                    currentActivity.toolbarMoved(messageListView.isScrolledAtTop())
                }
            }
        })

        topicPageViewModel.init(arguments?.getInt(ARG_PAGE_NUMBER) ?: 1)
        if (arguments?.getBoolean(ARG_IS_ACTIVE_FRAG) == true) {
            setIsActiveFragment(true)
        }

        topicPageViewModel.getListOfMessagesShowable().observe(this, Observer { listOfMessagesShowable ->
            messageListRefreshLayout.isRefreshing = (listOfMessagesShowable?.status == LoadableValue.STATUS_LOADING)
            if (listOfMessagesShowable != null) {
                messageListAdapter.listOfMessagesShowable = listOfMessagesShowable.value
                messageListAdapter.notifyDataSetChanged()
            }
        })

        topicPageViewModel.getCurrentPageNumber().observe(this, Observer { newCurrentPageNumber ->
            if (newCurrentPageNumber != null) {
                messageListAdapter.currentPageNumber = newCurrentPageNumber
                messageListAdapter.notifyItemChanged(TopicPageAdapter.HEADER_POSITION)
            }
        })

        topicViewModel.getLastPageNumber().observe(this, Observer { newLastPageNumber ->
            if (newLastPageNumber != null) {
                messageListAdapter.lastPageNumber = newLastPageNumber
                messageListAdapter.notifyItemChanged(TopicPageAdapter.HEADER_POSITION)
            }
        })

        messageListAdapter.authorClickedListener = object : TopicPageAdapter.OnItemClickedListener {
            override fun onItemClicked(position: Int) {
                Toast.makeText(requireActivity(), "Position d'auteur cliqué : " + position.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        messageListAdapter.dateClickedListener = object : TopicPageAdapter.OnItemClickedListener {
            override fun onItemClicked(position: Int) {
                Toast.makeText(requireActivity(), "Position de date cliquée : " + position.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setIsActiveFragment(isActive: Boolean) {
        if (isActive) {
            topicViewModel.setNewSourceForPageInfos(topicPageViewModel.getInfosForTopicPage())
            topicPageViewModel.updateTopicPageInfos(topicViewModel.topicUrl)
        } else {
            //TODO: stoper la récupération des infos etc
        }
    }

    fun clearMessages() {
        messageListAdapter.listOfMessagesShowable = ArrayList()
        messageListAdapter.notifyDataSetChanged()
    }
}