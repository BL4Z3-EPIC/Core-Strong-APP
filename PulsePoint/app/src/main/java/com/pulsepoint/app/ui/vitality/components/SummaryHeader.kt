package com.pulsepoint.app.ui.vitality.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulsepoint.app.core.data.WeightUnit
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.core.util.DateFormatting
import com.pulsepoint.app.ui.vitality.MetricType
import com.pulsepoint.app.ui.vitality.computeDelta
import com.pulsepoint.app.ui.vitality.valueForDisplay
import com.pulsepoint.app.ui.theme.HeroGradientEnd
import com.pulsepoint.app.ui.theme.HeroGradientStart
import java.util.Locale

@Composable
fun HeroCard(
    latestSnapshot: HealthSnapshotEntity?,
    weightUnit: WeightUnit,
    rangeDays: Int,
    snapshots: List<HealthSnapshotEntity>,
    modifier: Modifier = Modifier
) {
    if (latestSnapshot == null) {
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            Modifier
                .background(
                    Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd))
                )
                .padding(20.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's snapshot",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = DateFormatting.formatFullDate(latestSnapshot.dateEpochDay),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Body Weight",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatWeight(latestSnapshot.valueForDisplay(MetricType.WEIGHT, weightUnit)),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (weightUnit == WeightUnit.LB) "lb" else "kg",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        HeroDelta(snapshots, weightUnit, rangeDays)
                    }
                    Spacer(Modifier.width(16.dp))
                    ProgressRing(
                        progress = (latestSnapshot.fatPct / 100.0).toFloat().coerceIn(0f, 1f),
                        centerText = String.format(Locale.getDefault(), "%.1f%%", latestSnapshot.fatPct),
                        label = "Body Fat",
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroStat("BMI", String.format(Locale.getDefault(), "%.1f", latestSnapshot.bmi), Modifier.weight(1f))
                    HeroStat(
                        "Muscle",
                        String.format(Locale.getDefault(), "%.1f%%", latestSnapshot.muscleMassPct),
                        Modifier.weight(1f)
                    )
                    HeroStat(
                        "BMR",
                        "${latestSnapshot.bmr.toInt()} kcal",
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroDelta(
    snapshots: List<HealthSnapshotEntity>,
    weightUnit: WeightUnit,
    rangeDays: Int
) {
    val delta = computeDelta(snapshots, MetricType.WEIGHT, weightUnit, rangeDays)
    if (delta == null || delta.delta == 0.0) {
        Text(
            text = if (delta == null) "No change data" else "Unchanged",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        return
    }
    val sign = if (delta.delta > 0) "+" else ""
    Text(
        text = "$sign${String.format(Locale.getDefault(), "%.1f", delta.delta)} " +
            (if (weightUnit == WeightUnit.LB) "lb" else "kg") +
            " over the selected period",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    )
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    centerText: String,
    label: String,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    diameter: Int = 92
) {
    Box(
        modifier = modifier.size(diameter.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(diameter.dp)) {
            val stroke = 7.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val start = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = start,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = start,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(70.dp)
        ) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatWeight(value: Double): String =
    String.format(Locale.getDefault(), "%.1f", value)
