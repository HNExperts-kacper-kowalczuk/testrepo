package com.hnexperts.cosmetics.hazards.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import kotlin.test.Test
import kotlin.test.assertEquals

class UsageRestrictionInterpreterTest {
    private val mitJson: String = UsageRestriction.toJson(
        UsageRestriction(leaveOn = "PROHIBITED", rinseOff = "HIGH")
    )

    @Test
    fun leaveOnUsesOverride() {
        val level: DangerLevel = UsageRestrictionInterpreter.effectiveLevel(
            baseline = DangerLevel.HIGH,
            restrictionJson = mitJson,
            usage = ProductUsage.LEAVE_ON
        )
        assertEquals(DangerLevel.PROHIBITED, level)
    }

    @Test
    fun rinseOffKeepsRinseOverride() {
        val level: DangerLevel = UsageRestrictionInterpreter.effectiveLevel(
            baseline = DangerLevel.HIGH,
            restrictionJson = mitJson,
            usage = ProductUsage.RINSE_OFF
        )
        assertEquals(DangerLevel.HIGH, level)
    }

    @Test
    fun unknownUsageUsesStricterLeaveOn() {
        val level: DangerLevel = UsageRestrictionInterpreter.effectiveLevel(
            baseline = DangerLevel.HIGH,
            restrictionJson = mitJson,
            usage = ProductUsage.UNKNOWN
        )
        assertEquals(DangerLevel.PROHIBITED, level)
    }

    @Test
    fun missingJsonKeepsBaseline() {
        val level: DangerLevel = UsageRestrictionInterpreter.effectiveLevel(
            baseline = DangerLevel.LOW,
            restrictionJson = null,
            usage = ProductUsage.LEAVE_ON
        )
        assertEquals(DangerLevel.LOW, level)
    }
}
