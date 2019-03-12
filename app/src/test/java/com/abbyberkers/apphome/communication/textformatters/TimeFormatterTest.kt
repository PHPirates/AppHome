package com.abbyberkers.apphome.communication.textformatters

import com.abbyberkers.apphome.converters.toCalendar
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertEquals

object TimeFormatterTest : Spek({
    given("as a language Dutch and as a times format words") {
        val language = Language.DUTCH
        val timesFormat = TimesFormat.WORDS

        val testCases = mapOf(
                "12:00" to "twaalf uur",
                "13:05" to "vijf over één",
                "02:10" to "tien over twee",
                "15:15" to "kwart over drie",
                "04:20" to "tien voor half vijf",
                "17:25" to "vijf voor half zes",
                "06:30" to "half zeven",
                "19:35" to "vijf over half acht",
                "08:40" to "tien over half negen",
                "21:45" to "kwart voor tien",
                "10:50" to "tien voor elf",
                "23:55" to "vijf voor twaalf"
        )

        testCases.forEach { givenTime, expectedTime ->
            on("formatting $givenTime") {
                val time = givenTime.toCalendar()
                val formattedTime = time.format(language, timesFormat)
                it("should return $expectedTime") {
                    assertEquals(expectedTime, formattedTime)
                }
            }
        }
    }

    given("as a language Dutch and as a times format numbers") {
        val language = Language.DUTCH
        val timesFormat = TimesFormat.NUMBERS

        val testCases = mapOf(
                "12:00" to "12 uur",
                "13:05" to "5 over 1",
                "02:10" to "10 over 2",
                "15:15" to "15 over 3",
                "04:20" to "10 voor half 5",
                "17:25" to "5 voor half 6",
                "06:30" to "half 7",
                "19:35" to "5 over half 8",
                "08:40" to "10 over half 9",
                "21:45" to "15 voor 10",
                "10:50" to "10 voor 11",
                "23:55" to "5 voor 12"
        )

        testCases.forEach { givenTime, expectedTime ->
            on("formatting $givenTime") {
                val time = givenTime.toCalendar()
                val formattedTime = time.format(language, timesFormat)
                it("should return $expectedTime") {
                    assertEquals(expectedTime, formattedTime)
                }
            }
        }
    }

    given("as a language English and as a times format words") {
        val language = Language.ENGLISH
        val timesFormat = TimesFormat.WORDS

        val testCases = mapOf(
                "12:00" to "twelve o'clock",
                "13:05" to "five past one",
                "02:10" to "ten past two",
                "15:15" to "quarter past three",
                "04:20" to "twenty past four",
                "17:25" to "twenty-five past five",
                "06:30" to "thirty past six",
                "19:35" to "twenty-five to eight",
                "08:40" to "twenty to nine",
                "21:45" to "quarter to ten",
                "10:50" to "ten to eleven",
                "23:55" to "five to twelve"
        )

        testCases.forEach { givenTime, expectedTime ->
            on("formatting $givenTime") {
                val time = givenTime.toCalendar()
                val formattedTime = time.format(language, timesFormat)
                it("should return $expectedTime") {
                    assertEquals(expectedTime, formattedTime)
                }
            }
        }
    }

    given("as a language English and as a times format numbers") {
        val language = Language.ENGLISH
        val timesFormat = TimesFormat.NUMBERS

        val testCases = mapOf(
                "12:00" to "12 o'clock",
                "13:05" to "5 past 1",
                "02:10" to "10 past 2",
                "15:15" to "15 past 3",
                "04:20" to "20 past 4",
                "17:25" to "25 past 5",
                "06:30" to "30 past 6",
                "19:35" to "25 to 8",
                "08:40" to "20 to 9",
                "21:45" to "15 to 10",
                "10:50" to "10 to 11",
                "23:55" to "5 to 12"
        )

        testCases.forEach { givenTime, expectedTime ->
            on("formatting $givenTime") {
                val time = givenTime.toCalendar()
                val formattedTime = time.format(language, timesFormat)
                it("should return $expectedTime") {
                    assertEquals(expectedTime, formattedTime)
                }
            }
        }
    }
})