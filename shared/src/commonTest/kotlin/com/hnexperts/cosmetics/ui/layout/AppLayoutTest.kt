package com.hnexperts.cosmetics.ui.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppLayoutTest {
    @Test
    fun compactPhoneKeepsSixteenDpGutters() {
        assertEquals(16, AppLayout.horizontalGutterDp(360))
        assertEquals(328, AppLayout.contentWidthDp(360))
    }

    @Test
    fun mediumWidthUsesTwentyFourDpGutters() {
        assertEquals(24, AppLayout.horizontalGutterDp(700))
        assertEquals(652, AppLayout.contentWidthDp(700))
    }

    @Test
    fun expandedWidthCentersReadableColumn() {
        val gutter: Int = AppLayout.horizontalGutterDp(1000)
        assertEquals(200, gutter)
        assertEquals(600, AppLayout.contentWidthDp(1000))
        assertTrue(AppLayout.MIN_TOUCH_DP >= 48)
    }
}
