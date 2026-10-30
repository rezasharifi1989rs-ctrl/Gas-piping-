package com.example.data.models

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a gas pipeline polyline in CAD coordinates.
 * Coordinates are kept in a flat primitive DoubleArray [x0, y0, x1, y1, ...] for high performance
 * and minimal memory overhead with large datasets.
 */
data class Pipe(
    val id: String,
    val points: DoubleArray, // Flattened coordinates: [x0, y0, x1, y1, ...]
    val colorIndex: Int = 5,  // AutoCAD color index (e.g. 5=blue, 3=green, 1=red)
    val colorRgb: Long = 0xFF2979FF, // ARGB color
    val layer: String = "0"
) {
    val pointCount: Int
        get() = points.size / 2

    val segmentCount: Int
        get() = max(0, pointCount - 1)

    fun getX(index: Int): Double = points[index * 2]
    fun getY(index: Int): Double = points[index * 2 + 1]

    /**
     * Total length of the pipe in drawing units.
     */
    fun computeLength(): Double {
        var total = 0.0
        for (i in 0 until segmentCount) {
            val x1 = getX(i)
            val y1 = getY(i)
            val x2 = getX(i + 1)
            val y2 = getY(i + 1)
            total += hypot(x2 - x1, y2 - y1)
        }
        return total
    }

    /**
     * Projects a query point (qx, qy) onto this pipe.
     * Returns a ClosestResult with the snapped coordinates, distance, and segment index.
     */
    fun findClosestPoint(qx: Double, qy: Double): ClosestResult {
        var minDistance = Double.MAX_VALUE
        var bestX = if (pointCount > 0) getX(0) else qx
        var bestY = if (pointCount > 0) getY(0) else qy
        var bestSegment = 0

        for (i in 0 until segmentCount) {
            val x1 = getX(i)
            val y1 = getY(i)
            val x2 = getX(i + 1)
            val y2 = getY(i + 1)

            val dx = x2 - x1
            val dy = y2 - y1
            val lengthSquared = dx * dx + dy * dy

            val projX: Double
            val projY: Double

            if (lengthSquared == 0.0) {
                projX = x1
                projY = y1
            } else {
                // Projection parameter t clamped to [0, 1]
                val t = max(0.0, min(1.0, ((qx - x1) * dx + (qy - y1) * dy) / lengthSquared))
                projX = x1 + t * dx
                projY = y1 + t * dy
            }

            val dist = hypot(qx - projX, qy - projY)
            if (dist < minDistance) {
                minDistance = dist
                bestX = projX
                bestY = projY
                bestSegment = i
            }
        }

        return ClosestResult(
            pipeId = id,
            snappedX = bestX,
            snappedY = bestY,
            distance = minDistance,
            segmentIndex = bestSegment
        )
    }

    data class ClosestResult(
        val pipeId: String,
        val snappedX: Double,
        val snappedY: Double,
        val distance: Double,
        val segmentIndex: Int
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Pipe
        if (id != other.id) return false
        if (!points.contentEquals(other.points)) return false
        if (colorIndex != other.colorIndex) return false
        if (colorRgb != other.colorRgb) return false
        return layer == other.layer
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + points.contentHashCode()
        result = 31 * result + colorIndex
        result = 31 * result + colorRgb.hashCode()
        result = 31 * result + layer.hashCode()
        return result
    }
}
