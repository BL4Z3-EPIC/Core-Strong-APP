package com.pulsepoint.app.ui.vitality

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulsepoint.app.ui.components.EmptyState
import com.pulsepoint.app.ui.components.ErrorState
import com.pulsepoint.app.ui.components.LoadingState
import com.pulsepoint.app.ui.components.OfflineBanner
import com.pulsepoint.app.ui.vitality.components.MetricCard
import com.pulsepoint.app.ui.vitality.components.SummaryHeaderCard

private val rangeOptions = listOf(7, 30, 90)

@Composable
fun VitalityScreen(viewModel: VitalityViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    VitalityContent(
        state = state,
        onRefresh = viewModel::refresh,
        onRangeSelected = viewModel::selectRange,
        onOpenSettings = { showSettings = true }
    )
    if (showSettings) {
        SettingsDialog(
            currentUrl = state.serverUrl,
            onDismiss = { showSettings = false },
            onSave = { url ->
                viewModel.setServerUrl(url)
                showSettings = false
            }
        )
    }
}

@Composable
private fun SettingsDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Demo server URL") },
        text = {
            Column {
                Text(
                    text = "Where the app fetches health data and workouts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("http://host:port") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Emulator: http://10.0.2.2:8765/\nPhysical device: http://<your-pc-LAN-IP>:8765/",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(url) }) { Text("Save & sync") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VitalityContent(
    state: VitalityUiState,
    onRefresh: () -> Unit,
    onRangeSelected: (Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PulsePoint", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Server: ${state.serverUrl}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Server settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                message = "No health data yet. Check the server URL (gear icon) and pull refresh to sync.",
                modifier = Modifier.padding(innerPadding)
            )

            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                if (state.isOffline) {
                    OfflineBanner()
                }
                Spacer(Modifier.height(8.dp))
                SummaryHeaderCard(latestSnapshot = state.latestSnapshot)
                Spacer(Modifier.height(16.dp))
                RangeSelector(
                    selectedDays = state.selectedRangeDays,
                    onRangeSelected = onRangeSelected
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricType.entries.forEach { metric ->
                        MetricCard(
                            metric = metric,
                            snapshots = state.snapshots,
                            rangeDays = state.selectedRangeDays
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
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
