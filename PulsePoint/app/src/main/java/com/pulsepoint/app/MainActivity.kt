package com.pulsepoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulsepoint.app.ui.navigation.Routes
import com.pulsepoint.app.ui.navigation.bottomNavItems
import com.pulsepoint.app.ui.settings.SettingsScreen
import com.pulsepoint.app.ui.theme.PulsePointTheme
import com.pulsepoint.app.ui.training.TrainingScreen
import com.pulsepoint.app.ui.training.WorkoutDetailScreen
import com.pulsepoint.app.ui.vitality.MetricDetailScreen
import com.pulsepoint.app.ui.vitality.VitalityScreen
import com.pulsepoint.app.ui.vitality.metricTypeFromRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        (application as PulsePointApplication).container.scheduleBackgroundSync()
        setContent {
            val appContainer = (application as PulsePointApplication).container
            val themeMode by appContainer.userPreferences.themeMode.collectAsStateWithLifecycle(initialValue = com.pulsepoint.app.ui.theme.ThemeMode.SYSTEM)
            PulsePointTheme(themeMode = themeMode) {
                PulsePointApp()
            }
        }
    }
}

@Composable
private fun PulsePointApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isDetail = currentDestination?.route == Routes.METRIC_DETAIL ||
                currentDestination?.route == Routes.WORKOUT_DETAIL
            if (!isDetail) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.VITALITY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.VITALITY) {
                VitalityScreen(
                    onOpenMetric = { metric ->
                        navController.navigate(Routes.metricDetail(metric.name))
                    }
                )
            }
            composable(Routes.TRAINING) {
                TrainingScreen(
                    onOpenWorkout = { id ->
                        navController.navigate(Routes.workoutDetail(id))
                    }
                )
            }
            composable(Routes.WORKOUT_DETAIL) { backStackEntry ->
                val workoutId = backStackEntry.arguments
                    ?.getString("workoutId")
                    ?.toIntOrNull()
                    ?: return@composable
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = Routes.METRIC_DETAIL,
                arguments = listOf(navArgument("metricType") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val metric = backStackEntry.arguments
                    ?.getString("metricType")
                    ?.let { metricTypeFromRoute(it) }
                    ?: return@composable
                MetricDetailScreen(
                    metric = metric,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
