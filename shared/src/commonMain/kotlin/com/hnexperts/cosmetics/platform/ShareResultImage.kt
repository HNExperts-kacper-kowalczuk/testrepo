package com.hnexperts.cosmetics.platform

import com.hnexperts.cosmetics.evaluation.application.ShareResultImageLayout

expect fun encodeSharePng(layout: ShareResultImageLayout): ByteArray

expect fun sharePngBytes(title: String, png: ByteArray)
