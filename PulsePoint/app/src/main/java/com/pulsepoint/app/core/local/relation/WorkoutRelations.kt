package com.pulsepoint.app.core.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.pulsepoint.app.core.local.entity.ExerciseEntity
import com.pulsepoint.app.core.local.entity.ExerciseSetEntity
import com.pulsepoint.app.core.local.entity.WorkoutEntity

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutId")
    val exercises: List<ExerciseWithSets>
)

data class ExerciseWithSets(
    @Embedded val exercise: ExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "exerciseId")
    val sets: List<ExerciseSetEntity>
)
