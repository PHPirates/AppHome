package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import android.widget.NumberPicker
import com.abbyberkers.apphome.communication.UserPreferences
import com.abbyberkers.apphome.communication.textformatters.Language
import com.abbyberkers.apphome.communication.textformatters.TimesFormat
import com.abbyberkers.apphome.communication.textformatters.format
import com.abbyberkers.apphome.converters.nsToCalendar
import com.abbyberkers.apphome.converters.toCalendar
import com.abbyberkers.apphome.converters.toStrings
import com.abbyberkers.apphome.ns.json.Trip
import com.abbyberkers.apphome.storage.SharedPreferenceHelper
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import java.util.*

class TimesSpinner : NumberPicker {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        displayedValues = arrayOf("", "")
        wrapSelectorWheel = false
        minValue = 0
        maxValue = 1
        layoutParams = LayoutParams(wrapContent, matchParent)
    }

    private val sharedPref by lazy { SharedPreferenceHelper(context) }


    /**
     * Set the times of the journeys and their possible delays on the spinner.
     * If the journeys are null (there are no possible journeys), hide the spinner.
     * @param journeys A list of journeys to be on the spinner.
     */
    fun setTimes(trips: List<Trip>?) {
        if (trips != null) {
            wrapSelectorWheel = false  // Not including this will set it to true.
            displayedValues = trips.toStrings()
            minValue = 0
            maxValue = trips.size - 1
            value = trips.getIndexToBeSelected()
        } else {
            visibility = View.GONE
        }
    }

    /**
     * Get the index of the time that should be selected by default.
     *
     * This depends per user. For Abby, get the time from the train that left if she get's on the sprinter in Blerick now.
     * For Thomas, get the departure time of the next train that leaves.
     */
    private fun List<Trip>.getIndexToBeSelected(): Int {
        val now = Calendar.getInstance()
        // Convert all NS times to calendar times so we can compare them.
        val tripTimes = this.map { it.departureTime()!!.nsToCalendar() }
        // Get the index of the next train in the future.
        val indexOfNextTime = tripTimes.indexOf(tripTimes.first { now.before(it) })
        return when (sharedPref.getUserPreference()) {
            UserPreferences.ABBY -> indexOfNextTime - 2
            else -> indexOfNextTime
        }
    }
}

// For the use of timesSpinner().
fun ViewManager.timesSpinner(theme: Int = 0) = timesSpinner(theme) { }
// For the use of timesSpinner { }.
inline fun ViewManager.timesSpinner(theme: Int = 0, init: (TimesSpinner) -> Unit) = ankoView(::TimesSpinner, theme, init)