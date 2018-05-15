package com.franckrj.jva.topic

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.franckrj.jva.R
import com.franckrj.jva.base.CollapsibleToolbarActivity
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.PageNavigationHelper

class ViewTopicActivity : CollapsibleToolbarActivity() {
    companion object {
        const val EXTRA_TOPIC_URL: String = "EXTRA_TOPIC_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewtopic)

        val topicViewPager: ViewPager = findViewById(R.id.topic_pager_viewtopic)
        val topicNavigation = PageNavigationHelper(topicViewPager, { ViewTopicPageFragment() }, supportFragmentManager)
        val topicViewModel: TopicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        initToolbar(topicViewPager, findViewById(R.id.appbar_layout_viewtopic), findViewById(R.id.toolbar_layout_viewtopic),
                    findViewById(R.id.toolbar_card_viewtopic), findViewById(R.id.title_text_toolbar_viewtopic), findViewById(R.id.subtitle_text_toolbar_viewtopic))

        NavigationUtils.initContentViewPagerNavigation(this, topicViewPager, topicNavigation, topicViewModel)

        topicViewModel.getForumAndTopicName().observe(this, Observer { forumAndTopicName ->
            if (forumAndTopicName != null) {
                setTitle(forumAndTopicName.topicName)
                setSubTitle(getString(R.string.onForum, forumAndTopicName.forumName))
            }
        })

        val possibleNewTopicUrl: String = intent?.getStringExtra(EXTRA_TOPIC_URL) ?: ""
        if (possibleNewTopicUrl.isNotEmpty()) {
            topicViewModel.setUrlForTopic(possibleNewTopicUrl)
        }
    }
}
