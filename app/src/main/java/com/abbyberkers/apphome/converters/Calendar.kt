package com.abbyberkers.apphome.converters

import java.text.SimpleDateFormat
import java.util.*


/**
 * Convert a calendar object to a string in the given format.
 *
 * @param format The format for the time. Default format is "HH:mm".
 */
@JvmOverloads
fun Calendar.toFormattedString(format: String = "HH:mm"): String =
        SimpleDateFormat(format, Locale.getDefault()).format(this.time)

/**
 * Convert a calendar object to a string in NS format. Uses [Calendar.toFormattedString].
 */
fun Calendar.toNsString(): String =
        this.toFormattedString("yyyy-MM-dd'T'HH:mm:ssZ")

/**
 * Convert a string in NS format to a Date object.
 */
fun String.toNsDate(): Date = this.toDate("yyyy-MM-dd'T'HH:mm:ssZ")

/**
 * Convert a string in the given format to a calendar object.
 *
 * @param format The format for the given string. Default is "HH:mm".
 */
fun String.toDate(format: String = "HH:mm"): Date =
        SimpleDateFormat(format, Locale.getDefault()).parse(this)!!

/**
 * Convert a string in NS format to a Calendar object. Uses [String.toNsDate].
 */
fun String.nsToCalendar(): Calendar {
    val date = this.toNsDate()
    val calendar = GregorianCalendar()
    calendar.time = date
    return calendar
}

/**
 * Convert a HH:mm string to a Calendar object. Uses [String.toDate].
 */
fun String.toCalendar(): Calendar {
    val date = this.toDate()
    val calendar = GregorianCalendar()
    calendar.time = date
    return calendar
}

/**
 * Convert a string in NS format to a string in HH:mm format.
 */
fun String.fromNs(): String =
        this.nsToCalendar().toFormattedString()