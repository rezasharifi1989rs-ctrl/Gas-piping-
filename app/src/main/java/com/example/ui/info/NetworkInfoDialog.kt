package com.example.ui.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.GasNetworkDataset
import com.example.data.util.PersianUtils

/**
 * Information modal showing dataset metadata, statistics, and full map legend.
 */
@Composable
fun NetworkInfoDialog(
    dataset: GasNetworkDataset,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.info_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dataset Details Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(
                            label = stringResource(R.string.dataset_active),
                            value = dataset.name
                        )
                        InfoRow(
                            label = stringResource(R.string.import_date),
                            value = dataset.importDateShamsi
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow(
                            label = "تعداد خطوط لوله:",
                            value = PersianUtils.toPersianDigits(dataset.pipes.size)
                        )
                        InfoRow(
                            label = "تعداد شیرهای قطع گاز:",
                            value = PersianUtils.toPersianDigits(dataset.valves.size)
                        )
                        InfoRow(
                            label = "تعداد ایستگاه‌های تغذیه:",
                            value = PersianUtils.toPersianDigits(dataset.stations.size)
                        )
                        InfoRow(
                            label = "تعداد تقاطع‌ها:",
                            value = PersianUtils.toPersianDigits(dataset.crossings.size)
                        )
                    }
                }

                // Legend section
                Text(
                    text = stringResource(R.string.legend_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LegendItem(
                    symbol = {
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 5.dp)
                                .background(Color(0xFF2979FF), RoundedCornerShape(2.dp))
                        )
                    },
                    label = stringResource(R.string.legend_pipe)
                )

                LegendItem(
                    symbol = {
                        Text(
                            text = "⧓",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD600)
                        )
                    },
                    label = stringResource(R.string.legend_valve)
                )

                LegendItem(
                    symbol = {
                        Text(
                            text = "▲",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    },
                    label = stringResource(R.string.legend_station)
                )

                LegendItem(
                    symbol = {
                        Text(
                            text = "◗",
                            fontSize = 22.sp,
                            color = Color(0xFF00E676)
                        )
                    },
                    label = stringResource(R.string.legend_end)
                )

                LegendItem(
                    symbol = {
                        Text(
                            text = "⊕",
                            fontSize = 22.sp,
                            color = Color(0xFFD50000)
                        )
                    },
                    label = stringResource(R.string.legend_incident)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("dismiss_info_dialog")
            ) {
                Text(
                    text = stringResource(R.string.close),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LegendItem(
    symbol: @Composable () -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            symbol()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
