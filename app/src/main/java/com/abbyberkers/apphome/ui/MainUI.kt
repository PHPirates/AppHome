package com.abbyberkers.apphome.ui

import android.app.AlertDialog
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.MainAct
import com.abbyberkers.apphome.R
import com.abbyberkers.apphome.communication.MessageType
import com.abbyberkers.apphome.communication.UserPreferences
import com.abbyberkers.apphome.communication.WhatsappCommunication
import com.abbyberkers.apphome.communication.textformatters.Language
import com.abbyberkers.apphome.communication.textformatters.TextFormatter
import com.abbyberkers.apphome.converters.nsToCalendar
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid
import com.abbyberkers.apphome.storage.SharedPreferenceHelper
import com.abbyberkers.apphome.ui.components.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainAct> {

    lateinit var fromSpinner: Spinner
    lateinit var toSpinner: Spinner
    lateinit var languageSwitch: Switch
    lateinit var pluralSwitch: Switch
    lateinit var timesSpinner: TimesRow
    lateinit var previewText: TextView

    lateinit var userDialog: AlertBuilder<AlertDialog>
    lateinit var chooseUserItem: MenuItem

    override fun createView(ui: AnkoContext<MainAct>): View = with(ui) {
        tableLayout {

            // Note: user preferences are not null in any click/value listener, as the first thing
            // that happens on startup is that these user preferences will be set.

            padding = 16

            val prefs = SharedPreferenceHelper(context)

            textRow("From", "To", alignment = View.TEXT_ALIGNMENT_VIEW_START)

            tableRow {
                fromSpinner = spinnerWithListener(City.strings()) {
                    // Update the times spinner with the new value of the from spinner and the
                    // current value of the to spinner.
                    timesSpinner.update(City.values()[it], City.getSelectedCity(toSpinner))
                    // Hide the preview text as it is probably incorrect.
                    previewText.visibility = View.INVISIBLE
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                toSpinner = spinnerWithListener(City.strings()) {
                    // Update the times spinner with the current value of the from spinner and the
                    // new value of the to spinner.
                    timesSpinner.update(City.getSelectedCity(fromSpinner), City.values()[it])
                    // Hide the preview text as it is probably incorrect.
                    previewText.visibility = View.INVISIBLE
                    updateSwitch(prefs.getUserPreference())
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            languageSwitch = wordSwitch("English", "Dutch")
            languageSwitch.setOnCheckedChangeListener { _, boolean ->
                // Get the language corresponding to the new value of the languageSwitch.
                val language = if (boolean) Language.DUTCH else Language.ENGLISH
                val destination = City.getSelectedCity(toSpinner)
                // Update the preferred language for the current direction.
                val user = prefs.getUserPreference()!!
                user.alterLanguage(destination, language)
                prefs.saveUserPreference(user)
                // Update the preview text if the times are already available.
                if(timesSpinner.timePicker.visibility == View.VISIBLE) updatePreviewText(user)
                // Hide the preview text if the times are still loading.
                else previewText.visibility = View.INVISIBLE
            }

            pluralSwitch = wordSwitch("Singular", "Plural")
            pluralSwitch.setOnCheckedChangeListener { _, _ ->
                // Update the preview text if the times are already available.
                if(timesSpinner.timePicker.visibility == View.VISIBLE) updatePreviewText(prefs.getUserPreference()!!)
                // Hide the preview text if the times are still loading.
                else previewText.visibility = View.INVISIBLE
            }

            timesSpinner = timesRow()
            // When the selected time changes we should update the preview text.
            timesSpinner.timePicker.setOnValueChangedListener { _, _, newValue ->
                updatePreviewText(prefs.getUserPreference()!!, trip = timesSpinner.trips[newValue])
            }
            // When the times are loaded we should update the preview text.
            timesSpinner.onTimesLoaded = {
                updatePreviewText(prefs.getUserPreference()!!, trip = timesSpinner.selectedTrip())
            }

            previewText = textView { textAlignment = View.TEXT_ALIGNMENT_CENTER}

            tableRow {
                button {
                    textResource = R.string.send
                    onClick {
                        val whatsapp = WhatsappCommunication(getContext())
                        // Send the WhatsApp message.
                        whatsapp.sendMessage(trip = timesSpinner.selectedTrip(),
                                destination = City.getSelectedCity(toSpinner),
                                userPreferences = prefs.getUserPreference()!!,
                                plural = pluralSwitch.isChecked)
                    }
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                button {
                    textResource = R.string.send_delay
                    onClick {
                        val whatsapp = WhatsappCommunication(getContext())
                        // Send the delay.
                        whatsapp.sendMessage(trip = timesSpinner.selectedTrip(),
                                destination = City.getSelectedCity(toSpinner),
                                userPreferences = prefs.getUserPreference()!!,
                                plural = pluralSwitch.isChecked,
                                messageType = MessageType.DELAY)
                    }
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            userDialog = alert("Please choose a user to use this app.", "Choose a user.") {

                fun clickDialogButton(user: UserPreferences) {
                    // Change the name on the menu.
                    chooseUserItem.title = user.name
                    // Save the user to the shared preferences.
                    prefs.saveUserPreference(user = user)
                    updateDirection(user)
                    updateSwitch(user)
                    // Hide the preview text because it is probably incorrect when selecting another user.
                    previewText.visibility = View.INVISIBLE
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
     * Update the preview text.
     *
     * @param user of whom to use the language preferences.
     * @param destination of the trip to be formatted. The default value is the currently selected
     *      destination in the toSpinner.
     * @param trip to be formatted. The default value is the trip of which the time is currently
     *      selected.
     */
    private fun updatePreviewText(user: UserPreferences,
                          destination: City = City.getSelectedCity(toSpinner),
                          trip: ReisMogelijkheid = timesSpinner.selectedTrip()) {

        previewText.visibility = View.VISIBLE
        previewText.text = TextFormatter(user).applyTemplate(
                destination = destination,
                time = trip.arrivalTime.nsToCalendar(),
                plural = pluralSwitch.isChecked
        )
    }

    /**
     * Update the direction on the from and to spinners based on the preferences of a user.
     *
     * @param user of whom to use the preferences.
     */
    fun updateDirection(user: UserPreferences?) {
        // Only update if the user is not null (otherwise there is nothing to update).
        user?.let {
            // Get the preferred direction from the user.
            val direction = user.getDirection()
            // Set this direction on the from and to spinners.
            fromSpinner.setSelection(City.values().indexOf(direction.from))
            toSpinner.setSelection(City.values().indexOf(direction.to))
        }
    }

    /**
     * Set the value of the languageSwitch according to the preferences of the user.
     *
     * @param user of whom to use the preferences.
     */
    private fun updateSwitch(user: UserPreferences?) {
        // Only update if the user is not null (otherwise there is nothing to update).
        user?.let {
            val lang = user.languagePref.getValue(City.getSelectedCity(toSpinner)).first
            languageSwitch.isChecked = (lang == Language.DUTCH)
        }
    }
}