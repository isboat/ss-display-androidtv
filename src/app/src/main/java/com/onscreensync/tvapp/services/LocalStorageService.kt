package com.onscreensync.tvapp.services

import android.content.Context
import android.content.SharedPreferences

class LocalStorageService(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var accessToken: String?
        get() = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
        set(value) {
            sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, value).apply()
        }

    var refreshToken: String?
        get() = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
        set(value) {
            sharedPreferences.edit().putString(REFRESH_TOKEN_KEY, value).apply()
        }

    fun setData(dataStorageKey: String, dataValue: String?) {
        sharedPreferences.edit().putString(dataStorageKey, dataValue).apply()
    }

    fun getData(dataStorageKey: String): String? {
        return sharedPreferences.getString(dataStorageKey, null)
    }

    companion object {
        private const val PREFERENCES_NAME = "com.example.screen_service"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
