package com.franckrj.jva.topic

import com.franckrj.jva.base.AbsParser

class TopicParser private constructor() : AbsParser() {
    companion object {
        val instance: TopicParser by lazy { TopicParser() }
    }

    /* Regex pour récupérer les infos d'une page d'un topic. */
    private val allArianeStringPattern = Regex("""<div class="fil-ariane-crumb">.*?</h1>""", RegexOption.DOT_MATCHES_ALL)
    private val forumNameInArianeStringPattern = Regex("""<span><a href="/forums/0-[^"]*">([^<]*)</a></span>""")
    private val topicNameInArianeStringPattern = Regex("""<span><a href="/forums/(42|1)-[^"]*">([^<]*)</a></span>""")
    private val highlightInArianeStringPattern = Regex("""<h1 class="highlight">([^<]*)</h1>""")

    /* Regex pour récupérer les infos des messages. */
    private val wholeMessagePattern = Regex("""(<div class="bloc-message-forum[^"]*".*?)(<span id="post_[^"]*" class="bloc-message-forum-anchor">|<div class="bloc-outils-plus-modo bloc-outils-bottom">|<div class="bloc-pagi-default">)""", RegexOption.DOT_MATCHES_ALL)
    private val messageAvatarPattern = Regex("""<img src="[^"]*" data-srcset="(http:)?//([^"]*)" class="user-avatar-msg"""", RegexOption.DOT_MATCHES_ALL)
    private val messageAuthorInfosPattern = Regex("""<span class="JvCare [^ ]* bloc-pseudo-msg text-([^"]*)" target="_blank">[^a-zA-Z0-9_\[\]-]*([a-zA-Z0-9_\[\]-]*)[^<]*</span>""")
    private val messageDatePattern = Regex("""<div class="bloc-date-msg">([^<]*<span class="JvCare [^ ]* lien-jv" target="_blank">)?[^a-zA-Z0-9]*(([^ ]* [^ ]* [^ ]*) [^ ]* ([0-9:]*))""")
    private val messageContentPattern = Regex("""<div class="bloc-contenu">[^<]*<div class="txt-msg +text-[^-]*-forum ">((.*?)(?=<div class="info-edition-msg">)|(.*?)(?=<div class="signature-msg)|(.*))""", RegexOption.DOT_MATCHES_ALL)

    /* Regex pour parser le contenu des messages. */
    private val codeBlockPattern = Regex("""<pre class="pre-jv"><code class="code-jv">([^<]*)</code></pre>""")
    private val codeLinePattern = Regex("""<code class="code-jv">(.*?)</code>""", RegexOption.DOT_MATCHES_ALL)
    private val stickerPattern = Regex("""<img class="img-stickers" src="([^"]*)".*?/>""")
    private val smileyPattern = Regex("""<img src="http(s)?://image\.jeuxvideo\.com/smileys_img/([^"]*)" alt="[^"]*" data-code="([^"]*)" title="[^"]*" [^>]*>""")
    private val youtubeVideoPattern = Regex("""<div class="player-contenu"><div class="[^"]*"><iframe .*? src="http(s)?://www\.youtube\.com/embed/([^"]*)"[^>]*></iframe></div></div>""")
    private val jvcVideoPattern = Regex("""<div class="player-contenu">.*?</div>[^<]*</div>[^<]*</div>[^<]*</div>""", RegexOption.DOT_MATCHES_ALL)
    private val jvcLinkPattern = Regex("""<a href="([^"]*)"( )?( title="[^"]*")?>.*?</a>""")
    private val shortLinkPattern = Regex("""<span class="JvCare [^"]*" rel="nofollow[^"]*" target="_blank">([^<]*)</span>""")
    private val longLinkPattern = Regex("""<span class="JvCare [^"]*"[^i]*itle="([^"]*)">[^<]*<i></i><span>[^<]*</span>[^<]*</span>""")
    private val noelshackImagePattern = Regex("""<span class="JvCare[^>]*><img class="img-shack".*?src="http(s)?://([^"]*)" alt="([^"]*)"[^>]*></span>""")
    private val spoilLinePattern = Regex("""<div class="bloc-spoil-jv en-ligne">.*?<div class="contenu-spoil">(.*?)</div></div>""", RegexOption.DOT_MATCHES_ALL)
    private val spoilBlockPattern = Regex("""<div class="bloc-spoil-jv">.*?<div class="contenu-spoil">(.*?)</div></div>""", RegexOption.DOT_MATCHES_ALL)
    private val surroundedBlockquotePattern = Regex("""(<br /> *)*(<(/)?blockquote>)( *<br />)*""")
    private val jvCarePattern = Regex("""<span class="JvCare [^"]*">([^<]*)</span>""")

    /* Regex de pré-parsage du contenu des messages. */
    private val adPattern = Regex("""<ins[^>]*></ins>""")
    private val uolistOpenTagPattern = Regex("""<(ul|ol)[^>]*>""")
    private val overlySpoilPattern = Regex("""(<div class="bloc-spoil-jv[^"]*">.*?<div class="contenu-spoil">|</div></div>)""", RegexOption.DOT_MATCHES_ALL)

    /* Regex pour formater les paragraphes des messages (et supprimer les divs). */
    private val divOpenTagPattern = Regex("""<div[^>]*>""")
    private val largeParagraphePattern = Regex("""(<br /> *){0,2}</p> *<p>( *<br />){0,2}""")
    private val surroundedParagraphePattern = Regex("""<br /> *<(/)?p> *<br />""")
    private val leftParagraphePattern = Regex("""(<br /> *){1,2}<(/)?p>""")
    private val rightParagraphePattern = Regex("""<(/)?p>(<br /> *){1,2}""")
    private val smallParagraphePattern = Regex("""<(/)?p>""")

    fun getForumAndTopicNameFromPageSource(pageSource: String): ForumAndTopicName {
        var currentForumName = ""
        var currentTopicName = ""
        val allArianeStringMatcher: MatchResult? = allArianeStringPattern.find(pageSource)

        if (allArianeStringMatcher != null) {
            val allArianeString = allArianeStringMatcher.value
            var forumNameMatcher: MatchResult? = forumNameInArianeStringPattern.find(allArianeString)
            val topicNameMatcher: MatchResult? = topicNameInArianeStringPattern.find(allArianeString)
            val highlightMatcher: MatchResult? = highlightInArianeStringPattern.find(allArianeString)

            while (forumNameMatcher != null) {
                currentForumName = forumNameMatcher.groupValues[1]
                forumNameMatcher = forumNameInArianeStringPattern.find(allArianeString, forumNameMatcher.range.endInclusive + 1)
            }
            if (topicNameMatcher != null) {
                currentTopicName = topicNameMatcher.groupValues[2]
            } else if (highlightMatcher != null) {
                currentTopicName = highlightMatcher.groupValues[1]
            }

            currentForumName = specialCharToNormalChar(currentForumName.removePrefix("Forum").trim())
            currentTopicName = specialCharToNormalChar(currentTopicName.removePrefix("Topic").trim())
        }

        return ForumAndTopicName(currentForumName, currentTopicName)
    }

    fun getListOfMessagesFromPageSource(pageSource: String): ArrayList<MessageInfos> {
        val listOfMessages: ArrayList<MessageInfos> = ArrayList()
        var wholeMessageMatcher: MatchResult? = wholeMessagePattern.find(pageSource)

        while (wholeMessageMatcher != null) {
            listOfMessages.add(createMessageInfosFromWholeMessage(wholeMessageMatcher.groupValues[1]))
            wholeMessageMatcher = wholeMessageMatcher.next()
        }

        return listOfMessages
    }

    fun formatMessageToPrettyMessage(message: String): String {
        val messageInBuilder = StringBuilder(message)
        val makeLinkDependingOnSettingsAndForceMake = MakeShortenedLinkIfPossible(50, true)

        parseMessageWithRegexAndModif(messageInBuilder, codeBlockPattern, 1, "<p><font face=\"monospace\">", "</font></p>", MakeCodeTagGreatAgain(true))
        parseMessageWithRegexAndModif(messageInBuilder, codeLinePattern, 1, " <font face=\"monospace\">", "</font> ", MakeCodeTagGreatAgain(false))
        messageInBuilder.replaceInside("\n", "")

        /* TODO: Gérer les différents noms de stickers identiques. */
        parseMessageWithRegexAndModif(messageInBuilder, stickerPattern, 1, "<img src=\"sticker_", ".png\"/>", ConvertUrlToStickerId(), ConvertStringToString("-", "_"))
        parseMessageWithRegex(messageInBuilder, smileyPattern, 2, "<img src=\"smiley_", "\"/>")

        parseMessageWithRegex(messageInBuilder, youtubeVideoPattern, 2, "<a href=\"http://youtu.be/", "\">http://youtu.be/", 2, "</a>")
        parseMessageWithRegex(messageInBuilder, jvcVideoPattern, -1, "[[Vidéo non supportée par l'application]]")
        parseMessageWithRegexAndModif(messageInBuilder, jvcLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)
        parseMessageWithRegexAndModif(messageInBuilder, shortLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)
        parseMessageWithRegexAndModif(messageInBuilder, longLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)

        parseMessageWithRegex(messageInBuilder, noelshackImagePattern, 3, "<a href=\"", "\"><img src=\"http://", 2, "\"/></a>")

        /* TODO: Check s'il y a des spoils avant d'exécuter (comme sur RespawnIRC) pour économiser les perfs ? */
        parseMessageWithRegex(messageInBuilder, spoilLinePattern, -1, "<bg_spoil_button><font color=\"#FFFFFF\">&nbsp;SPOIL&nbsp;</font></bg_spoil_button>")
        parseMessageWithRegex(messageInBuilder, spoilBlockPattern, -1, "<p><bg_spoil_button><font color=\"#FFFFFF\">&nbsp;SPOIL&nbsp;</font></bg_spoil_button></p>")

        removeDivAndAdaptParagraphInMessage(messageInBuilder)
        parseMessageWithRegex(messageInBuilder, surroundedBlockquotePattern, 2)

        parseMessageWithRegexAndModif(messageInBuilder, jvCarePattern, 1, "", "", MakeShortenedLinkIfPossible(50, false))

        removeFirstAndLastBrInMessage(messageInBuilder)

        /* TODO: Gérer les quotes imbriquées. */

        return messageInBuilder.toString()
    }

    private fun createMessageInfosFromWholeMessage(wholeMessage: String): MessageInfos {
        val infosForMessage = MutableMessageInfos()

        val messageAvatarMatcher: MatchResult? = messageAvatarPattern.find(wholeMessage)
        val messageAuthorInfosMatcher: MatchResult? = messageAuthorInfosPattern.find(wholeMessage)
        val messageDateMatcher: MatchResult? = messageDatePattern.find(wholeMessage)
        val messageContentMatcher: MatchResult? = messageContentPattern.find(wholeMessage)

        if (messageAvatarMatcher != null) {
            infosForMessage.avatarLink = "http://" + messageAvatarMatcher.groupValues[2]
        }

        if (messageAuthorInfosMatcher != null) {
            infosForMessage.author = messageAuthorInfosMatcher.groupValues[2]
        } else {
            infosForMessage.author = "Pseudo supprimé"
        }

        if (messageDateMatcher != null) {
            infosForMessage.date = messageDateMatcher.groupValues[3]
        }

        if (messageContentMatcher != null) {
            infosForMessage.content = makeBasicFormatOfMessage(messageContentMatcher.groupValues[1])
        }

        return infosForMessage
    }

    private fun makeBasicFormatOfMessage(message: String): String {
        val messageInBuilder = StringBuilder(message)

        parseMessageWithRegex(messageInBuilder, adPattern, -1)
        messageInBuilder.replaceInside("\r", "")
        parseListInMessageIfNeeded(messageInBuilder)
        messageInBuilder.replaceInside("""<blockquote class="blockquote-jv">""", "<blockquote>")

        /* TODO: Check s'il y a des spoils avant d'exécuter (comme sur RespawnIRC) pour économiser les perfs ? */
        removeOverlySpoils(messageInBuilder)

        return messageInBuilder.toString()
    }

    private fun parseListInMessageIfNeeded(message: StringBuilder) {
        if (message.indexOf("<li>") != -1) {
            parseMessageWithRegex(message, uolistOpenTagPattern, -1, "<p>")
            message.replaceInside("</ul>", "</p>")
            message.replaceInside("</ol>", "</p>")
            message.replaceInside("<li><p><li>", "<li><li>")
            message.replaceInside("<li><p><li>", "<li><li>")
            message.replaceInside("<li>", " • ")
            message.replaceInside("</li></p></li>", "</li>")
            message.replaceInside("</li></p></li>", "</li>")
            message.replaceInside("</li>", "<br />")
        }
    }

    private fun removeOverlySpoils(message: StringBuilder) {
        var overlySpoilMatcher: MatchResult? = overlySpoilPattern.find(message)
        var currentSpoilTagDeepness = 0

        while (overlySpoilMatcher != null) {
            val itsEndingTag = overlySpoilMatcher.value == "</div></div>"

            if (!itsEndingTag) {
                ++currentSpoilTagDeepness
            }

            overlySpoilMatcher = if (currentSpoilTagDeepness > 1) {
                message.delete(overlySpoilMatcher.range.start, overlySpoilMatcher.range.endInclusive + 1)
                overlySpoilPattern.find(message, overlySpoilMatcher.range.start)
            } else {
                overlySpoilPattern.find(message, overlySpoilMatcher.range.endInclusive + 1)
            }

            if (itsEndingTag) {
                --currentSpoilTagDeepness

                if (currentSpoilTagDeepness < 0) {
                    currentSpoilTagDeepness = 0
                }
            }
        }
    }

    private fun removeDivAndAdaptParagraphInMessage(message: StringBuilder) {
        parseMessageWithRegex(message, divOpenTagPattern, -1)
        message.replaceInside("</div>", "")
        parseMessageWithRegex(message, largeParagraphePattern, -1, "<br /><br />")
        parseMessageWithRegex(message, surroundedParagraphePattern, -1, "<br /><br />")
        parseMessageWithRegex(message, leftParagraphePattern, -1, "<br /><br />")
        parseMessageWithRegex(message, rightParagraphePattern, -1, "<br /><br />")
        parseMessageWithRegex(message, smallParagraphePattern, -1, "<br /><br />")
    }

    private fun removeFirstAndLastBrInMessage(message: StringBuilder) {
        message.trimInside()

        while (message.startsWith("<br />")) {
            message.delete(0, 6)
            message.trimInside()
        }

        while (message.endsWith("<br />")) {
            message.delete(message.length - 6, message.length)
            message.trimInside()
        }
    }

    private class ConvertStringToString(private val stringToRemplace: String, private val stringNew: String) : AbsParser.StringModifier {
        override fun changeString(baseString: String): String {
            return baseString.replace(stringToRemplace, stringNew)
        }
    }

    private class ConvertUrlToStickerId : AbsParser.StringModifier {
        override fun changeString(baseString: String): String {
            var newString: String = baseString

            newString = newString.removeSuffix("/")

            return if (newString.contains("/")) {
                newString.substring(newString.lastIndexOf("/") + 1)
            } else {
                newString
            }
        }
    }

    private class MakeCodeTagGreatAgain(private val isCodeBlock: Boolean) : AbsParser.StringModifier {
        override fun changeString(baseString: String): String {
            var newString: String = baseString

            if (isCodeBlock) {
                while (newString.startsWith("\n")) {
                    newString = newString.removePrefix("\n")
                }

                while (newString.endsWith("\n")) {
                    newString = newString.removeSuffix("\n")
                }

                newString = newString.replace("\n", "<br />")
            } else {
                if (newString.startsWith(" ")) {
                    newString = "&nbsp;" + newString.removePrefix(" ")
                }

                if (newString.endsWith(" ")) {
                    newString = newString.removeSuffix(" ") + "&nbsp;"
                }
            }

            return newString.replace("  ", "&nbsp;&nbsp;")
        }
    }

    private class MakeShortenedLinkIfPossible(private val maxStringSize: Int, private val forceLinkCreation: Boolean) : AbsParser.StringModifier {
        override fun changeString(baseString: String): String {
            var newString: String = baseString

            if (forceLinkCreation || ((newString.startsWith("http://") || newString.startsWith("https://")) && !newString.contains(" "))) {
                var linkShowed = newString

                if (maxStringSize > 0 && linkShowed.length > maxStringSize + 3) {
                    linkShowed = linkShowed.substring(0, maxStringSize / 2) + "[…]" + linkShowed.substring(linkShowed.length - maxStringSize / 2)
                }

                newString = """<a href="$newString">$linkShowed</a>"""
            }

            return newString
        }
    }
}