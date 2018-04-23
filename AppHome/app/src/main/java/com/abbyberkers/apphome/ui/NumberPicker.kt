package com.abbyberkers.apphome.ui

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
        visibility = View.VISIBLE
        minValue = 1
        maxValue = journeys.size
        displayedValues = journeys.toStrings()
        wrapSelectorWheel = false
        value = journeys.size / 2 + 1
        value
    } else {
        // If journeys is null: hide the numberpicker.
        visibility = View.GONE
        0
    }
}