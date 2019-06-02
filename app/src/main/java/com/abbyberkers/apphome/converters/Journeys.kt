package com.abbyberkers.apphome.converters

import com.abbyberkers.apphome.ns.json.Trip

/**
 * Convert a list of trips to a list of strings of the form HH:mm +x,
 * where x are the minutes of delay. When there is no delay, the format
 * is HH:mm (with a few spaces to align with the delayed strings in the
 * number picker).
 *
 */
fun List<Trip>.toStrings(): Array<String> =
        this.map { "${it.departureTime()!!.fromNs()} ${if (it.delay() != "+0") it.delay() else "     "}" }.toTypedArray()