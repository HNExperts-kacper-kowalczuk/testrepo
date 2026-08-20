package com.hnexperts.cosmetics.catalog.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InciIdentityTest {
    @Test
    fun aquaGlycerinHasStableKnownHash() {
        assertEquals(
            "257d0935fe564f75e20e72ca49708d1f771220e4ad4a90e6a6ae04e8c6dfea0d",
            InciIdentity.hash("Aqua, Glycerin")
        )
    }

    @Test
    fun spacingAndCaseDoNotChangeHash() {
        val compact: String = InciIdentity.hash("Aqua, Glycerin")
        assertEquals(compact, InciIdentity.hash("aqua,  glycerin"))
        assertEquals(compact, InciIdentity.hash("AQUA,GLYCERIN"))
    }

    @Test
    fun addingFormaldehydeChangesHash() {
        assertNotEquals(
            InciIdentity.hash("Aqua, Glycerin"),
            InciIdentity.hash("Aqua, Glycerin, Formaldehyde")
        )
    }
}
