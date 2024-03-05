package com.onscreensync.tvapp.datamodels;

import com.google.gson.annotations.SerializedName;

public class MenuDataModel {
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
    @SerializedName("createdOn")
    private String createdOn;

    @SerializedName("updatedOn")
    private String updatedOn;
    @SerializedName("menuItems")
    private MenuItemDataModel[] menuItems;

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

    public MenuItemDataModel[] getMenuItems() {
        return menuItems;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public String getCreatedOn() {
        return createdOn;
    }
}

