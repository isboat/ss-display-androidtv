package com.onscreensync.tvapp.signalR;

import com.google.gson.annotations.SerializedName;

public class NegotiateApiResponse {

    @SerializedName("url")
    private String url;

    @SerializedName("accessToken")
    private String accessToken;

    public String getUrl() {
        return url;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
