package com.abbyberkers.apphome.ui

import android.view.View
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.MainAct
import com.abbyberkers.apphome.R
import com.abbyberkers.apphome.ui.components.TimesRow
import com.abbyberkers.apphome.ui.components.spinnerWithListener
import com.abbyberkers.apphome.ui.components.textRow
import com.abbyberkers.apphome.ui.components.timesRow
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainAct> {
    override fun createView(ui: AnkoContext<MainAct>): View = with(ui) {
        tableLayout {

            lateinit var timesSpinner: TimesRow

            padding = 16

            textRow("From", "To", alignment = View.TEXT_ALIGNMENT_VIEW_START)

            // TODO replace by function that refreshes the times
            val spinnerItemSelected: (Int) -> Unit = {position ->
                timesSpinner.update(position)
            }
            tableRow {
                val fromSpinner = spinnerWithListener(City.strings(), spinnerItemSelected)
                        .lparams(height = wrapContent, width = 0, initWeight = 1f)

                val toSpinner = spinnerWithListener(City.strings(), spinnerItemSelected)
                        .lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            tableRow {
                button {
                    textResource = R.string.send
                    onClick {
                        toast("send message")
                    } // TODO replace by function that sends WhatsApp message.
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                button {
                    textResource = R.string.send_delay
                    onClick {
                        toast("send delay")
                    } // TODO replace by function that sends delay.
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            timesSpinner = timesRow()
        }
    }
}