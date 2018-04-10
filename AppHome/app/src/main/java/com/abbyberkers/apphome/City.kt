package com.abbyberkers.apphome

/**
 * The possible cities
 */
enum class City(val string: String, val station: String = string) {
    EINDHOVEN("Eindhoven"),
    ROOSENDAAL("Roosendaal"),
    OVERLOON("Overloon", "Vierlingsbeek"),
    HEEZE("Heeze");

    companion object {
        /**
         * Returns all the string values of the City values in a String Array.
         *
         * So: {"Eindhoven", "Roosendaal", ... }
         */
        fun strings(): Array<String> {
            return City.values().map { it.string }.toTypedArray()
        }
    }
}