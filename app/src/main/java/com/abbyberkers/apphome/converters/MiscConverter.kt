package com.abbyberkers.apphome.converters

fun String.delayToInt(): Int {
    return this.replace("+", "").replace(" min", "").toInt()
}

fun Int.toDelayString(): String {
    return "+${this}"
}