package com.onscreensync.tvapp.signalR

import com.google.gson.annotations.SerializedName

data class NegotiateApiResponse(
    @SerializedName("url")
    val url: String?,
    @SerializedName("accessToken")
    val accessToken: String?
)
