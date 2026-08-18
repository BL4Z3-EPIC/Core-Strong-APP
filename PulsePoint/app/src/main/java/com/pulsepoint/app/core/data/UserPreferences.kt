package com.pulsepoint.app.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulsepoint.app.BuildConfig
import com.pulsepoint.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulsepoint_preferences")

enum class WeightUnit { KG, LB }

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
    private val serverBaseUrlKey = stringPreferencesKey("server_base_url")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val weightUnitKey = stringPreferencesKey("weight_unit")

    companion object {
        const val DEFAULT_RANGE_DAYS = 30
    }

    val lastSyncEpochMillis: Flow<Long?> =
        context.dataStore.data.map { it[lastSyncKey] }

    val serverBaseUrl: Flow<String> =
        context.dataStore.data.map { it[serverBaseUrlKey] ?: BuildConfig.BASE_URL }

    val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map {
            when (it[themeModeKey]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    val weightUnit: Flow<WeightUnit> =
        context.dataStore.data.map { prefs ->
            if (prefs[weightUnitKey] == "LB") WeightUnit.LB else WeightUnit.KG
        }

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

    suspend fun setServerBaseUrl(url: String) {
        context.dataStore.edit { it[serverBaseUrlKey] = url }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { it[weightUnitKey] = unit.name }
    }

    suspend fun clearLastSync() {
        context.dataStore.edit { it.remove(lastSyncKey) }
    }

    suspend fun updateProfile(gender: String, age: Int, bmr: Double) {
        context.dataStore.edit {
            it[genderKey] = gender
            it[ageKey] = age
            it[bmrKey] = bmr.toString()
        }
    }
}
