package com.abbyberkers.apphome.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.abbyberkers.apphome.communication.UserPreferences
import com.google.gson.Gson

/**
 * Class to handle all 'communication' with the shared preferences.
 */
class SharedPreferenceHelper(private val context: Context) {

    val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    /**
     * Save a user preference to the shared preferences.
     *
     * @param user the user to save.
     * @param key (optional) the key to save the user to. This is "user" by default
     */
    fun saveUserPreference(user: UserPreferences, key: String = "user") {
        val editor = sharedPref.edit()
        // Convert the UserPreferences object to a string.
        val gson = Gson()
        val json = gson.toJson(user)
        // Put the string in the shared preferences.
        editor.putString(key, json)
        // Save the shared preferences.
        editor.apply()
    }

    /**
     * Get a user preference from the shared preferences.
     *
     * @param key (optional) the key from the preference to get. This is "user" by default.
     *
     * @return [UserPreferences] if they were set, null otherwise.
     */
    fun getUserPreference(key: String = "user",
                          user: UserPreferences = UserPreferences.ABBY): UserPreferences? {
        val json = sharedPref.getString(key, "")
        // Convert the string to a UserPreference object.
        return Gson().fromJson(json, user::class.java)
    }
}

