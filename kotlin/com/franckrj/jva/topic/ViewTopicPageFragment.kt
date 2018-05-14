package com.franckrj.jva.topic

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.PageNavigationHeaderAdapter
import com.franckrj.jva.pagenav.ViewNavigablePageFragment
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.MovableToolbar
import com.franckrj.jva.utils.SmoothScrollbarRecyclerView

class ViewTopicPageFragment : ViewNavigablePageFragment() {
    companion object {
        private const val SAVE_IS_ACTIVE: String = "SAVE_IS_ACTIVE"
    }

    private lateinit var messageListRefreshLayout: SwipeRefreshLayout
    private lateinit var messageListView: SmoothScrollbarRecyclerView
    private lateinit var messageListAdapter: TopicPageAdapter
    private lateinit var topicViewModel: TopicViewModel
    private lateinit var topicPageViewModel: TopicPageViewModel
    private var isActive: Boolean = false

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

        if (savedInstanceState?.getBoolean(SAVE_IS_ACTIVE) == true) {
            setIsActiveFragment(true)
        }

        topicPageViewModel.getListOfMessagesShowable().observe(this, Observer { listOfMessagesShowable ->
            messageListRefreshLayout.isRefreshing = (listOfMessagesShowable?.status == LoadableValue.STATUS_LOADING)
            if (listOfMessagesShowable != null && (listOfMessagesShowable.value.isNotEmpty() || messageListAdapter.listOfMessagesShowable.isNotEmpty())) {
                messageListAdapter.listOfMessagesShowable = listOfMessagesShowable.value
                messageListAdapter.notifyDataSetChanged()
            }
        })

        topicPageViewModel.getInvalidateTextViewNeeded().observe(this, Observer { newInvalidateTextViewNeeded ->
            if (newInvalidateTextViewNeeded == true) {
                /* TODO: Vérifier pour être sur que ça fonctionne vraiment correctement.
                 * Il est possible que toutes les vues créée ne soient pas invalidées (certaines sont dans un cache), mais ces dites vues
                 * ne sont normalement pas affichées et donc le onDraw n'a pas encore été appelé. */
                for (childIndex: Int in 0..(messageListView.childCount - 1)) {
                    messageListAdapter.invalidateTextViewOfThisViewHolder(messageListView.getChildViewHolder(messageListView.getChildAt(childIndex)))
                }
            }
        })

        topicPageViewModel.getCurrentPageNumber().observe(this, Observer { newCurrentPageNumber ->
            if (newCurrentPageNumber != null) {
                messageListAdapter.currentPageNumber = newCurrentPageNumber
                messageListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
            }
        })

        topicViewModel.getLastPageNumber().observe(this, Observer { newLastPageNumber ->
            if (newLastPageNumber != null) {
                messageListAdapter.lastPageNumber = newLastPageNumber
                messageListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
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

        messageListAdapter.pageNavigationButtonClickedListener = object : PageNavigationHeaderAdapter.OnPageNavigationButtonClickedListener {
            override fun onPageNavigationButtonClicked(idOfButton: Int) {
                when (idOfButton) {
                    R.id.firstpage_button_header_row -> topicViewModel.setCurrentPageNumber(1)
                    R.id.previouspage_button_header_row -> topicViewModel.setCurrentPageNumber((topicViewModel.getCurrentPageNumber().value ?: 2) - 1)
                    R.id.nextpage_button_header_row -> topicViewModel.setCurrentPageNumber((topicViewModel.getCurrentPageNumber().value ?: 1) + 1)
                    R.id.lastpage_button_header_row -> topicViewModel.setCurrentPageNumber(topicViewModel.getLastPageNumber().value ?: 1)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVE_IS_ACTIVE, isActive)
    }

    override fun setIsActiveFragment(newIsActive: Boolean) {
        isActive = newIsActive
        if (isActive) {
            topicViewModel.setNewSourceForPageInfos(topicPageViewModel.getInfosForTopicPage())
            messageListAdapter.showAllPageInfos = true
            topicPageViewModel.getTopicPageInfosIfNeeded(topicViewModel.topicUrl)
        } else {
            messageListAdapter.showAllPageInfos = false
            topicPageViewModel.clearInfosForTopicPage()
            topicPageViewModel.cancelGetTopicPageInfos()
        }
        messageListAdapter.notifyItemChanged(PageNavigationHeaderAdapter.HEADER_POSITION)
    }

    override fun clearContent() {
        topicPageViewModel.clearListOfMessagesShowable()
    }
}
