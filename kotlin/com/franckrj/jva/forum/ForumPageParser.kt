package com.franckrj.jva.forum

import android.text.Spannable
import android.text.SpannableString
import com.franckrj.jva.base.AbsParser
import com.franckrj.jva.utils.UndeprecatorUtils
import com.franckrj.jva.utils.Utils

class ForumPageParser private constructor() : AbsParser() {
    companion object {
        val instance: ForumPageParser by lazy { ForumPageParser() }
    }

    /* Regex pour les liens des forums. */
    private val pageForumUrlNumberPattern = Regex("""^(http://www\.jeuxvideo\.com/forums/[0-9]*-([0-9]*)-[0-9]*-[0-9]*-[0-9]*-)([0-9]*)(-[0-9]*-[^.]*\.htm)""")

    /* Regex pour récupérer les infos des topics. */
    private val wholeTopicPattern = Regex("""<li (class="[^"]*" data-id="[^"]*"|class="message[^"]*")>.*?<span class="topic-subject">.*?</li>""", RegexOption.DOT_MATCHES_ALL)
    private val topicNameAndLinkPattern = Regex("""<a class="lien-jv topic-title[^"]*" href="([^"]*" title="[^"]*)"[^>]*>""")
    private val topicNumberMessagesPattern = Regex("""<span class="topic-count">[^0-9]*([0-9]*)""")
    private val topicNumberMessagesAdmPattern = Regex("""<span class="topic-count-adm">[^0-9]*([0-9]*)""")
    private val topicAuthorPattern = Regex("""<span class=".*?text-([^ ]*) topic-author[^>]*>[^A-Za-z0-9\[\]_-]*([^<\n\r ]*)""")
    private val topicDatePattern = Regex("""<span class="topic-date">[^<]*<span[^>]*>[^0-9/:]*([0-9/:]*)""")
    private val topicTypePattern = Regex("""<img src="/img/forums/topic-(.*?)\.png" alt="[^"]*" title="[^"]*" class="topic-img"""")

    fun getPageNumberOfThisForumUrl(forumUrl: String): Int {
        val pageForumUrlNumberMatcher: MatchResult? = pageForumUrlNumberPattern.find(forumUrl)

        return if (pageForumUrlNumberMatcher != null) {
            pageForumUrlNumberMatcher.groupValues[3].toIntOrNull()?.let { ((it - 1) / 25) + 1 } ?: -1
        } else {
            -1
        }
    }

    fun setPageNumberForThisForumUrl(forumUrl: String, newPageNumber: Int): String {
        val pageForumUrlNumberMatcher: MatchResult? = pageForumUrlNumberPattern.find(forumUrl)

        return if (pageForumUrlNumberMatcher != null) {
            pageForumUrlNumberMatcher.groupValues[1] + newPageNumber.let { ((it - 1) * 25) + 1 }.toString() + pageForumUrlNumberMatcher.groupValues[4]
        } else {
            ""
        }
    }

    fun getForumNameFromPageSource(pageSource: String): String {
        var currentForumName = ""
        val allArianeStringMatcher: MatchResult? = ForumAndTopicCommonRegex.allArianeStringPattern.find(pageSource)

        if (allArianeStringMatcher != null) {
            val allArianeString = allArianeStringMatcher.value
            var forumNameMatcher: MatchResult? = ForumAndTopicCommonRegex.forumNameInArianeStringPattern.find(allArianeString)
            val highlightMatcher: MatchResult? = ForumAndTopicCommonRegex.highlightInArianeStringPattern.find(allArianeString)

            while (forumNameMatcher != null) {
                currentForumName = forumNameMatcher.groupValues[1]
                forumNameMatcher = ForumAndTopicCommonRegex.forumNameInArianeStringPattern.find(allArianeString, forumNameMatcher.range.endInclusive + 1)
            }
            if (highlightMatcher != null) {
                /* La comparaison des deux strings a pour but ne pas récupérer "Forum Android - Page 2" au lieu de "Forum Android" par exemple. */
                if (currentForumName.isEmpty() || currentForumName != highlightMatcher.groupValues[1].substring(0, minOf(currentForumName.length, highlightMatcher.groupValues[1].length))) {
                    currentForumName = highlightMatcher.groupValues[1]
                }
            }

            currentForumName = specialCharToNormalChar(currentForumName.removePrefix("Forum").trim())
        }

        return currentForumName
    }

    fun getListOfTopicsFromPageSource(pageSource: String): ArrayList<TopicInfos> {
        val listOfTopics: ArrayList<TopicInfos> = ArrayList()
        var wholeTopicMatcher: MatchResult? = wholeTopicPattern.find(pageSource)

        while (wholeTopicMatcher != null) {
            listOfTopics.add(createTopicInfosFromWholeTopic(wholeTopicMatcher.groupValues[0]))
            wholeTopicMatcher = wholeTopicMatcher.next()
        }

        return listOfTopics
    }

    fun createTopicTitleShowable(infosForTopic: TopicInfos, settingsForTopics: TopicSettings): Spannable {
        return SpannableString(Utils.applyEmojiCompatIfPossible(
                UndeprecatorUtils.fromHtml("""<b><font color="${settingsForTopics.topicTitleColorString}">${infosForTopic.title}</font> (${infosForTopic.numberOfReplys})</b>""")))
    }

    fun createTopicAuthorShowable(infosForTopic: TopicInfos): Spannable {
        return SpannableString(UndeprecatorUtils.fromHtml("""<small>${infosForTopic.author}</small>"""))
    }

    fun createTopicDateOfLastReplyShowable(infosForTopic: TopicInfos): Spannable {
        return SpannableString(UndeprecatorUtils.fromHtml("""<small>${infosForTopic.dateOfLastReply}</small>"""))
    }

    private fun createTopicInfosFromWholeTopic(wholeTopic: String): TopicInfos {
        val infosForTopic = MutableTopicInfos()

        val topicNameAndLinkMatcher: MatchResult? = topicNameAndLinkPattern.find(wholeTopic)
        val topicNumberMessagesMatcher: MatchResult? = topicNumberMessagesPattern.find(wholeTopic)
        val topicNumberMessagesAdmMatcher: MatchResult? = topicNumberMessagesAdmPattern.find(wholeTopic)
        val topicAuthorMatcher: MatchResult? = topicAuthorPattern.find(wholeTopic)
        val topicDateMatcher: MatchResult? = topicDatePattern.find(wholeTopic)
        val topicTypeMatcher: MatchResult? = topicTypePattern.find(wholeTopic)

        if (topicAuthorMatcher != null) {
            infosForTopic.author = topicAuthorMatcher.groupValues[2].trim()
        } else {
            infosForTopic.author = "Pseudo supprimé"
        }

        if (topicNumberMessagesAdmMatcher != null) {
            infosForTopic.numberOfReplys = (topicNumberMessagesAdmMatcher.groupValues[1].toIntOrNull() ?: -1)
        } else if (topicNumberMessagesMatcher != null) {
            infosForTopic.numberOfReplys = (topicNumberMessagesMatcher.groupValues[1].toIntOrNull() ?: -1)
        }

        if (topicNameAndLinkMatcher != null) {
            val topicNameAndLinkString: String = topicNameAndLinkMatcher.groupValues[1]
            infosForTopic.topicUrl = "http://www.jeuxvideo.com" + topicNameAndLinkString.substring(0, topicNameAndLinkString.indexOf("\""))
            infosForTopic.title = topicNameAndLinkString.substring(topicNameAndLinkString.indexOf("title=\"") + 7)
        }

        if (topicDateMatcher != null) {
            infosForTopic.dateOfLastReply = topicDateMatcher.groupValues[1]
        }

        if (topicTypeMatcher != null) {
            infosForTopic.typeOfTopic = when (topicTypeMatcher.groupValues[1]) {
                "marque-on" -> TopicType.PINNED_OPENED
                "marque-off" -> TopicType.PINNED_LOCKED
                "dossier2" -> TopicType.MULTIPLE_PAGE
                "lock" -> TopicType.LOCKED
                "resolu" -> TopicType.SOLVED
                "ghost" -> TopicType.DELETED
                else -> TopicType.SINGLE_PAGE // "dossier1"
            }
        }

        return TopicInfos(infosForTopic)
    }

    class TopicSettings(val topicTitleColorString: String)
}
