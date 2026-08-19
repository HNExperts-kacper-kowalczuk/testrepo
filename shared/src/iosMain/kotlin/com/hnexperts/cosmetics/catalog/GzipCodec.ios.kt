package com.hnexperts.cosmetics.catalog

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.zlib.MAX_WBITS
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.z_stream

@OptIn(ExperimentalForeignApi::class)
actual object GzipCodec {
    actual fun inflate(input: ByteArray): ByteArray {
        memScoped {
            val stream = alloc<z_stream>()
            val init: Int = inflateInit2(stream.ptr, 16 + MAX_WBITS)
            if (init != Z_OK) {
                error("gzip inflateInit2 failed: $init")
            }
            try {
                return inflateAll(stream, input)
            } finally {
                inflateEnd(stream.ptr)
            }
        }
    }

    private fun inflateAll(stream: z_stream, input: ByteArray): ByteArray {
        val parts: MutableList<ByteArray> = mutableListOf()
        val chunk: ByteArray = ByteArray(CHUNK)
        input.usePinned { pinnedIn ->
            stream.next_in = pinnedIn.addressOf(0).reinterpret()
            stream.avail_in = input.size.toUInt()
            while (true) {
                val produced: Int = chunk.usePinned { pinnedOut ->
                    stream.next_out = pinnedOut.addressOf(0).reinterpret()
                    stream.avail_out = chunk.size.toUInt()
                    val status: Int = inflate(stream.ptr, Z_NO_FLUSH)
                    if (status != Z_OK && status != Z_STREAM_END) {
                        error("gzip inflate failed: $status")
                    }
                    val count: Int = chunk.size - stream.avail_out.toInt()
                    if (status == Z_STREAM_END) {
                        parts.add(chunk.copyOf(count))
                        return@usePinned -1
                    }
                    parts.add(chunk.copyOf(count))
                    count
                }
                if (produced < 0) {
                    break
                }
            }
        }
        return concat(parts)
    }

    private fun concat(parts: List<ByteArray>): ByteArray {
        val total: Int = parts.sumOf { part -> part.size }
        val out: ByteArray = ByteArray(total)
        var offset: Int = 0
        for (part in parts) {
            part.copyInto(out, offset)
            offset += part.size
        }
        return out
    }

    private const val CHUNK: Int = 256 * 1024
}
