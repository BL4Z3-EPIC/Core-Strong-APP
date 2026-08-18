package com.pulsepoint.app.ui.vitality

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: ImageVector,
    val higherIsBetter: Boolean,
    val weightBased: Boolean = false
) {
    WEIGHT("Body Weight", "kg", 1, Teal, Icons.Filled.MonitorWeight, higherIsBetter = false, weightBased = true),
    MUSCLE_WEIGHT("Muscle Weight", "kg", 1, Mint, Icons.Filled.FitnessCenter, higherIsBetter = true, weightBased = true),
    MUSCLE_MASS("Muscle Mass", "%", 1, Sky, Icons.Filled.SportsGymnastics, higherIsBetter = true),
    FAT_PERCENT("Body Fat", "%", 1, Amber, Icons.Filled.WaterDrop, higherIsBetter = false),
    BMI("Body Mass Index", "", 1, Coral, Icons.Filled.Speed, higherIsBetter = false),
    BMR("Basal Metabolic Rate", "kcal/day", 0, Lavender, Icons.Filled.LocalFireDepartment, higherIsBetter = true)
}

fun HealthSnapshotEntity.valueFor(metric: MetricType): Double = when (metric) {
    MetricType.WEIGHT -> weightKg
    MetricType.MUSCLE_WEIGHT -> muscleWeightKg
    MetricType.MUSCLE_MASS -> muscleMassPct
    MetricType.FAT_PERCENT -> fatPct
    MetricType.BMI -> bmi
    MetricType.BMR -> bmr
}

fun metricTypeFromRoute(route: String): MetricType? =
    MetricType.entries.firstOrNull { it.name == route }
