package com.example.data.models

/**
 * Represents an incident or pipeline rupture marked by the responder.
 * Stored with both user touch and exact pipe projection coordinates.
 */
data class IncidentPoint(
    val rawX: Double,
    val rawY: Double,
    val snappedX: Double,
    val snappedY: Double,
    val pipeId: String,
    val segmentIndex: Int,
    val isConfirmed: Boolean = false
)
