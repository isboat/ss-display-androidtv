package com.onscreensync.tvapp.apiresponses;

import com.google.gson.annotations.SerializedName;
import com.onscreensync.tvapp.apiresponses.configs.DisplayApiConfig;

public class ConfigApiResponse {

    @SerializedName("display-api")
    private DisplayApiConfig displayApiConfig;

    public DisplayApiConfig getDisplayApiConfig() {
        return displayApiConfig;
    }
}
