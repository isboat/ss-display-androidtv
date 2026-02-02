package com.onscreensync.tvapp.datamodels

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItemDataModel(
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("iconUrl")
    val iconUrl: String?,
    @SerializedName("price")
    val price: String?,
    @SerializedName("discountPrice")
    val discountPrice: String?,
    @SerializedName("createdOn")
    val createdOn: String?,
    @SerializedName("updatedOn")
    val updatedOn: String?
) : Parcelable
