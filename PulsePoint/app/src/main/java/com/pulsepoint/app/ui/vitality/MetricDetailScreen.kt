package com.pulsepoint.app.ui.vitality

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsepoint.app.ui.components.EmptyState
import com.pulsepoint.app.ui.theme.pulseNegativeColor
import com.pulsepoint.app.ui.theme.pulsePositiveColor
import com.pulsepoint.app.ui.vitality.components.MetricChart
import java.util.Locale

private val detailRangeOptions = listOf(7, 30, 90)

@Composable
fun MetricDetailScreen(
    metric: MetricType,
    onBack: () -> Unit,
    viewModel: MetricDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MetricDetailViewModel.factory(metric)
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle(initialValue = MetricDetailUiState(metric = metric))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.metric.label, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Last ${state.rangeDays} days",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.snapshots.isEmpty()) {
            EmptyState(
                message = "No data for ${state.metric.label} in this range.",
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            DetailValueHeader(state)
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Box(Modifier.padding(8.dp)) {
                    MetricChart(
                        metric = state.metric,
                        snapshots = state.snapshots,
                        rangeDays = state.rangeDays,
                        weightUnit = state.weightUnit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                detailRangeOptions.forEachIndexed { index, days ->
                    SegmentedButton(
                        selected = state.rangeDays == days,
                        onClick = { viewModel.selectRange(days) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = detailRangeOptions.size)
                    ) {
                        Text("${days}D")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            StatsRow(state)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailValueHeader(state: MetricDetailUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.getDefault(), "%.${state.metric.decimals}f", state.latestValue),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = unitSuffix(state.metric, state.weightUnit),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            val color = if (state.isImprovement) pulsePositiveColor() else pulseNegativeColor()
            val sign = if (state.delta > 0) "+" else ""
            val arrow = if (state.delta > 0) "\u25B2" else "\u25BC"
            Text(
                text = "$arrow $sign${String.format(Locale.getDefault(), "%.${state.metric.decimals}f", state.delta)} " +
                    "${unitSuffix(state.metric, state.weightUnit)} vs range start",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(state.metric.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = state.metric.icon,
                contentDescription = null,
                tint = state.metric.color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(state: MetricDetailUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            label = "Min",
            value = String.format(Locale.getDefault(), "%.${state.metric.decimals}f", state.minValue),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Average",
            value = String.format(Locale.getDefault(), "%.${state.metric.decimals}f", state.avgValue),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Max",
            value = String.format(Locale.getDefault(), "%.${state.metric.decimals}f", state.maxValue),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(10.dp))
    Text(
        text = "${state.pointCount} data points in the last ${state.rangeDays} days",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
