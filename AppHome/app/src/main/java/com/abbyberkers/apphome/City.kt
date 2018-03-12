package com.abbyberkers.apphome

enum class City(val string: String) {
    EINDHOVEN("Eindhoven"),
    ROOSENDAAL("Roosendaal"),
    OVERLOON("Overloon"),
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