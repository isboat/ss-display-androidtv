package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class ContentDataApiResponse {
    @SerializedName("id")
    private String id;
    @SerializedName("tenantId")
    private String tenantId;
    @SerializedName("displayName")
    private String displayName;

    @SerializedName("layout")
    private ContentDataLayout layout;

    @SerializedName("menu")
    private ContentDataMenu menu;

    @SerializedName("mediaAsset")
    private ContentDataMediaAsset mediaAsset;

    @SerializedName("externalMediaSource")
    private String externalMediaSource;

    public String getExternalMediaSource() {
        return externalMediaSource;
    }

    public ContentDataMediaAsset getMediaAsset() {
        return mediaAsset;
    }

    public ContentDataLayout getLayout() {
        return layout;
    }
}

