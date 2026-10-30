package com.example.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.data.models.GasNetworkDataset
import com.example.data.models.Pipe
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Manages 2D pan/zoom viewport state, coordinate transformations, and geometry snapping.
 */
class MapState(
    initialScale: Float = 2.5f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
) {
    var scale by mutableFloatStateOf(initialScale)
    var offsetX by mutableFloatStateOf(initialOffsetX)
    var offsetY by mutableFloatStateOf(initialOffsetY)

    var canvasSize by mutableStateOf(Size.Zero)

    val minScale = 0.5f
    val maxScale = 25f

    fun worldToScreen(worldX: Double, worldY: Double): Offset {
        val sx = (worldX * scale + offsetX).toFloat()
        val sy = (worldY * scale + offsetY).toFloat()
        return Offset(sx, sy)
    }

    fun screenToWorld(screenX: Float, screenY: Float): Pair<Double, Double> {
        val wx = (screenX - offsetX) / scale.toDouble()
        val wy = (screenY - offsetY) / scale.toDouble()
        return Pair(wx, wy)
    }

    fun applyPan(deltaX: Float, deltaY: Float) {
        offsetX += deltaX
        offsetY += deltaY
    }

    fun applyZoom(zoomFactor: Float, centroid: Offset) {
        val oldScale = scale
        val newScale = (oldScale * zoomFactor).coerceIn(minScale, maxScale)
        if (newScale != oldScale) {
            // Zoom centered around gesture centroid
            offsetX = centroid.x - (centroid.x - offsetX) * (newScale / oldScale)
            offsetY = centroid.y - (centroid.y - offsetY) * (newScale / oldScale)
            scale = newScale
        }
    }

    fun zoomIn() {
        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        applyZoom(1.35f, center)
    }

    fun zoomOut() {
        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        applyZoom(0.74f, center)
    }

    /**
     * Centers the map viewport on a specific world coordinate.
     */
    fun centerOn(worldX: Double, worldY: Double, targetScale: Float = 5.0f) {
        scale = targetScale.coerceIn(minScale, maxScale)
        offsetX = (canvasSize.width / 2f) - (worldX * scale).toFloat()
        offsetY = (canvasSize.height / 2f) - (worldY * scale).toFloat()
    }

    /**
     * Fits the whole dataset inside the visible canvas viewport with margins.
     */
    fun fitToBounds(dataset: GasNetworkDataset) {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return
        val bounds = dataset.bounds
        val scaleX = (canvasSize.width * 0.85f) / bounds.width.toFloat()
        val scaleY = (canvasSize.height * 0.85f) / bounds.height.toFloat()
        val fittedScale = min(scaleX, scaleY).coerceIn(minScale, maxScale)

        scale = fittedScale
        offsetX = (canvasSize.width / 2f) - (bounds.centerX * fittedScale).toFloat()
        offsetY = (canvasSize.height / 2f) - (bounds.centerY * fittedScale).toFloat()
    }

    /**
     * Snaps a screen tap (screenX, screenY) to the nearest pipeline segment
     * within a maximum touch tolerance radius in pixels.
     */
    fun snapToNearestPipe(
        screenX: Float,
        screenY: Float,
        dataset: GasNetworkDataset,
        maxScreenTolerancePx: Float
    ): Pipe.ClosestResult? {
        val (worldX, worldY) = screenToWorld(screenX, screenY)
        val maxWorldTolerance = maxScreenTolerancePx / scale.toDouble()

        var bestResult: Pipe.ClosestResult? = null
        var bestDist = Double.MAX_VALUE

        for (pipe in dataset.pipes) {
            val res = pipe.findClosestPoint(worldX, worldY)
            if (res.distance < bestDist && res.distance <= maxWorldTolerance) {
                bestDist = res.distance
                bestResult = res
            }
        }

        return bestResult
    }
}

@Composable
fun rememberMapState(): MapState {
    return remember { MapState() }
}
