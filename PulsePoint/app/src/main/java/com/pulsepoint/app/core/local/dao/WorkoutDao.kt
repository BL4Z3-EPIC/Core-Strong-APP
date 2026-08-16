package com.pulsepoint.app.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pulsepoint.app.core.local.entity.ExerciseEntity
import com.pulsepoint.app.core.local.entity.ExerciseSetEntity
import com.pulsepoint.app.core.local.entity.WorkoutEntity
import com.pulsepoint.app.core.local.entity.WorkoutSessionEntity
import com.pulsepoint.app.core.local.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {

    @Transaction
    @Query("SELECT * FROM workouts WHERE isActive = 1 ORDER BY id ASC")
    abstract fun observeActiveWorkouts(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    abstract fun observeWorkout(workoutId: Int): Flow<WorkoutWithExercises?>

    @Query("SELECT * FROM workout_sessions ORDER BY completedAtEpochMillis DESC")
    abstract fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT COUNT(*) FROM workouts")
    abstract suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWorkouts(workouts: List<WorkoutEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Insert
    abstract suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Transaction
    open suspend fun replaceAll(
        workouts: List<WorkoutEntity>,
        exercises: List<ExerciseEntity>,
        sets: List<ExerciseSetEntity>
    ) {
        insertWorkouts(workouts)
        insertExercises(exercises)
        insertSets(sets)
    }
}
