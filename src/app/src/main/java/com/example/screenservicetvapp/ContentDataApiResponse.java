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
    private MediaAsset mediaAsset;

    @SerializedName("textEditorData")
    private String textEditorData;

    @SerializedName("externalMediaSource")
    private String externalMediaSource;

    @SerializedName("checksum")
    private String checksum;

    @SerializedName("playlistData")
    private PlaylistData playlistData;

    public String getExternalMediaSource() {
        return externalMediaSource;
    }

    public MediaAsset getMediaAsset() {
        return mediaAsset;
    }

    public ContentDataLayout getLayout() {
        return layout;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getTextEditorData() {
        return textEditorData;
    }

    public PlaylistData getPlaylistData() {
        return playlistData;
    }

    public ContentDataMenu getMenu() {
        return menu;
    }
}

