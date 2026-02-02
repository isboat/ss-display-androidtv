package com.onscreensync.tvapp.network

import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.apirequests.TokenApiRequest
import com.onscreensync.tvapp.apirequests.TokenApiRequestBody
import com.onscreensync.tvapp.services.LocalStorageService
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val storageService: LocalStorageService,
    private val tokenApiRequestProvider: Provider<TokenApiRequest>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = storageService.refreshToken
        if (refreshToken.isNullOrEmpty()) return null

        synchronized(this) {
            val currentAccessToken = storageService.accessToken
            val requestAccessToken = response.request.header("Authorization")?.replace("Bearer ", "")

            // If the token has already been refreshed by another thread
            if (currentAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            // Refresh the token
            val url = storageService.getData(DisplayApiConfigConstants.DEVICE_REFRESH_TOKEN_REQUEST_URL) ?: return null
            val refreshResponse = tokenApiRequestProvider.get().refreshTokenRequest(
                url,
                TokenApiRequestBody("", "string", "", Constants.TOKEN_REFRESH_GRANT_TYPE),
                "Bearer $refreshToken"
            ).execute()

            return if (refreshResponse.isSuccessful) {
                val newTokens = refreshResponse.body()
                if (newTokens != null) {
                    storageService.accessToken = newTokens.accessToken
                    storageService.refreshToken = newTokens.refreshToken
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                } else null
            } else null
        }
    }
}