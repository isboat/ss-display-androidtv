package com.example.screenservicetvapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ContentDataPlaylistData {

    @SerializedName("itemDuration")
    private String itemDuration;

    @SerializedName("assetItems")
    private ContentDataMediaAsset[] assetItems;

    public ContentDataMediaAsset[] getAssetItems() {
        return assetItems;
    }

    public String getItemDuration() {
        return itemDuration;
    }
}
