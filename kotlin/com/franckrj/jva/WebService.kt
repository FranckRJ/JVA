package com.franckrj.jva

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WebService private constructor(private val userAgentToUse: String) {
    companion object {
        val instance: WebService by lazy { WebService("JVA") }
    }

    fun sendRequest(newLinkToPage: String, requestMethod: String, requestParameters: String, cookiesInAString: String, currentInfos: WebInfos): String? {
        var linkToPage = newLinkToPage
        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val buffer = StringBuilder()
            var line: String?

            if (requestMethod == "GET" && requestParameters.isNotEmpty()) {
                linkToPage = "$linkToPage?$requestParameters"
            }

            val urlToPage = URL(linkToPage)
            urlConnection = urlToPage.openConnection() as HttpURLConnection
            currentInfos.currentUrl = urlConnection.url?.toString()

            urlConnection.instanceFollowRedirects = currentInfos.followRedirects
            urlConnection.connectTimeout = 5000
            urlConnection.readTimeout = 5000

            urlConnection.requestMethod = requestMethod
            urlConnection.setRequestProperty("User-Agent", userAgentToUse)
            urlConnection.setRequestProperty("Connection", "Keep-Alive")
            urlConnection.setRequestProperty("Cookie", cookiesInAString)
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            if (requestMethod == "POST") {
                urlConnection.doOutput = true
                urlConnection.setFixedLengthStreamingMode(requestParameters.toByteArray().size)

                val writer = DataOutputStream(urlConnection.outputStream)
                writer.writeBytes(requestParameters)
                writer.flush()
                writer.close()
            }

            /* Retourne null si inputStream vaut null. */
            val inputStream: InputStream = urlConnection.inputStream ?: return null

            reader = BufferedReader(InputStreamReader(inputStream))
            line = reader.readLine()
            while (line != null) {
                buffer.append(line).append("\n")
                line = reader.readLine()
            }

            if (currentInfos.followRedirects) {
                if (currentInfos.currentUrl == urlConnection.url?.toString()) {
                    currentInfos.currentUrl = ""
                } else {
                    currentInfos.currentUrl = urlConnection.url?.toString()
                }
            } else {
                currentInfos.currentUrl = urlConnection.getHeaderField("Location")
            }

            if (currentInfos.currentUrl == null) {
                currentInfos.currentUrl = ""
            }

            return if (buffer.isEmpty()) {
                null
            } else {
                buffer.toString()
            }
        } catch (e: Exception) {
            return null
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: Exception) {
                    //rien
                }

            }
        }
    }

    class WebInfos (var followRedirects: Boolean = false,
                    var currentUrl: String? = "")
}
