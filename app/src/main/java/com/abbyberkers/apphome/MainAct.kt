package com.abbyberkers.apphome

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.abbyberkers.apphome.storage.getUserPreference
import com.abbyberkers.apphome.ui.MainUI
import org.jetbrains.anko.setContentView

class MainAct : AppCompatActivity() {

    val mainUI by lazy { MainUI() }
    val sharedPref by lazy { getPreferences(Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainUI.setContentView(this)

        sharedPref.let {
            // Check if the user is configured by checking if the key exists in the
            // shared preferences.
            if(it.contains("user")) {
                // Update the direction on the from and to spinners based on the currently configured
                // user.
                mainUI.updateDirection(sharedPref.getUserPreference())
            } else {
                // Display dialog to pick user. By clicking a button in this dialog the new user will
                // be stored in the preferences.
                mainUI.userDialog.show()
            }
        }
    }
}