package com.hnexperts.cosmetics.hazards.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage

object UsageRestrictionInterpreter {
    fun effectiveLevel(
        baseline: DangerLevel,
        restrictionJson: String?,
        usage: ProductUsage
    ): DangerLevel {
        val restriction: UsageRestriction = UsageRestriction.fromJson(restrictionJson) ?: return baseline
        val overrideName: String? = overrideName(restriction, usage.scoringUsage())
        if (overrideName.isNullOrBlank()) {
            return baseline
        }
        return DangerLevelParser.parse(overrideName)
    }

    private fun overrideName(restriction: UsageRestriction, usage: ProductUsage): String? {
        return when (usage) {
            ProductUsage.RINSE_OFF -> restriction.rinseOff
            ProductUsage.LEAVE_ON -> restriction.leaveOn
            ProductUsage.LIP -> restriction.lip ?: restriction.leaveOn
            ProductUsage.EYE -> restriction.eye ?: restriction.leaveOn
            ProductUsage.SPRAY -> restriction.spray ?: restriction.leaveOn
            ProductUsage.UNKNOWN -> restriction.leaveOn
        }
    }
}
