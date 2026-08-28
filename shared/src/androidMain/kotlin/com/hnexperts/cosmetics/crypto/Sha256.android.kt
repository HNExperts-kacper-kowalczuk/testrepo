package com.hnexperts.cosmetics.crypto

import java.security.MessageDigest

actual object Sha256 {
    actual fun hex(text: String): String {
        return hex(text.encodeToByteArray())
    }

    actual fun hex(bytes: ByteArray): String {
        val digest: ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { byte ->
            val unsigned: Int = byte.toInt() and 0xFF
            unsigned.toString(16).padStart(2, '0')
        }
    }
}
