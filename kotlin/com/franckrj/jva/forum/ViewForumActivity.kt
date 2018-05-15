package com.franckrj.jva.forum

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.franckrj.jva.R
import com.franckrj.jva.base.CollapsibleToolbarActivity
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.PageNavigationHelper

class ViewForumActivity : CollapsibleToolbarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewforum)

        val forumViewPager: ViewPager = findViewById(R.id.forum_pager_viewforum)
        val forumNavigation = PageNavigationHelper(forumViewPager, { ViewForumPageFragment() }, supportFragmentManager)
        val forumViewModel: ForumViewModel = ViewModelProviders.of(this).get(ForumViewModel::class.java)

        initToolbar(forumViewPager, findViewById(R.id.appbar_layout_viewforum), findViewById(R.id.toolbar_layout_viewforum),
                    findViewById(R.id.toolbar_card_viewforum), findViewById(R.id.title_text_toolbar_viewforum))

        NavigationUtils.initContentViewPagerNavigation(this, forumViewPager, forumNavigation, forumViewModel)

        forumViewModel.getForumName().observe(this, Observer { forumName ->
            if (forumName != null) {
                setTitle(forumName)
            }
        })

        forumViewModel.setUrlForForum("http://www.jeuxvideo.com/forums/0-1000005-0-1-0-1-0-android.htm")
    }
}
