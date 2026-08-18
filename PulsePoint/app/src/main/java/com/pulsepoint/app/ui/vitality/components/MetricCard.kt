package com.pulsepoint.app.ui.vitality.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsepoint.app.core.data.WeightUnit
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.ui.vitality.MetricType
import com.pulsepoint.app.ui.vitality.computeDelta
import com.pulsepoint.app.ui.vitality.isImprovement
import com.pulsepoint.app.ui.vitality.unitSuffix
import com.pulsepoint.app.ui.vitality.valueForDisplay
import com.pulsepoint.app.ui.theme.pulseNegativeColor
import com.pulsepoint.app.ui.theme.pulsePositiveColor
import java.util.Locale

@Composable
fun MetricTile(
    metric: MetricType,
    snapshots: List<HealthSnapshotEntity>,
    rangeDays: Int,
    weightUnit: WeightUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latest = snapshots.lastOrNull()
    val delta = computeDelta(snapshots, metric, weightUnit, rangeDays)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(metric.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = metric.icon,
                    contentDescription = null,
                    tint = metric.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatValue(latest?.valueForDisplay(metric, weightUnit), metric.decimals),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unitSuffix(metric, weightUnit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                DeltaBadge(metric, delta, weightUnit)
                Spacer(Modifier.height(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DeltaBadge(
    metric: MetricType,
    delta: com.pulsepoint.app.ui.vitality.MetricDelta?,
    weightUnit: WeightUnit
) {
    if (delta == null) {
        Text(
            text = "No data",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    if (delta.delta == 0.0) {
        Text(
            text = "0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val improved = isImprovement(metric, delta.delta)
    val sign = if (delta.delta > 0) "+" else ""
    val arrow = if (delta.delta > 0) "\u25B2" else "\u25BC"
    val color = if (improved) pulsePositiveColor() else pulseNegativeColor()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = arrow,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "$sign${formatValue(delta.delta, metric.decimals)} ${unitSuffix(metric, weightUnit)}",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatValue(value: Double?, decimals: Int): String =
    if (value == null) {
        "--"
    } else {
        String.format(Locale.getDefault(), "%.${decimals}f", value)
    }
