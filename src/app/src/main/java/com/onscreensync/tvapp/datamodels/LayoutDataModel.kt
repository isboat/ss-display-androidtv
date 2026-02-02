package com.onscreensync.tvapp.datamodels

import com.google.gson.annotations.SerializedName

data class LayoutDataModel(
    @SerializedName("templateKey")
    val templateKey: String?,
    @SerializedName("subType")
    val subType: String?,
    @SerializedName("templateProperties")
    val templateProperties: Array<LayoutTemplatePropertyDataModel>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LayoutDataModel

        if (templateKey != other.templateKey) return false
        if (subType != other.subType) return false
        if (templateProperties != null) {
            if (other.templateProperties == null) return false
            if (!templateProperties.contentEquals(other.templateProperties)) return false
        } else if (other.templateProperties != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = templateKey?.hashCode() ?: 0
        result = 31 * result + (subType?.hashCode() ?: 0)
        result = 31 * result + (templateProperties?.contentHashCode() ?: 0)
        return result
    }
}
