package com.franckrj.jva.utils

import android.content.Context
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.franckrj.jva.services.WebService
import java.io.InputStream

/**
 * Une AppGlideModule customisée pour que Glide et le reste du programme utilise le même OkHttpUrlLoaderFactory.
 */
@Excludes(OkHttpLibraryGlideModule::class)
@GlideModule
class CustomAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val webServiceToUse: WebService = WebService.instance
        registry.replace(GlideUrl::class.java, InputStream::class.java, webServiceToUse.getOkHttpUrlLoaderFactory())
    }
}
