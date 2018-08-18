package com.abbyberkers.apphome.communication

import android.content.Context
import android.content.Intent
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.communication.textformatters.TextFormatter
import com.abbyberkers.apphome.converters.nsToCalendar
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid

class WhatsappCommunication(val context: Context) {

    fun sendMessage(trip: ReisMogelijkheid, destination: City, userPreferences: UserPreferences) {
        val text = TextFormatter(userPreferences).applyTemplate(destination, trip.arrivalTime.nsToCalendar())
        send(text)
    }

    fun send(text: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }

}