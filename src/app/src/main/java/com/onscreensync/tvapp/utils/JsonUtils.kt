package com.onscreensync.tvapp.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object JsonUtils {
    private val gson = Gson()

    @JvmStatic
    fun <T> fromJson(jsonString: String?, classOfT: Class<T>): T? {
        if (jsonString.isNullOrEmpty()) return null

        return try {
            gson.fromJson(jsonString, classOfT)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}