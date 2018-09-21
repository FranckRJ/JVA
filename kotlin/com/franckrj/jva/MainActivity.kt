package com.franckrj.jva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.franckrj.jva.forum.ViewForumActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, ViewForumActivity::class.java))
        finish()
    }
}
