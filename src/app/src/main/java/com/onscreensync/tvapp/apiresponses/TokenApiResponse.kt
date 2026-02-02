package com.onscreensync.tvapp.apiresponses

import com.google.gson.annotations.SerializedName

data class TokenApiResponse(
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("expiresIn")
    val expiresIn: Long,
    @SerializedName("scope")
    var scope: String?,
    @SerializedName("tokenType")
    val tokenType: String?,
    @SerializedName("refreshToken")
    val refreshToken: String?
)
