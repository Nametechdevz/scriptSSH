package com.iptvapp.player.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.player.data.local.dao.AppUserDao
import com.iptvapp.player.data.local.entity.AppUserEntity
import com.iptvapp.player.data.preferences.EncryptedPreferencesManager
import com.iptvapp.player.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUser(
    val id: Int = 0,
    val displayName: String = "",
    val serverUrl: String = "",
    val xtreamUsername: String = "",
    val xtreamPassword: String = "",
    val accessPin: String = "",
    val isActive: Boolean = true
)

data class AdminUiState(
    // Auth
    val showPinDialog: Boolean = true,
    val pinError: String? = null,
    val isAuthenticated: Boolean = false,
    // Server config
    val serverUrl: String = "",
    val username: String = "",
    // PIN change
    val newAdminPin: String = "",
    val pinSaveSuccess: Boolean = false,
    // User management
    val users: List<AppUser> = emptyList(),
    val showAddUserDialog: Boolean = false,
    val editingUser: AppUser? = null,
    val userFormName: String = "",
    val userFormServer: String = "",
    val userFormUsername: String = "",
    val userFormPassword: String = "",
    val userFormPin: String = "",
    val userFormError: String? = null,
    // Tabs
    val selectedTab: AdminTab = AdminTab.SERVER,
    val saveSuccess: Boolean = false
)

enum class AdminTab { SERVER, USERS, SECURITY }

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val prefs: EncryptedPreferencesManager,
    private val authRepository: AuthRepository,
    private val appUserDao: AppUserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            appUserDao.getAllUsers().collect { entities ->
                _uiState.update { it.copy(users = entities.map { e -> e.toUi() }) }
            }
        }
    }

    // ── Authentication ──────────────────────────────────────────────────────

    fun verifyPin(pin: String) {
        if (pin == (prefs.getAdminPin() ?: "1234")) {
            _uiState.update {
                it.copy(
                    showPinDialog = false,
                    isAuthenticated = true,
                    pinError = null,
                    serverUrl = prefs.getServerUrl() ?: "",
                    username = prefs.getUsername() ?: "",
                    userFormServer = prefs.getServerUrl() ?: ""
                )
            }
        } else {
            _uiState.update { it.copy(pinError = "PIN incorrecto. Inténtalo de nuevo.") }
        }
    }

    fun dismissPinDialog(onDismiss: () -> Unit) {
        _uiState.update { it.copy(showPinDialog = false) }
        onDismiss()
    }

    // ── Tabs ────────────────────────────────────────────────────────────────

    fun selectTab(tab: AdminTab) = _uiState.update { it.copy(selectedTab = tab) }

    // ── Server Config ───────────────────────────────────────────────────────

    fun onServerUrlChange(v: String) = _uiState.update { it.copy(serverUrl = v) }

    fun saveServerConfig() {
        val state = _uiState.value
        if (state.serverUrl.isBlank()) return
        val normalizedUrl = if (state.serverUrl.endsWith("/")) state.serverUrl else "${state.serverUrl}/"
        prefs.saveCredentials(
            normalizedUrl,
            prefs.getUsername() ?: "",
            prefs.getPassword() ?: ""
        )
        _uiState.update { it.copy(serverUrl = normalizedUrl, saveSuccess = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(saveSuccess = false) }
        }
    }

    // ── Security (Admin PIN) ─────────────────────────────────────────────────

    fun onNewAdminPinChange(v: String) {
        if (v.length <= 4 && v.all { it.isDigit() })
            _uiState.update { it.copy(newAdminPin = v, pinSaveSuccess = false) }
    }

    fun saveAdminPin() {
        val pin = _uiState.value.newAdminPin
        if (pin.length == 4) {
            prefs.saveAdminPin(pin)
            _uiState.update { it.copy(newAdminPin = "", pinSaveSuccess = true) }
        }
    }

    // ── User Management ──────────────────────────────────────────────────────

    fun openAddUserDialog() {
        val defaultServer = prefs.getServerUrl() ?: ""
        _uiState.update {
            it.copy(
                showAddUserDialog = true,
                editingUser = null,
                userFormName = "",
                userFormServer = defaultServer,
                userFormUsername = "",
                userFormPassword = "",
                userFormPin = "",
                userFormError = null
            )
        }
    }

    fun openEditUserDialog(user: AppUser) {
        _uiState.update {
            it.copy(
                showAddUserDialog = true,
                editingUser = user,
                userFormName = user.displayName,
                userFormServer = user.serverUrl,
                userFormUsername = user.xtreamUsername,
                userFormPassword = user.xtreamPassword,
                userFormPin = user.accessPin,
                userFormError = null
            )
        }
    }

    fun closeUserDialog() = _uiState.update { it.copy(showAddUserDialog = false, editingUser = null) }

    fun onUserFormNameChange(v: String) = _uiState.update { it.copy(userFormName = v, userFormError = null) }
    fun onUserFormServerChange(v: String) = _uiState.update { it.copy(userFormServer = v, userFormError = null) }
    fun onUserFormUsernameChange(v: String) = _uiState.update { it.copy(userFormUsername = v, userFormError = null) }
    fun onUserFormPasswordChange(v: String) = _uiState.update { it.copy(userFormPassword = v, userFormError = null) }
    fun onUserFormPinChange(v: String) {
        if (v.length <= 4 && v.all { it.isDigit() })
            _uiState.update { it.copy(userFormPin = v, userFormError = null) }
    }

    fun saveUser() {
        val state = _uiState.value
        if (state.userFormName.isBlank()) {
            _uiState.update { it.copy(userFormError = "El nombre es obligatorio") }; return
        }
        if (state.userFormServer.isBlank()) {
            _uiState.update { it.copy(userFormError = "La URL del servidor es obligatoria") }; return
        }
        if (state.userFormUsername.isBlank()) {
            _uiState.update { it.copy(userFormError = "El usuario de Xtream es obligatorio") }; return
        }
        if (state.userFormPassword.isBlank()) {
            _uiState.update { it.copy(userFormError = "La contraseña es obligatoria") }; return
        }
        if (state.userFormPin.length != 4) {
            _uiState.update { it.copy(userFormError = "El PIN debe tener 4 dígitos") }; return
        }

        val normalizedServer = if (state.userFormServer.endsWith("/"))
            state.userFormServer else "${state.userFormServer}/"

        viewModelScope.launch {
            val entity = AppUserEntity(
                id = state.editingUser?.id ?: 0,
                displayName = state.userFormName.trim(),
                serverUrl = normalizedServer,
                xtreamUsername = state.userFormUsername.trim(),
                xtreamPassword = state.userFormPassword.trim(),
                accessPin = state.userFormPin
            )
            appUserDao.insertUser(entity)
            _uiState.update { it.copy(showAddUserDialog = false, editingUser = null) }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch { appUserDao.deleteUser(userId) }
    }

    fun toggleUserActive(user: AppUser) {
        viewModelScope.launch {
            val entity = appUserDao.getUserById(user.id) ?: return@launch
            appUserDao.updateUser(entity.copy(isActive = !entity.isActive))
        }
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.logout()
        onLoggedOut()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun AppUserEntity.toUi() = AppUser(
        id = id,
        displayName = displayName,
        serverUrl = serverUrl,
        xtreamUsername = xtreamUsername,
        xtreamPassword = xtreamPassword,
        accessPin = accessPin,
        isActive = isActive
    )
}
