package com.franckrj.jva

import java.util.regex.Matcher
import java.util.regex.Pattern

class TopicParser private constructor() {
    companion object {
        val instance: TopicParser by lazy { TopicParser() }
    }

    private val wholeMessagePattern = Pattern.compile("(<div class=\"bloc-message-forum[^\"]*\".*?)(<span id=\"post_[^\"]*\" class=\"bloc-message-forum-anchor\">|<div class=\"bloc-outils-plus-modo bloc-outils-bottom\">|<div class=\"bloc-pagi-default\">)", Pattern.DOTALL)
    private val messageContentPattern = Pattern.compile("<div class=\"bloc-contenu\">[^<]*<div class=\"txt-msg +text-[^-]*-forum \">((.*?)(?=<div class=\"info-edition-msg\">)|(.*?)(?=<div class=\"signature-msg)|(.*))", Pattern.DOTALL)
    private val messageAuthorInfosPattern = Pattern.compile("<span class=\"JvCare [^ ]* bloc-pseudo-msg text-([^\"]*)\" target=\"_blank\">[^a-zA-Z0-9_\\[\\]-]*([a-zA-Z0-9_\\[\\]-]*)[^<]*</span>")
    private val messageDatePattern = Pattern.compile("<div class=\"bloc-date-msg\">([^<]*<span class=\"JvCare [^ ]* lien-jv\" target=\"_blank\">)?[^a-zA-Z0-9]*(([^ ]* [^ ]* [^ ]*) [^ ]* ([0-9:]*))")

    fun getListOfMessagesFromPageSource(pageSource: String): ArrayList<MessageInfos> {
        val listOfMessages: ArrayList<MessageInfos> = ArrayList()
        val entireMessageMatcher: Matcher = wholeMessagePattern.matcher(pageSource)

        while (entireMessageMatcher.find()) {
            listOfMessages.add(createMessageInfosFromWholeMessage(entireMessageMatcher.group(1)))
        }

        return listOfMessages
    }

    private fun createMessageInfosFromWholeMessage(wholeMessage: String): MessageInfos {
        val infosForMessage = MessageInfos()

        val messageContentMatcher: Matcher = messageContentPattern.matcher(wholeMessage)
        val messageAuthorInfosMatcher: Matcher = messageAuthorInfosPattern.matcher(wholeMessage)
        val messageDateMatcher: Matcher = messageDatePattern.matcher(wholeMessage)

        if (messageContentMatcher.find()) {
            infosForMessage.content = messageContentMatcher.group(1)
        }

        if (messageAuthorInfosMatcher.find()) {
            infosForMessage.author = messageAuthorInfosMatcher.group(2)
        } else {
            infosForMessage.author = "Pseudo supprimé"
        }

        if (messageDateMatcher.find()) {
            infosForMessage.date = messageDateMatcher.group(3)
        }

        return infosForMessage
    }
}
