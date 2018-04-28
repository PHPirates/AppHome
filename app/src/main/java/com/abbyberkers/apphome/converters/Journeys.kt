package com.abbyberkers.apphome.converters

import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid

/**
 * Convert a list of journeys to a list of strings of the form HH:mm +x,
 * where x are the minutes of delay. When there is no delay, the format
 * is HH:mm (with a few spaces to align with the delayed strings in the
 * number picker).
 */
fun List<ReisMogelijkheid>.toStrings(): Array<String> =
        this.map { "${it.departureTime.fromNs()} ${it.departureDelay ?: "    "}" }
                .map { it.replace(" min", "") }
                .toTypedArray()