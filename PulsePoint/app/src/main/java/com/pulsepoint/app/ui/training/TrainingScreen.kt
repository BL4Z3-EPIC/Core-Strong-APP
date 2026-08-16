package com.pulsepoint.app.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulsepoint.app.ui.components.EmptyState
import com.pulsepoint.app.ui.components.LoadingState
import com.pulsepoint.app.ui.components.OfflineBanner
import com.pulsepoint.app.ui.training.components.WorkoutCard

@Composable
fun TrainingScreen(
    onOpenWorkout: (Int) -> Unit,
    viewModel: TrainingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TrainingContent(
        state = state,
        onOpenWorkout = onOpenWorkout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingContent(
    state: TrainingUiState,
    onOpenWorkout: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Training Studio", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Your assigned programs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(innerPadding))

            state.workouts.isEmpty() -> EmptyState(
                message = "No workouts assigned yet. They will appear here when your server sends them.",
                modifier = Modifier.padding(innerPadding)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isOffline) {
                    item { OfflineBanner() }
                }
                items(state.workouts, key = { it.workout.workout.id }) { item ->
                    WorkoutCard(
                        item = item,
                        onClick = { onOpenWorkout(item.workout.workout.id) }
                    )
                }
            }
        }
    }
}
