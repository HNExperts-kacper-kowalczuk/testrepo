package com.hnexperts.cosmetics.scanning.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class SelectionQuadTest {
    @Test
    fun defaultQuadIsInsetFromEveryEdge() {
        val quad: SelectionQuad = SelectionQuad.defaultInset()
        assertEquals(0.1f, quad.topLeft.x)
        assertEquals(0.1f, quad.topLeft.y)
        assertEquals(0.9f, quad.bottomRight.x)
        assertEquals(0.9f, quad.bottomRight.y)
    }

    @Test
    fun movingACornerClampsToTheImage() {
        val quad: SelectionQuad = SelectionQuad.defaultInset()
            .withCorner(QuadCorner.TOP_RIGHT, CornerPoint(x = 1.4f, y = -0.2f))
        assertEquals(1f, quad.topRight.x)
        assertEquals(0f, quad.topRight.y)
    }

    @Test
    fun cornersAreReportedInStableOrder() {
        val quad: SelectionQuad = SelectionQuad.defaultInset()
        val order: List<QuadCorner> = quad.corners().map { pair -> pair.first }
        assertEquals(
            listOf(QuadCorner.TOP_LEFT, QuadCorner.TOP_RIGHT, QuadCorner.BOTTOM_RIGHT, QuadCorner.BOTTOM_LEFT),
            order
        )
    }
}
