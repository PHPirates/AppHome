package com.abbyberkers.apphome.converters

import android.content.res.Resources
import com.abbyberkers.apphome.ns.xml.ReisMogelijkheid

/**
 * Convert a list of journeys to a list of strings of the form HH:mm +x,
 * where x are the minutes of delay. When there is no delay, the format
 * is HH:mm (with a few spaces to align with the delayed strings in the
 * number picker).
 *
 * TODO: String resources not available because we're not in an activity.
 */
fun List<ReisMogelijkheid>.toStrings(): Array<String> =
        this.map {
            if (it.status == "NIET-MOGELIJK") "Trip impossible."
                else "${it.departureTime.fromNs()} ${it.delay() ?: "    "}"
        }.toTypedArray()