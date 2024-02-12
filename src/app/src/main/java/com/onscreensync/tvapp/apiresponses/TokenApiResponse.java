package com.onscreensync.tvapp.apiresponses;

import com.google.gson.annotations.SerializedName;

public class TokenApiResponse {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("expiresIn")
    private long expiresIn;

    @SerializedName("scope")
    private String scope;

    @SerializedName("tokenType")
    private String tokenType;

    @SerializedName("refreshToken")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
