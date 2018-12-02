package com.franckrj.jva.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.franckrj.jva.R
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.ViewNavigablePageFragment
import com.franckrj.jva.utils.LoadableValue

class ViewTopicPageFragment : ViewNavigablePageFragment() {
    private lateinit var messageListAdapter: TopicPageAdapter
    private lateinit var topicPageViewModel: TopicPageViewModel
    private lateinit var topicViewModel: TopicViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView: View = inflater.inflate(R.layout.fragment_viewtopicpage, container, false)

        initListViewAndRefreshLayout(mainView.findViewById(R.id.message_list_viewtopicpage), mainView.findViewById(R.id.messagelist_refresh_viewtopicpage),
                                     R.dimen.messageListPadding, R.dimen.messageCardSpacing)

        return mainView
    }

    override fun createActivityDependentObjectsAndViewModels() {
        messageListAdapter = TopicPageAdapter(requireActivity(), resources.getDimensionPixelSize(R.dimen.avatarSize), resources.getDimensionPixelSize(R.dimen.defaultCardCornerRadius))
        topicPageViewModel = ViewModelProviders.of(this).get(TopicPageViewModel::class.java)
        topicViewModel = ViewModelProviders.of(requireActivity()).get(TopicViewModel::class.java)

        contentListAdapter = messageListAdapter
        contentPageViewModel = topicPageViewModel
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contentListView.layoutManager = LinearLayoutManager(requireActivity())
        contentListView.adapter = messageListAdapter

        NavigationUtils.initPageNavHeaderAdapterNavigation(this, messageListAdapter, topicViewModel, topicPageViewModel)

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
    }

    override fun setIsActiveFragment(newIsActive: Boolean, fromProcessRecreation: Boolean) {
        super.setIsActiveFragment(newIsActive, fromProcessRecreation)

        if (isActive) {
            topicViewModel.setNewSourceForPageInfos(topicPageViewModel.getInfosForTopicPage())
            topicPageViewModel.getTopicPageInfosIfNeeded(topicViewModel.topicUrl)
        }
    }
}
