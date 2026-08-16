package com.pulsepoint.app.ui.training

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.local.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutListItem(
    val workout: WorkoutWithExercises,
    val lastCompletedMillis: Long?
)

data class TrainingUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val workouts: List<WorkoutListItem> = emptyList()
)

class TrainingViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PulsePointApplication).container
    private val repository = container.workoutRepository

    val state: StateFlow<TrainingUiState> = combine(
        repository.workouts,
        repository.sessions,
        container.connectionMonitor.isOnline
    ) { workouts, sessions, online ->
        val lastByWorkout = sessions
            .groupBy { it.workoutId }
            .mapValues { (_, list) -> list.maxOf { it.completedAtEpochMillis } }
        TrainingUiState(
            isLoading = false,
            isOffline = !online,
            workouts = workouts.map { workout ->
                WorkoutListItem(
                    workout = workout,
                    lastCompletedMillis = lastByWorkout[workout.workout.id]
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrainingUiState())

    init {
        viewModelScope.launch {
            if (container.workoutRepository.isDatabaseEmpty()) {
                try {
                    container.workoutRepository.refresh()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun markComplete(workoutId: Int) {
        viewModelScope.launch {
            repository.logSession(workoutId)
        }
    }
}
