package com.onscreensync.tvapp.apirequests

import com.onscreensync.tvapp.apiresponses.ConfigApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface ConfigApiRequest {
    @GET("config.json")
    fun loadConfig(): Call<ConfigApiResponse>
}
