package com.onscreensync.tvapp.apiresponses

import com.google.gson.annotations.SerializedName
import com.onscreensync.tvapp.datamodels.LayoutDataModel
import com.onscreensync.tvapp.datamodels.MediaAssetDataModel
import com.onscreensync.tvapp.datamodels.MenuDataModel
import com.onscreensync.tvapp.datamodels.PlaylistData

data class ContentDataApiResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("tenantId")
    val tenantId: String?,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("layout")
    val layout: LayoutDataModel?,
    @SerializedName("menu")
    val menu: MenuDataModel?,
    @SerializedName("mediaAsset")
    val mediaAsset: MediaAssetDataModel?,
    @SerializedName("textEditorData")
    val textEditorData: String?,
    @SerializedName("externalMediaSource")
    val externalMediaSource: String?,
    @SerializedName("checksum")
    val checksum: String?,
    @SerializedName("playlistData")
    val playlistData: PlaylistData?
)
