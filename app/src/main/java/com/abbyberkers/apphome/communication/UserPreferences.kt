package com.abbyberkers.apphome.communication

import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.communication.textformatters.Language
import com.abbyberkers.apphome.communication.textformatters.TimesFormat
import java.util.*

enum class UserPreferences(
        /** Default trip in the morning. */
        val morning: Direction,
        /** Default trip in the afternoon. */
        val afternoon: Direction,
        /** Map containing the preferences for formatting the time in a message.
         *      Language: the language of the time, corresponding to the template.
         *      TimesFormat: the format of the time.
         *      Int: the local travel time. E.g., bike time. */
        val languagePref: Map<City, Triple<Language, TimesFormat, Int>>,
        /** Map containing message templates given the times. */
        val textTemplate: Map<City, (String) -> String>) {

    THOMAS(Direction(City.ROOSENDAAL, City.EINDHOVEN),
            Direction(City.EINDHOVEN, City.ROOSENDAAL),
            mapOf(City.EINDHOVEN to Triple(Language.ENGLISH, TimesFormat.WORDS, 0),
                    City.OVERLOON to Triple(Language.ENGLISH, TimesFormat.WORDS, 0),
                    City.HEEZE to Triple(Language.ENGLISH, TimesFormat.WORDS, 0),
                    City.ROOSENDAAL to Triple(Language.DUTCH, TimesFormat.WORDS, 25)),
            mapOf(City.EINDHOVEN to {time -> "ETA $time."},
                    City.OVERLOON to {time -> "Yay at $time!"},
                    City.HEEZE to {time -> "Yay at $time!"},
                    City.ROOSENDAAL to {time -> "Ik ben rond $time thuis."})
    ),
    ABBY(Direction(City.OVERLOON, City.EINDHOVEN),
            Direction(City.EINDHOVEN, City.OVERLOON),
            mapOf(City.EINDHOVEN to Triple(Language.ENGLISH, TimesFormat.WORDS, 0),
                    City.OVERLOON to Triple(Language.DUTCH, TimesFormat.WORDS, 25),
                    City.HEEZE to Triple(Language.DUTCH, TimesFormat.WORDS, 15),
                    City.ROOSENDAAL to Triple(Language.ENGLISH, TimesFormat.WORDS, 0)),
            mapOf(City.EINDHOVEN to {time -> "ETA $time."},
                    City.OVERLOON to {time -> "Ik ben rond $time thuis."},
                    City.HEEZE to {time -> "Ik ben rond $time thuis."},
                    City.ROOSENDAAL to {time -> "Yay at $time!"})
    );

    /**
     * Get the prefered direction of the user at the current time.
     */
    fun getDirection(): Direction {
        // Create a Calendar object.
        val cal = GregorianCalendar()
        // Set the current time on the calendar.
        cal.time = Calendar.getInstance().time
        // If the time is before noon return the morning direction, else return the afternoon direction.
        return if(cal.get(Calendar.HOUR_OF_DAY) < 12) morning else afternoon
    }
}