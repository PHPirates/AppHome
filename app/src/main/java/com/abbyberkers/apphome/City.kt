package com.abbyberkers.apphome

import android.widget.Spinner

/**
 * The possible cities.
 */
enum class City(val string: String, val station: String = string) {
    EINDHOVEN("Eindhoven Centraal"),
    ROOSENDAAL("Roosendaal"),
    OVERLOON("Overloon", "Vierlingsbeek"),
    HELMOND("Helmond Brouwhuis"),
    STRIJP("Strijp-S", "Eindhoven Strijp-S"),
    ;

    companion object {
        /**
         * Returns all the string values of the City values in a String Array.
         *
         * So: {"Eindhoven", "Roosendaal", ... }
         */
        fun strings(): Array<String> {
            return City.values().map { it.string }.toTypedArray()
        }

        fun getSelectedCity(spinner: Spinner): City {
            return City.values()[spinner.selectedItemPosition]
        }
    }
}