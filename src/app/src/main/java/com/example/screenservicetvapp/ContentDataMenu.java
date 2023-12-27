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

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public ContentDataMenuItem[] getMenuItems() {
        return menuItems;
    }
}

