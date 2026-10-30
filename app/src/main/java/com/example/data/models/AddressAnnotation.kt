package com.example.data.models

/**
 * Represents a street address text annotation found on the CAD drawing.
 * Used to provide human-readable references for nearby emergency valves.
 */
data class AddressAnnotation(
    val id: String,
    val text: String,
    val x: Double,
    val y: Double,
    val textHeight: Double = 6.0
)
