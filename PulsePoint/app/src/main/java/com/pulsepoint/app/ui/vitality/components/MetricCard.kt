package com.pulsepoint.app.ui.vitality.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.ui.vitality.MetricType
import com.pulsepoint.app.ui.vitality.valueFor
import java.util.Locale

@Composable
fun MetricCard(
    metric: MetricType,
    snapshots: List<HealthSnapshotEntity>,
    rangeDays: Int,
    modifier: Modifier = Modifier
) {
    val latest = snapshots.lastOrNull()
    val latestValue = latest?.valueFor(metric)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatValue(latestValue, metric.decimals),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = metric.unit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                MetricDeltaBadge(metric, snapshots, rangeDays)
            }
            MetricChart(
                metric = metric,
                snapshots = snapshots,
                rangeDays = rangeDays,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }
    }
}

@Composable
private fun MetricDeltaBadge(
    metric: MetricType,
    snapshots: List<HealthSnapshotEntity>,
    rangeDays: Int
) {
    val cutoff = java.time.LocalDate.now().minusDays(rangeDays.toLong()).toEpochDay()
    val inRange = snapshots.filter { it.dateEpochDay >= cutoff }
    val first = inRange.firstOrNull()
    val latest = snapshots.lastOrNull()
    if (first == null || latest == null) {
        return
    }

    val delta = latest.valueFor(metric) - first.valueFor(metric)
    if (delta == 0.0) {
        Text(
            text = "0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val improved = if (metric.higherIsBetter) delta > 0 else delta < 0
    val sign = if (delta > 0) "+" else ""
    val arrow = if (improved) "\u25B2" else "\u25BC"
    val color = if (improved) Color(0xFF2E7D32) else Color(0xFFC62828)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = arrow,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "$sign${formatValue(delta, metric.decimals)} ${metric.unit}",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

private fun formatValue(value: Double?, decimals: Int): String =
    if (value == null) {
        "--"
    } else {
        String.format(Locale.getDefault(), "%.${decimals}f", value)
    }
