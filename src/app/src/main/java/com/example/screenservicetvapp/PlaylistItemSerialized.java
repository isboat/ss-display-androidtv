package com.example.screenservicetvapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class PlaylistItemSerialized implements Parcelable {

    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    protected PlaylistItemSerialized(Parcel in) {
        key = in.readString();
        value = in.readString();
    }

    public static final Creator<PlaylistItemSerialized> CREATOR = new Creator<PlaylistItemSerialized>() {
        @Override
        public PlaylistItemSerialized createFromParcel(Parcel in) {
            return new PlaylistItemSerialized(in);
        }

        @Override
        public PlaylistItemSerialized[] newArray(int size) {
            return new PlaylistItemSerialized[size];
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
