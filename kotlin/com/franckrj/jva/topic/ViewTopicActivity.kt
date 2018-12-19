package com.franckrj.jva.topic

import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.franckrj.jva.R
import com.franckrj.jva.base.BaseActivity
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.PageNavigationHelper
import com.google.android.material.appbar.AppBarLayout

class ViewTopicActivity : BaseActivity() {
    companion object {
        const val EXTRA_TOPIC_URL: String = "EXTRA_TOPIC_URL"
        const val PAGE_TO_GO: String = "PAGE_TO_GO"

        private const val SAVE_TOPIC_URL: String = "SAVE_TOPIC_URL"
        private const val SAVE_LAST_PAGE_NUMBER: String = "SAVE_LAST_PAGE_NUMBER"
    }

    private lateinit var topicViewModel: TopicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewtopic)

        val arrowBackButton: ImageView = findViewById(R.id.arrow_back_button_viewtopic)
        val topicViewPager: ViewPager = findViewById(R.id.topic_pager_viewtopic)
        val topicNavigation = PageNavigationHelper(topicViewPager, { ViewTopicPageFragment() }, supportFragmentManager)
        topicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        initSysbars(findViewById(R.id.statusbar_background_viewtopic))

        val lol: AppBarLayout = findViewById(R.id.appbar_layout_viewtopic)
        initCollapsibleToolbar(topicViewPager, lol, findViewById(R.id.toolbar_layout_viewtopic),
                    findViewById(R.id.toolbar_card_viewtopic), findViewById(R.id.title_text_toolbar_viewtopic), findViewById(R.id.subtitle_text_toolbar_viewtopic))
        lol.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            topicViewModel.mdr.value = offset
        })

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
            val possibleNewPageToGo: Int = intent?.getIntExtra(PAGE_TO_GO, -1) ?: -1
            if (possibleNewTopicUrl.isNotEmpty()) {
                if (possibleNewPageToGo > 0) {
                    topicViewModel.setUrlForTopicWithPage(possibleNewTopicUrl, possibleNewPageToGo)
                }
                else {
                    topicViewModel.setUrlForTopic(possibleNewTopicUrl)
                }
            }
        }
        else {
            topicViewModel.restoreOldState(savedInstanceState.getString(SAVE_TOPIC_URL, ""), savedInstanceState.getInt(SAVE_LAST_PAGE_NUMBER, -1))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVE_TOPIC_URL, topicViewModel.topicUrl)
        outState.putInt(SAVE_LAST_PAGE_NUMBER, topicViewModel.getLastPageNumber().value ?: -1)
    }
}
