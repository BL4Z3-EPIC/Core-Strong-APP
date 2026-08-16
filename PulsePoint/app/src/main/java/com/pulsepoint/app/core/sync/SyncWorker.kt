package com.pulsepoint.app.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.data.SyncResult

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PulsePointApplication).container
        val healthResult = container.healthRepository.refresh()
        if (healthResult is SyncResult.NetworkUnavailable) {
            return Result.retry()
        }
        return try {
            container.workoutRepository.refresh()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
