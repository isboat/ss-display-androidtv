package com.onscreensync.tvapp.apirequests

import com.onscreensync.tvapp.apiresponses.DeviceApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface DeviceApiRequest {
    @GET("{fullUrl}")
    fun getName(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Header("Authorization") authHeader: String
    ): Call<DeviceApiResponse>
}
