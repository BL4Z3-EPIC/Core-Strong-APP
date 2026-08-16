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

data class WorkoutDetailUiState(
    val isLoading: Boolean = true,
    val workout: WorkoutWithExercises? = null,
    val lastCompletedMillis: Long? = null
)

class WorkoutDetailViewModel(
    application: Application,
    private val workoutId: Int
) : AndroidViewModel(application) {

    private val container = (application as PulsePointApplication).container
    private val repository = container.workoutRepository

    val state: StateFlow<WorkoutDetailUiState> = combine(
        repository.observeWorkout(workoutId),
        repository.sessions,
        container.connectionMonitor.isOnline
    ) { workout, sessions, _ ->
        WorkoutDetailUiState(
            isLoading = workout == null,
            workout = workout,
            lastCompletedMillis = sessions
                .filter { it.workoutId == workoutId }
                .maxOfOrNull { it.completedAtEpochMillis }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutDetailUiState())

    fun markComplete() {
        viewModelScope.launch {
            repository.logSession(workoutId)
        }
    }
}
