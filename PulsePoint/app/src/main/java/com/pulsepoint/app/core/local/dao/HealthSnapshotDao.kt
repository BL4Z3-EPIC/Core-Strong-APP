package com.pulsepoint.app.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthSnapshotDao {

    @Query("SELECT * FROM health_snapshots ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<HealthSnapshotEntity>>

    @Query("SELECT * FROM health_snapshots ORDER BY dateEpochDay DESC LIMIT 1")
    suspend fun getLatest(): HealthSnapshotEntity?

    @Query("SELECT COUNT(*) FROM health_snapshots")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<HealthSnapshotEntity>)

    @Query("DELETE FROM health_snapshots")
    suspend fun clearAll()
}
