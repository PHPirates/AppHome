package com.abbyberkers.apphome.storage

import android.content.SharedPreferences
import com.abbyberkers.apphome.communication.UserPreferences
import com.google.gson.Gson

fun SharedPreferences.saveUserPreference(user: UserPreferences, key: String = "user") {
    val editor = edit()
    val gson = Gson()
    val json = gson.toJson(user)
    editor.putString(key, json)
    editor.apply()
}

fun SharedPreferences.getUserPreference(key: String = "user",
                                        user: UserPreferences = UserPreferences.ABBY): UserPreferences {
    val gson = Gson()
    val json = getString(key, "")
    return gson.fromJson(json, user::class.java)
}