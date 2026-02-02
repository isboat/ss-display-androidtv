package com.onscreensync.tvapp.apiresponses.configs

import com.google.gson.annotations.SerializedName

data class DisplayApiConfig(
    @SerializedName("base-endpoint")
    val baseEndpoint: String?,
    @SerializedName("device-code-url")
    val deviceCodeUrl: String?,
    @SerializedName("device-info-url")
    val deviceInfoUrl: String?,
    @SerializedName("device-token-request-url")
    val deviceTokenRequestUrl: String?,
    @SerializedName("device-refresh-token-request-url")
    val deviceRefreshTokenRequestUrl: String?,
    @SerializedName("content-data-url")
    val contentDataUrl: String?,
    @SerializedName("signalr-negotiation-url")
    val signalrNegotiationUrl: String?,
    @SerializedName("signalr-add-connection-url")
    val signalrAddConnectionUrl: String?,
    @SerializedName("signalr-remove-connection-url")
    val signalrRemoveConnectionUrl: String?,
    @SerializedName("messages")
    val configMessages: DisplayApiConfigMessages?
)
