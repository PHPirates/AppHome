package com.abbyberkers.apphome.ns

import com.abbyberkers.apphome.converters.delayToInt
import com.abbyberkers.apphome.converters.toDelayString
import kotlin.math.max

fun maximumDelay(delay: String?, otherDelay: String?): String? {
    return when {
        delay != null && otherDelay != null -> max(delay.delayToInt(), otherDelay.delayToInt()).toDelayString()
        delay == null && otherDelay != null -> otherDelay.delayToInt().toDelayString()
        delay != null && otherDelay == null -> delay.delayToInt().toDelayString()
        else -> null
    }
}