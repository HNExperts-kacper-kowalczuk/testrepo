package com.hnexperts.cosmetics.platform

import com.hnexperts.cosmetics.catalog.fixture.EvaluationFactory
import com.hnexperts.cosmetics.evaluation.application.ShareCopy
import com.hnexperts.cosmetics.evaluation.application.ShareResultText
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ShareResultPngTest {
    @Test
    fun encodedPngContainsDisclaimerPayloadAndPngSignature() {
        val assessment = EvaluationFactory.create().evaluate(
            inciRaw = "Aqua, Glycerin",
            profile = UserAvoidanceProfile.EMPTY,
            productName = "Gentle Cream Cleanser"
        )
        val copy = ShareCopy(
            scannedProduct = "Scanned product",
            suitable = "No personal avoid-list hits.",
            notSuitable = "Not suitable for your current filters.",
            disclaimer = "Informational only. This is not a medical device or a substitute for the ingredient list, a dermatologist, or official EU annexes.",
            overallLabel = "Generally acceptable",
            scannedAtLabel = "Scanned"
        )
        val layout = ShareResultText.layout(
            assessment = assessment,
            copy = copy,
            scannedAt = Instant.parse("2026-08-19T12:00:00Z")
        )
        assertTrue(layout.payloadText().contains(copy.disclaimer))
        val png: ByteArray = encodeSharePng(layout)
        assertTrue(png.size > 100)
        assertEquals(0x89.toByte(), png[0])
        assertEquals('P'.code.toByte(), png[1])
        assertEquals('N'.code.toByte(), png[2])
        assertEquals('G'.code.toByte(), png[3])
    }
}
