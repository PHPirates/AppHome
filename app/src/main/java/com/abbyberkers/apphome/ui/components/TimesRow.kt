package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.TableRow
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.ns.NsApiService
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheden
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimesRow(context: Context) : TableRow(context) {
    private val timesSpinner = timesSpinner { gravity = Gravity.CENTER }
    private val progress = progressBar()

    private lateinit var trips: List<ReisMogelijkheid>

    init {
        tableRow {
            timesSpinner.lparams(height = wrapContent, width = wrapContent)
            progress.lparams(height = wrapContent, width = 0, initWeight = 1f)
        }
        showTimes()

    }

    /**
     * Get the new times from NS and update the times in the spinner accordingly.
     * @param from Departure City.
     * @param to Arrival City.
     */
    fun update(from: City, to: City) {
        hideTimes()

        if (from == to) {
            timesSpinner.setTimes(null)
            progress.visibility = View.GONE
        } else {
            // Prepare a call to the API.
            val call = NsApiService.create().listTrips(
                    fromStation = from.station,
                    toStation = to.station
            )

            // Make the call on another thread.
            call.enqueue(object : Callback<ReisMogelijkheden> {
                override fun onResponse(call: Call<ReisMogelijkheden>, response: Response<ReisMogelijkheden>) {
                    // Get the trips from the response.
                    trips = response.body()?.journeys?.filter { it.status != "NIET-MOGELIJK"} as List<ReisMogelijkheid>
                    // Set the new times on the number picker.
                    timesSpinner.setTimes(trips)
                    showTimes()
                }

                override fun onFailure(call: Call<ReisMogelijkheden>?, t: Throwable?) {
                    context.toast("Could not find NS.")
                    showTimes()
                }
            })
        }
    }

    /**
     * Get the trip [ReisMogelijkheid] that is currently selected.
     */
    fun selectedTrip() : ReisMogelijkheid = trips[timesSpinner.selectedItemPosition]

    /**
     * Hide the TimeSpinner and show the ProgressBar.
     */
    fun hideTimes() {
        timesSpinner.visibility = View.GONE
        progress.visibility = View.VISIBLE
    }

    /**
     * Show the TimeSpinner and hide the ProgressBar.
     */
    fun showTimes() {
        timesSpinner.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }
}

fun ViewManager.timesRow(theme: Int = 0) = timesRow(theme) {}
inline fun ViewManager.timesRow(theme: Int = 0, init: TimesRow.() -> Unit) = ankoView({ TimesRow(it) }, theme, init)

