package com.pulsepoint.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulsepoint.app.BuildConfig
import com.pulsepoint.app.core.data.WeightUnit
import com.pulsepoint.app.core.util.DateFormatting
import com.pulsepoint.app.ui.theme.ThemeMode
import com.pulsepoint.app.ui.theme.pulseNegativeColor
import com.pulsepoint.app.ui.theme.pulsePositiveColor

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(initialValue = SettingsUiState())
    var showClearConfirm by remember { mutableStateOf(false) }

    SettingsContent(
        state = state,
        onThemeModeChange = viewModel::setThemeMode,
        onWeightUnitChange = viewModel::setWeightUnit,
        onRangeChange = viewModel::setChartRangeDays,
        onSaveUrl = viewModel::saveServerUrl,
        onTestConnection = viewModel::testConnection,
        onRefreshNow = viewModel::refreshNow,
        onClearData = {
            showClearConfirm = true
        }
    )

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear local data?") },
            text = {
                Text(
                    "This deletes all health snapshots and workouts from this device. " +
                        "A sync will restore them from the server."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLocalData()
                    showClearConfirm = false
                }) {
                    Text("Clear", color = pulseNegativeColor())
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onRangeChange: (Int) -> Unit,
    onSaveUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    onRefreshNow: () -> Unit,
    onClearData: () -> Unit
) {
    var urlText by remember { mutableStateOf(state.serverBaseUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SectionHeader("Appearance") }
            item {
                SettingCard {
                    SettingLabel("Theme mode")
                    Spacer(Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        val options = listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark")
                        options.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = state.themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Units") }
            item {
                SettingCard {
                    SettingLabel("Weight unit")
                    Spacer(Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        val options = listOf(WeightUnit.KG to "kg", WeightUnit.LB to "lb")
                        options.forEachIndexed { index, (unit, label) ->
                            SegmentedButton(
                                selected = state.weightUnit == unit,
                                onClick = { onWeightUnitChange(unit) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Data") }
            item {
                SettingCard {
                    SettingLabel("Default chart range")
                    Spacer(Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf(7, 30, 90).forEachIndexed { index, days ->
                            SegmentedButton(
                                selected = state.chartRangeDays == days,
                                onClick = { onRangeChange(days) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                            ) {
                                Text("${days}D")
                            }
                        }
                    }
                }
            }
            item {
                SettingCard {
                    SettingLabel("Demo server URL")
                    Text(
                        text = "Emulator: http://10.0.2.2:8765/  \u00B7  Physical device: http://<PC-LAN-IP>:8765/",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        singleLine = true,
                        label = { Text("http://host:port") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onSaveUrl(urlText) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save URL")
                        }
                        TextButton(
                            onClick = onTestConnection,
                            modifier = Modifier.weight(1f),
                            enabled = !state.isConnectionChecking
                        ) {
                            if (state.isConnectionChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Test connection")
                            }
                        }
                    }
                    state.connectionStatus?.let { status ->
                        Spacer(Modifier.height(8.dp))
                        val isOk = status.startsWith("Connected")
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOk) pulsePositiveColor() else pulseNegativeColor()
                        )
                    }
                }
            }
            item {
                SettingCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            SettingLabel("Sync now")
                            Text(
                                text = if (state.lastSyncMillis != null) {
                                    "Last synced ${DateFormatting.formatEpochMillis(state.lastSyncMillis)}"
                                } else {
                                    "Never synced"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onRefreshNow,
                            enabled = !state.isRefreshing
                        ) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Refresh")
                            }
                        }
                    }
                }
            }
            item {
                SettingCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            SettingLabel("Clear local data")
                            Text(
                                text = "Deletes stored snapshots and workouts from this device",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = onClearData) {
                            Text("Clear", color = pulseNegativeColor())
                        }
                    }
                }
            }
            if (state.hasClearedData) {
                item {
                    Text(
                        text = "Local data cleared. Pull to refresh in Vitality to re-sync.",
                        style = MaterialTheme.typography.labelMedium,
                        color = pulsePositiveColor()
                    )
                }
            }

            item { SectionHeader("About") }
            item {
                SettingCard {
                    Column {
                        SettingLabel("PulsePoint")
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Offline-first health tracking with a demo server feeding " +
                                "pre-calculated metrics. No health calculations are performed on-device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
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

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
}
