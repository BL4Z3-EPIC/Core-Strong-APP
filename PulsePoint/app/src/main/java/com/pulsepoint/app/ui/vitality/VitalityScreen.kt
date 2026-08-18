package com.pulsepoint.app.ui.vitality

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulsepoint.app.core.util.DateFormatting
import com.pulsepoint.app.ui.components.EmptyState
import com.pulsepoint.app.ui.components.ErrorState
import com.pulsepoint.app.ui.components.LoadingState
import com.pulsepoint.app.ui.components.OfflineBanner
import com.pulsepoint.app.ui.vitality.components.HeroCard
import com.pulsepoint.app.ui.vitality.components.MetricTile

private val rangeOptions = listOf(7, 30, 90)

@Composable
fun VitalityScreen(
    onOpenMetric: (MetricType) -> Unit,
    viewModel: VitalityViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle(initialValue = VitalityUiState())
    VitalityContent(
        state = state,
        onRefresh = viewModel::refresh,
        onRangeSelected = viewModel::selectRange,
        onOpenMetric = onOpenMetric
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VitalityContent(
    state: VitalityUiState,
    onRefresh: () -> Unit,
    onRangeSelected: (Int) -> Unit,
    onOpenMetric: (MetricType) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PulsePoint", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (state.lastSyncMillis != null) {
                                "Synced ${DateFormatting.formatEpochMillis(state.lastSyncMillis)}"
                            } else {
                                "Waiting for sync"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(innerPadding))

            state.hasError && state.snapshots.isEmpty() -> ErrorState(
                message = state.errorMessage ?: "Sync failed",
                onRetry = onRefresh,
                modifier = Modifier.padding(innerPadding)
            )

            state.snapshots.isEmpty() -> EmptyState(
                message = "No health data yet. Check the server URL in Settings and pull refresh to sync.",
                modifier = Modifier.padding(innerPadding)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (state.isOffline) {
                        OfflineBanner()
                        Spacer(Modifier.height(4.dp))
                    }
                }
                item {
                    HeroCard(
                        latestSnapshot = state.latestSnapshot,
                        weightUnit = state.weightUnit,
                        rangeDays = state.selectedRangeDays,
                        snapshots = state.snapshots
                    )
                }
                item {
                    RangeSelector(
                        selectedDays = state.selectedRangeDays,
                        onRangeSelected = onRangeSelected
                    )
                }
                item {
                    SectionHeader("Body Composition")
                }
                items(MetricType.entries.size, key = { it }) { index ->
                    val metric = MetricType.entries[index]
                    MetricTile(
                        metric = metric,
                        snapshots = state.snapshots,
                        rangeDays = state.selectedRangeDays,
                        weightUnit = state.weightUnit,
                        onClick = { onOpenMetric(metric) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeSelector(
    selectedDays: Int,
    onRangeSelected: (Int) -> Unit
) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        rangeOptions.forEachIndexed { index, days ->
            SegmentedButton(
                selected = selectedDays == days,
                onClick = { onRangeSelected(days) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = rangeOptions.size)
            ) {
                Text("${days}D")
            }
        }
    }
}
