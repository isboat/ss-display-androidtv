package com.onscreensync.tvapp.apirequests

import com.onscreensync.tvapp.apiresponses.CodeActivationApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CodeActivationApiRequest {
    @POST("{fullUrl}")
    fun deviceCode(
        @Path(value = "fullUrl", encoded = true) fullUrl: String,
        @Body requestBody: CodeActivationRequestBody
    ): Call<CodeActivationApiResponse>
}
