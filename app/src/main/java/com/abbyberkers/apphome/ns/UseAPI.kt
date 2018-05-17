package com.abbyberkers.apphome.ns

import android.view.View
import android.widget.NumberPicker
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.MainActivity
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheden
import com.abbyberkers.apphome.ui.setDepartures
import com.abbyberkers.apphome.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Calls the NS API and on success update the numberpicker with new
 * departure times and updates the journeys.
 */
fun NumberPicker.updateNumberPicker(from: City,
                                    to: City,
                                    activity: MainActivity) {

    // If the from and to station that are selected are the same,
    // set nothing on the numberpicker.
    if(from == to) {
        this.setDepartures(null)
    } else {

        val progressBar = activity.progressBar

        // Show the progress bar.
        progressBar.visibility = View.VISIBLE

        // Prepare a call to the API.
        val call = NsApiService.create().listTrips(
                fromStation = from.station,
                toStation = to.station
        )

        // Make the call on another thread.
        call.enqueue(object : Callback<ReisMogelijkheden> {
            override fun onResponse(call: Call<ReisMogelijkheden>, response: Response<ReisMogelijkheden>) {
                // Get the journeys from the response.
                activity.journeys = response.body()?.journeys?.filter { it.status != "NIET-MOGELIJK"}
                // Hide the progressbar.
                progressBar.visibility = View.GONE
                // Set the new times on the number picker.
//                depart.setter.call(this@updateNumberPicker.setDepartures(journeys.getter.call()))
                activity.depart = this@updateNumberPicker.setDepartures(journeys = activity.journeys)
            }

            override fun onFailure(call: Call<ReisMogelijkheden>?, t: Throwable?) {
                context.toast("Could not find NS.")
                progressBar.visibility = View.GONE
            }

        })
    }
}