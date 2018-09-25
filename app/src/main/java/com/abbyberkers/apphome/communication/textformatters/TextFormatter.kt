package com.abbyberkers.apphome.communication.textformatters

import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.communication.UserPreferences
import java.util.*

/**
 * Class to build a message for a user given their preferences.
 */
class TextFormatter(private val userPreferences: UserPreferences) {

    /**
     * Format the time as a string for a user given the destination and the time.
     *
     * @param destination the [City] the user is traveling to.
     * @param time the time the user arrives, rounded to the nearest five minutes.
     *
     * @return
     */
    private fun formatTime(destination: City, time: Calendar) : String {
        // Get the language preferences of a user based on their destination.
        val languagePref = userPreferences.languagePref[destination]!!
        val language = languagePref.first
        val timesFormat = languagePref.second
        val localTravelMinutes = languagePref.third

        time.add(Calendar.MINUTE, localTravelMinutes)
        val roundedTime = time.roundToFive()

        // Format the time using these preferences.
        return roundedTime.format(language, timesFormat) // Dummy, return the time as HH:mm
    }

    /**
     * Apply the text template from the user to the time string.
     *
     * @param destination The destination of the user. The text template depends on the time.
     * @param time The arrival time of the user.
     */
    fun applyTemplate(destination: City, time: Calendar) : String {
        // Get the template from the user.
        val template = {
            val temp = userPreferences.textTemplate[destination]!!
            // Get the template corresponding to the current user preference.
            if(userPreferences.languagePref[destination]!!.first == Language.ENGLISH)
                temp.english else temp.dutch
        }
        // Format the time according to the users preferences
        val formattedTime = formatTime(destination, time)
        // Apply the template.
        return template()(formattedTime)
        }
}