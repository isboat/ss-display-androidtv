package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class ContentDataLayout {
    @SerializedName("templateKey")
    private String templateKey;
    @SerializedName("subType")
    private String subType;
    @SerializedName("templateProperties")
    private LayoutTemplateProperty[] templateProperties;

    public String getTemplateKey() {
        return templateKey;
    }

    public String getSubType() {
        return subType;
    }

    public LayoutTemplateProperty[] getTemplateProperties() {
        return templateProperties;
    }
}

