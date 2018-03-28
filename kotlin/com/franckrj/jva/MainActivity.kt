package com.franckrj.jva

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var topicViewModel: TopicViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView: RecyclerView = findViewById(R.id.message_list_main)
        val listAdapter = TopicAdapter()

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = listAdapter
        topicViewModel = ViewModelProviders.of(this).get(TopicViewModel::class.java)

        topicViewModel?.getListOfMessagesShowable()?.observe(this, Observer { listOfMessagesShowable ->
            listAdapter.listOfMessagesShowable = listOfMessagesShowable ?: ArrayList()
            listAdapter.notifyDataSetChanged()
        })
    }
}
