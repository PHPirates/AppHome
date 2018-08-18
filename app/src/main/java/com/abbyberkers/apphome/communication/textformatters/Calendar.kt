package com.abbyberkers.apphome.communication.textformatters

import com.abbyberkers.apphome.converters.toFormattedString
import java.util.*


/**
 * Format the time in the given language and time format.
 *
 * @param language The language to translate the time to.
 * @param timesFormat The [TimesFormat] to put the time in.
 *
 * @return a String with the time.
 */
fun Calendar.format(language: Language, timesFormat: TimesFormat): String {

    // Get the hour and minutes from the calendar.
    val hour = this.get(Calendar.HOUR_OF_DAY)
    val min = this.get(Calendar.MINUTE)

    // Compute the hour to translate. This is the actual hour, or the next, depending on spoken language.
    var translateHour = if (min > language.nextHourThreshold) (hour + 1) % 12 else hour % 12
    // Compute the minutes to be translated.
    val translateMinutes = minutesToBeTranslated(min, language.maxMinutes)

    // Depending on the given TimesFormat, build and return the time string.
    return when(timesFormat) {
        TimesFormat.WORDS -> {
            when(min) {
                0 -> "${language.hours[translateHour]} ${language.minutes[0]}"
                30 -> "${language.minutes[30]}${language.connectors[30]} ${language.hours[translateHour]}"
                else -> "${language.minutes[translateMinutes]} ${language.connectors[min]} ${language.hours[translateHour]}"
            }
        }
        TimesFormat.NUMBERS -> {
            if (translateHour == 0) translateHour = 12
            when(min) {
                0 -> "$translateHour ${language.minutes[0]}"
                30 -> "${if(language == Language.ENGLISH) {"30"} else {""}}${language.connectors[30]} $translateHour"
                else -> "$translateMinutes ${language.connectors[min]} ${translateHour}"
            }
        }
        TimesFormat.HM -> this.toFormattedString()
    }
}

/**
 * Compute the number that should be translated when given the actual minutes within an hour.
 *
 * @param minutes The original minutes.
 * @param maxMinutes The maximum number of minutes that should be used (in practice 15 or 30).
 *
 * @return an Int with the number that should be translated.
 */
fun minutesToBeTranslated(minutes: Int, maxMinutes: Int): Int {
    // Compute the minutes modulo (twice masMinutes), because after twice the maxMinutes the
    // values are the same again.
    val reducedMinutes = minutes % (2 * maxMinutes)
    // The number of minutes that should be added or subtracted to get to the closest multiple
    // of 2 * maxMinutes.
    // Compute the difference between reducedMintues and maxMinutes, and subtract that
    // difference from maxMinutes.
    return maxMinutes - Math.abs(reducedMinutes - maxMinutes)
}


/**
 * Round the time to the nearest five minutes.
 *
 * @return a Calendar set at the rounded time.
 */
fun Calendar.roundToFive(): Calendar {
    // Get the minutes from the calendar.
    val minutes = this.get(Calendar.MINUTE)
    // Round the minutes to the nearest five. Divide by 5, round to the nearest integer,
    // and multiply by 5.
    val roundedMinutes = Math.round(minutes / 5.0) * 5.0
    // When the rounded value is 60 the time should be rounded to the next hour.
    // Otherwise set the minutes to be the rounded value.
    when(roundedMinutes) {
        60.0 -> {
            this.set(Calendar.MINUTE, 0)
            this.add(Calendar.HOUR, 1)
        }
        else -> this.set(Calendar.MINUTE, roundedMinutes.toInt())
    }
    return this
}