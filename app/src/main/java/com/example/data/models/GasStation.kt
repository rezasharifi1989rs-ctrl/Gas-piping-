package com.example.data.models

/**
 * Represents a gas intake/reducing station (CGS, TBS, City Gate Station).
 * Represented in CAD by a solid-filled cyan triangle (color index 4) whose tip touches a pipe end.
 * Accompanied by capacity and name text. Acts as a network source.
 */
data class GasStation(
    val id: String,
    val name: String,
    val capacity: String, // e.g. "400 m³/h"
    val tipX: Double,     // The anchor point touching the pipe end
    val tipY: Double,
    val rotationDegrees: Double = 0.0 // Direction the station body points away from pipe
)
