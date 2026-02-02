package com.onscreensync.tvapp.datamodels

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistItemSerializedDataModel(
    @SerializedName("key")
    val key: String?,
    @SerializedName("value")
    val value: String?
) : Parcelable
