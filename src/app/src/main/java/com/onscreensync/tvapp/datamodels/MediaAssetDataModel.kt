package com.onscreensync.tvapp.datamodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaAssetDataModel(
    val type: Int,
    val assetUrl: String?,
    val description: String?,
    val name: String?
) : Parcelable
