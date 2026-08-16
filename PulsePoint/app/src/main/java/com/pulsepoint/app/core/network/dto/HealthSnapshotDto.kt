package com.pulsepoint.app.core.network.dto

import com.google.gson.annotations.SerializedName

data class HealthSnapshotDto(
    @SerializedName("date") val date: String,
    @SerializedName("weightKg") val weightKg: Double,
    @SerializedName("heightCm") val heightCm: Double,
    @SerializedName("muscleMassPct") val muscleMassPct: Double,
    @SerializedName("fatPct") val fatPct: Double,
    @SerializedName("bmi") val bmi: Double,
    @SerializedName("muscleWeightKg") val muscleWeightKg: Double,
    @SerializedName("age") val age: Int,
    @SerializedName("gender") val gender: String,
    @SerializedName("bmr") val bmr: Double
)
