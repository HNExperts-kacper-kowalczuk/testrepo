package com.hnexperts.cosmetics.hazards.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UsageRestriction(
    val leaveOn: String? = null,
    val rinseOff: String? = null,
    val lip: String? = null,
    val eye: String? = null,
    val spray: String? = null
) {
    companion object {
        private val json: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

        fun fromJson(raw: String?): UsageRestriction? {
            if (raw.isNullOrBlank()) {
                return null
            }
            return json.decodeFromString(serializer(), raw)
        }

        fun toJson(restriction: UsageRestriction): String {
            return json.encodeToString(serializer(), restriction)
        }
    }
}
