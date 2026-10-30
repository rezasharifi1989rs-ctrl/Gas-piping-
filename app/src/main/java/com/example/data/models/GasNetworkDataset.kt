package com.example.data.models

import kotlin.math.max
import kotlin.math.min

/**
 * Encapsulates the entire active gas network dataset loaded into memory.
 */
data class GasNetworkDataset(
    val id: String,
    val name: String,
    val isDemo: Boolean = true,
    val importDateShamsi: String,
    val pipes: List<Pipe>,
    val valves: List<Valve>,
    val stations: List<GasStation>,
    val endMarkers: List<LineEndMarker>,
    val addresses: List<AddressAnnotation>,
    val crossings: List<Crossing> = emptyList(),
    val scaleMetersPerUnit: Double = 1.0
) {
    val bounds: NetworkBounds by lazy {
        var minX = Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxX = -Double.MAX_VALUE
        var maxY = -Double.MAX_VALUE

        for (pipe in pipes) {
            for (i in 0 until pipe.pointCount) {
                val x = pipe.getX(i)
                val y = pipe.getY(i)
                minX = min(minX, x)
                minY = min(minY, y)
                maxX = max(maxX, x)
                maxY = max(maxY, y)
            }
        }

        for (station in stations) {
            minX = min(minX, station.tipX)
            minY = min(minY, station.tipY)
            maxX = max(maxX, station.tipX)
            maxY = max(maxY, station.tipY)
        }

        if (minX > maxX) {
            NetworkBounds(0.0, 0.0, 100.0, 100.0)
        } else {
            // Add slight margin around bounds
            val paddingX = max(10.0, (maxX - minX) * 0.05)
            val paddingY = max(10.0, (maxY - minY) * 0.05)
            NetworkBounds(
                minX = minX - paddingX,
                minY = minY - paddingY,
                maxX = maxX + paddingX,
                maxY = maxY + paddingY
            )
        }
    }
}

data class NetworkBounds(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
) {
    val width: Double get() = max(1.0, maxX - minX)
    val height: Double get() = max(1.0, maxY - minY)
    val centerX: Double get() = (minX + maxX) / 2.0
    val centerY: Double get() = (minY + maxY) / 2.0
}
