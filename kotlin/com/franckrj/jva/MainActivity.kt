package com.franckrj.jva

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var topicViewModel: TopicViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val idOfNavBarHeight: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarHeight: Int = if (idOfNavBarHeight > 0) resources.getDimensionPixelSize(idOfNavBarHeight) else 0
        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val defaultMessageListPadding = resources.getDimensionPixelSize(R.dimen.messageListPadding)
        val messageListView: RecyclerView = findViewById(R.id.message_list_main)
        val messageListAdapter = TopicAdapter()
        var navBarIsInApp = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= 24) {
            if (isInMultiWindowMode) {
                navBarIsInApp = false
            }
        }

        messageListView.setPaddingRelative(defaultMessageListPadding,
                                           defaultMessageListPadding + statusBarHeight,
                                           defaultMessageListPadding,
                                           defaultMessageListPadding + if (navBarIsInApp) navBarHeight else 0)

        messageListView.layoutManager = LinearLayoutManager(this)
        messageListView.adapter = messageListAdapter
        topicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        topicViewModel?.getListOfMessagesShowable()?.observe(this, Observer { listOfMessagesShowable ->
            messageListAdapter.listOfMessagesShowable = listOfMessagesShowable ?: ArrayList()
            messageListAdapter.notifyDataSetChanged()
        })
    }
}
