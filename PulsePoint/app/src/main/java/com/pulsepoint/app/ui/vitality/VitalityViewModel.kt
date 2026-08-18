package com.pulsepoint.app.ui.vitality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.data.SyncResult
import com.pulsepoint.app.core.data.UserPreferences
import com.pulsepoint.app.core.data.WeightUnit
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
    val serverUrl: String = "",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val lastSyncMillis: Long? = null
)

private data class VitalitySettings(
    val rangeDays: Int,
    val refreshing: Boolean,
    val serverUrl: String,
    val weightUnit: WeightUnit
)

class VitalityViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PulsePointApplication).container
    private val repository = container.healthRepository

    private val isRefreshing = MutableStateFlow(false)

    private val rangeDays: StateFlow<Int> = container.userPreferences.chartRangeDays
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences.DEFAULT_RANGE_DAYS)

    // Nested combine keeps the flow count at the 5-flow overload maximum:
    // rangeDays + isRefreshing + serverUrl + weightUnit are bundled into one value.
    val state: StateFlow<VitalityUiState> = combine(
        repository.snapshots,
        container.connectionMonitor.isOnline,
        repository.lastSyncResult,
        container.userPreferences.lastSyncEpochMillis,
        combine(
            rangeDays,
            isRefreshing,
            container.userPreferences.serverBaseUrl,
            container.userPreferences.weightUnit,
            ::VitalitySettings
        )
    ) { snapshots, online, lastResult, lastSyncMillis, settings ->
        val empty = snapshots.isEmpty()
        val networkError = lastResult == SyncResult.NetworkUnavailable && empty
        VitalityUiState(
            isLoading = empty && lastResult == null,
            isRefreshing = settings.refreshing,
            isOffline = !online,
            hasError = lastResult is SyncResult.Failure || networkError,
            errorMessage = when {
                lastResult is SyncResult.Failure -> lastResult.message
                networkError -> "Could not reach the server at ${settings.serverUrl}"
                else -> null
            },
            snapshots = snapshots,
            latestSnapshot = snapshots.lastOrNull(),
            selectedRangeDays = settings.rangeDays,
            serverUrl = settings.serverUrl,
            weightUnit = settings.weightUnit,
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
        viewModelScope.launch {
            container.userPreferences.setChartRangeDays(days)
        }
    }

    fun setServerUrl(url: String) {
        val trimmed = url.trim().removeSuffix("/")
        if (trimmed.isEmpty()) {
            return
        }
        viewModelScope.launch {
            container.userPreferences.setServerBaseUrl("$trimmed/")
            refresh()
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
