package com.onscreensync.tvapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
    private static final Gson gson = new Gson();

    // Generic method for deserialization
    public static <T> T fromJson(String jsonString, Class<T> classOfT) {
        if(ObjectExtensions.isNullOrEmpty(jsonString)) return null;

        try {
            return gson.fromJson(jsonString, classOfT);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
