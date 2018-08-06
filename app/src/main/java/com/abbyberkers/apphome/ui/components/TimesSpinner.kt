package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.abbyberkers.apphome.R
import com.abbyberkers.apphome.converters.toStrings
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

class TimesSpinner : Spinner {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        adapter = ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, arrayOf(" "))
        layoutParams = LayoutParams(wrapContent, matchParent)
    }

    /**
     * Set the times of the journeys and their possible delays on the spinner.
     * If the journeys are null (there are no possible journeys), hide the spinner.
     * @param journeys A list of journeys to be on the spinner.
     */
    fun setTimes(journeys: List<ReisMogelijkheid>?) {
        if (journeys != null) {
            adapter = ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item,
                    journeys.toStrings())
        } else {
            visibility = View.GONE
        }
    }
}

// For the use of timesSpinner().
fun ViewManager.timesSpinner(theme: Int = 0) = timesSpinner(theme) { }
// For the use of timesSpinner { }.
inline fun ViewManager.timesSpinner(theme: Int = 0, init: (TimesSpinner) -> Unit) = ankoView(::TimesSpinner, theme, init)