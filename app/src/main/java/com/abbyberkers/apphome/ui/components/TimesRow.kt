package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.TableRow
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.ns.NsApiService
import com.abbyberkers.apphome.ns.json.Trip
import com.abbyberkers.apphome.ns.json.Trips
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.tableRow
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimesRow(context: Context) : TableRow(context) {
    // Function that has to be called when the times are loaded. Does nothing by default.
    var onTimesLoaded : () -> Unit = {}
    val timePicker = timesSpinner { gravity = Gravity.CENTER }
    private val progress = progressBar()

    lateinit var trips: List<Trip>

    init {
        tableRow {
            timePicker.lparams(height = wrapContent, width = 0, initWeight = 1f)
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
            timePicker.setTimes(null)
            progress.visibility = View.GONE
        } else {
            // Prepare a call to the API.
            val call = NsApiService.create().getTrips(
                    fromStation = from.station,
                    toStation = to.station
            )

            // Make the call on another thread.
            call.enqueue(object : Callback<Trips> {
                override fun onResponse(call: Call<Trips>, response: Response<Trips>) {
                    trips = response.body()?.trips!!
                    trips = response.body()?.trips as List<Trip>
                    timePicker.setTimes(trips)
                    showTimes()
                    onTimesLoaded()
                }

                override fun onFailure(call: Call<Trips>, t: Throwable) {
                    context.toast("NS unreachable.")
                    hideTimes()
                }
            })
        }
    }

    /**
     * Get the [Trip] that is currently selected.
     */
    fun selectedTrip() : Trip = trips[timePicker.value]

    /**
     * Hide the TimeSpinner and show the ProgressBar.
     */
    fun hideTimes() {
        timePicker.visibility = View.GONE
        progress.visibility = View.VISIBLE
    }

    /**
     * Show the TimeSpinner and hide the ProgressBar.
     */
    fun showTimes() {
        timePicker.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }

}

fun ViewManager.timesRow(theme: Int = 0) = timesRow(theme) {}
inline fun ViewManager.timesRow(theme: Int = 0, init: TimesRow.() -> Unit) = ankoView({ TimesRow(it) }, theme, init)