package com.pulsepoint.app.core.network

import com.pulsepoint.app.core.network.dto.HealthSnapshotDto
import com.pulsepoint.app.core.network.dto.WorkoutDto
import retrofit2.http.GET

interface ApiService {

    @GET("api/metrics")
    suspend fun getMetrics(): List<HealthSnapshotDto>

    @GET("api/workouts")
    suspend fun getWorkouts(): List<WorkoutDto>
}
