package com.onscreensync.tvapp.datamodels

import com.google.gson.annotations.SerializedName

data class MenuDataModel(
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("currency")
    val currency: String?,
    @SerializedName("iconUrl")
    val iconUrl: String?,
    @SerializedName("createdOn")
    val createdOn: String?,
    @SerializedName("updatedOn")
    val updatedOn: String?,
    @SerializedName("menuItems")
    val menuItems: Array<MenuItemDataModel>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuDataModel

        if (name != other.name) return false
        if (description != other.description) return false
        if (title != other.title) return false
        if (currency != other.currency) return false
        if (iconUrl != other.iconUrl) return false
        if (createdOn != other.createdOn) return false
        if (updatedOn != other.updatedOn) return false
        if (menuItems != null) {
            if (other.menuItems == null) return false
            if (!menuItems.contentEquals(other.menuItems)) return false
        } else if (other.menuItems != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (currency?.hashCode() ?: 0)
        result = 31 * result + (iconUrl?.hashCode() ?: 0)
        result = 31 * result + (createdOn?.hashCode() ?: 0)
        result = 31 * result + (updatedOn?.hashCode() ?: 0)
        result = 31 * result + (menuItems?.contentHashCode() ?: 0)
        return result
    }
}
