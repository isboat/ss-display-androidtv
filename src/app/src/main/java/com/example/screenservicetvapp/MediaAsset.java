package com.example.screenservicetvapp;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaAsset implements Parcelable {
    private int type;
    private String assetUrl;
    private String description;
    private String name;

    protected MediaAsset(Parcel in) {
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

    public static final Creator<MediaAsset> CREATOR = new Creator<MediaAsset>() {
        @Override
        public MediaAsset createFromParcel(Parcel in) {
            return new MediaAsset(in);
        }

        @Override
        public MediaAsset[] newArray(int size) {
            return new MediaAsset[size];
        }
    };

    public int getType() {
        return type;
    }

    public String getAssetUrl() {
        return assetUrl;
    }
}
