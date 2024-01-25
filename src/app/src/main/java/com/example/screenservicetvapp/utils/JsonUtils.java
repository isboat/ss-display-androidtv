package com.example.screenservicetvapp.utils;

import com.example.screenservicetvapp.ObjectExtensions;
import com.google.gson.Gson;

public class JsonUtils {
    private static final Gson gson = new Gson();

    // Generic method for deserialization
    public static <T> T fromJson(String jsonString, Class<T> classOfT) {
        if(ObjectExtensions.isNullOrEmpty(jsonString)) return null;

        return gson.fromJson(jsonString, classOfT);
    }
}
