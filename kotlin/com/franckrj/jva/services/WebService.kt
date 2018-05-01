package com.franckrj.jva.services

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WebService private constructor(private val userAgentToUse: String) {
    companion object {
        val instance: WebService by lazy { WebService("JVA") }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()

    fun cancelRequest(withThisTag: Any) {
        for (call in client.dispatcher().queuedCalls()) {
            if (call.request().tag() == withThisTag) {
                call.cancel()
            }
        }
        for (call in client.dispatcher().runningCalls()) {
            if (call.request().tag() == withThisTag) {
                call.cancel()
            }
        }
    }

    fun getPage(urlForPage: String, tagToUse: Any): String? {
        return try {
            val request = Request.Builder()
                    .url(urlForPage)
                    .header("User-Agent", userAgentToUse)
                    .tag(tagToUse)
                    .build()

            client.newCall(request).execute().body()?.string()
        } catch (e: Exception) {
            /* Je sais pas vraiment quelles exceptions peuvent être lancées mais
             * je sais qu'il y en a. */
            null
        }
    }
}
