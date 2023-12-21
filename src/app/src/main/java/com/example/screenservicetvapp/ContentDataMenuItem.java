package com.example.screenservicetvapp;

import com.google.gson.annotations.SerializedName;

public class ContentDataMenuItem {
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;

    @SerializedName("iconUrl")
    private String iconUrl;

    @SerializedName("price")
    private String price;

    @SerializedName("discountPrice")
    private String discountPrice;
}
