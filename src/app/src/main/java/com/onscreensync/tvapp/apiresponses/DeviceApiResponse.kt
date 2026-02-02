package com.onscreensync.tvapp.apiresponses

import com.google.gson.annotations.SerializedName

data class DeviceApiResponse(
    @SerializedName("deviceName")
    val name: String?,
    @SerializedName("tenantId")
    val tenantId: String?,
    @SerializedName("id")
    val id: String?
)
