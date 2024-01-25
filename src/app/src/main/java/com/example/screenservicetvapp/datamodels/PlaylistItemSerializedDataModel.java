package com.example.screenservicetvapp.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class PlaylistItemSerializedDataModel implements Parcelable {

    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    protected PlaylistItemSerializedDataModel(Parcel in) {
        key = in.readString();
        value = in.readString();
    }

    public static final Creator<PlaylistItemSerializedDataModel> CREATOR = new Creator<PlaylistItemSerializedDataModel>() {
        @Override
        public PlaylistItemSerializedDataModel createFromParcel(Parcel in) {
            return new PlaylistItemSerializedDataModel(in);
        }

        @Override
        public PlaylistItemSerializedDataModel[] newArray(int size) {
            return new PlaylistItemSerializedDataModel[size];
        }
    };

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(value);
    }
}
