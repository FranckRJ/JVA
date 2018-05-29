package com.franckrj.jva.base

import android.text.Spannable

abstract class AbsParser {
    protected object ForumAndTopicCommonRegex {
        /* Regex pour récupérer les infos d'une page d'un forum / topic. */
        val allArianeStringPattern = Regex("""<div class="fil-ariane-crumb">.*?</h1>""", RegexOption.DOT_MATCHES_ALL)
        val forumNameInArianeStringPattern = Regex("""<span><a href="/forums/0-[^"]*">([^<]*)</a></span>""")
        val highlightInArianeStringPattern = Regex("""<h1 class="highlight">([^<]*)</h1>""")
    }

    protected fun StringBuilder.replaceInside(base: String, replacement: String) {
        var index = this.indexOf(base)
        while (index != -1) {
            this.replace(index, index + base.length, replacement)
            index += replacement.length
            index = this.indexOf(base, index)
        }
    }

    protected fun StringBuilder.trimInside() {
        while (this.isNotEmpty() && this[0] == ' ') {
            this.deleteCharAt(0)
        }

        while (this.isNotEmpty() && this[this.length - 1] == ' ') {
            this.deleteCharAt(this.length - 1)
        }
    }

    protected fun replaceSpanByAnotherSpan(inThisSpan: Spannable, oldSpan: Any, newSpan: Any) {
        val start = inThisSpan.getSpanStart(oldSpan)
        val end = inThisSpan.getSpanEnd(oldSpan)
        val flags = inThisSpan.getSpanFlags(oldSpan)
        inThisSpan.setSpan(newSpan, start, end, flags)
        inThisSpan.removeSpan(oldSpan)
    }

    protected fun parseMessageWithRegex(messageToParse: StringBuilder, regexToUse: Regex, groupToUse: Int, stringBefore: String = "",
                              stringAfter: String = "", secondGroupToUse: Int = -1, stringAfterAfter: String = "") {
        var matcherToUse: MatchResult? = regexToUse.find(messageToParse)

        while (matcherToUse != null) {
            val newMessage = StringBuilder(stringBefore)

            if (groupToUse != -1) {
                newMessage.append(matcherToUse.groupValues[groupToUse])
            }

            newMessage.append(stringAfter)

            if (secondGroupToUse != -1) {
                newMessage.append(matcherToUse.groupValues[secondGroupToUse])
            }

            newMessage.append(stringAfterAfter)

            messageToParse.replace(matcherToUse.range.start, matcherToUse.range.endInclusive + 1, newMessage.toString())
            matcherToUse = regexToUse.find(messageToParse, matcherToUse.range.start + newMessage.length)
        }
    }

    protected fun parseMessageWithRegexAndModif(messageToParse: StringBuilder, regexToUse: Regex, groupToUse: Int, stringBefore: String = "", stringAfter: String = "",
                                                firstStringModifier: ((String) -> String)? = null, secondStringModifier: ((String) -> String)? = null) {
        var matcherToUse: MatchResult? = regexToUse.find(messageToParse)

        while (matcherToUse != null) {
            val newMessage = StringBuilder(stringBefore)
            var messageToUse: String = if (groupToUse == -1) "" else matcherToUse.groupValues[groupToUse]

            if (firstStringModifier != null) {
                messageToUse = firstStringModifier(messageToUse)
            }
            if (secondStringModifier != null) {
                messageToUse = secondStringModifier(messageToUse)
            }

            newMessage.append(messageToUse).append(stringAfter)

            messageToParse.replace(matcherToUse.range.start, matcherToUse.range.endInclusive + 1, newMessage.toString())
            matcherToUse = regexToUse.find(messageToParse, matcherToUse.range.start + newMessage.length)
        }
    }

    protected fun specialCharToNormalChar(baseMessage: String): String {
        return baseMessage.replace("&amp;", "&")
                          .replace("&quot;", "\"")
                          .replace("&#039;", "\'")
                          .replace("&lt;", "<")
                          .replace("&gt;", ">")
    }

    fun formatThisUrlToClassicJvcUrl(urlToChange: String): String {
        var newUrl = urlToChange

        if (newUrl.isNotEmpty()) {
            if (newUrl.startsWith("https://")) {
                newUrl = "http://" + newUrl.substring("https://".length)
            } else if (!newUrl.startsWith("http://")) {
                newUrl = "http://$newUrl"
            }

            if (newUrl.startsWith("http://m.jeuxvideo.com/")) {
                newUrl = "http://www.jeuxvideo.com/" + newUrl.substring("http://m.jeuxvideo.com/".length)
            } else if (newUrl.startsWith("http://jeuxvideo.com/")) {
                newUrl = "http://www.jeuxvideo.com/" + newUrl.substring("http://jeuxvideo.com/".length)
            }
        }

        return newUrl
    }
}
