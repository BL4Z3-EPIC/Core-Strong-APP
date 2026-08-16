package com.pulsepoint.app.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulsepoint_preferences")

data class UserProfilePreferences(
    val gender: String = "",
    val age: Int = 0,
    val bmr: Double = 0.0
)

class UserPreferences(private val context: Context) {

    private val lastSyncKey = longPreferencesKey("last_sync_epoch_millis")
    private val chartRangeDaysKey = intPreferencesKey("chart_range_days")
    private val genderKey = stringPreferencesKey("gender")
    private val ageKey = intPreferencesKey("age")
    private val bmrKey = stringPreferencesKey("bmr")

    val lastSyncEpochMillis: Flow<Long?> =
        context.dataStore.data.map { it[lastSyncKey] }

    val chartRangeDays: Flow<Int> =
        context.dataStore.data.map { it[chartRangeDaysKey] ?: DEFAULT_RANGE_DAYS }

    val latestProfile: Flow<UserProfilePreferences> =
        context.dataStore.data.map { prefs ->
            UserProfilePreferences(
                gender = prefs[genderKey] ?: "",
                age = prefs[ageKey] ?: 0,
                bmr = prefs[bmrKey]?.toDoubleOrNull() ?: 0.0
            )
        }

    suspend fun setLastSyncEpochMillis(value: Long) {
        context.dataStore.edit { it[lastSyncKey] = value }
    }

    suspend fun setChartRangeDays(days: Int) {
        context.dataStore.edit { it[chartRangeDaysKey] = days }
    }

    suspend fun updateProfile(gender: String, age: Int, bmr: Double) {
        context.dataStore.edit {
            it[genderKey] = gender
            it[ageKey] = age
            it[bmrKey] = bmr.toString()
        }
    }

    companion object {
        const val DEFAULT_RANGE_DAYS = 30
    }
}
