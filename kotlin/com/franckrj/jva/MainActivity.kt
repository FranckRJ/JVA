package com.franckrj.jva

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.widget.Toast
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    private lateinit var topicViewModel: TopicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val idOfNavBarHeight: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarHeight: Int = if (idOfNavBarHeight > 0) resources.getDimensionPixelSize(idOfNavBarHeight) else 0
        val idOfStatusBarHeight: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = if (idOfStatusBarHeight > 0) resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
        val defaultMessageListPadding = resources.getDimensionPixelSize(R.dimen.messageListPadding)
        val messageCardBottomMargin = resources.getDimensionPixelSize(R.dimen.messageCardBottomMargin)
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
                                           defaultMessageListPadding - messageCardBottomMargin + if (navBarIsInApp) navBarHeight else 0)

        messageListView.layoutManager = LinearLayoutManager(this)
        messageListView.adapter = messageListAdapter
        topicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        topicViewModel.getListOfMessagesShowable().observe(this, Observer { listOfMessagesShowable ->
            messageListAdapter.listOfMessagesShowable = listOfMessagesShowable ?: ArrayList()
            messageListAdapter.notifyDataSetChanged()
        })

        topicViewModel.getForumAndTopicName().observe(this, Observer { forumAndTopicName ->
            if (forumAndTopicName != null) {
                messageListAdapter.listOfHeaders = listOf(TopicAdapter.HeaderInfos(SpannableString(forumAndTopicName.forumName), true),
                                                          TopicAdapter.HeaderInfos(SpannableString(forumAndTopicName.topicName), false))
                messageListAdapter.notifyDataSetChanged()
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
                    topicViewModel.updateAllTopicInfos(editText.text.toString())
                } else {
                    topicViewModel.updateAllTopicInfos("http://www.jeuxvideo.com/forums/42-800-55783559-1-0-1-0-tests.htm")
                }
            })

            alertDialog.show()
        }
    }
}
