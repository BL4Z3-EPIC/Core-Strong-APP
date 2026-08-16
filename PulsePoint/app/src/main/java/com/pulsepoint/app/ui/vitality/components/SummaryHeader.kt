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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.core.util.DateFormatting
import java.util.Locale

@Composable
fun SummaryHeaderCard(
    latestSnapshot: HealthSnapshotEntity?,
    modifier: Modifier = Modifier
) {
    if (latestSnapshot == null) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's snapshot",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = DateFormatting.formatFullDate(latestSnapshot.dateEpochDay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", latestSnapshot.weightKg),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "kg",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderStat("BMI", String.format(Locale.getDefault(), "%.1f", latestSnapshot.bmi), Modifier.weight(1f))
                HeaderStat(
                    "Body fat",
                    String.format(Locale.getDefault(), "%.1f%%", latestSnapshot.fatPct),
                    Modifier.weight(1f)
                )
                HeaderStat(
                    "Muscle",
                    String.format(Locale.getDefault(), "%.1f%%", latestSnapshot.muscleMassPct),
                    Modifier.weight(1f)
                )
                HeaderStat(
                    "BMR",
                    "${latestSnapshot.bmr.toInt()} kcal",
                    Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${latestSnapshot.age} yrs \u00B7 ${capitalize(latestSnapshot.gender)} \u00B7 " +
                    "${String.format(Locale.getDefault(), "%.0f", latestSnapshot.heightCm)} cm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private fun capitalize(value: String): String =
    value.lowercase().replaceFirstChar { it.uppercase() }
