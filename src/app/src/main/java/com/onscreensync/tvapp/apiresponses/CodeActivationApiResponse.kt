package com.onscreensync.tvapp.apiresponses

import com.google.gson.annotations.SerializedName

data class CodeActivationApiResponse(
    @SerializedName("deviceCode")
    val deviceCode: String?,
    @SerializedName("userCode")
    val userCode: String?,
    @SerializedName("verificationUrl")
    val verificationUrl: String?,
    @SerializedName("expiresIn")
    val expiresIn: Long,
    @SerializedName("interval")
    val interval: Long,
    @SerializedName("deviceName")
    val deviceName: String?,
    @SerializedName("clientId")
    val clientId: String?
)
