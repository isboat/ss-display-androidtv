package com.example.screenservicetvapp.apiresponses;

import com.google.gson.annotations.SerializedName;
public class CodeActivationApiResponse {
    @SerializedName("deviceCode")
    private String deviceCode;

    @SerializedName("userCode")
    private String userCode;

    @SerializedName("verificationUrl")
    private String verificationUrl;

    @SerializedName("expiresIn")
    private long expiresIn;

    @SerializedName("interval")
    private long interval;

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

    public long getExpiresIn() {
        return expiresIn;
    }

    public long getInterval() {
        return interval;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getClientId() {
        return clientId;
    }
}
