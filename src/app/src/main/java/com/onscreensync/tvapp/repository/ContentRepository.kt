package com.onscreensync.tvapp.repository

import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.apirequests.ContentDataApiRequest
import com.onscreensync.tvapp.apiresponses.ContentDataApiResponse
import com.onscreensync.tvapp.services.LocalStorageService
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val contentDataApiRequest: ContentDataApiRequest,
    private val storageService: LocalStorageService
) {
    suspend fun getContentData(): Result<ContentDataApiResponse> {
        val url = storageService.getData(DisplayApiConfigConstants.CONTENT_DATA_URL)
            ?: return Result.failure(Exception("Content data URL not found"))
        
        // Note: Auth header is now handled by AuthInterceptor
        return try {
            val response = contentDataApiRequest.getData(url, "").awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load content data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}