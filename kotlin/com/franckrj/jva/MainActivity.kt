package com.franckrj.jva

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.view.View
import android.widget.Toast
import android.widget.EditText
import android.widget.TextView
import com.franckrj.jva.topic.TopicAdapter
import com.franckrj.jva.topic.TopicViewModel
import com.franckrj.jva.utils.LoadableValue

class MainActivity : AppCompatActivity() {
    private lateinit var titleTextToolbar: TextView
    private lateinit var subtitleTextToolbar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val idOfNavBarHeight: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarHeight: Int = if (idOfNavBarHeight > 0) resources.getDimensionPixelSize(idOfNavBarHeight) else 0
        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val defaultMessageListPadding = resources.getDimensionPixelSize(R.dimen.messageListPadding)
        val messageCardBottomMargin = resources.getDimensionPixelSize(R.dimen.messageCardBottomMargin)
        val refreshSpinnerTopMargin = resources.getDimensionPixelSize(R.dimen.refreshSpinnerTopMargin)

        val toolbarLayout: View = findViewById(R.id.toolbar_layout_main)
        val messageListRefreshLayout: SwipeRefreshLayout = findViewById(R.id.messagelist_refresh_main)
        val messageListView: RecyclerView = findViewById(R.id.message_list_main)
        val messageListAdapter = TopicAdapter(this, resources.getDimensionPixelSize(R.dimen.avatarSize), resources.getDimensionPixelSize(R.dimen.defaultCardCornerRadius))
        val topicViewModel: TopicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)
        var navBarIsInApp = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        titleTextToolbar = findViewById(R.id.title_text_toolbar_main)
        subtitleTextToolbar = findViewById(R.id.subtitle_text_toolbar_main)

        if (Build.VERSION.SDK_INT >= 24) {
            if (isInMultiWindowMode) {
                navBarIsInApp = false
            }
        }

        messageListRefreshLayout.isEnabled = false
        messageListRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        messageListRefreshLayout.setProgressViewOffset(false, -messageListRefreshLayout.progressCircleDiameter, refreshSpinnerTopMargin + statusBarHeight)
        messageListView.setPaddingRelative(defaultMessageListPadding,
                                           defaultMessageListPadding,
                                           defaultMessageListPadding,
                                           defaultMessageListPadding - messageCardBottomMargin + if (navBarIsInApp) navBarHeight else 0)
        toolbarLayout.setPaddingRelative(0, statusBarHeight, 0, 0)

        messageListView.layoutManager = LinearLayoutManager(this)
        messageListView.adapter = messageListAdapter

        topicViewModel.getInfosForTopicLoadingStatus().observe(this, Observer { infosForTopicLoadingStatus ->
            messageListRefreshLayout.isRefreshing = (infosForTopicLoadingStatus == LoadableValue.STATUS_LOADING)
        })

        topicViewModel.getListOfMessagesShowable().observe(this, Observer { listOfMessagesShowable ->
            messageListAdapter.listOfMessagesShowable = listOfMessagesShowable ?: ArrayList()
            messageListAdapter.notifyDataSetChanged()
        })

        topicViewModel.getForumAndTopicName().observe(this, Observer { forumAndTopicName ->
            if (forumAndTopicName != null) {
                messageListAdapter.listOfHeaders = listOf(TopicAdapter.HeaderInfos(SpannableString(forumAndTopicName.forumName), SpannableString(forumAndTopicName.topicName)))
                messageListAdapter.notifyDataSetChanged()
                setTitle(forumAndTopicName.forumName)
                setSubTitle(forumAndTopicName.topicName)
            }
        })

        messageListAdapter.authorClickedListener = object : TopicAdapter.OnItemClickedListener {
            override fun onItemClicked(position: Int) {
                Toast.makeText(this@MainActivity, "Position d'auteur cliqué : " + position.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        messageListAdapter.dateClickedListener = object : TopicAdapter.OnItemClickedListener {
            override fun onItemClicked(position: Int) {
                Toast.makeText(this@MainActivity, "Position de date cliquée : " + position.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        if (savedInstanceState == null) {
            val alertDialog = AlertDialog.Builder(this)
            val editText = EditText(this)
            alertDialog.setTitle("Lien du topic")

            alertDialog.setView(editText)

            alertDialog.setPositiveButton("Valider", { _, _ ->
                if (editText.text.toString().isNotEmpty()) {
                    topicViewModel.updateAllTopicPageInfos(editText.text.toString())
                } else {
                    topicViewModel.updateAllTopicPageInfos("http://www.jeuxvideo.com/forums/42-1000005-47929326-3-0-1-0-ok-google-blabla-android.htm")
                }
            })

            alertDialog.show()
        }
    }

    private fun setTitle(newTitle: String) {
        titleTextToolbar.text = newTitle
    }

    private fun setSubTitle(newSubtitle: String) {
        subtitleTextToolbar.text = newSubtitle

        if (newSubtitle.isEmpty()) {
            subtitleTextToolbar.visibility = View.GONE
        } else {
            subtitleTextToolbar.visibility = View.VISIBLE
        }
    }
}
