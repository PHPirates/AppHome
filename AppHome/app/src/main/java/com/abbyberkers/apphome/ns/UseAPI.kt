package com.abbyberkers.apphome.ns

import android.view.View
import android.widget.NumberPicker
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.MainActivity
import com.abbyberkers.apphome.R
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheden
import com.abbyberkers.apphome.ui.setDepartures
import com.abbyberkers.apphome.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Calls the NS API and on success update the numberpicker with new
 * departure times and update the journeys.
 */
fun MainActivity.updateNumberPicker(from: City, to: City) {

    // Get a reference to the departure number picker.
    val departurePicker = findViewById<NumberPicker>(R.id.numberPickerDepartures)

    // If the from and to station that are selected are the same,
    // set nothing on the numberpicker.
    if(from == to) {
        departurePicker.setDepartures(null)
    } else {

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
                journeys = response.body()?.journeys
                // Hide the progressbar.
                progressBar.visibility = View.GONE

                if (journeys != null) {
                    // Set the new times on the number picker.
                    depart = departurePicker.setDepartures(journeys!!)
                } else {
                    toast("Didn't get journeys from NS.")
                }
            }

            override fun onFailure(call: Call<ReisMogelijkheden>?, t: Throwable?) {
                toast("Could not find NS.")
                progressBar.visibility = View.GONE
            }

        })
    }
}