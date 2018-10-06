package com.abbyberkers.apphome.communication.textformatters

import com.abbyberkers.apphome.converters.toCalendar
import com.abbyberkers.apphome.converters.toFormattedString
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals
import kotlin.math.round

object TimeRoundingTest : Spek({
    val testCases = mapOf<String, String>(
            "12:02" to "12:00",
            "12:07" to "12:05",
            "12:08" to "12:10",
            "12:13" to "12:15",
            "12:20" to "12:20",
            "12:22" to "12:20",
            "12:24" to "12:25",
            "12:28" to "12:30",
            "12:34" to "12:35",
            "12:41" to "12:40",
            "12:47" to "12:45",
            "12:50" to "12:50",
            "12:54" to "12:55",
            "12:58" to "13:00")

    testCases.forEach {
        givenTime, expectedTime ->
        given("a calendar with time $givenTime") {
            on("rounding the time to the nearest 5 minutes") {
                val roundedTime = givenTime.toCalendar().roundToFive()
                it("should be $expectedTime") {
                    assertEquals(expectedTime.toCalendar(), roundedTime)
                }
            }
        }
    }
})