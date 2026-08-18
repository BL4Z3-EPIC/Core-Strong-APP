package com.pulsepoint.app.ui.vitality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.data.UserPreferences
import com.pulsepoint.app.core.data.WeightUnit
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MetricDetailUiState(
    val metric: MetricType,
    val snapshots: List<HealthSnapshotEntity> = emptyList(),
    val rangeDays: Int = UserPreferences.DEFAULT_RANGE_DAYS,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val latestValue: Double = 0.0,
    val rangeStartValue: Double = 0.0,
    val delta: Double = 0.0,
    val minValue: Double = 0.0,
    val avgValue: Double = 0.0,
    val maxValue: Double = 0.0,
    val pointCount: Int = 0,
    val isImprovement: Boolean = false
)

class MetricDetailViewModel(
    application: Application,
    private val metric: MetricType
) : AndroidViewModel(application) {

    private val container = (application as PulsePointApplication).container
    private val repository = container.healthRepository

    val state: StateFlow<MetricDetailUiState> = combine(
        repository.snapshots,
        container.userPreferences.chartRangeDays,
        container.userPreferences.weightUnit
    ) { snapshots, rangeDays, weightUnit ->
        val inRange = snapshotsInRange(snapshots, rangeDays)
        val values = inRange.map { it.valueForDisplay(metric, weightUnit) }
        val latest = snapshots.lastOrNull()?.valueForDisplay(metric, weightUnit) ?: 0.0
        val first = inRange.firstOrNull()?.valueForDisplay(metric, weightUnit) ?: 0.0
        val delta = latest - first
        MetricDetailUiState(
            metric = metric,
            snapshots = inRange,
            rangeDays = rangeDays,
            weightUnit = weightUnit,
            latestValue = latest,
            rangeStartValue = first,
            delta = delta,
            minValue = values.minOrNull() ?: 0.0,
            avgValue = if (values.isEmpty()) 0.0 else values.average(),
            maxValue = values.maxOrNull() ?: 0.0,
            pointCount = values.size,
            isImprovement = isImprovement(metric, delta)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetricDetailUiState(metric = metric))

    fun selectRange(days: Int) {
        viewModelScope.launch {
            container.userPreferences.setChartRangeDays(days)
        }
    }

    companion object {
        fun factory(metric: MetricType): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MetricDetailViewModel(
                    application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY],
                    metric = metric
                )
            }
        }
    }
}
