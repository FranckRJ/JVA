package com.franckrj.jva.forum

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.franckrj.jva.R
import com.franckrj.jva.base.BaseActivity
import com.franckrj.jva.pagenav.NavigationUtils
import com.franckrj.jva.pagenav.PageNavigationHelper
import com.google.android.material.appbar.AppBarLayout

class ViewForumActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewforum)

        val forumViewPager: ViewPager = findViewById(R.id.forum_pager_viewforum)
        val forumNavigation = PageNavigationHelper(forumViewPager, { ViewForumPageFragment() }, supportFragmentManager)
        val forumViewModel: ForumViewModel = ViewModelProviders.of(this).get(ForumViewModel::class.java)

        initSysbars(findViewById(R.id.statusbar_background_viewforum))

        val lol: AppBarLayout = findViewById(R.id.appbar_layout_viewforum)
        initCollapsibleToolbar(forumViewPager, lol, findViewById(R.id.toolbar_layout_viewforum),
                    findViewById(R.id.toolbar_card_viewforum), findViewById(R.id.title_text_toolbar_viewforum))
        lol.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            forumViewModel.mdr.value = offset
        })

        NavigationUtils.initContentViewPagerNavigation(this, forumViewPager, forumNavigation, forumViewModel)

        forumViewModel.getForumName().observe(this, Observer { forumName ->
            if (forumName != null) {
                setToolbarTitle(forumName)
            }
        })

        forumViewModel.setUrlForForum("http://www.jeuxvideo.com/forums/0-1000005-0-1-0-1-0-android.htm")
    }
}
