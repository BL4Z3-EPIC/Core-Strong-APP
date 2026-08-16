package com.pulsepoint.app.core.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    indices = [Index("id")]
)
data class WorkoutEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subtitle: String,
    val muscleGroups: List<String>,
    val weeklyFrequency: Int,
    val isActive: Boolean
)

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId"), Index("id")]
)
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val workoutId: Int,
    val name: String,
    val muscleGroup: String
)

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Int,
    val setOrder: Int,
    val reps: Int,
    val weightKg: Double,
    val restSec: Int
)

@Entity(
    tableName = "workout_sessions",
    indices = [Index("workoutId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Int,
    val completedAtEpochMillis: Long
)
