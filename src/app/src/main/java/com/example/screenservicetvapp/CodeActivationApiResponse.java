package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;
public class CodeActivationApiResponse {
    @SerializedName("deviceCode")
    private String deviceCode;

    @SerializedName("userCode")
    private String userCode;

    @SerializedName("verificationUrl")
    private String verificationUrl;

    @SerializedName("expiresIn")
    private int expiresIn;

    @SerializedName("interval")
    private int interval;

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("clientId")
    private String clientId;

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public int getInterval() {
        return interval;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getClientId() {
        return clientId;
    }
}
