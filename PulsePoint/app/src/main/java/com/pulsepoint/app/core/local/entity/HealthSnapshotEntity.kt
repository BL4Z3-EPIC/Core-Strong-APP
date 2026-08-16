package com.pulsepoint.app.core.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_snapshots")
data class HealthSnapshotEntity(
    @PrimaryKey val dateEpochDay: Long,
    val weightKg: Double,
    val heightCm: Double,
    val muscleMassPct: Double,
    val fatPct: Double,
    val bmi: Double,
    val muscleWeightKg: Double,
    val age: Int,
    val gender: String,
    val bmr: Double
)
