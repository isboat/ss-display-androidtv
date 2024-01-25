package com.example.screenservicetvapp.datamodels;

import com.google.gson.annotations.SerializedName;

public class LayoutTemplatePropertyDataModel {
    @SerializedName("key")
    private String key;
    @SerializedName("value")
    private String value;
    @SerializedName("label")
    private String label;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
