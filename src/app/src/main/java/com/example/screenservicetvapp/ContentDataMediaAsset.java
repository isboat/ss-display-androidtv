package com.example.screenservicetvapp;

import android.os.Parcel;
import android.os.Parcelable;

public class ContentDataMediaAsset implements Parcelable {
    private int type;
    private String assetUrl;
    private String description;
    private String name;

    protected ContentDataMediaAsset(Parcel in) {
        type = in.readInt();
        assetUrl = in.readString();
        description = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(assetUrl);
        dest.writeString(description);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ContentDataMediaAsset> CREATOR = new Creator<ContentDataMediaAsset>() {
        @Override
        public ContentDataMediaAsset createFromParcel(Parcel in) {
            return new ContentDataMediaAsset(in);
        }

        @Override
        public ContentDataMediaAsset[] newArray(int size) {
            return new ContentDataMediaAsset[size];
        }
    };

    public int getType() {
        return type;
    }

    public String getAssetUrl() {
        return assetUrl;
    }
}
