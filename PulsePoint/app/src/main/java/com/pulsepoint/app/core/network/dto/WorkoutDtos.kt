package com.pulsepoint.app.core.network.dto

import com.google.gson.annotations.SerializedName

data class WorkoutDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("muscleGroups") val muscleGroups: List<String>,
    @SerializedName("weeklyFrequency") val weeklyFrequency: Int,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("exercises") val exercises: List<ExerciseDto>
)

data class ExerciseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("muscleGroup") val muscleGroup: String,
    @SerializedName("sets") val sets: List<ExerciseSetDto>
)

data class ExerciseSetDto(
    @SerializedName("setOrder") val setOrder: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("weightKg") val weightKg: Double,
    @SerializedName("restSec") val restSec: Int
)
