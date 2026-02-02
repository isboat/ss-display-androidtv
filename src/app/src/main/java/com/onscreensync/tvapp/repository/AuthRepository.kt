package com.onscreensync.tvapp.repository

import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.apirequests.TokenApiRequest
import com.onscreensync.tvapp.apirequests.TokenApiRequestBody
import com.onscreensync.tvapp.apiresponses.TokenApiResponse
import com.onscreensync.tvapp.services.LocalStorageService
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val tokenApiRequest: TokenApiRequest,
    private val storageService: LocalStorageService
) {
    suspend fun refreshAccessToken(): Result<TokenApiResponse> {
        val refreshToken = storageService.refreshToken ?: return Result.failure(Exception("No refresh token available"))
        val url = storageService.getData(DisplayApiConfigConstants.DEVICE_REFRESH_TOKEN_REQUEST_URL) 
            ?: return Result.failure(Exception("Refresh token URL not found"))

        return try {
            val response = tokenApiRequest.refreshTokenRequest(
                url,
                TokenApiRequestBody("", "string", "", Constants.TOKEN_REFRESH_GRANT_TYPE),
                "Bearer $refreshToken"
            ).awaitResponse()

            if (response.isSuccessful && response.body() != null) {
                val tokens = response.body()!!
                storageService.accessToken = tokens.accessToken
                storageService.refreshToken = tokens.refreshToken
                Result.success(tokens)
            } else {
                Result.failure(Exception("Failed to refresh token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}