package com.hnexperts.cosmetics.hazards.domain

enum class DangerLevel {
    SAFE,
    LOW,
    MODERATE,
    RESTRICTED,
    HIGH,
    PROHIBITED,
    UNKNOWN
    ;

    fun isMatchedHazard(): Boolean {
        return this != UNKNOWN
    }
}

object DangerLevelOrdering {
    fun worse(left: DangerLevel, right: DangerLevel): DangerLevel {
        if (!left.isMatchedHazard()) {
            return right
        }
        if (!right.isMatchedHazard()) {
            return left
        }
        if (rank(left) >= rank(right)) {
            return left
        }
        return right
    }

    fun overall(levels: Collection<DangerLevel>): DangerLevel {
        var current: DangerLevel = DangerLevel.UNKNOWN
        for (level in levels) {
            current = worse(current, level)
        }
        return current
    }

    private fun rank(level: DangerLevel): Int {
        return when (level) {
            DangerLevel.UNKNOWN -> 0
            DangerLevel.SAFE -> 1
            DangerLevel.LOW -> 2
            DangerLevel.MODERATE -> 3
            DangerLevel.RESTRICTED -> 4
            DangerLevel.HIGH -> 5
            DangerLevel.PROHIBITED -> 6
        }
    }
}
