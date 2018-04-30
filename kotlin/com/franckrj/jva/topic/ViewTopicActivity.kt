package com.franckrj.jva.topic

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.franckrj.jva.R
import com.franckrj.jva.utils.MovableToolbar

class ViewTopicActivity : AppCompatActivity(), MovableToolbar {
    private lateinit var titleTextToolbar: TextView
    private lateinit var subtitleTextToolbar: TextView
    private lateinit var toolbarCard: CardView
    private var defaultToolbarCardElevation: Float = 0f
    private var aboveToolbarCardElevation: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewtopic)

        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val defaultToolbarMargin: Int = resources.getDimensionPixelSize(R.dimen.defaultToolbarMargin)
        defaultToolbarCardElevation = resources.getDimensionPixelSize(R.dimen.defaultToolbarCardElevation).toFloat()
        aboveToolbarCardElevation = resources.getDimensionPixelSize(R.dimen.aboveToolbarCardElevation).toFloat()

        val appbarLayout: AppBarLayout = findViewById(R.id.appbar_layout_viewtopic)
        val toolbarLayout: FrameLayout = findViewById(R.id.toolbar_layout_viewtopic)
        val topicViewPager: ViewPager = findViewById(R.id.topic_pager_viewtopic)
        val topicNavigation = TopicNavigationHelper(topicViewPager, supportFragmentManager)
        val topicViewModel: TopicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)
        toolbarCard = findViewById(R.id.toolbar_card_viewtopic)
        titleTextToolbar = findViewById(R.id.title_text_toolbar_viewtopic)
        subtitleTextToolbar = findViewById(R.id.subtitle_text_toolbar_viewtopic)

        toolbarLayout.setPaddingRelative(defaultToolbarMargin, defaultToolbarMargin + statusBarHeight, defaultToolbarMargin, defaultToolbarMargin)
        toolbarCard.translationZ = defaultToolbarCardElevation
        topicViewPager.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                topicViewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)
                topicViewPager.translationY = -(appbarLayout.height.toFloat())
                topicViewPager.layoutParams.height = topicViewPager.height + appbarLayout.height
            }
        })

        topicViewModel.getForumAndTopicName().observe(this, Observer { forumAndTopicName ->
            if (forumAndTopicName != null) {
                setTitle(forumAndTopicName.topicName)
                setSubTitle(getString(R.string.onForum, forumAndTopicName.forumName))
            }
        })

        topicViewModel.getLastPageNumber().observe(this, Observer { newLastPageNumber ->
            if (newLastPageNumber != null) {
                topicNavigation.setNumberOfPages(newLastPageNumber)
            }
        })

        if (savedInstanceState == null) {
            val alertDialog = AlertDialog.Builder(this)
            val editText = EditText(this)
            alertDialog.setTitle("Lien du topic")

            alertDialog.setView(editText)

            alertDialog.setPositiveButton("Valider", { _, _ ->
                if (editText.text.toString().isNotEmpty()) {
                    topicViewModel.topicUrl = editText.text.toString()
                } else {
                    topicViewModel.topicUrl = "http://www.jeuxvideo.com/forums/42-1000005-47929326-3-0-1-0-ok-google-blabla-android.htm"
                }
                topicNavigation.setNumberOfPages((topicViewModel.getLastPageNumber().value ?: 1))
                topicNavigation.setCurrentItemIndex((topicViewModel.getLastPageNumber().value ?: 1) - 1)
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

    override fun toolbarMoved(toolbarIsOnTop: Boolean) {
        if (toolbarCard.translationZ == aboveToolbarCardElevation && toolbarIsOnTop) {
            toolbarCard.animate().translationZ(defaultToolbarCardElevation).setDuration(200).start()
        } else if (toolbarCard.translationZ == defaultToolbarCardElevation && !toolbarIsOnTop) {
            toolbarCard.translationZ = aboveToolbarCardElevation
        }
    }
}
