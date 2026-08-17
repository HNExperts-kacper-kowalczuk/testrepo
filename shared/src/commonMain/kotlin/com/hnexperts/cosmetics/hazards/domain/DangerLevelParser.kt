package com.hnexperts.cosmetics.hazards.domain

object DangerLevelParser {
    fun parse(raw: String): DangerLevel {
        val match: DangerLevel? = DangerLevel.entries.find { level -> level.name == raw }
        if (match == null) {
            throw IllegalArgumentException("Unknown danger_level '$raw' in catalog")
        }
        return match
    }
}
