package com.odevs.photodiary.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "settings")

object NotificationPreferences {

    private val REMINDER_HOUR_KEY = intPreferencesKey("reminder_hour")
    private val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")

    /**
     * Értesítési óra lekérése (alapértelmezett: 8)
     */
    fun getReminderHour(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[REMINDER_HOUR_KEY] ?: 8
        }
    }

    /**
     * Értesítési óra mentése
     */
    suspend fun saveReminderHour(context: Context, hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_HOUR_KEY] = hour
        }
    }

    private val TASK_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("task_notifications_enabled")

    fun isTaskNotificationEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[TASK_NOTIFICATIONS_ENABLED_KEY] ?: true
        }
    }

    suspend fun setTaskNotificationEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TASK_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    /**
     * Értesítések engedélyezve vannak-e (alapértelmezett: igen)
     */
    fun isReminderEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[REMINDER_ENABLED_KEY] ?: true
        }
    }

    /**
     * Értesítések beállítása (engedélyezés vagy tiltás)
     */
    suspend fun setReminderEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED_KEY] = enabled
        }
    }
}
