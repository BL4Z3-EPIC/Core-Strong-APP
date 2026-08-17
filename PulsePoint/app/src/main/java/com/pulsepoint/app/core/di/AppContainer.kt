package com.pulsepoint.app.core.di

import android.content.Context
import com.pulsepoint.app.core.data.HealthRepository
import com.pulsepoint.app.core.data.UserPreferences
import com.pulsepoint.app.core.data.WorkoutRepository
import com.pulsepoint.app.core.local.AppDatabase
import com.pulsepoint.app.core.local.dao.HealthSnapshotDao
import com.pulsepoint.app.core.local.dao.WorkoutDao
import com.pulsepoint.app.core.network.ConnectionMonitor
import com.pulsepoint.app.core.sync.SyncScheduler

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase = AppDatabase.build(appContext)

    private val healthSnapshotDao: HealthSnapshotDao = database.healthSnapshotDao()
    private val workoutDao: WorkoutDao = database.workoutDao()

    val userPreferences: UserPreferences = UserPreferences(appContext)
    val connectionMonitor: ConnectionMonitor = ConnectionMonitor(appContext)

    val healthRepository: HealthRepository =
        HealthRepository(healthSnapshotDao, userPreferences)

    val workoutRepository: WorkoutRepository =
        WorkoutRepository(workoutDao, userPreferences)

    private val syncScheduler = SyncScheduler(appContext, healthRepository, workoutRepository)

    fun scheduleBackgroundSync() {
        syncScheduler.schedulePeriodic()
    }
}
