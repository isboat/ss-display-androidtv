package com.example.screenservicetvapp.datamodels;

import com.google.gson.annotations.SerializedName;

public class LayoutDataModel {
    @SerializedName("templateKey")
    private String templateKey;
    @SerializedName("subType")
    private String subType;
    @SerializedName("templateProperties")
    private LayoutTemplatePropertyDataModel[] templateProperties;

    public String getTemplateKey() {
        return templateKey;
    }

    public String getSubType() {
        return subType;
    }

    public LayoutTemplatePropertyDataModel[] getTemplateProperties() {
        return templateProperties;
    }
}

