package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.demo.DemoNetworkData
import com.example.data.models.GasNetworkDataset
import com.example.data.models.IncidentPoint
import com.example.data.models.Pipe
import com.example.data.models.Valve
import com.example.data.util.PersianUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel managing the active gas dataset, incident marking, valve search, and UI state.
 */
class MainViewModel : ViewModel() {

    private val _dataset = MutableStateFlow<GasNetworkDataset>(DemoNetworkData.createDataset())
    val dataset: StateFlow<GasNetworkDataset> = _dataset.asStateFlow()

    private val _incidentPoint = MutableStateFlow<IncidentPoint?>(null)
    val incidentPoint: StateFlow<IncidentPoint?> = _incidentPoint.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _highlightedValve = MutableStateFlow<Valve?>(null)
    val highlightedValve: StateFlow<Valve?> = _highlightedValve.asStateFlow()

    private val _showInfoDialog = MutableStateFlow(false)
    val showInfoDialog: StateFlow<Boolean> = _showInfoDialog.asStateFlow()

    // Filter valves matching query
    val searchResults: StateFlow<List<Valve>> = combine(_searchQuery, _dataset) { query, ds ->
        if (query.isBlank()) {
            emptyList()
        } else {
            ds.valves.filter { valve ->
                PersianUtils.matchesValve(valve.name, query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Places a candidate incident point snapped to the closest pipeline segment.
     */
    fun setCandidateIncident(rawX: Double, rawY: Double, snapResult: Pipe.ClosestResult) {
        _incidentPoint.value = IncidentPoint(
            rawX = rawX,
            rawY = rawY,
            snappedX = snapResult.snappedX,
            snappedY = snapResult.snappedY,
            pipeId = snapResult.pipeId,
            segmentIndex = snapResult.segmentIndex,
            isConfirmed = false
        )
    }

    /**
     * Updates candidate incident point during drag adjustments.
     */
    fun updateCandidateIncident(rawX: Double, rawY: Double, snapResult: Pipe.ClosestResult) {
        val current = _incidentPoint.value
        if (current != null && !current.isConfirmed) {
            _incidentPoint.value = current.copy(
                rawX = rawX,
                rawY = rawY,
                snappedX = snapResult.snappedX,
                snappedY = snapResult.snappedY,
                pipeId = snapResult.pipeId,
                segmentIndex = snapResult.segmentIndex
            )
        }
    }

    /**
     * Confirms the marked incident point.
     */
    fun confirmIncident() {
        val current = _incidentPoint.value ?: return
        _incidentPoint.value = current.copy(isConfirmed = true)
    }

    /**
     * Cancels and clears the current incident point.
     */
    fun cancelIncident() {
        _incidentPoint.value = null
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectValve(valve: Valve) {
        _highlightedValve.value = valve
    }

    fun clearHighlightedValve() {
        _highlightedValve.value = null
    }

    fun setShowInfoDialog(show: Boolean) {
        _showInfoDialog.value = show
    }
}
