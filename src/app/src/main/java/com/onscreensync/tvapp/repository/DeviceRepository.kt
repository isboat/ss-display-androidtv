package com.onscreensync.tvapp.repository

import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.apirequests.DeviceApiRequest
import com.onscreensync.tvapp.apiresponses.DeviceApiResponse
import com.onscreensync.tvapp.services.LocalStorageService
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceApiRequest: DeviceApiRequest,
    private val storageService: LocalStorageService
) {
    suspend fun updateDeviceInfo(): Result<DeviceApiResponse> {
        val accessToken = storageService.accessToken ?: return Result.failure(Exception("No access token"))
        val url = storageService.getData(DisplayApiConfigConstants.DEVICE_INFO_URL) ?: return Result.failure(Exception("Device info URL not found"))

        return try {
            val response = deviceApiRequest.getName(url, "Bearer $accessToken").awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                storageService.setData(Constants.DEVICE_NAME, data.name)
                storageService.setData(Constants.DEVICE_ID, data.id)
                storageService.setData(Constants.TENANT_ID, data.tenantId)
                Result.success(data)
            } else {
                Result.failure(Exception("Failed to get device info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}