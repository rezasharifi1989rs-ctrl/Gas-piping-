package com.example.data.models

/**
 * Marks a legitimate dead-end cap on a gas pipeline.
 * Represented in CAD by a small semicircle arc whose center lies at the terminal point of a pipe.
 */
data class LineEndMarker(
    val id: String,
    val centerX: Double,
    val centerY: Double,
    val radius: Double = 4.0,
    val angleDegrees: Double = 0.0 // Facing outward from pipe end
)
