package com.pulsepoint.app.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pulsepoint.app.PulsePointApplication
import com.pulsepoint.app.core.local.relation.WorkoutWithExercises
import com.pulsepoint.app.core.util.DateFormatting
import com.pulsepoint.app.ui.components.EmptyState
import com.pulsepoint.app.ui.components.LoadingState
import com.pulsepoint.app.ui.training.components.ExerciseCard
import com.pulsepoint.app.ui.training.components.MuscleGroupChips
import kotlinx.coroutines.launch

@Composable
fun WorkoutDetailScreen(
    workoutId: Int,
    onBack: () -> Unit
) {
    val viewModel: WorkoutDetailViewModel = viewModel(
        key = "workout_detail_$workoutId",
        factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY]
                    as PulsePointApplication
                WorkoutDetailViewModel(app, workoutId)
            }
        }
    )

    val state by viewModel.state.collectAsStateWithLifecycle(initialValue = WorkoutDetailUiState())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    WorkoutDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMarkComplete = {
            viewModel.markComplete()
            scope.launch {
                snackbarHostState.showSnackbar("Workout marked as complete")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailContent(
    state: WorkoutDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onMarkComplete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.workout?.workout?.title ?: "Workout",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.workout != null) {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark complete", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(innerPadding))

            state.workout == null -> EmptyState(
                message = "Workout not found",
                modifier = Modifier.padding(innerPadding)
            )

            else -> WorkoutDetailBody(
                workout = state.workout,
                lastCompletedMillis = state.lastCompletedMillis,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun WorkoutDetailBody(
    workout: WorkoutWithExercises,
    lastCompletedMillis: Long?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = workout.workout.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    MuscleGroupChips(workout.workout.muscleGroups)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "${workout.workout.weeklyFrequency}x / week",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val totalSets = workout.exercises.sumOf { it.sets.size }
                        Text(
                            text = "$totalSets total sets",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    lastCompletedMillis?.let { millis ->
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Completed ${DateFormatting.formatEpochMillis(millis)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        items(workout.exercises, key = { it.exercise.id }) { exercise ->
            ExerciseCard(exercise)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
