package com.onscreensync.tvapp.datamodels

import com.google.gson.annotations.SerializedName

data class PlaylistData(
    @SerializedName("itemDuration")
    val itemDuration: String?,
    @SerializedName("items")
    val items: Array<Any>?,
    @SerializedName("itemsSerialized")
    val itemsSerialized: Array<PlaylistItemSerializedDataModel>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaylistData

        if (itemDuration != other.itemDuration) return false
        if (items != null) {
            if (other.items == null) return false
            if (!items.contentEquals(other.items)) return false
        } else if (other.items != null) return false
        if (itemsSerialized != null) {
            if (other.itemsSerialized == null) return false
            if (!itemsSerialized.contentEquals(other.itemsSerialized)) return false
        } else if (other.itemsSerialized != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemDuration?.hashCode() ?: 0
        result = 31 * result + (items?.contentHashCode() ?: 0)
        result = 31 * result + (itemsSerialized?.contentHashCode() ?: 0)
        return result
    }
}
