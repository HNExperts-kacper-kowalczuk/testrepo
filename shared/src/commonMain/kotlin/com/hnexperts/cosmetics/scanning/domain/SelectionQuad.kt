package com.hnexperts.cosmetics.scanning.domain

/** A point in normalized image coordinates; both axes are in 0..1. */
data class CornerPoint(
    val x: Float,
    val y: Float
) {
    fun clamped(): CornerPoint {
        return CornerPoint(x = x.coerceIn(0f, 1f), y = y.coerceIn(0f, 1f))
    }
}

enum class QuadCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT
}

/**
 * Four user-draggable corners marking where the ingredient list sits on the
 * captured still. Normalized coordinates keep the quad independent of the
 * bitmap resolution and of how the preview is letterboxed on screen.
 */
data class SelectionQuad(
    val topLeft: CornerPoint,
    val topRight: CornerPoint,
    val bottomRight: CornerPoint,
    val bottomLeft: CornerPoint
) {
    fun corner(corner: QuadCorner): CornerPoint {
        return when (corner) {
            QuadCorner.TOP_LEFT -> topLeft
            QuadCorner.TOP_RIGHT -> topRight
            QuadCorner.BOTTOM_RIGHT -> bottomRight
            QuadCorner.BOTTOM_LEFT -> bottomLeft
        }
    }

    fun withCorner(corner: QuadCorner, point: CornerPoint): SelectionQuad {
        val clamped: CornerPoint = point.clamped()
        return when (corner) {
            QuadCorner.TOP_LEFT -> copy(topLeft = clamped)
            QuadCorner.TOP_RIGHT -> copy(topRight = clamped)
            QuadCorner.BOTTOM_RIGHT -> copy(bottomRight = clamped)
            QuadCorner.BOTTOM_LEFT -> copy(bottomLeft = clamped)
        }
    }

    fun corners(): List<Pair<QuadCorner, CornerPoint>> {
        return listOf(
            QuadCorner.TOP_LEFT to topLeft,
            QuadCorner.TOP_RIGHT to topRight,
            QuadCorner.BOTTOM_RIGHT to bottomRight,
            QuadCorner.BOTTOM_LEFT to bottomLeft
        )
    }

    companion object {
        private const val DEFAULT_INSET: Float = 0.1f

        fun defaultInset(): SelectionQuad {
            return SelectionQuad(
                topLeft = CornerPoint(DEFAULT_INSET, DEFAULT_INSET),
                topRight = CornerPoint(1f - DEFAULT_INSET, DEFAULT_INSET),
                bottomRight = CornerPoint(1f - DEFAULT_INSET, 1f - DEFAULT_INSET),
                bottomLeft = CornerPoint(DEFAULT_INSET, 1f - DEFAULT_INSET)
            )
        }
    }
}
