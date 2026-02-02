package com.onscreensync.tvapp.apirequests

import com.google.gson.annotations.SerializedName

data class TokenApiRequestBody(
    @SerializedName("clientId")
    var clientId: String?,
    @SerializedName("clientSecret")
    var clientSecret: String?,
    @SerializedName("deviceCode")
    var deviceCode: String?,
    @SerializedName("grantType")
    var grantType: String?
)
