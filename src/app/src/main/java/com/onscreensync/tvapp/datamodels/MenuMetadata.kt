package com.onscreensync.tvapp.datamodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuMetadata(
    val currency: String?,
    val description: String?,
    val title: String?,
    val iconUrl: String?,
    val subType: String?
) : Parcelable
