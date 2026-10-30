package com.example.data.models

/**
 * Represents a physical crossing where two pipelines intersect in 2D space.
 * Unless they share ends (T-junction), crossing lines with different colors do NOT connect.
 * Same-color crossings are flagged as ambiguous.
 */
data class Crossing(
    val id: String,
    val x: Double,
    val y: Double,
    val pipe1Id: String,
    val pipe1ColorIndex: Int,
    val pipe2Id: String,
    val pipe2ColorIndex: Int,
    val isConnected: Boolean,
    val isAmbiguous: Boolean
)
