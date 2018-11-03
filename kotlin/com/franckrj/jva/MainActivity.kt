package com.franckrj.jva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.franckrj.jva.forum.ViewForumActivity

/**
 * Première Activity lancée, servant de splashscreen et lançant directement une autre Activity.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, ViewForumActivity::class.java))
        finish()
    }
}
