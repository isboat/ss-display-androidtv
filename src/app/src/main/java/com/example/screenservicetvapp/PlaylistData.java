package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class PlaylistData {

    @SerializedName("itemDuration")
    private String itemDuration;

    @SerializedName("items")
    private Object[] items;

    @SerializedName("itemsSerialized")
    private PlaylistItemSerialized[] itemsSerialized;

    /*
    * @SerializedName("assetItems")
    private ContentDataMediaAsset[] assetItems;
    * */

    public Object[] getItems() {
        return items;
    }

    public String getItemDuration() {
        return itemDuration;
    }

    public PlaylistItemSerialized[] getItemsSerialized() {
        return itemsSerialized;
    }
}
