package com.pulsepoint.app.ui.vitality

import androidx.compose.ui.graphics.Color
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.ui.theme.Amber
import com.pulsepoint.app.ui.theme.Coral
import com.pulsepoint.app.ui.theme.Lavender
import com.pulsepoint.app.ui.theme.Mint
import com.pulsepoint.app.ui.theme.Sky
import com.pulsepoint.app.ui.theme.Teal

enum class MetricType(
    val label: String,
    val unit: String,
    val decimals: Int,
    val color: Color,
    val higherIsBetter: Boolean
) {
    WEIGHT("Body Weight", "kg", 1, Teal, higherIsBetter = false),
    MUSCLE_WEIGHT("Muscle Weight", "kg", 1, Mint, higherIsBetter = true),
    MUSCLE_MASS("Muscle Mass", "%", 1, Sky, higherIsBetter = true),
    FAT_PERCENT("Body Fat", "%", 1, Amber, higherIsBetter = false),
    BMI("Body Mass Index", "", 1, Coral, higherIsBetter = false),
    BMR("Basal Metabolic Rate", "kcal/day", 0, Lavender, higherIsBetter = true)
}

fun HealthSnapshotEntity.valueFor(metric: MetricType): Double = when (metric) {
    MetricType.WEIGHT -> weightKg
    MetricType.MUSCLE_WEIGHT -> muscleWeightKg
    MetricType.MUSCLE_MASS -> muscleMassPct
    MetricType.FAT_PERCENT -> fatPct
    MetricType.BMI -> bmi
    MetricType.BMR -> bmr
}
