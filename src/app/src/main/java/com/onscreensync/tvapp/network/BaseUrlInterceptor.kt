package com.onscreensync.tvapp.network

import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.services.LocalStorageService
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseUrlInterceptor @Inject constructor(
    private val storageService: LocalStorageService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val newBaseUrl = storageService.getData(DisplayApiConfigConstants.BASEURL)
        
        if (newBaseUrl != null) {
            val newUrl = newBaseUrl.toHttpUrlOrNull()
            if (newUrl != null) {
                val updatedUrl = request.url.newBuilder()
                    .scheme(newUrl.scheme)
                    .host(newUrl.host)
                    .port(newUrl.port)
                    .build()
                request = request.newBuilder()
                    .url(updatedUrl)
                    .build()
            }
        }
        
        return chain.proceed(request)
    }
}