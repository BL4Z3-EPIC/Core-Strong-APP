package com.pulsepoint.app.ui.vitality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.data.SyncResult
import com.pulsepoint.app.core.data.UserPreferences
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VitalityUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val snapshots: List<HealthSnapshotEntity> = emptyList(),
    val latestSnapshot: HealthSnapshotEntity? = null,
    val selectedRangeDays: Int = UserPreferences.DEFAULT_RANGE_DAYS,
    val lastSyncMillis: Long? = null
)

class VitalityViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PulsePointApplication).container
    private val repository = container.healthRepository

    private val isRefreshing = MutableStateFlow(false)
    private val rangeDays = MutableStateFlow(UserPreferences.DEFAULT_RANGE_DAYS)

    val state: StateFlow<VitalityUiState> = combine(
        repository.snapshots,
        container.connectionMonitor.isOnline,
        repository.lastSyncResult,
        container.userPreferences.lastSyncEpochMillis,
        rangeDays,
        isRefreshing
    ) { snapshots, online, lastResult, lastSyncMillis, range, refreshing ->
        val empty = snapshots.isEmpty()
        VitalityUiState(
            isLoading = empty && lastResult == null,
            isRefreshing = refreshing,
            isOffline = !online,
            hasError = lastResult is SyncResult.Failure,
            errorMessage = (lastResult as? SyncResult.Failure)?.message,
            snapshots = snapshots,
            latestSnapshot = snapshots.lastOrNull(),
            selectedRangeDays = range,
            lastSyncMillis = lastSyncMillis
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VitalityUiState())

    init {
        viewModelScope.launch {
            if (repository.isDatabaseEmpty()) {
                refresh()
            }
        }
    }

    fun selectRange(days: Int) {
        rangeDays.value = days
        viewModelScope.launch {
            container.userPreferences.setChartRangeDays(days)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.refresh()
            try {
                container.workoutRepository.refresh()
            } catch (_: Exception) {
            }
            isRefreshing.value = false
        }
    }
}
