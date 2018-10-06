package com.abbyberkers.apphome

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.abbyberkers.apphome.storage.SharedPreferenceHelper
import com.abbyberkers.apphome.ui.MainUI
import org.jetbrains.anko.*

class MainAct : AppCompatActivity() {

    val mainUI by lazy { MainUI() }
    val sharedPref by lazy { SharedPreferenceHelper(applicationContext) }

    /**
     * Called when the app is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainUI.setContentView(this)
        // Refreshing the direction on the user is done by onResume(), which is always executed
        // when opening the app.
    }

    /**
     * Called when the app is resumed. E.g., exit the app by pressing the home button and then
     * reopening the app.
     */
    override fun onResume() {
        super.onResume()
        // Refresh the direction of the user.
        setDefaultDirection()
    }

    /**
     * Check if a user is chosen and set their preferred direction on the spinners.
     * If no user is chosen show the dialog to choose a user.
     */
    fun setDefaultDirection() {
        sharedPref.let {
            // Check if the user is configured by checking if the key exists in the
            // shared preferences.
            if(it.sharedPref.contains("user")) {
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

    /**
     * Create and inflate the menu from the menu xml.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        mainUI.chooseUserItem = menu!!.getItem(0)
        if(sharedPref.sharedPref.contains("user")) {
            val user = sharedPref.getUserPreference()!!
            menu.getItem(0).title = user.name
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Set the listener on the menu.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.change_user -> {
                // Show the dialog to choose a user.
                mainUI.userDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}