package com.abbyberkers.apphome

enum class City(val string: String) {
    EINDHOVEN("Eindhoven"),
    ROOSENDAAL("Roosendaal"),
    OVERLOON("Overloon"),
    HEEZE("Heeze");

    companion object {
        fun strings(): Array<String> {
            return City.values().map { it.string }.toTypedArray()
        }
    }


}