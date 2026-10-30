package com.example.data.models

/**
 * Represents a gas shut-off valve.
 * Identified on drawings by a bow-tie symbol (two small triangles touching at tips)
 * centered on a pipe and labeled with a name like 24V1, 101V1, etc.
 */
data class Valve(
    val id: String,
    val name: String, // Normalized name (e.g. "24V1", "101V1")
    val x: Double,
    val y: Double,
    val rotationDegrees: Double = 0.0, // Angle along pipe for symbol alignment
    val pipeId: String? = null,
    val nearestAddress: String? = null,
    val distanceToAddress: Double = 0.0
)
