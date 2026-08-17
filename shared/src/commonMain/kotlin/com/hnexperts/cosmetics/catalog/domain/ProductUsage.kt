package com.hnexperts.cosmetics.catalog.domain

enum class ProductUsage {
    RINSE_OFF,
    LEAVE_ON,
    LIP,
    EYE,
    SPRAY,
    UNKNOWN
    ;

    fun scoringUsage(): ProductUsage {
        return if (this == UNKNOWN) LEAVE_ON else this
    }

    companion object {
        fun parse(raw: String?): ProductUsage {
            if (raw.isNullOrBlank()) {
                return UNKNOWN
            }
            return entries.find { usage -> usage.name == raw.trim() } ?: UNKNOWN
        }
    }
}
