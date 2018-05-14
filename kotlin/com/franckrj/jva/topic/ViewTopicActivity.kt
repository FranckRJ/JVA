package com.franckrj.jva.topic

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.widget.EditText
import com.franckrj.jva.R
import com.franckrj.jva.base.CollapsibleToolbarActivity
import com.franckrj.jva.pagenav.PageNavigationHelper

class ViewTopicActivity : CollapsibleToolbarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewtopic)

        val topicViewPager: ViewPager = findViewById(R.id.topic_pager_viewtopic)
        val topicNavigation = PageNavigationHelper(topicViewPager, { ViewTopicPageFragment() }, supportFragmentManager)
        val topicViewModel: TopicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        initToolbar(topicViewPager, findViewById(R.id.appbar_layout_viewtopic), findViewById(R.id.toolbar_layout_viewtopic),
                    findViewById(R.id.toolbar_card_viewtopic), findViewById(R.id.title_text_toolbar_viewtopic), findViewById(R.id.subtitle_text_toolbar_viewtopic))

        topicViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //rien
            }

            override fun onPageSelected(position: Int) {
                topicViewModel.setCurrentPageNumber(position + 1)
            }

            override fun onPageScrollStateChanged(state: Int) {
                //rien
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

        topicViewModel.getCurrentPageNumber().observe(this, Observer { newCurrentPageNumber ->
            if (newCurrentPageNumber != null && (newCurrentPageNumber - 1) != topicNavigation.getCurrentItemIndex()) {
                topicNavigation.setCurrentItemIndex(newCurrentPageNumber - 1)
            }
        })

        if (savedInstanceState == null) {
            val alertDialog = AlertDialog.Builder(this)
            val editText = EditText(this)
            alertDialog.setTitle("Lien du topic")

            alertDialog.setView(editText)

            alertDialog.setPositiveButton("Valider", { _, _ ->
                if (editText.text.toString().isNotEmpty()) {
                    topicViewModel.setUrlForTopic(editText.text.toString())
                } else {
                    topicViewModel.setUrlForTopic("http://www.jeuxvideo.com/forums/42-1000005-47929326-3-0-1-0-ok-google-blabla-android.htm")
                }

                /* Ce n'est pas censé être utile car normalement ViewTopicActivity connait l'url du topic dès le début. */
                if (topicViewModel.getCurrentPageNumber().value == 1) {
                    topicNavigation.setCurrentItemIndex(0)
                }
            })

            alertDialog.show()
        }
    }
}
