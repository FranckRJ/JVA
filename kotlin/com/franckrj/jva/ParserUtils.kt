package com.franckrj.jva

fun StringBuilder.replaceInside(base: String, replacement: String) {
    var index = this.indexOf(base)
    while (index != -1) {
        this.replace(index, index + base.length, replacement)
        index += replacement.length
        index = this.indexOf(base, index)
    }
}

fun StringBuilder.trimInside() {
    while (this.isNotEmpty() && this[0] == ' ') {
        this.deleteCharAt(0)
    }

    while (this.isNotEmpty() && this[this.length - 1] == ' ') {
        this.deleteCharAt(this.length - 1)
    }
}

object ParserUtils {
    fun parseMessageWithRegex(messageToParse: StringBuilder, regexToUse: Regex, groupToUse: Int, stringBefore: String = "",
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

    fun parseMessageWithRegexAndModif(messageToParse: StringBuilder, regexToUse: Regex, groupToUse: Int, stringBefore: String = "",
                                      stringAfter: String = "", firstModifier: StringModifier? = null, secondModifier: StringModifier? = null) {
        var matcherToUse: MatchResult? = regexToUse.find(messageToParse)

        while (matcherToUse != null) {
            val newMessage = StringBuilder(stringBefore)
            var messageToUse: String = if (groupToUse == -1) "" else matcherToUse.groupValues[groupToUse]

            if (firstModifier != null) {
                messageToUse = firstModifier.changeString(messageToUse)
            }
            if (secondModifier != null) {
                messageToUse = secondModifier.changeString(messageToUse)
            }

            newMessage.append(messageToUse).append(stringAfter)

            messageToParse.replace(matcherToUse.range.start, matcherToUse.range.endInclusive + 1, newMessage.toString())
            matcherToUse = regexToUse.find(messageToParse, matcherToUse.range.start + newMessage.length)
        }
    }

    interface StringModifier {
        fun changeString(baseString: String): String
    }
}
