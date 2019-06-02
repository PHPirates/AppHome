package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import android.widget.NumberPicker
import com.abbyberkers.apphome.converters.toStrings
import com.abbyberkers.apphome.ns.json.Trip
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

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
            value = (trips.size - 1) / 2
        } else {
            visibility = View.GONE
        }
    }
}

// For the use of timesSpinner().
fun ViewManager.timesSpinner(theme: Int = 0) = timesSpinner(theme) { }
// For the use of timesSpinner { }.
inline fun ViewManager.timesSpinner(theme: Int = 0, init: (TimesSpinner) -> Unit) = ankoView(::TimesSpinner, theme, init)