package com.example.screenservicetvapp;

import android.content.Intent;
import android.os.Parcelable;

public class ObjectExtensions {
    public static boolean isNullOrEmpty(String input) {
        return input == null || input.equals("");
    }

    public static <T extends Parcelable> T[] getParcelableArrayExtra(Intent intent, String key, Class<T> type) {
        Parcelable[] array = intent.getParcelableArrayExtra(key);

        if (array != null && type.isInstance(array[0])) {
            T[] result = (T[]) java.lang.reflect.Array.newInstance(type, array.length);

            for (int i = 0; i < array.length; i++) {
                //noinspection unchecked
                result[i] = (T) array[i];
            }

            return result;
        } else {
            // Handle the case where the array is null or the type doesn't match
            return null;
        }
    }



    public static int convertToInt(String input) {
        if(ObjectExtensions.isNullOrEmpty(input)) return 0;
        // Convert String to int
        try {
            int intValue = Integer.parseInt(input);
            return intValue;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
