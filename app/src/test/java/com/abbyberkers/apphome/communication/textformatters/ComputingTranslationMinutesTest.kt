package com.abbyberkers.apphome.communication.textformatters

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals

object ComputingTranslationMinutesTest : Spek({
    given("a maximum of 15 minutes") {
        val maxMinutes = 15
        val testCases = mapOf(
                0 to 0,
                5 to 5,
                10 to 10,
                15 to 15,
                20 to 10,
                25 to 5,
                30 to 0,
                35 to 5,
                40 to 10,
                45 to 15,
                50 to 10,
                55 to 5)

        testCases.forEach { givenMinutes, expectedMinutes ->
            on("computing the minutes to be translated of $givenMinutes") {
                val computedMinutes = minutesToBeTranslated(givenMinutes, maxMinutes)
                it("should return $expectedMinutes") {
                    assertEquals(expectedMinutes, computedMinutes)
                }
            }
        }
    }

    given("a maximum of 30 minutes") {
        val maxMinutes = 30
        val testCases = mapOf(
                0 to 0,
                5 to 5,
                10 to 10,
                15 to 15,
                20 to 20,
                25 to 25,
                30 to 30,
                35 to 25,
                40 to 20,
                45 to 15,
                50 to 10,
                55 to 5)

        testCases.forEach { givenMinutes, expectedMinutes ->
            on("computing the minutes to be translated of $givenMinutes") {
                val computedMinutes = minutesToBeTranslated(givenMinutes, maxMinutes)
                it("should return $expectedMinutes") {
                    assertEquals(expectedMinutes, computedMinutes)
                }
            }
        }
    }

})