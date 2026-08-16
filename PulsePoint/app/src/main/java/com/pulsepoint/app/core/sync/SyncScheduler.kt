package com.pulsepoint.app.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pulsepoint.app.core.data.HealthRepository
import com.pulsepoint.app.core.data.WorkoutRepository
import java.util.concurrent.TimeUnit

class SyncScheduler(
    context: Context,
    private val healthRepository: HealthRepository,
    private val workoutRepository: WorkoutRepository
) {

    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodic() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        private const val WORK_NAME = "pulsepoint_sync"
    }
}
