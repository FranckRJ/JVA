package com.franckrj.jva

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.franckrj.jva.topic.TopicAdapter
import com.franckrj.jva.topic.TopicViewModel
import com.franckrj.jva.utils.LoadableValue
import com.franckrj.jva.utils.SmoothScrollbarRecyclerView

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
        val toolbarHeight: Int = resources.getDimensionPixelSize(R.dimen.toolbarHeight)
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        val defaultToolbarCardElevation: Float = resources.getDimensionPixelSize(R.dimen.defaultToolbarCardElevation).toFloat()
        val aboveToolbarCardElevation: Float = resources.getDimensionPixelSize(R.dimen.aboveToolbarCardElevation).toFloat()
        val defaultMessageListPadding: Int = resources.getDimensionPixelSize(R.dimen.messageListPadding)
        val messageCardBottomMargin: Int = resources.getDimensionPixelSize(R.dimen.messageCardBottomMargin)
        val refreshSpinnerTopMargin: Int = resources.getDimensionPixelSize(R.dimen.refreshSpinnerTopMargin)

        val appbarLayout: AppBarLayout = findViewById(R.id.appbar_layout_main)
        val toolbarLayout: FrameLayout = findViewById(R.id.toolbar_layout_main)
        val toolbarCard: CardView = findViewById(R.id.toolbar_card_main)
        val messageListRefreshLayout: SwipeRefreshLayout = findViewById(R.id.messagelist_refresh_main)
        val messageListView: SmoothScrollbarRecyclerView = findViewById(R.id.message_list_main)
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

        val messageListLayoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = messageListLayoutManager
        messageListView.adapter = messageListAdapter

        toolbarLayout.setPaddingRelative(defaultToolbarMargin, defaultToolbarMargin + statusBarHeight, defaultToolbarMargin, defaultToolbarMargin)
        toolbarCard.translationZ = defaultToolbarCardElevation
        messageListRefreshLayout.isEnabled = false
        messageListRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        messageListRefreshLayout.setProgressViewOffset(false, -messageListRefreshLayout.progressCircleDiameter, refreshSpinnerTopMargin + toolbarHeight + (defaultToolbarMargin * 2) + statusBarHeight)
        messageListRefreshLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                messageListRefreshLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                messageListRefreshLayout.translationY = -(appbarLayout.height.toFloat())
                messageListRefreshLayout.layoutParams.height = messageListRefreshLayout.height + appbarLayout.height
            }
        })
        messageListView.setPaddingRelative(defaultMessageListPadding,
                                           toolbarHeight + (defaultToolbarMargin * 2) + statusBarHeight,
                                           defaultMessageListPadding,
                                           defaultMessageListPadding - messageCardBottomMargin + if (navBarIsInApp) navBarHeight else 0)
        messageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (toolbarCard.translationZ == aboveToolbarCardElevation && messageListView.isScrolledAtTop()) {
                    toolbarCard.animate().translationZ(defaultToolbarCardElevation).setDuration(200).start()
                } else if (toolbarCard.translationZ == defaultToolbarCardElevation && !messageListView.isScrolledAtTop()) {
                    toolbarCard.translationZ = aboveToolbarCardElevation
                }
            }
        })

        topicViewModel.getInfosForTopicLoadingStatus().observe(this, Observer { infosForTopicLoadingStatus ->
            messageListRefreshLayout.isRefreshing = (infosForTopicLoadingStatus == LoadableValue.STATUS_LOADING)
        })

        topicViewModel.getListOfMessagesShowable().observe(this, Observer { listOfMessagesShowable ->
            messageListAdapter.listOfMessagesShowable = listOfMessagesShowable ?: ArrayList()
            messageListAdapter.notifyDataSetChanged()
        })

        topicViewModel.getForumAndTopicName().observe(this, Observer { forumAndTopicName ->
            if (forumAndTopicName != null) {
                messageListAdapter.listOfHeaders = listOf(TopicAdapter.HeaderInfos(1, 10, 100))
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
