package com.franckrj.jva.topic

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.widget.ImageView
import com.franckrj.jva.R
import com.franckrj.jva.base.BaseActivity
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.PageNavigationHelper

class ViewTopicActivity : BaseActivity() {
    companion object {
        const val EXTRA_TOPIC_URL: String = "EXTRA_TOPIC_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewtopic)

        val arrowBackButton: ImageView = findViewById(R.id.arrow_back_button_viewtopic)
        val topicViewPager: ViewPager = findViewById(R.id.topic_pager_viewtopic)
        val topicNavigation = PageNavigationHelper(topicViewPager, { ViewTopicPageFragment() }, supportFragmentManager)
        val topicViewModel: TopicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        initSysbars(findViewById(R.id.statusbar_background_viewtopic), findViewById(R.id.navbar_background_viewtopic))

        initCollapsibleToolbar(topicViewPager, findViewById(R.id.appbar_layout_viewtopic), findViewById(R.id.toolbar_layout_viewtopic),
                    findViewById(R.id.toolbar_card_viewtopic), findViewById(R.id.title_text_toolbar_viewtopic), findViewById(R.id.subtitle_text_toolbar_viewtopic))

        arrowBackButton.setOnClickListener {
            onBackPressed()
        }

        NavigationUtils.initContentViewPagerNavigation(this, topicViewPager, topicNavigation, topicViewModel)

        topicViewModel.getForumAndTopicName().observe(this, Observer { forumAndTopicName ->
            if (forumAndTopicName != null) {
                setToolbarTitle(forumAndTopicName.topicName)
                setToolbarSubTitle(getString(R.string.onForum, forumAndTopicName.forumName))
            }
        })

        if (savedInstanceState == null) {
            val possibleNewTopicUrl: String = intent?.getStringExtra(EXTRA_TOPIC_URL) ?: ""
            if (possibleNewTopicUrl.isNotEmpty()) {
                topicViewModel.setUrlForTopic(possibleNewTopicUrl)
            }
        }
    }
}
