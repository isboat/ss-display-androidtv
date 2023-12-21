package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class ContentDataLayout {
    @SerializedName("templateKey")
    private String templateKey;
    @SerializedName("subType")
    private String subType;
    @SerializedName("templateProperties")
    private LayoutTemplateProperty[] templateProperties;
}

