package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.util.PersianUtils
import com.example.ui.info.NetworkInfoDialog
import com.example.ui.map.IncidentConfirmBar
import com.example.ui.map.MapControls
import com.example.ui.map.MapView
import com.example.ui.map.ValveSearchField
import com.example.ui.map.rememberMapState

/**
 * Main application screen.
 * Forces Right-to-Left (RTL) layout direction, shows full-screen high-contrast canvas map,
 * top dataset and search bars, incident marking workflow, and 56 dp action buttons.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val dataset by viewModel.dataset.collectAsState()
    val incidentPoint by viewModel.incidentPoint.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val highlightedValve by viewModel.highlightedValve.collectAsState()
    val showInfoDialog by viewModel.showInfoDialog.collectAsState()

    val mapState = rememberMapState()

    // When a valve is highlighted via search, center the map on it
    LaunchedEffect(highlightedValve) {
        highlightedValve?.let { valve ->
            mapState.centerOn(valve.x, valve.y, targetScale = 6.0f)
        }
    }

    // Force RTL layout direction across the entire screen
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. Full-screen MapView
                MapView(
                    dataset = dataset,
                    incidentPoint = incidentPoint,
                    highlightedValve = highlightedValve,
                    mapState = mapState,
                    onTapPipe = { rawX, rawY, snapResult ->
                        viewModel.setCandidateIncident(rawX, rawY, snapResult)
                    },
                    onDragIncident = { rawX, rawY, snapResult ->
                        viewModel.updateCandidateIncident(rawX, rawY, snapResult)
                    },
                    onSelectValve = { valve ->
                        viewModel.selectValve(valve)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Top Header and Search Bar (Floating)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Top Bar: Title, Demo badge, Solar Hijri date, Info button
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                // Demo Badge
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.demo_badge),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dataset.importDateShamsi,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = { viewModel.setShowInfoDialog(true) },
                                    modifier = Modifier.testTag("info_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = stringResource(R.string.info_title),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Valve Search Field (dual Persian/Latin)
                    ValveSearchField(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        searchResults = searchResults,
                        onSelectValve = { valve ->
                            viewModel.selectValve(valve)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Floating Map Controls (Zoom in, Zoom out, Center) on side
                MapControls(
                    onZoomIn = { mapState.zoomIn() },
                    onZoomOut = { mapState.zoomOut() },
                    onResetView = {
                        viewModel.clearHighlightedValve()
                        mapState.fitToBounds(dataset)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                )

                // 4. Initial hint pill when no incident has been marked
                AnimatedVisibility(
                    visible = incidentPoint == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.tap_incident_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 5. Floating Candidate Incident Confirm Bar (Buttons >= 56 dp)
                IncidentConfirmBar(
                    incidentPoint = incidentPoint,
                    onConfirm = { viewModel.confirmIncident() },
                    onCancel = { viewModel.cancelIncident() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // 6. Confirmed Incident Status Pill
                AnimatedVisibility(
                    visible = incidentPoint?.isConfirmed == true,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "نقطه حادثه تأیید شد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "خط لوله: ${incidentPoint?.pipeId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelIncident() },
                                    modifier = Modifier
                                        .height(56.dp)
                                        .testTag("reset_incident_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = "حذف حادثه",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Info Dialog Modal
                if (showInfoDialog) {
                    NetworkInfoDialog(
                        dataset = dataset,
                        onDismiss = { viewModel.setShowInfoDialog(false) }
                    )
                }
            }
        }
    }
}
