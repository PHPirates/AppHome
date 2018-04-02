package com.abbyberkers.apphome.converters

import java.text.SimpleDateFormat
import java.util.*


/**
 * Convert a calendar object to a string in the given format.
 *
 * @param calendar The calendar object to be converted.
 * @param format The format for the time. Default format is "HH:mm".
 */
@JvmOverloads
fun calendarToString(calendar: Calendar, format: String = "HH:mm"): String {
    return SimpleDateFormat(format, java.util.Locale.getDefault()).format(calendar.time)
}

/**
 * Convert a calendar object to a string in NS format. Uses [calendarToString].
 *
 * @param calendarThe The calendar object to be converted.
 */
fun calendarToNS(calendar: Calendar): String = calendarToString(calendar, "yyyy-MM-dd'T'HH:mm:ssZ")

/**
 * Convert a string in NS format to a Date object.
 *
 * @param nsString The string to be converted.
 */
fun nsToDate(nsString: String): Date {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.getDefault()).parse(nsString)
}

/**
 * Convert a string in NS format to a Calendar object. Uses [nsToDate].
 *
 * @param nsString String to be converted.
 */
fun nsToCalendar(nsString: String): Calendar {
    val date = nsToDate(nsString)
    val calendar = GregorianCalendar()
    calendar.time = date
    return calendar
}