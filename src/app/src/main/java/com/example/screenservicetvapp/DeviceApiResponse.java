package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class DeviceApiResponse {
    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }
}
