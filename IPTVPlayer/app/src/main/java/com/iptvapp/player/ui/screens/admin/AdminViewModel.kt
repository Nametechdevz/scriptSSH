package com.iptvapp.player.ui.screens.admin

import androidx.lifecycle.ViewModel
import com.iptvapp.player.data.preferences.EncryptedPreferencesManager
import com.iptvapp.player.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AdminUiState(
    val showPinDialog: Boolean = true,
    val pinError: String? = null,
    val isAuthenticated: Boolean = false,
    val serverUrl: String = "",
    val username: String = "",
    val newPin: String = "",
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val prefs: EncryptedPreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun verifyPin(pin: String) {
        val storedPin = prefs.getAdminPin()
        if (pin == storedPin) {
            _uiState.update {
                it.copy(
                    showPinDialog = false,
                    isAuthenticated = true,
                    pinError = null,
                    serverUrl = prefs.getServerUrl() ?: "",
                    username = prefs.getUsername() ?: ""
                )
            }
        } else {
            _uiState.update { it.copy(pinError = "Incorrect PIN. Please try again.") }
        }
    }

    fun onNewPinChange(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(newPin = value) }
        }
    }

    fun saveNewPin() {
        val pin = _uiState.value.newPin
        if (pin.length == 4) {
            prefs.saveAdminPin(pin)
            _uiState.update { it.copy(saveSuccess = true, newPin = "") }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.logout()
        onLoggedOut()
    }

    fun dismissPinDialog(onDismiss: () -> Unit) {
        _uiState.update { it.copy(showPinDialog = false) }
        onDismiss()
    }
}
