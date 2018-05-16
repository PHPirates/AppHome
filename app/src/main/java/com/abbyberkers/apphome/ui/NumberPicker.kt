package com.abbyberkers.apphome.ui

import android.util.Log
import android.view.View
import android.widget.NumberPicker
import com.abbyberkers.apphome.converters.toStrings
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid

/**
 * Set departures on the departure number picker and return the currently selected
 * (by default) value.
 *
 * @param journeys List with all the possible journeys that have to be shown in the
 *      numberpicker.
 *
 * @return The index of the currently selected value.
 */
fun NumberPicker.setDepartures(journeys: List<ReisMogelijkheid>?): Int {
    return if (journeys != null) {
        // Get the strings of all possible trips.
        val journeyStrings = journeys.toStrings()

        visibility = View.VISIBLE
        minValue = 1
        maxValue = journeyStrings.size - 1
        displayedValues = journeyStrings
        wrapSelectorWheel = false
        value = journeyStrings.size / 2 + 1  // Default position of the numberpicker.
        value
    } else {
        // If journeys is null: hide the numberpicker.
        visibility = View.GONE
        0
    }
}