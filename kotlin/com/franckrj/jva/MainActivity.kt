package com.franckrj.jva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.franckrj.jva.forum.ViewForumActivity
import com.franckrj.jva.services.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Première Activity lancée, servant de splashscreen et lançant directement une autre Activity.
 * Sert aussi à initialiser certaines valeurs qui ne doivent l'être qu'au lancement de l'application
 * et non à sa recréation si le processus a été tué.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* TODO: Supprimer les pages lorsqu'elles ne sont plus utilisées plutôt qu'au lancement de l'app. */
        GlobalScope.launch {
            AppDatabase.instance.forumPageDao().deleteAllForumPages()
            AppDatabase.instance.topicPageDao().deleteAllTopicPages()
        }
        startActivity(Intent(this, ViewForumActivity::class.java))
        finish()
    }
}
