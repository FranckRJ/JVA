package com.franckrj.jva.topic

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.PageNavigationHeaderAdapter
import com.franckrj.jva.pagenav.ViewNavigablePageFragment
import com.franckrj.jva.utils.LoadableValue

class ViewTopicPageFragment : ViewNavigablePageFragment() {
    private lateinit var messageListAdapter: TopicPageAdapter
    private lateinit var topicViewModel: TopicViewModel
    private lateinit var topicPageViewModel: TopicPageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView: View = inflater.inflate(R.layout.fragment_viewtopicpage, container, false)

        initListViewAndRefreshLayout(mainView.findViewById(R.id.message_list_viewtopicpage), mainView.findViewById(R.id.messagelist_refresh_viewtopicpage),
                                     R.dimen.messageListPadding, R.dimen.messageCardSpacing)

        return mainView
    }

    override fun createActivityDependentObjectsAndViewModels() {
        messageListAdapter = TopicPageAdapter(requireActivity(), resources.getDimensionPixelSize(R.dimen.avatarSize), resources.getDimensionPixelSize(R.dimen.defaultCardCornerRadius))
        topicViewModel = ViewModelProviders.of(requireActivity()).get(TopicViewModel::class.java)
        topicPageViewModel = ViewModelProviders.of(this).get(TopicPageViewModel::class.java)
        contentPageViewModel = topicPageViewModel
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val messageListLayoutManager = LinearLayoutManager(requireActivity())
        contentListView.layoutManager = messageListLayoutManager
        contentListView.adapter = messageListAdapter

        topicPageViewModel.getListOfMessagesShowable().observe(this, Observer { listOfMessagesShowable ->
            contentListRefreshLayout.isRefreshing = (listOfMessagesShowable?.status == LoadableValue.STATUS_LOADING)
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
                for (childIndex: Int in 0 until contentListView.childCount) {
                    messageListAdapter.invalidateTextViewOfThisViewHolder(contentListView.getChildViewHolder(contentListView.getChildAt(childIndex)))
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

        messageListAdapter.authorClickedListener = { position ->
            if (position != null) {
                Toast.makeText(requireActivity(), "Position d'auteur cliqué : " + position.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        messageListAdapter.dateClickedListener = { position ->
            if (position != null) {
                Toast.makeText(requireActivity(), "Position de date cliquée : " + position.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        messageListAdapter.pageNavigationButtonClickedListener = { idOfButton ->
            when (idOfButton) {
                R.id.firstpage_button_header_row -> topicViewModel.setCurrentPageNumber(1)
                R.id.previouspage_button_header_row -> topicViewModel.setCurrentPageNumber((topicViewModel.getCurrentPageNumber().value ?: 2) - 1)
                R.id.nextpage_button_header_row -> topicViewModel.setCurrentPageNumber((topicViewModel.getCurrentPageNumber().value ?: 1) + 1)
                R.id.lastpage_button_header_row -> topicViewModel.setCurrentPageNumber(topicViewModel.getLastPageNumber().value ?: 1)
            }
        }
    }

    override fun setIsActiveFragment(newIsActive: Boolean) {
        super.setIsActiveFragment(newIsActive)

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
