package com.hnexperts.cosmetics.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual object Sha256 {
    actual fun hex(text: String): String {
        return hex(text.encodeToByteArray())
    }

    actual fun hex(bytes: ByteArray): String {
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        hashInto(bytes, digest)
        return digest.joinToString(separator = "") { value ->
            value.toString(16).padStart(2, '0')
        }
    }

    private fun hashInto(bytes: ByteArray, digest: UByteArray) {
        if (bytes.isEmpty()) {
            digest.usePinned { output ->
                CC_SHA256(null, 0u, output.addressOf(0))
            }
            return
        }
        bytes.usePinned { input ->
            digest.usePinned { output ->
                CC_SHA256(input.addressOf(0), bytes.size.toUInt(), output.addressOf(0))
            }
        }
    }
}
