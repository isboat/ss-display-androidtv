package com.onscreensync.tvapp.datamodels

import com.google.gson.annotations.SerializedName

data class LayoutTemplatePropertyDataModel(
    @SerializedName("key")
    val key: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("label")
    val label: String?
)
