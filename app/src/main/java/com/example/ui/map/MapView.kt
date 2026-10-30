package com.example.ui.map

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.data.models.AddressAnnotation
import com.example.data.models.Crossing
import com.example.data.models.GasNetworkDataset
import com.example.data.models.GasStation
import com.example.data.models.IncidentPoint
import com.example.data.models.LineEndMarker
import com.example.data.models.Pipe
import com.example.data.models.Valve
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Full-screen pan and pinch-zoom map rendering pipeline network, valves, stations,
 * dead-ends, addresses, and candidate/confirmed incident markers.
 */
@Composable
fun MapView(
    dataset: GasNetworkDataset,
    incidentPoint: IncidentPoint?,
    highlightedValve: Valve?,
    mapState: MapState,
    onTapPipe: (Double, Double, Pipe.ClosestResult) -> Unit,
    onDragIncident: (Double, Double, Pipe.ClosestResult) -> Unit,
    onSelectValve: (Valve) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val isDark = isSystemInDarkTheme()

    // Minimum touch tolerance (24 dp minimum per brief, 36 dp for outdoor/glove ease)
    val snapTolerancePx = with(density) { 36.dp.toPx() }
    val pipeStrokeWidthPx = with(density) { 6.dp.toPx() } // Constant thick on-screen width

    // Animation for pulse on incident marker
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Initial centering when canvas size becomes known
    var hasInitiallyCentered by remember { mutableStateOf(false) }

    // Reusable Paints for native text rendering on canvas
    val textPaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        }
    }

    val badgePaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            color = if (isDark) 0xDD1E293B.toInt() else 0xEEFFFFFF.toInt()
            style = Paint.Style.FILL
        }
    }

    val addressPaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = if (isDark) 0xFF94A3B8.toInt() else 0xFF475569.toInt()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                mapState.canvasSize = size.toSize()
                if (!hasInitiallyCentered && size.width > 0 && size.height > 0) {
                    mapState.fitToBounds(dataset)
                    hasInitiallyCentered = true
                }
            }
            // Pan and Pinch-to-zoom
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    mapState.applyZoom(zoom, centroid)
                    mapState.applyPan(pan.x, pan.y)
                }
            }
            // Drag to adjust incident marker
            .pointerInput(incidentPoint, dataset) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        val res = mapState.snapToNearestPipe(
                            change.position.x,
                            change.position.y,
                            dataset,
                            snapTolerancePx * 1.5f
                        )
                        if (res != null) {
                            val (wx, wy) = mapState.screenToWorld(change.position.x, change.position.y)
                            onDragIncident(wx, wy, res)
                        }
                    }
                )
            }
            // Tap to mark incident or select valve
            .pointerInput(dataset) {
                detectTapGestures { offset ->
                    // First check if user tapped directly on a valve
                    var tappedValve: Valve? = null
                    for (v in dataset.valves) {
                        val screenPos = mapState.worldToScreen(v.x, v.y)
                        val dist = hypot(screenPos.x - offset.x, screenPos.y - offset.y)
                        if (dist <= snapTolerancePx) {
                            tappedValve = v
                            break
                        }
                    }

                    if (tappedValve != null) {
                        onSelectValve(tappedValve)
                    } else {
                        // Snap to nearest pipe
                        val snapResult = mapState.snapToNearestPipe(
                            offset.x,
                            offset.y,
                            dataset,
                            snapTolerancePx
                        )
                        if (snapResult != null) {
                            val (wx, wy) = mapState.screenToWorld(offset.x, offset.y)
                            onTapPipe(wx, wy, snapResult)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw Pipelines (thick constant on-screen width)
            for (pipe in dataset.pipes) {
                drawPipe(pipe, mapState, pipeStrokeWidthPx)
            }

            // 2. Draw Crossings
            for (crossing in dataset.crossings) {
                drawCrossing(crossing, mapState, isDark)
            }

            // 3. Draw Line End Markers (semicircle caps)
            for (endMarker in dataset.endMarkers) {
                drawLineEndMarker(endMarker, mapState)
            }

            // 4. Draw Gas Stations (cyan triangle with capacity & name)
            for (station in dataset.stations) {
                drawGasStation(station, mapState, textPaint, isDark)
            }

            // 5. Draw Street Address Annotations
            for (addr in dataset.addresses) {
                drawAddress(addr, mapState, addressPaint, badgePaint)
            }

            // 6. Draw Valves (bow-tie symbols + normalized names)
            for (valve in dataset.valves) {
                val isHighlighted = valve.id == highlightedValve?.id
                drawValve(
                    valve = valve,
                    mapState = mapState,
                    isHighlighted = isHighlighted,
                    pulseScale = if (isHighlighted) pulseScale else 1.0f,
                    textPaint = textPaint,
                    badgePaint = badgePaint,
                    isDark = isDark
                )
            }

            // 7. Draw Incident Marker (Candidate or Confirmed)
            if (incidentPoint != null) {
                drawIncidentMarker(
                    incident = incidentPoint,
                    mapState = mapState,
                    pulseScale = pulseScale,
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * Draws a pipe polyline with constant screen stroke width.
 */
private fun DrawScope.drawPipe(
    pipe: Pipe,
    mapState: MapState,
    strokeWidthPx: Float
) {
    if (pipe.pointCount < 2) return

    val path = Path()
    val firstScreen = mapState.worldToScreen(pipe.getX(0), pipe.getY(0))
    path.moveTo(firstScreen.x, firstScreen.y)

    for (i in 1 until pipe.pointCount) {
        val nextScreen = mapState.worldToScreen(pipe.getX(i), pipe.getY(i))
        path.lineTo(nextScreen.x, nextScreen.y)
    }

    val pipeColor = Color(pipe.colorRgb)

    // Under-glow/outline for high contrast in dark mode
    drawPath(
        path = path,
        color = Color(0x66000000),
        style = Stroke(
            width = strokeWidthPx + 4f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Core thick pipe
    drawPath(
        path = path,
        color = pipeColor,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draws a bow-tie valve symbol (two triangles touching at tips centered on the pipe)
 * and its normalized identifier label (e.g. 24V1).
 */
private fun DrawScope.drawValve(
    valve: Valve,
    mapState: MapState,
    isHighlighted: Boolean,
    pulseScale: Float,
    textPaint: Paint,
    badgePaint: Paint,
    isDark: Boolean
) {
    val center = mapState.worldToScreen(valve.x, valve.y)

    // Highlight aura if searched
    if (isHighlighted) {
        drawCircle(
            color = Color(0xFFFFD600).copy(alpha = 0.35f),
            radius = 36f * pulseScale,
            center = center
        )
        drawCircle(
            color = Color(0xFFFFD600),
            radius = 28f,
            center = center,
            style = Stroke(width = 4f)
        )
    }

    // Bow-tie dimensions (constant screen size for clarity)
    val halfW = 16f
    val halfH = 12f
    val angleRad = Math.toRadians(valve.rotationDegrees)
    val cosA = cos(angleRad).toFloat()
    val sinA = sin(angleRad).toFloat()

    fun rotatePoint(dx: Float, dy: Float): Offset {
        val rx = dx * cosA - dy * sinA
        val ry = dx * sinA + dy * cosA
        return Offset(center.x + rx, center.y + ry)
    }

    // Left triangle
    val path = Path().apply {
        val p1 = rotatePoint(-halfW, -halfH)
        val p2 = rotatePoint(-halfW, halfH)
        val pCenter = center

        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(pCenter.x, pCenter.y)
        close()

        // Right triangle
        val p3 = rotatePoint(halfW, -halfH)
        val p4 = rotatePoint(halfW, halfH)
        moveTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        lineTo(pCenter.x, pCenter.y)
        close()
    }

    // Bow-tie fill: bright amber
    val valveColor = Color(0xFFFFD600)
    drawPath(path = path, color = valveColor)

    // Bow-tie outline: black
    drawPath(
        path = path,
        color = Color.Black,
        style = Stroke(width = 3f, join = StrokeJoin.Miter)
    )

    // Center pivot point
    drawCircle(
        color = Color.Black,
        radius = 3.5f,
        center = center
    )

    // Valve Name Label Badge (e.g. 24V1, 101V1)
    val label = valve.name
    val bounds = Rect()
    textPaint.getTextBounds(label, 0, label.length, bounds)

    val labelX = center.x - bounds.width() / 2f
    val labelY = center.y - halfH - 12f

    val padH = 8f
    val padV = 5f
    val badgeRect = android.graphics.RectF(
        labelX - padH,
        labelY - bounds.height() - padV,
        labelX + bounds.width() + padH,
        labelY + padV
    )

    drawContext.canvas.nativeCanvas.apply {
        drawRoundRect(badgeRect, 8f, 8f, badgePaint)
        // Outline on badge
        val borderPaint = Paint().apply {
            color = if (isDark) 0xFF475569.toInt() else 0xFFCBD5E1.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        drawRoundRect(badgeRect, 8f, 8f, borderPaint)
        drawText(label, labelX, labelY, textPaint)
    }
}

/**
 * Draws a Gas Station (solid-filled cyan triangle whose tip touches the pipe end,
 * plus its capacity and name).
 */
private fun DrawScope.drawGasStation(
    station: GasStation,
    mapState: MapState,
    textPaint: Paint,
    isDark: Boolean
) {
    val tip = mapState.worldToScreen(station.tipX, station.tipY)
    val angleRad = Math.toRadians(station.rotationDegrees)
    val cosA = cos(angleRad).toFloat()
    val sinA = sin(angleRad).toFloat()

    // Station triangle pointing away from pipe
    val baseLength = 32f
    val baseWidth = 28f

    fun rotatePoint(dx: Float, dy: Float): Offset {
        val rx = dx * cosA - dy * sinA
        val ry = dx * sinA + dy * cosA
        return Offset(tip.x + rx, tip.y + ry)
    }

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        val p1 = rotatePoint(baseLength, -baseWidth / 2f)
        val p2 = rotatePoint(baseLength, baseWidth / 2f)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }

    // Cyan color (AutoCAD color index 4)
    val cyan = Color(0xFF00E5FF)
    drawPath(path = path, color = cyan)
    drawPath(path = path, color = Color.Black, style = Stroke(width = 3f))

    // Station label & capacity
    val label = "${station.name} (${station.capacity})"
    val stationPaint = Paint(textPaint).apply {
        textSize = 30f
        color = if (isDark) 0xFF00E5FF.toInt() else 0xFF00838F.toInt()
    }
    val bounds = Rect()
    stationPaint.getTextBounds(label, 0, label.length, bounds)

    val labelX = tip.x + 20f
    val labelY = tip.y - 15f

    drawContext.canvas.nativeCanvas.drawText(label, labelX, labelY, stationPaint)
}

/**
 * Draws a line-end marker (semicircle arc indicating a legitimate dead-end cap).
 */
private fun DrawScope.drawLineEndMarker(
    marker: LineEndMarker,
    mapState: MapState
) {
    val center = mapState.worldToScreen(marker.centerX, marker.centerY)
    val arcRadiusPx = 14f

    drawArc(
        color = Color(0xFF00E676),
        startAngle = (marker.angleDegrees - 90).toFloat(),
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - arcRadiusPx, center.y - arcRadiusPx),
        size = Size(arcRadiusPx * 2, arcRadiusPx * 2),
        style = Stroke(width = 5f, cap = StrokeCap.Round)
    )

    // Center dot
    drawCircle(
        color = Color(0xFF00E676),
        radius = 4f,
        center = center
    )
}

/**
 * Draws a physical pipeline crossing indicator.
 */
private fun DrawScope.drawCrossing(
    crossing: Crossing,
    mapState: MapState,
    isDark: Boolean
) {
    val center = mapState.worldToScreen(crossing.x, crossing.y)

    // Outer cross indicator
    val crossColor = if (crossing.isConnected) {
        Color(0xFFFFD600) // Amber for same-color ambiguous crossing
    } else {
        Color(0xFFE2E8F0) // Clean white/grey for non-connected crossing
    }

    drawCircle(
        color = crossColor.copy(alpha = 0.35f),
        radius = 16f,
        center = center
    )
    drawCircle(
        color = crossColor,
        radius = 12f,
        center = center,
        style = Stroke(width = 2.5f)
    )
}

/**
 * Draws street address text annotation.
 */
private fun DrawScope.drawAddress(
    addr: AddressAnnotation,
    mapState: MapState,
    addressPaint: Paint,
    badgePaint: Paint
) {
    val pos = mapState.worldToScreen(addr.x, addr.y)
    val bounds = Rect()
    addressPaint.getTextBounds(addr.text, 0, addr.text.length, bounds)

    val padH = 8f
    val padV = 4f
    val badgeRect = android.graphics.RectF(
        pos.x - padH,
        pos.y - bounds.height() - padV,
        pos.x + bounds.width() + padH,
        pos.y + padV
    )

    drawContext.canvas.nativeCanvas.apply {
        drawRoundRect(badgeRect, 6f, 6f, badgePaint)
        drawText(addr.text, pos.x, pos.y, addressPaint)
    }
}

/**
 * Draws the emergency incident marker (crosshair target, glowing pulse, and label).
 */
private fun DrawScope.drawIncidentMarker(
    incident: IncidentPoint,
    mapState: MapState,
    pulseScale: Float,
    isDark: Boolean
) {
    val snappedScreen = mapState.worldToScreen(incident.snappedX, incident.snappedY)
    val emergencyRed = Color(0xFFD50000)
    val safetyOrange = Color(0xFFFF6D00)

    // Outer pulsating wave
    drawCircle(
        color = if (incident.isConfirmed) emergencyRed.copy(alpha = 0.25f) else safetyOrange.copy(alpha = 0.25f),
        radius = 38f * pulseScale,
        center = snappedScreen
    )

    // Middle boundary ring
    drawCircle(
        color = if (incident.isConfirmed) emergencyRed else safetyOrange,
        radius = 22f,
        center = snappedScreen,
        style = Stroke(width = 4f)
    )

    // Crosshairs
    val crosshairLen = 14f
    drawLine(
        color = Color.White,
        start = Offset(snappedScreen.x - crosshairLen, snappedScreen.y),
        end = Offset(snappedScreen.x + crosshairLen, snappedScreen.y),
        strokeWidth = 3f
    )
    drawLine(
        color = Color.White,
        start = Offset(snappedScreen.x, snappedScreen.y - crosshairLen),
        end = Offset(snappedScreen.x, snappedScreen.y + crosshairLen),
        strokeWidth = 3f
    )

    // Center focal point
    drawCircle(
        color = if (incident.isConfirmed) emergencyRed else safetyOrange,
        radius = 6f,
        center = snappedScreen
    )

    // Floating status tag above marker
    val tagText = if (incident.isConfirmed) "نقطه حادثه تأیید شده" else "محل حادثه (قابل جابجایی)"
    val tagPaint = Paint().apply {
        isAntiAlias = true
        textSize = 30f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.WHITE
    }
    val bounds = Rect()
    tagPaint.getTextBounds(tagText, 0, tagText.length, bounds)

    val tagX = snappedScreen.x - bounds.width() / 2f
    val tagY = snappedScreen.y - 36f

    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = if (incident.isConfirmed) 0xFFD50000.toInt() else 0xFFE65100.toInt()
        style = Paint.Style.FILL
    }

    val tagRect = android.graphics.RectF(
        tagX - 12f,
        tagY - bounds.height() - 8f,
        tagX + bounds.width() + 12f,
        tagY + 8f
    )

    drawContext.canvas.nativeCanvas.apply {
        drawRoundRect(tagRect, 10f, 10f, bgPaint)
        drawText(tagText, tagX, tagY, tagPaint)
    }
}
