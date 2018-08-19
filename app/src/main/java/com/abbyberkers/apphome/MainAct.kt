package com.abbyberkers.apphome

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.abbyberkers.apphome.storage.getUserPreference
import com.abbyberkers.apphome.ui.MainUI
import org.jetbrains.anko.setContentView

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainUI = MainUI()
        mainUI.setContentView(this)

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val user = sharedPreferences.let {
            if(it.contains("user")) {
                it.getUserPreference()
            } else {
                // Display fragment to pick user. In this fragment the new user will be stored in
                // the preferences.
                it.getUserPreference()
            }
        }

        // Get the prefered direction from the user.
        val direction = user.getDirection()
        // Set this direction on the from and to spinners.
        mainUI.fromSpinner.setSelection(City.values().indexOf(direction.from))
        mainUI.toSpinner.setSelection(City.values().indexOf(direction.to))
    }

}