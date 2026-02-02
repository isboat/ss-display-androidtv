package com.onscreensync.tvapp.apirequests

import com.onscreensync.tvapp.apiresponses.TokenApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TokenApiRequest {
    @POST("{fullUrl}")
    fun tokenRequest(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Body requestBody: TokenApiRequestBody
    ): Call<TokenApiResponse>

    @POST("{fullUrl}")
    fun refreshTokenRequest(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Body requestBody: TokenApiRequestBody,
        @Header("Authorization") authHeader: String
    ): Call<TokenApiResponse>
}
