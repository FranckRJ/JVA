package com.franckrj.jva

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.franckrj.jva.topic.ViewTopicActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewTopicIntent = Intent(this, ViewTopicActivity::class.java)
        viewTopicIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(viewTopicIntent)
        finish()
    }
}
