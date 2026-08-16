package com.pulsepoint.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val VITALITY = "vitality"
    const val TRAINING = "training"
    const val WORKOUT_DETAIL = "training/{workoutId}"

    fun workoutDetail(workoutId: Int): String = "training/$workoutId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.VITALITY, "Vitality", Icons.Filled.MonitorHeart),
    BottomNavItem(Routes.TRAINING, "Training Studio", Icons.Filled.FitnessCenter)
)
