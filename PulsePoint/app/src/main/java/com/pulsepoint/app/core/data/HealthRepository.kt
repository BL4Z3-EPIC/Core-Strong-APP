package com.pulsepoint.app.core.data

import com.pulsepoint.app.core.local.dao.HealthSnapshotDao
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.core.network.NetworkClient
import com.pulsepoint.app.core.network.dto.HealthSnapshotDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.time.LocalDate

sealed interface SyncResult {
    data object Success : SyncResult
    data object NetworkUnavailable : SyncResult
    data class Failure(val message: String) : SyncResult
}

class HealthRepository(
    private val snapshotDao: HealthSnapshotDao,
    private val userPreferences: UserPreferences
) {

    val snapshots: Flow<List<HealthSnapshotEntity>> = snapshotDao.observeAll()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult

    suspend fun isDatabaseEmpty(): Boolean = snapshotDao.count() == 0

    suspend fun clearAll() {
        snapshotDao.clearAll()
        userPreferences.clearLastSync()
    }

    suspend fun refresh(): SyncResult {
        return try {
            val url = userPreferences.serverBaseUrl.first()
            NetworkClient.configure(url)
            val dto = NetworkClient.apiService.getMetrics()
            snapshotDao.insertAll(dto.map { it.toEntity() })
            val latest = dto.maxByOrNull { it.date }
            latest?.let {
                userPreferences.updateProfile(it.gender, it.age, it.bmr)
            }
            userPreferences.setLastSyncEpochMillis(System.currentTimeMillis())
            SyncResult.Success.also { _lastSyncResult.value = it }
        } catch (e: IOException) {
            SyncResult.NetworkUnavailable.also { _lastSyncResult.value = it }
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: "Unknown error").also { _lastSyncResult.value = it }
        }
    }

    private fun HealthSnapshotDto.toEntity() = HealthSnapshotEntity(
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        weightKg = weightKg,
        heightCm = heightCm,
        muscleMassPct = muscleMassPct,
        fatPct = fatPct,
        bmi = bmi,
        muscleWeightKg = muscleWeightKg,
        age = age,
        gender = gender,
        bmr = bmr
    )
}
