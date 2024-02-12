package com.onscreensync.tvapp.datamodels;

import com.google.gson.annotations.SerializedName;

public class PlaylistData {

    @SerializedName("itemDuration")
    private String itemDuration;

    @SerializedName("items")
    private Object[] items;

    @SerializedName("itemsSerialized")
    private PlaylistItemSerializedDataModel[] itemsSerialized;

    public Object[] getItems() {
        return items;
    }

    public String getItemDuration() {
        return itemDuration;
    }

    public PlaylistItemSerializedDataModel[] getItemsSerialized() {
        return itemsSerialized;
    }
}
