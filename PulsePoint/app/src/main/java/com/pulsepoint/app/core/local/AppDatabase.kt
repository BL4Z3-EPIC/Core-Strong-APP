package com.pulsepoint.app.core.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pulsepoint.app.core.local.converter.Converters
import com.pulsepoint.app.core.local.dao.HealthSnapshotDao
import com.pulsepoint.app.core.local.dao.WorkoutDao
import com.pulsepoint.app.core.local.entity.ExerciseEntity
import com.pulsepoint.app.core.local.entity.ExerciseSetEntity
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.core.local.entity.WorkoutEntity
import com.pulsepoint.app.core.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        HealthSnapshotEntity::class,
        WorkoutEntity::class,
        ExerciseEntity::class,
        ExerciseSetEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun healthSnapshotDao(): HealthSnapshotDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "pulsepoint.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
