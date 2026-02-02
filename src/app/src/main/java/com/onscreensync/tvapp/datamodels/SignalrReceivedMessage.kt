package com.onscreensync.tvapp.datamodels

import com.google.gson.annotations.SerializedName

data class SignalrReceivedMessage(
    @SerializedName("deviceId")
    val deviceId: String?,
    @SerializedName("tenantId")
    val tenantId: String?,
    @SerializedName("messageType")
    val messageType: String?,
    @SerializedName("messageData")
    val messageData: String?,
    @SerializedName("messageStatus")
    val messageStatus: String?
)
