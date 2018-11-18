package com.franckrj.jva.services

import android.graphics.Color
import android.text.Spannable
import android.text.Editable
import android.text.style.BackgroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.Html
import org.xml.sax.XMLReader

/**
 * Html.TagHandler remplaçant les tags customs par des Spannable.
 */
class TagHandlerService private constructor() : Html.TagHandler {
    companion object {
        val instance: TagHandlerService by lazy { TagHandlerService() }
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (tag.toLowerCase() == "s") {
            processAddOfSpan(opening, output, StrikethroughSpan())
        } else if (tag.toLowerCase() == "bg_spoil_button") {
            processAddOfSpan(opening, output, BackgroundColorSpan(Color.BLACK))
        }
    }

    /**
     * Fonction ajoutant le Span correspondant dans l'Editable à la place d'un tag.
     *
     * @param   opening     Si vrai indique que le tag actuel à remplacer est un tag ouvrant, sinon c'est un tag fermant.
     * @param   output      L'Editable dans lequel le tag est remplacé.
     * @param   thisSpan    Le Span à utiliser pour remplacer le tag.
     */
    private fun processAddOfSpan(opening: Boolean, output: Editable, thisSpan: Any) {
        val len: Int = output.length

        if (opening) {
            output.setSpan(thisSpan, len, len, Spannable.SPAN_MARK_MARK)
        } else {
            val obj: Any? = getLast(output, thisSpan.javaClass)

            if (obj != null) {
                val where: Int = output.getSpanStart(obj)

                output.removeSpan(obj)

                if (where != len && where != -1) {
                    output.setSpan(thisSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    /**
     * Retourne le dernier Spannable du type spanType dans l'Editable text.
     *
     * @param   text        L'Editable dans lequel chercher le dernier Spannable.
     * @param   spanType    Le type de Spannable à chercher.
     */
    private fun getLast(text: Editable, spanType: Class<*>): Any? {
        val objectArray: Array<out Any> = text.getSpans(0, text.length, spanType)

        for (i in objectArray.indices.reversed()) {
            if (text.getSpanFlags(objectArray[i]) == Spannable.SPAN_MARK_MARK) {
                return objectArray[i]
            }
        }

        return null
    }
}
