package com.onscreensync.tvapp.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MenuMetadata implements Parcelable {
    private String currency;
    private String description;
    private String title;
    private String iconUrl;
    private String subType;

    protected MenuMetadata(Parcel in) {
        currency = in.readString();
        description = in.readString();
        title = in.readString();
        iconUrl = in.readString();
        subType = in.readString();
    }
    public MenuMetadata(String currency, String description, String title, String iconUrl, String subType) {
        this.currency = currency;
        this.description = description;
        this.title = title;
        this.iconUrl = iconUrl;
        this.subType = subType;
    }

    public static final Creator<MenuMetadata> CREATOR = new Creator<MenuMetadata>() {
        @Override
        public MenuMetadata createFromParcel(Parcel in) {
            return new MenuMetadata(in);
        }

        @Override
        public MenuMetadata[] newArray(int size) {
            return new MenuMetadata[size];
        }
    };

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getSubType() {
        return subType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(currency);
        parcel.writeString(description);
        parcel.writeString(title);
        parcel.writeString(iconUrl);
        parcel.writeString(subType);
    }
}
