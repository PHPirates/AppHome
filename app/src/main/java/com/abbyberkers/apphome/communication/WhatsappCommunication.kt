package com.abbyberkers.apphome.communication

import android.content.Context
import android.content.Intent
import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.communication.textformatters.Language
import com.abbyberkers.apphome.communication.textformatters.TextFormatter
import com.abbyberkers.apphome.converters.nsToCalendar
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid
import org.jetbrains.anko.toast

/**
 * Class to handle the WhatsApp communication.
 */
class WhatsappCommunication(val context: Context) {

    /**
     * Creates and sends a messages using WhatsApp.
     *
     * @param trip The ReisMogelijkheid that is used to create the message. This is selected in the
     *      time spinner.
     * @param destination The city that is the destination of the trip. This has influence on the
     *      template of the message. It is selected in the to spinner.
     * @param userPreferences The preferences of the user. This has influence on the template of the
     *      message. It is selected in the menu in the toolbar.
     * @param messageType The type of the message. Can be ETA (the default) or a delay.
     */
    fun sendMessage(trip: ReisMogelijkheid,
                    destination: City,
                    userPreferences: UserPreferences,
                    messageType: MessageType = MessageType.ETA) {
        // If the delay is null and the message type is DELAY give the user a toast that there is no delay.
        if (trip.delay() == null && messageType == MessageType.DELAY) context.toast("There is no delay.")
        else {
            // Get the text depending on the message type.
            val text = when (messageType) {
                MessageType.ETA -> TextFormatter(userPreferences).applyTemplate(destination, trip.arrivalTime.nsToCalendar())
                MessageType.DELAY -> trip.delay()
            }
            // Send the message.
            send(text)
        }
    }

    /**
     * Send text using WhatsApp. Opens WhatsApp with a screen to pick a chat to send the text to.
     *
     * @param text is the text to be sent.
     */
    fun send(text: String) {
        // Toast the text in AppHome so the user can see what text will be sent.
        context.toast(text)
        // Create the intent for opening WhatsApp.
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        // Start the WhatsApp activity.
        context.startActivity(intent)
    }

}