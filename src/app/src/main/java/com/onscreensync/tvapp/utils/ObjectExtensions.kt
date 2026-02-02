package com.onscreensync.tvapp.utils

import android.content.Intent
import android.os.Build
import android.os.Parcelable

/**
 * Extension functions and utility methods for object manipulations.
 */

object ObjectExtensions {

    @JvmStatic
    fun isNullOrEmpty(str: String?): Boolean {
        return str == null || str.trim().isEmpty() || str == "null"
    }

    @JvmStatic
    fun convertToInt(str: String?, defaultValue: Int = 0): Int {
        if (isNullOrEmpty(str)) return defaultValue
        return try {
            str!!.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    @JvmStatic
    fun <T : Parcelable> getParcelableArrayExtra(intent: Intent, name: String, clazz: Class<T>): Array<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(name, clazz)
        } else {
            @Suppress("DEPRECATION")
            val parcelableArray = intent.getParcelableArrayExtra(name)
            if (parcelableArray != null) {
                java.lang.reflect.Array.newInstance(clazz, parcelableArray.size) as? Array<T>
            } else {
                null
            }
        }
    }
}

// Extension functions for convenience
fun String?.isNullOrEmpty(): Boolean = ObjectExtensions.isNullOrEmpty(this)
fun String?.convertToInt(defaultValue: Int = 0): Int = ObjectExtensions.convertToInt(this, defaultValue)
