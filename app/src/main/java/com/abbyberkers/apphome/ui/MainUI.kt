package com.abbyberkers.apphome.ui

import android.app.AlertDialog
import android.preference.PreferenceManager
import android.view.View
import android.widget.Spinner
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.MainAct
import com.abbyberkers.apphome.R
import com.abbyberkers.apphome.communication.UserPreferences
import com.abbyberkers.apphome.communication.WhatsappCommunication
import com.abbyberkers.apphome.storage.saveUserPreference
import com.abbyberkers.apphome.ui.components.TimesRow
import com.abbyberkers.apphome.ui.components.spinnerWithListener
import com.abbyberkers.apphome.ui.components.textRow
import com.abbyberkers.apphome.ui.components.timesRow
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainAct> {

    lateinit var fromSpinner: Spinner
    lateinit var toSpinner: Spinner
    lateinit var timesSpinner: TimesRow
    lateinit var userDialog: AlertBuilder<AlertDialog>

    override fun createView(ui: AnkoContext<MainAct>): View = with(ui) {
        tableLayout {

            padding = 16

            textRow("From", "To", alignment = View.TEXT_ALIGNMENT_VIEW_START)

            tableRow {
                fromSpinner = spinnerWithListener(City.strings()) {
                    // Update the times spinner with the new value of the from spinner and the
                    // current value of the to spinner.
                    timesSpinner.update(City.values()[it], City.values()[toSpinner.selectedItemPosition])
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                toSpinner = spinnerWithListener(City.strings()) {
                    // Update the times spinner with the current value of the from spinner and the
                    // new value of the to spinner.
                    timesSpinner.update(City.values()[fromSpinner.selectedItemPosition], City.values()[it])
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            tableRow {
                button {
                    textResource = R.string.send
                    onClick {
                        val whatsapp = WhatsappCommunication(getContext())
                        // Send the WhatsApp message.
                        whatsapp.sendMessage(trip = timesSpinner.selectedTrip(),
                                destination = City.values()[toSpinner.selectedItemPosition],
                                userPreferences = UserPreferences.ABBY)
                    }
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                button {
                    textResource = R.string.send_delay
                    onClick {
                        val whatsapp = WhatsappCommunication(getContext())
                        // Send the delay.
                        whatsapp.send(timesSpinner.selectedTrip().delay())
                    }
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            timesSpinner = timesRow()

            userDialog = alert("Choose a user.", "Please choose a user to use this app.") {

                fun clickDialogButton(user: UserPreferences) {
                    // Get the shared preferences.
                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    // Save the user to the shared preferences.
                    prefs.saveUserPreference(user = user)
                    updateDirection(user)
                }
                // Make sure that the user chooses a user, by disabling the possibility that they
                // click outside the dialog to dismiss it.
                isCancelable = false
                positiveButton("Abby") {
                    clickDialogButton(UserPreferences.ABBY)
                }
                negativeButton("Thomas") {
                    clickDialogButton(UserPreferences.THOMAS)
                }
            }
        }
    }

    /**
     * Update the direction on the from and to spinners based on the preferences of a user.
     *
     * @param user of whom to use the preferences.
     */
    fun updateDirection(user: UserPreferences) {
        // Get the preferred direction from the user.
        val direction = user.getDirection()
        // Set this direction on the from and to spinners.
        fromSpinner.setSelection(City.values().indexOf(direction.from))
        toSpinner.setSelection(City.values().indexOf(direction.to))
    }
}