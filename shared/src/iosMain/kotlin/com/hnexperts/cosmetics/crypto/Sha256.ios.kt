package com.hnexperts.cosmetics.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual object Sha256 {
    actual fun hex(text: String): String {
        val bytes: ByteArray = text.encodeToByteArray()
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        bytes.usePinned { input ->
            digest.usePinned { output ->
                CC_SHA256(input.addressOf(0), bytes.size.toUInt(), output.addressOf(0))
            }
        }
        return digest.joinToString(separator = "") { value ->
            value.toString(16).padStart(2, '0')
        }
    }
}
