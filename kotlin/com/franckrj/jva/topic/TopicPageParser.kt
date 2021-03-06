package com.franckrj.jva.topic

import android.text.Html
import android.text.Spannable
import android.text.Spanned
import com.franckrj.jva.base.AbsParser
import android.text.style.QuoteSpan
import android.text.SpannableString
import com.franckrj.jva.utils.BetterQuoteSpan
import com.franckrj.jva.utils.UndeprecatorUtils
import com.franckrj.jva.utils.Utils

class TopicPageParser private constructor() : AbsParser() {
    companion object {
        val instance: TopicPageParser by lazy { TopicPageParser() }
    }

    /* Regex pour les liens des topics. */
    private val pageTopicUrlNumberPattern = Regex("""^(http://www\.jeuxvideo\.com/forums/[0-9]*-([0-9]*)-([0-9]*)-)([0-9]*)(-[0-9]*-[0-9]*-[0-9]*-[^.]*\.htm)""")

    /* Regex pour récupérer les infos d'une page d'un topic. */
    private val topicNameInArianeStringPattern = Regex("""<span><a href="/forums/(42|1)-[^"]*">([^<]*)</a></span>""")
    private val currentPagePattern = Regex("""<span class="page-active">([^<]*)</span>""")
    private val pageLinkPattern = Regex("""<span><a href="([^"]*)" class="lien-jv">([0-9]*)</a></span>""")

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
    private val embedVideoPattern = Regex("""<div class="player-contenu"><div class="[^"]*"><iframe.*?src="([^"]*)"[^>]*></iframe></div></div>""")
    private val jvcVideoPattern = Regex("""<div class="player-contenu">.*?<div class="player-jv" id="player-jv-([^-]*)-.*?</div>[^<]*</div>[^<]*</div>[^<]*</div>""", RegexOption.DOT_MATCHES_ALL)
    private val jvcLinkPattern = Regex("""<a href="([^"]*)"( )?( title="[^"]*")?>.*?</a>""")
    private val shortLinkPattern = Regex("""<span class="JvCare [^"]*"[^>]*?target="_blank">([^<]*)</span>""")
    private val longLinkPattern = Regex("""<span class="JvCare [^"]*"[^i]*itle="([^"]*)">[^<]*<i></i><span>[^<]*</span>[^<]*</span>""")
    private val noelshackImagePattern = Regex("""<span class="JvCare[^>]*><img class="img-shack".*?src="http(s)?://([^"]*)" alt="([^"]*)"[^>]*></span>""")
    private val spoilLinePattern = Regex("""<span class="bloc-spoil-jv en-ligne">.*?<span class="contenu-spoil">(.*?)</span></span>""", RegexOption.DOT_MATCHES_ALL)
    private val spoilBlockPattern = Regex("""<div class="bloc-spoil-jv">.*?<div class="contenu-spoil">(.*?)</div></div>""", RegexOption.DOT_MATCHES_ALL)
    private val surroundedBlockquotePattern = Regex("""(<br /> *)*(<(/)?blockquote>)( *<br />)*""")
    private val jvCarePattern = Regex("""<span class="JvCare [^"]*">([^<]*)</span>""")
    private val overlyBetterQuotePattern = Regex("""<(/)?blockquote>""")

    /* Regex de pré-parsage du contenu des messages. */
    private val adPattern = Regex("""<ins[^>]*></ins>""")
    private val uolistOpenTagPattern = Regex("""<(ul|ol)[^>]*>""")
    private val overlySpoilPattern = Regex("""(<(span|div) class="bloc-spoil-jv[^"]*">.*?<(span|div) class="contenu-spoil">|</span></span>|</div></div>)""", RegexOption.DOT_MATCHES_ALL)

    /* Regex pour formater les paragraphes des messages (et supprimer les divs). */
    private val divOpenTagPattern = Regex("""<div[^>]*>""")
    private val largeParagraphePattern = Regex("""(<br /> *){0,2}</p> *<p>( *<br />){0,2}""")
    private val surroundedParagraphePattern = Regex("""<br /> *<(/)?p> *<br />""")
    private val leftParagraphePattern = Regex("""(<br /> *){1,2}<(/)?p>""")
    private val rightParagraphePattern = Regex("""<(/)?p>(<br /> *){1,2}""")
    private val smallParagraphePattern = Regex("""<(/)?p>""")

    private val stickerChangeMap: MutableMap<String, String>

    init {
        stickerChangeMap = hashMapOf("1jc3" to "1jc3-fr",
                                     "1lej" to "1lej-en",
                                     "1leq" to "1leq-en",
                                     "1n1q" to "1n1q-fr",
                                     "1n1t" to "1n1t-fr",
                                     "1n1r" to "1n1r-fr",
                                     "1n1o" to "1n1o-fr",
                                     "1n1n" to "1n1n-fr",
                                     "1n1m" to "1n1m-fr",
                                     "1n1p" to "1n1p-fr",
                                     "zuc" to "zuc-fr")
    }

    fun getPageNumberOfThisTopicUrl(topicUrl: String): Int {
        val pageTopicUrlNumberMatcher: MatchResult? = pageTopicUrlNumberPattern.find(topicUrl)

        return if (pageTopicUrlNumberMatcher != null) {
            pageTopicUrlNumberMatcher.groupValues[4].toIntOrNull() ?: -1
        } else {
            -1
        }
    }

    fun setPageNumberForThisTopicUrl(topicUrl: String, newPageNumber: Int): String {
        val pageTopicUrlNumberMatcher: MatchResult? = pageTopicUrlNumberPattern.find(topicUrl)

        return if (pageTopicUrlNumberMatcher != null) {
            pageTopicUrlNumberMatcher.groupValues[1] + newPageNumber.toString() + pageTopicUrlNumberMatcher.groupValues[5]
        } else {
            ""
        }
    }

    fun getForumAndTopicNameFromPageSource(pageSource: String): ForumAndTopicName {
        var currentForumName = ""
        var currentTopicName = ""
        val allArianeStringMatcher: MatchResult? = ForumAndTopicCommonRegex.allArianeStringPattern.find(pageSource)

        if (allArianeStringMatcher != null) {
            val allArianeString = allArianeStringMatcher.value
            var forumNameMatcher: MatchResult? = ForumAndTopicCommonRegex.forumNameInArianeStringPattern.find(allArianeString)
            val topicNameMatcher: MatchResult? = topicNameInArianeStringPattern.find(allArianeString)
            val highlightMatcher: MatchResult? = ForumAndTopicCommonRegex.highlightInArianeStringPattern.find(allArianeString)

            while (forumNameMatcher != null) {
                currentForumName = forumNameMatcher.groupValues[1]
                forumNameMatcher = ForumAndTopicCommonRegex.forumNameInArianeStringPattern.find(allArianeString, forumNameMatcher.range.endInclusive + 1)
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

    /* Retourne -1 si la page courante est la dernière page. */
    fun getLastPageNumberFromPageSource(pageSource: String): Int {
        val currentPageMatcher: MatchResult? = currentPagePattern.find(pageSource)
        val allPageLinkMatcher: Sequence<MatchResult> = pageLinkPattern.findAll(pageSource)
        val currentPageNumber: Int
        var lastPageNumber = -1

        currentPageNumber = if (currentPageMatcher != null) {
            Integer.parseInt(currentPageMatcher.groupValues[1])
        } else {
            0
        }

        for (currentPageLinkMatcher in allPageLinkMatcher) {
            lastPageNumber = currentPageLinkMatcher.groupValues[2].toIntOrNull()?.let { newLastPageNumber ->
                if (newLastPageNumber > currentPageNumber && newLastPageNumber > lastPageNumber) {
                    newLastPageNumber
                } else {
                    null
                }
            } ?: lastPageNumber
        }

        return lastPageNumber
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

    fun createMessageContentShowable(infosForMessage: MessageInfos, settingsForMessages: MessageSettings): Spannable {
        val messageContent: String = formatMessageToPrettyMessage(infosForMessage.content, infosForMessage.containSpoilTag, settingsForMessages.maxNumberOfOverlyQuotes)
        /* Dans un <span></span> pour corriger un bug de BackgroundSpan qui se ferme jamais si ouvert tout au début. */
        return replaceNeededSpansAndEmojis(UndeprecatorUtils.fromHtml("<span>$messageContent</span>", settingsForMessages.imageGetterToUse, settingsForMessages.tagHandlerToUse), settingsForMessages.settingsForBetterQuotes)
    }

    private fun replaceNeededSpansAndEmojis(spanToChange: Spanned, settingsForBetterQuotes: BetterQuoteSpan.BetterQuoteSettings): Spannable {
        val spannable = SpannableString(Utils.applyEmojiCompatIfPossible(spanToChange))

        val quoteSpanArray: Array<QuoteSpan> = spannable.getSpans(0, spannable.length, QuoteSpan::class.java)
        for (quoteSpan in quoteSpanArray) {
            replaceSpanByAnotherSpan(spannable, quoteSpan, BetterQuoteSpan(settingsForBetterQuotes))
        }

        return spannable
    }

    private fun formatMessageToPrettyMessage(message: String, containSpoilTag: Boolean, maxNumberOfOverlyQuotes: Int): String {
        val messageInBuilder = StringBuilder(message)
        val makeLinkDependingOnSettingsAndForceMake: (String) -> String = { it: String -> makeShortenedLinkIfPossible(it, 50, true) }

        parseMessageWithRegexAndModif(messageInBuilder, codeBlockPattern, 1, """<p><font face="monospace">""", """</font></p>""", ::makeBetterCodeBlockTag)
        parseMessageWithRegexAndModif(messageInBuilder, codeLinePattern, 1, """ <font face="monospace">""", """</font> """, ::makeBetterCodeLineTag)
        messageInBuilder.replaceInside("\n", "")

        /* Remplace le code des stickers par celui par défaut quand le sticker a plusieurs noms différents. */
        parseMessageWithRegexAndModif(messageInBuilder, stickerPattern, 1, "", "", ::createCorrectStickerCodeForThisStickerUrl)
        parseMessageWithRegexAndModif(messageInBuilder, stickerPattern, 1, """<img src="sticker_""", """.png"/>""", ::urlToStickerId) { it: String -> it.replace("-", "_") }
        parseMessageWithRegex(messageInBuilder, smileyPattern, 2, """<img src="smiley_""", """"/>""")

        parseMessageWithRegexAndModif(messageInBuilder, embedVideoPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)
        parseMessageWithRegexAndModif(messageInBuilder, jvcVideoPattern, 1, "", "", { it: String -> "http://www.jeuxvideo.com/videos/iframe/$it" }, makeLinkDependingOnSettingsAndForceMake)
        parseMessageWithRegexAndModif(messageInBuilder, jvcLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)
        parseMessageWithRegexAndModif(messageInBuilder, shortLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)
        parseMessageWithRegexAndModif(messageInBuilder, longLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake)

        parseMessageWithRegex(messageInBuilder, noelshackImagePattern, 3, """<a href="""", """"><img src="http://""", 2, """"/></a>""")

        if (containSpoilTag) {
            parseMessageWithRegex(messageInBuilder, spoilLinePattern, -1, """<bg_spoil_button><font color="#FFFFFF">&nbsp;SPOIL&nbsp;</font></bg_spoil_button>""")
            parseMessageWithRegex(messageInBuilder, spoilBlockPattern, -1, """<p><bg_spoil_button><font color="#FFFFFF">&nbsp;SPOIL&nbsp;</font></bg_spoil_button></p>""")
        }

        removeDivAndAdaptParagraphInMessage(messageInBuilder)
        parseMessageWithRegex(messageInBuilder, surroundedBlockquotePattern, 2)

        parseMessageWithRegexAndModif(messageInBuilder, jvCarePattern, 1, "", "", { it: String -> makeShortenedLinkIfPossible(it, 50, false) })

        removeFirstAndLastBrInMessage(messageInBuilder)

        removeOverlyQuoteInPrettyMessage(messageInBuilder, maxNumberOfOverlyQuotes)

        return messageInBuilder.toString()
    }

    private fun createMessageInfosFromWholeMessage(wholeMessage: String): MessageInfos {
        val infosForMessage = MutableMessageInfos()

        val messageAvatarMatcher: MatchResult? = messageAvatarPattern.find(wholeMessage)
        val messageAuthorInfosMatcher: MatchResult? = messageAuthorInfosPattern.find(wholeMessage)
        val messageDateMatcher: MatchResult? = messageDatePattern.find(wholeMessage)
        val messageContentMatcher: MatchResult? = messageContentPattern.find(wholeMessage)

        if (messageAvatarMatcher != null) {
            infosForMessage.avatarUrl = "http://" + messageAvatarMatcher.groupValues[2]
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
            infosForMessage.content = messageContentMatcher.groupValues[1]
            infosForMessage.containSpoilTag = infosForMessage.content.contains(""" class="contenu-spoil">""")
            infosForMessage.content = makeBasicFormatOfMessage(messageContentMatcher.groupValues[1], infosForMessage.containSpoilTag)
        }

        return MessageInfos(infosForMessage)
    }

    private fun makeBasicFormatOfMessage(message: String, containSpoilTag: Boolean): String {
        val messageInBuilder = StringBuilder(message)

        parseMessageWithRegex(messageInBuilder, adPattern, -1)
        messageInBuilder.replaceInside("\r", "")
        parseListInMessageIfNeeded(messageInBuilder)
        messageInBuilder.replaceInside("""<blockquote class="blockquote-jv">""", "<blockquote>")

        if (containSpoilTag) {
            removeOverlySpoils(messageInBuilder)
        }

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
            val itsEndingTag = overlySpoilMatcher.value.startsWith("</")

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

    private fun removeOverlyQuoteInPrettyMessage(prettyMessage: StringBuilder, maxNumberOfOverlyQuotes: Int) {
        var numberOfOverlyQuotesRemaining = maxNumberOfOverlyQuotes + 1
        var quoteTagMatcher = overlyBetterQuotePattern.find(prettyMessage)

        while (quoteTagMatcher != null) {
            if (quoteTagMatcher.value == "<blockquote>") {
                --numberOfOverlyQuotesRemaining
            } else {
                ++numberOfOverlyQuotesRemaining
            }

            if (numberOfOverlyQuotesRemaining <= 0) {
                var secQuoteTagMatcher = overlyBetterQuotePattern.find(prettyMessage, quoteTagMatcher.range.endInclusive + 1)
                var lastStartOfMatch = -1
                var tmpNumberQuote = 0

                while (secQuoteTagMatcher != null) {
                    if (secQuoteTagMatcher.value == "<blockquote>") {
                        ++tmpNumberQuote
                    } else {
                        --tmpNumberQuote
                    }

                    lastStartOfMatch = secQuoteTagMatcher.range.start

                    if (tmpNumberQuote < 0) {
                        break
                    }

                    secQuoteTagMatcher = overlyBetterQuotePattern.find(prettyMessage, secQuoteTagMatcher.range.endInclusive + 1)
                }

                if (lastStartOfMatch != -1) {
                    prettyMessage.replace(quoteTagMatcher.range.endInclusive + 1, lastStartOfMatch, "[...]")
                }
            }

            quoteTagMatcher = overlyBetterQuotePattern.find(prettyMessage, quoteTagMatcher.range.endInclusive + 1)
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

    private fun urlToStickerId(stickerUrl: String): String {
        val stickerId: String = stickerUrl.removeSuffix("/")

        return if (stickerId.contains("/")) {
            stickerId.substring(stickerId.lastIndexOf("/") + 1)
        } else {
            stickerId
        }
    }

    private fun makeBetterCodeBlockTag(codeTagContent: String): String {
        var newString: String = codeTagContent

        while (newString.startsWith("\n")) {
            newString = newString.removePrefix("\n")
        }

        while (newString.endsWith("\n")) {
            newString = newString.removeSuffix("\n")
        }

        newString = newString.replace("\n", "<br />")

        return newString.replace("  ", "&nbsp;&nbsp;")
    }

    private fun makeBetterCodeLineTag(codeTagContent: String): String {
        var newString: String = codeTagContent

        if (newString.startsWith(" ")) {
            newString = "&nbsp;" + newString.removePrefix(" ")
        }

        if (newString.endsWith(" ")) {
            newString = newString.removeSuffix(" ") + "&nbsp;"
        }

        return newString.replace("  ", "&nbsp;&nbsp;")
    }

    private fun makeShortenedLinkIfPossible(possibleLink: String, maxStringSize: Int, forceLinkCreation: Boolean): String {
        var newString: String = possibleLink

        if (forceLinkCreation || ((newString.startsWith("http://") || newString.startsWith("https://")) && !newString.contains(" "))) {
            var linkShowed = newString

            if (maxStringSize > 0 && linkShowed.length > maxStringSize + 3) {
                linkShowed = linkShowed.substring(0, maxStringSize / 2) + "[…]" + linkShowed.substring(linkShowed.length - maxStringSize / 2)
            }

            newString = """<a href="$newString">$linkShowed</a>"""
        }

        return newString
    }

    private fun createCorrectStickerCodeForThisStickerUrl(stickerUrl: String): String {
        val idOfCurrentSticker = urlToStickerId(stickerUrl)

        return """<img class="img-stickers" src="""" + (stickerChangeMap[idOfCurrentSticker] ?: idOfCurrentSticker) + """"/>"""
    }

    class MessageSettings(val settingsForBetterQuotes: BetterQuoteSpan.BetterQuoteSettings,
                          val imageGetterToUse: Html.ImageGetter,
                          val tagHandlerToUse: Html.TagHandler,
                          val maxNumberOfOverlyQuotes: Int)
}
