package com.example.screenservicetvapp.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaAssetDataModel implements Parcelable {
    private int type;
    private String assetUrl;
    private String description;
    private String name;

    protected MediaAssetDataModel(Parcel in) {
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

    public static final Creator<MediaAssetDataModel> CREATOR = new Creator<MediaAssetDataModel>() {
        @Override
        public MediaAssetDataModel createFromParcel(Parcel in) {
            return new MediaAssetDataModel(in);
        }

        @Override
        public MediaAssetDataModel[] newArray(int size) {
            return new MediaAssetDataModel[size];
        }
    };

    public int getType() {
        return type;
    }

    public String getAssetUrl() {
        return assetUrl;
    }
}
