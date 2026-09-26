package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_READER_MODE = stringPreferencesKey("reader_mode")
        val KEY_KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val KEY_ADMIN_PIN = stringPreferencesKey("admin_pin")
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "en"
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val readerModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_READER_MODE] ?: "LIGHT"
    }

    val keepScreenAwakeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_KEEP_SCREEN_AWAKE] ?: true
    }

    val adminPinFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_ADMIN_PIN] ?: "1234"
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_FIRST_LAUNCH] ?: true
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setReaderMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_READER_MODE] = mode
        }
    }

    suspend fun setKeepScreenAwake(keep: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KEEP_SCREEN_AWAKE] = keep
        }
    }

    suspend fun setAdminPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ADMIN_PIN] = pin
        }
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_FIRST_LAUNCH] = false
        }
    }
}
