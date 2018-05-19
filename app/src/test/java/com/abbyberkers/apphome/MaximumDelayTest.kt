package com.abbyberkers.apphome

import com.abbyberkers.apphome.ns.maximumDelay
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals

object MaximumDelayTest: Spek({
    given("Some delays which are not null") {
        val delay = "+5 min"
        val otherDelay = "+3 min"

        on("finding the maximum") {
            val max = maximumDelay(delay, otherDelay)

            it("should return the maximum delay") {
                assertEquals("+5", max)
            }
        }
    }

    given("Some delays which can be null") {
        val delay = "+5 min"
        val otherDelay = null

        on("finding the maximum") {
            val max = maximumDelay(delay, otherDelay)

            it("should return the maximum delay") {
                assertEquals("+5", max)
            }
        }
    }

    given("Delays that are null") {
        val delay = null
        val otherDelay = null

        on("finding the maximum") {
            val max = maximumDelay(delay, otherDelay)

            it("should return the maximum delay") {
                assertEquals(null, max)
            }
        }
    }
})