package com.franckrj.jva.services

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Service ayant pour but de simplifier les requêtes web.
 *
 * @property    userAgentToUse      User-Agent à utiliser pour les requêtes.
 */
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

    /**
     * Permet de récupérer l'OkHttpUrlLoader.Factory utilisé par le service.
     *
     * @return  L'OkHttpUrlLoader.Factory utilisé par le service.
     */
    fun getOkHttpUrlLoaderFactory() : OkHttpUrlLoader.Factory {
        return OkHttpUrlLoader.Factory(client)
    }

    /**
     * Annule une requête avec un tag donné.
     *
     * @param   withThisTag     Le tag de la requête à annuler.
     */
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

    /**
     * Permet de récupérer le contenu d'une page web.
     *
     * @param   urlForPage  Url de la page à récupérer.
     * @param   tagToUse    Le tag à associer à la requête (pour une éventuelle annulation par exemple).
     * @return              Le contenu de la page web, ou null s'il y a eu une erreur.
     */
    fun getPage(urlForPage: String, tagToUse: Any): String? {
        return try {
            val request = Request.Builder()
                    .url(urlForPage)
                    .header("User-Agent", userAgentToUse)
                    .tag(tagToUse)
                    .build()

            client.newCall(request).execute().body()?.string()
        } catch (_: Exception) {
            /* Je sais pas vraiment quelles exceptions peuvent être lancées mais
             * je sais qu'il y en a. */
            null
        }
    }
}
