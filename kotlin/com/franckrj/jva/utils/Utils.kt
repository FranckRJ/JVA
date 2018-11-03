package com.franckrj.jva.utils

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorInt
import androidx.emoji.text.EmojiCompat

/**
 * Namespace (en gros) contenant quelques fonctions utilitaires.
 */
object Utils {
    /**
     * Convertis une couleur (entier) en couleur au format HTML (string).
     *
     * @param   colorValue      La couleur à convertir au format ColorInt.
     * @return                  La couleur convertie au format HTML ("#FFFFFF" pour du blanc).
     */
    fun colorToString(@ColorInt colorValue: Int): String {
        return String.format("#%06X", 0xFFFFFF and colorValue)
    }

    /**
     * Convertis les émojis de la CharSequence selon les règles utilisées par EmojiCompat.
     *
     * @param   baseMessage     Le message à convertir.
     * @return                  Le résultat de la conversion.
     */
    fun applyEmojiCompatIfPossible(baseMessage: CharSequence): CharSequence {
        return if (EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            EmojiCompat.get().process(baseMessage)
        } else {
            baseMessage
        }
    }

    /**
     * Retourne la hauteur de la Statusbar de l'Activity.
     *
     * @param   fromThisActivity    L'Activity dont la Statusbar doit être mesurée.
     * @return                      La hauteur de la Statusbar.
     */
    fun getStatusbarHeight(fromThisActivity: Activity): Int {
        val idOfStatusBarHeight: Int = fromThisActivity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (idOfStatusBarHeight > 0) fromThisActivity.resources.getDimensionPixelSize(idOfStatusBarHeight) else 0
    }

    /**
     * Retourne la hauteur de la Navbar de l'Activity.
     *
     * @param   fromThisActivity    L'Activity dont la Navbar doit être mesurée.
     * @return                      La hauteur de la Navbar.
     */
    fun getNavbarHeight(fromThisActivity: Activity): Int {
        val idOfNavBarHeight: Int = fromThisActivity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (idOfNavBarHeight > 0) fromThisActivity.resources.getDimensionPixelSize(idOfNavBarHeight) else 0
    }

    /**
     * Permet de savoir si la Navbar est affichée dans l'Application. Plus précisément si elle peut être
     * transparente pour que du contenus soit affichée derrière (elle doit être en bas, pas sur les côtés).
     *
     * @param   fromThisActivity    L'Activity depuis laquelle l'information doit être vérifiée.
     * @return                      Vrai si l'information est validée, faux sinon.
     */
    fun getNavbarIsInApp(fromThisActivity: Activity): Boolean {
        var navBarIsInApp: Boolean = fromThisActivity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= 24) {
            if (fromThisActivity.isInMultiWindowMode) {
                navBarIsInApp = false
            }
        }

        return navBarIsInApp
    }
}
