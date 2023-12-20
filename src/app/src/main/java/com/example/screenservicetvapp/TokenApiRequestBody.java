package com.example.screenservicetvapp;

public class TokenApiRequestBody {
    private String clientId;

    private String clientSecret;

    private String deviceCode;

    private String grantType;

    public TokenApiRequestBody(String clientId, String clientSecret, String deviceCode, String grantType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.deviceCode = deviceCode;
        this.grantType = grantType;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
