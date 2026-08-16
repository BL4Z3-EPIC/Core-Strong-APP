package com.pulsepoint.app.core.data

import com.pulsepoint.app.core.local.dao.WorkoutDao
import com.pulsepoint.app.core.local.entity.ExerciseEntity
import com.pulsepoint.app.core.local.entity.ExerciseSetEntity
import com.pulsepoint.app.core.local.entity.WorkoutEntity
import com.pulsepoint.app.core.local.entity.WorkoutSessionEntity
import com.pulsepoint.app.core.local.relation.WorkoutWithExercises
import com.pulsepoint.app.core.network.ApiService
import com.pulsepoint.app.core.network.dto.ExerciseDto
import com.pulsepoint.app.core.network.dto.ExerciseSetDto
import com.pulsepoint.app.core.network.dto.WorkoutDto
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val api: ApiService,
    private val workoutDao: WorkoutDao
) {

    val workouts: Flow<List<WorkoutWithExercises>> = workoutDao.observeActiveWorkouts()

    val sessions: Flow<List<WorkoutSessionEntity>> = workoutDao.observeSessions()

    fun observeWorkout(workoutId: Int): Flow<WorkoutWithExercises?> =
        workoutDao.observeWorkout(workoutId)

    suspend fun isDatabaseEmpty(): Boolean = workoutDao.count() == 0

    suspend fun refresh() {
        val dto = api.getWorkouts()
        val workouts = dto.map { it.toEntity() }
        val exercises = dto.flatMap { workout -> workout.exercises.map { it.toEntity(workout.id) } }
        val sets = dto.flatMap { workout ->
            workout.exercises.flatMap { exercise -> exercise.sets.map { it.toEntity(exercise.id) } }
        }
        workoutDao.replaceAll(workouts, exercises, sets)
    }

    suspend fun logSession(workoutId: Int) {
        workoutDao.insertSession(
            WorkoutSessionEntity(
                workoutId = workoutId,
                completedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    private fun WorkoutDto.toEntity() = WorkoutEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        muscleGroups = muscleGroups,
        weeklyFrequency = weeklyFrequency,
        isActive = isActive
    )

    private fun ExerciseDto.toEntity(workoutId: Int) = ExerciseEntity(
        id = id,
        workoutId = workoutId,
        name = name,
        muscleGroup = muscleGroup
    )

    private fun ExerciseSetDto.toEntity(exerciseId: Int) = ExerciseSetEntity(
        exerciseId = exerciseId,
        setOrder = setOrder,
        reps = reps,
        weightKg = weightKg,
        restSec = restSec
    )
}
