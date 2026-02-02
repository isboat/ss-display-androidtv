package com.onscreensync.tvapp.apiresponses

import com.google.gson.annotations.SerializedName
import com.onscreensync.tvapp.apiresponses.configs.DisplayApiConfig

data class ConfigApiResponse(
    @SerializedName("display-api")
    val displayApiConfig: DisplayApiConfig?
)
