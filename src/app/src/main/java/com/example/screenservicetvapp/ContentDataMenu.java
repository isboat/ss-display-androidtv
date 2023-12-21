package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class ContentDataMenu {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;

    @SerializedName("title")
    private String title;
    @SerializedName("currency")
    private String currency;

    @SerializedName("iconUrl")
    private String iconUrl;
    @SerializedName("menuItems")
    private ContentDataMenuItem[] menuItems;
}

