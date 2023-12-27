package com.example.screenservicetvapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ContentDataMenuItem implements Parcelable {
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }


    protected ContentDataMenuItem(Parcel in) {
        name = in.readString();
        description = in.readString();
        iconUrl = in.readString();
        price = in.readString();
        discountPrice = in.readString();
    }

    public static final Creator<ContentDataMenuItem> CREATOR = new Creator<ContentDataMenuItem>() {
        @Override
        public ContentDataMenuItem createFromParcel(Parcel in) {
            return new ContentDataMenuItem(in);
        }

        @Override
        public ContentDataMenuItem[] newArray(int size) {
            return new ContentDataMenuItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(iconUrl);
        parcel.writeString(price);
        parcel.writeString(discountPrice);
    }
}
