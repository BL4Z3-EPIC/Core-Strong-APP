package com.pulsepoint.app.ui.vitality

import com.pulsepoint.app.core.data.WeightUnit
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import java.time.LocalDate

private const val KG_TO_LB = 2.2046226218

fun convertWeight(value: Double, unit: WeightUnit): Double =
    if (unit == WeightUnit.LB) value * KG_TO_LB else value

fun unitSuffix(metric: MetricType, unit: WeightUnit): String =
    if (metric.weightBased) {
        if (unit == WeightUnit.LB) "lb" else "kg"
    } else {
        metric.unit
    }

fun HealthSnapshotEntity.valueForDisplay(metric: MetricType, unit: WeightUnit): Double =
    convertWeight(valueFor(metric), unit)

fun snapshotsInRange(
    snapshots: List<HealthSnapshotEntity>,
    rangeDays: Int
): List<HealthSnapshotEntity> {
    val cutoff = LocalDate.now().minusDays(rangeDays.toLong()).toEpochDay()
    return snapshots.filter { it.dateEpochDay >= cutoff }
}

data class MetricDelta(
    val firstValue: Double,
    val latestValue: Double,
    val delta: Double
)

fun computeDelta(
    snapshots: List<HealthSnapshotEntity>,
    metric: MetricType,
    unit: WeightUnit,
    rangeDays: Int
): MetricDelta? {
    val inRange = snapshotsInRange(snapshots, rangeDays)
    val first = inRange.firstOrNull() ?: return null
    val latest = snapshots.lastOrNull() ?: return null
    return MetricDelta(
        firstValue = first.valueForDisplay(metric, unit),
        latestValue = latest.valueForDisplay(metric, unit),
        delta = latest.valueForDisplay(metric, unit) - first.valueForDisplay(metric, unit)
    )
}

fun isImprovement(metric: MetricType, delta: Double): Boolean =
    if (delta == 0.0) false else if (metric.higherIsBetter) delta > 0 else delta < 0
