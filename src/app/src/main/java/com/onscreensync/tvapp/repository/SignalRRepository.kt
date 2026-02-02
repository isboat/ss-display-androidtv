package com.onscreensync.tvapp.repository

import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.services.LocalStorageService
import com.onscreensync.tvapp.signalR.AddToGroupApiResponse
import com.onscreensync.tvapp.signalR.NegotiateApiResponse
import com.onscreensync.tvapp.signalR.RemoveConnectionApiResponse
import com.onscreensync.tvapp.signalR.SignalRServerApiRequest
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRRepository @Inject constructor(
    private val signalRServerApiRequest: SignalRServerApiRequest,
    private val storageService: LocalStorageService
) {
    suspend fun negotiate(): Result<NegotiateApiResponse> {
        val deviceId = storageService.getData(Constants.DEVICE_ID) ?: ""
        val url = storageService.getData(DisplayApiConfigConstants.SIGNALR_NEGOTIATION_URL)
            ?: return Result.failure(Exception("SignalR Negotiation URL not found"))

        return try {
            // Auth header is now handled by AuthInterceptor
            val response = signalRServerApiRequest.negotiate(url, deviceId, "").awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("SignalR Negotiation failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToGroup(connectionId: String): Result<AddToGroupApiResponse> {
        val deviceId = storageService.getData(Constants.DEVICE_ID) ?: ""
        val deviceName = storageService.getData(Constants.DEVICE_NAME) ?: ""
        val url = storageService.getData(DisplayApiConfigConstants.SIGNALR_ADD_CONNECTION_URL)
            ?: return Result.failure(Exception("SignalR Add Connection URL not found"))

        return try {
            val response = signalRServerApiRequest.addToGroup(url, deviceId, deviceName, connectionId, "").awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                storageService.setData(Constants.CONNECTION_ID, connectionId)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("SignalR Add to Group failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeConnection(connectionId: String? = null): Result<RemoveConnectionApiResponse> {
        val deviceId = storageService.getData(Constants.DEVICE_ID) ?: ""
        val deviceName = storageService.getData(Constants.DEVICE_NAME) ?: ""
        val connId = connectionId ?: storageService.getData(Constants.CONNECTION_ID)
            ?: return Result.failure(Exception("No connection ID found"))
        val url = storageService.getData(DisplayApiConfigConstants.SIGNALR_REMOVE_CONNECTION_URL)
            ?: return Result.failure(Exception("SignalR Remove Connection URL not found"))

        return try {
            val response = signalRServerApiRequest.removeConnection(url, deviceId, deviceName, connId, "").awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("SignalR Remove Connection failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}