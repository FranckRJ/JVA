package com.franckrj.jva.utils

import android.support.text.emoji.EmojiCompat

object Utils {
    fun applyEmojiCompatIfPossible(baseMessage: CharSequence): CharSequence {
        return if (EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            EmojiCompat.get().process(baseMessage)
        } else {
            baseMessage
        }
    }
}
