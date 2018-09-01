package com.abbyberkers.apphome.storage

import android.content.Context
import android.preference.PreferenceManager
import com.abbyberkers.apphome.communication.UserPreferences
import com.google.gson.Gson

class SharedPreferenceHelper(context: Context) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun saveUserPreference(user: UserPreferences) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(user)
        editor.putString("user", json)
        editor.apply()
    }

    fun getUserPreference() : UserPreferences {
        val gson = Gson()
        val json = sharedPreferences.getString("user", "")
        return gson.fromJson(json, UserPreferences::class.java)
    }

    fun contains(key: String) = sharedPreferences.contains(key)
}