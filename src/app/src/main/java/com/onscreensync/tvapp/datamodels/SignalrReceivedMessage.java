package com.onscreensync.tvapp.datamodels;

import com.google.gson.annotations.SerializedName;

public class SignalrReceivedMessage {
    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("tenantId")
    private String tenantId;

    @SerializedName("messageType")
    private String messageType;

    @SerializedName("messageData")
    private String messageData;

    @SerializedName("messageStatus")
    private String messageStatus;

    public String getMessageType() {
        return messageType;
    }

    public String getMessageData() {
        return messageData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
