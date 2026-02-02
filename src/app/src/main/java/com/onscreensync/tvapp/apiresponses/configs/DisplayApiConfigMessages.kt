package com.onscreensync.tvapp.apiresponses.configs

import com.google.gson.annotations.SerializedName

data class DisplayApiConfigMessages(
    @SerializedName("app-title")
    val appTitle: String?
)
