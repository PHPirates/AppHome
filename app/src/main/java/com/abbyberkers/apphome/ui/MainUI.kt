package com.abbyberkers.apphome.ui

import android.view.View
import android.widget.Spinner
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.MainAct
import com.abbyberkers.apphome.R
import com.abbyberkers.apphome.communication.WhatsappMessage
import com.abbyberkers.apphome.converters.fromNs
import com.abbyberkers.apphome.ui.components.TimesRow
import com.abbyberkers.apphome.ui.components.spinnerWithListener
import com.abbyberkers.apphome.ui.components.textRow
import com.abbyberkers.apphome.ui.components.timesRow
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainAct> {
    override fun createView(ui: AnkoContext<MainAct>): View = with(ui) {
        tableLayout {

            lateinit var fromSpinner: Spinner
            lateinit var toSpinner: Spinner
            lateinit var timesSpinner: TimesRow

            padding = 16

            textRow("From", "To", alignment = View.TEXT_ALIGNMENT_VIEW_START)

            tableRow {
                fromSpinner = spinnerWithListener(City.strings()) {
                    timesSpinner.update(City.values()[it], City.values()[toSpinner.selectedItemPosition])
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                toSpinner = spinnerWithListener(City.strings()) {
                    timesSpinner.update(City.values()[fromSpinner.selectedItemPosition], City.values()[it])
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            tableRow {
                button {
                    textResource = R.string.send
                    onClick {
                        toast(timesSpinner.selectedTrip().arrivalTime.fromNs())
                        val message = WhatsappMessage("ETA ${timesSpinner.selectedTrip().arrivalTime.fromNs()}")
                        message sendWith getContext()
                    }
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)

                button {
                    textResource = R.string.send_delay
                    onClick {
                        toast("send delay")
                        val message = WhatsappMessage(timesSpinner.selectedTrip().delay().toString())
                        message sendWith getContext()
                    }
                }.lparams(height = wrapContent, width = 0, initWeight = 1f)
            }

            timesSpinner = timesRow()
        }
    }
}