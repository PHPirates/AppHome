package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.view.ViewManager
import android.widget.TableRow
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView

class TimesRow(context: Context) : TableRow(context) {
    val dummyText = textView(" ")
    val timesSpinner = stringSpinner()
    val delayText = textView("hi?")

    init {
        tableRow {
            dummyText.lparams(height = wrapContent, width = 0, initWeight = 1f)
            timesSpinner.lparams(height = wrapContent, width = 0, initWeight = 1f)
            delayText.lparams(height = wrapContent, width = 0, initWeight = 1f)
        }
    }

    /**
     * Get the new times from NS and update the times in the spinner accordingly.
     * @param from Departure City.
     * @param to Arrival City.
     */
    fun update(i: Int) {
        // TODO (from, to) -> new times & delays
        delayText.text = "$i"
        timesSpinner.setItems("$i", "${i+1}", "hi")
    }
}

fun ViewManager.timesRow(theme: Int = 0) = timesRow(theme) {}
inline fun ViewManager.timesRow(theme: Int = 0, init: TimesRow.() -> Unit) = ankoView({ TimesRow(it) }, theme, init)

