package com.example.screenservicetvapp.services;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorageService {
    private static final String PREFERENCES_NAME = "com.example.screen_service";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final SharedPreferences sharedPreferences;

    public LocalStorageService(Context context) {
        // Initialize SharedPreferences
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setAccessToken(String accessToken) {
        // Save the access token in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.apply();
    }

    public String getAccessToken() {
        // Retrieve the access token from SharedPreferences
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public void setRefreshToken(String refreshToken) {
        // Save the access token in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.apply();
    }

    public String getRefreshToken() {
        // Retrieve the access token from SharedPreferences
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }

    public void setData(String dataStorageKey, String dataValue) {
        // Save the access token in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(dataStorageKey, dataValue);
        editor.apply();
    }

    public String getData(String dataStorageKey) {
        // Retrieve the access token from SharedPreferences
        return sharedPreferences.getString(dataStorageKey, null);
    }
}
