package com.onscreensync.tvapp.apiresponses.configs;

import com.google.gson.annotations.SerializedName;

public class DisplayApiConfig {
    @SerializedName("base-endpoint")
    private String baseEndpoint;
    @SerializedName("device-code-url")
    private String deviceCodeUrl;
    @SerializedName("device-info-url")
    private String deviceInfoUrl;
    @SerializedName("device-token-request-url")
    private String deviceTokenRequestUrl;
    @SerializedName("device-refresh-token-request-url")
    private String deviceRefreshTokenRequestUrl;
    @SerializedName("content-data-url")
    private String contentDataUrl;
    @SerializedName("signalr-negotiation-url")
    private String signalrNegotiationUrl;
    @SerializedName("signalr-add-connection-url")
    private String signalrAddConnectionUrl;
    @SerializedName("signalr-remove-connection-url")
    private String signalrRemoveConnectionUrl;
    @SerializedName("messages")
    private DisplayApiConfigMessages configMessages;

    public String getBaseEndpoint() {
        return baseEndpoint;
    }

    public String getDeviceCodeUrl() {
        return deviceCodeUrl;
    }

    public String getDeviceInfoUrl() {
        return deviceInfoUrl;
    }

    public String getDeviceTokenRequestUrl() {
        return deviceTokenRequestUrl;
    }

    public String getDeviceRefreshTokenRequestUrl() {
        return deviceRefreshTokenRequestUrl;
    }

    public String getContentDataUrl() {
        return contentDataUrl;
    }

    public String getSignalrNegotiationUrl() {
        return signalrNegotiationUrl;
    }

    public String getSignalrAddConnectionUrl() {
        return signalrAddConnectionUrl;
    }

    public String getSignalrRemoveConnectionUrl() {
        return signalrRemoveConnectionUrl;
    }

    public DisplayApiConfigMessages getConfigMessages() {
        return configMessages;
    }
}
