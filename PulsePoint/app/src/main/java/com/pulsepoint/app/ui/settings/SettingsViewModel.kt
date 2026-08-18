package com.pulsepoint.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.data.SyncResult
import com.pulsepoint.app.core.data.WeightUnit
import com.pulsepoint.app.core.network.NetworkClient
import com.pulsepoint.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val serverBaseUrl: String = "",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val chartRangeDays: Int = 30,
    val lastSyncMillis: Long? = null,
    val isRefreshing: Boolean = false,
    val connectionStatus: String? = null,
    val isConnectionChecking: Boolean = false,
    val hasClearedData: Boolean = false
)

private data class SettingsPrefs(
    val themeMode: ThemeMode,
    val serverBaseUrl: String,
    val weightUnit: WeightUnit,
    val chartRangeDays: Int,
    val lastSyncMillis: Long?
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PulsePointApplication).container

    private val isRefreshing = MutableStateFlow(false)
    private val connectionStatus = MutableStateFlow<String?>(null)
    private val isConnectionChecking = MutableStateFlow(false)
    private val hasClearedData = MutableStateFlow(false)

    private val prefs: kotlinx.coroutines.flow.Flow<SettingsPrefs> = combine(
        container.userPreferences.themeMode,
        container.userPreferences.serverBaseUrl,
        container.userPreferences.weightUnit,
        container.userPreferences.chartRangeDays,
        container.userPreferences.lastSyncEpochMillis,
        ::SettingsPrefs
    )

    val state: StateFlow<SettingsUiState> = combine(
        prefs,
        isRefreshing,
        connectionStatus,
        isConnectionChecking,
        hasClearedData
    ) { prefs, refreshing, status, checking, cleared ->
        SettingsUiState(
            themeMode = prefs.themeMode,
            serverBaseUrl = prefs.serverBaseUrl,
            weightUnit = prefs.weightUnit,
            chartRangeDays = prefs.chartRangeDays,
            lastSyncMillis = prefs.lastSyncMillis,
            isRefreshing = refreshing,
            connectionStatus = status,
            isConnectionChecking = checking,
            hasClearedData = cleared
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            container.userPreferences.setThemeMode(mode)
        }
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            container.userPreferences.setWeightUnit(unit)
        }
    }

    fun setChartRangeDays(days: Int) {
        viewModelScope.launch {
            container.userPreferences.setChartRangeDays(days)
        }
    }

    fun saveServerUrl(url: String) {
        val trimmed = url.trim().removeSuffix("/")
        if (trimmed.isEmpty()) {
            connectionStatus.value = "URL cannot be empty"
            return
        }
        viewModelScope.launch {
            container.userPreferences.setServerBaseUrl("$trimmed/")
            connectionStatus.value = null
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            isConnectionChecking.value = true
            connectionStatus.value = null
            val result = runCatching {
                val url = container.userPreferences.serverBaseUrl.first()
                NetworkClient.configure(url)
                val response = NetworkClient.apiService.health()
                response.isSuccessful
            }.getOrElse { false }
            isConnectionChecking.value = false
            connectionStatus.value = if (result) {
                "Connected successfully"
            } else {
                "Connection failed - is the demo server running?"
            }
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            isRefreshing.value = true
            val result = container.healthRepository.refresh()
            if (result is SyncResult.Failure) {
                connectionStatus.value = result.message
            }
            try {
                container.workoutRepository.refresh()
            } catch (_: Exception) {
            }
            isRefreshing.value = false
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            container.healthRepository.clearAll()
            container.workoutRepository.clearAll()
            hasClearedData.value = true
        }
    }

    fun resetConnectionFeedback() {
        connectionStatus.value = null
        hasClearedData.value = false
    }
}
