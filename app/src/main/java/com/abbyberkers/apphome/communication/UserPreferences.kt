package com.abbyberkers.apphome.communication

import com.abbyberkers.apphome.City
import com.abbyberkers.apphome.communication.textformatters.Language
import com.abbyberkers.apphome.communication.textformatters.Template
import com.abbyberkers.apphome.communication.textformatters.TimesFormat
import java.util.*

enum class UserPreferences(
        /** Default trip in the morning. */
        private val morning: Direction,
        /** Default trip in the afternoon. */
        private val afternoon: Direction,
        /** Map containing the preferences for formatting the time in a message.
         *      Language: the language of the time, corresponding to the template.
         *      TimesFormat: the format of the time.
         *      Int: the local travel time. E.g., bike time. */
        var languagePref: MutableMap<City, Triple<Language, TimesFormat, Int>>,
        /** Map containing message templates given the times. */
        val textTemplate: Map<City, Template>) {

    THOMAS(Direction(City.ROOSENDAAL, City.HELMOND),
            Direction(City.HELMOND, City.ROOSENDAAL),
            mutableMapOf(
                City.EINDHOVEN to Triple(Language.ENGLISH, TimesFormat.WORDS, 0),
                City.OVERLOON to Triple(Language.ENGLISH, TimesFormat.WORDS, 25),
                City.HELMOND to Triple(Language.ENGLISH, TimesFormat.WORDS, 6),
                City.ROOSENDAAL to Triple(Language.DUTCH, TimesFormat.WORDS, 25),
                City.STRIJP to Triple(Language.DUTCH, TimesFormat.WORDS, 0),
            ),
            mapOf(
                City.EINDHOVEN to Template.YAY,
                City.OVERLOON to Template.YAY,
                City.HELMOND to Template.YAY,
                City.ROOSENDAAL to Template.GENERIC,
                City.STRIJP to Template.GENERIC,
            )
    ),
    ABBY(Direction(City.HELMOND, City.STRIJP),
            Direction(City.STRIJP, City.HELMOND),
            mutableMapOf(
                City.EINDHOVEN to Triple(Language.ENGLISH, TimesFormat.WORDS, 0),
                City.OVERLOON to Triple(Language.DUTCH, TimesFormat.HM, 25),
                City.HELMOND to Triple(Language.DUTCH, TimesFormat.WORDS, 6),
                City.ROOSENDAAL to Triple(Language.ENGLISH, TimesFormat.WORDS, 25),
                City.STRIJP to Triple(Language.DUTCH, TimesFormat.WORDS, 5),
            ),
            mapOf(
                City.EINDHOVEN to Template.YAY,
                City.OVERLOON to Template.GENERIC,
                City.HELMOND to Template.YAY,
                City.ROOSENDAAL to Template.YAY,
                City.STRIJP to Template.GENERIC,
            )
    );

    /**
     * Get the preferred direction of the user at the current time.
     */
    fun getDirection(): Direction {
        // Create a Calendar object.
        val cal = GregorianCalendar()
        // Set the current time on the calendar.
        cal.time = Calendar.getInstance().time
        // If the time is before noon return the morning direction, else return the afternoon direction.
        return if(cal.get(Calendar.HOUR_OF_DAY) < 12) morning else afternoon
    }

    /**
     * Alter the language in the user preferences for a given direction.
     */
    fun alterLanguage(destination: City, language: Language) {
        // Get the triple with the current settings.
        val langTriple = languagePref[destination]!!
        // Create a new triple with different language, copy the rest.
        languagePref[destination] = Triple(language, langTriple.second, langTriple.third)
    }
}