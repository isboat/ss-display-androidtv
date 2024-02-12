package com.onscreensync.tvapp.apiresponses;

import com.onscreensync.tvapp.datamodels.LayoutDataModel;
import com.onscreensync.tvapp.datamodels.MediaAssetDataModel;
import com.onscreensync.tvapp.datamodels.MenuDataModel;
import com.onscreensync.tvapp.datamodels.PlaylistData;
import com.google.gson.annotations.SerializedName;

public class ContentDataApiResponse {
    @SerializedName("id")
    private String id;
    @SerializedName("tenantId")
    private String tenantId;
    @SerializedName("displayName")
    private String displayName;

    @SerializedName("layout")
    private LayoutDataModel layout;

    @SerializedName("menu")
    private MenuDataModel menu;

    @SerializedName("mediaAsset")
    private MediaAssetDataModel mediaAsset;

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

    public MediaAssetDataModel getMediaAsset() {
        return mediaAsset;
    }

    public LayoutDataModel getLayout() {
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

    public MenuDataModel getMenu() {
        return menu;
    }
}

