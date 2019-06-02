package com.abbyberkers.apphome.ns.json

class Trip {

    var plannedDurationInMinutes: Int = 0
    var actualDurationInMinutes: Int = 0

    // A trip consists of one or more legs.
    var legs: List<Leg>? = null

    // Get the planned departure and arrival times, as actual times are probably not known yet.
    fun departureTime(): String? = legs?.first()?.origin?.plannedDateTime
    fun arrivalTime(): String? = legs?.last()?.destination?.plannedDateTime
    // Compute the delay as the difference between the actual and planned duration of the trip.
    fun delay(): String = "+" + (actualDurationInMinutes - plannedDurationInMinutes).toString()
}