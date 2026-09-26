package com.example.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val language: StateFlow<String> = preferencesRepository.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val themeMode: StateFlow<String> = preferencesRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val readerMode: StateFlow<String> = preferencesRepository.readerModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "LIGHT")

    val keepScreenAwake: StateFlow<Boolean> = preferencesRepository.keepScreenAwakeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val adminPin: StateFlow<String> = preferencesRepository.adminPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1234")

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(lang)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setReaderMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setReaderMode(mode)
        }
    }

    fun setKeepScreenAwake(keep: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setKeepScreenAwake(keep)
        }
    }

    fun updateAdminPin(newPin: String) {
        viewModelScope.launch {
            preferencesRepository.setAdminPin(newPin)
        }
    }
}
