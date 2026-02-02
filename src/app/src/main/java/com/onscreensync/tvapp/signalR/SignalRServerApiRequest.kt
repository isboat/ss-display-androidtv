package com.onscreensync.tvapp.signalR

import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SignalRServerApiRequest {
    @POST("{fullUrl}")
    fun negotiate(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Query("deviceId") deviceId: String,
        @Header("Authorization") authHeader: String
    ): Call<NegotiateApiResponse>

    @POST("{fullUrl}")
    fun addToGroup(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Query("deviceId") deviceId: String,
        @Query("deviceName") deviceName: String,
        @Query("connectionId") connectionId: String,
        @Header("Authorization") authHeader: String
    ): Call<AddToGroupApiResponse>

    @POST("{fullUrl}")
    fun removeConnection(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Query("deviceId") deviceId: String,
        @Query("deviceName") deviceName: String,
        @Query("connectionId") connectionId: String,
        @Header("Authorization") authHeader: String
    ): Call<RemoveConnectionApiResponse>
}
