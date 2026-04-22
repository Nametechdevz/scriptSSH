package com.iptvapp.player.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptvapp.player.ui.components.PinDialog
import com.iptvapp.player.ui.theme.*

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // PIN gate
    if (uiState.showPinDialog) {
        PinDialog(
            onDismiss = { viewModel.dismissPinDialog(onBack) },
            onPinConfirmed = viewModel::verifyPin,
            errorMessage = uiState.pinError
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Text(
                text = "Panel de Administración",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = NetflixDivider)

        // Tab row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = NetflixDarkGray,
            contentColor = NetflixRed,
            divider = { HorizontalDivider(color = NetflixDivider) }
        ) {
            AdminTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon(),
                            contentDescription = null,
                            tint = if (uiState.selectedTab == tab) NetflixRed else TextSecondary
                        )
                    },
                    text = {
                        Text(
                            text = tab.label(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.selectedTab == tab) NetflixRed else TextSecondary
                        )
                    }
                )
            }
        }

        when (uiState.selectedTab) {
            AdminTab.SERVER -> ServerConfigTab(uiState, viewModel, onLogout)
            AdminTab.USERS -> UsersTab(uiState, viewModel)
            AdminTab.SECURITY -> SecurityTab(uiState, viewModel)
        }
    }

    // Add/Edit user dialog
    if (uiState.showAddUserDialog) {
        UserFormDialog(uiState = uiState, viewModel = viewModel)
    }
}

// ── Server Config Tab ─────────────────────────────────────────────────────────

@Composable
private fun ServerConfigTab(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminCard(title = "Configuración del Servidor Xtream Codes") {
            Text(
                text = "URL del Servidor (DNS:Puerto)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.serverUrl,
                onValueChange = viewModel::onServerUrlChange,
                placeholder = { Text("http://miservidor.com:8080", color = TextDisabled) },
                leadingIcon = {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = NetflixRed)
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Incluye el protocolo (http:// o https://) y el puerto.",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = viewModel::saveServerConfig,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar URL", color = Color.White)
                }
            }
            AnimatedVisibility(visible = uiState.saveSuccess) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("URL guardada correctamente", style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                }
            }
        }

        AdminCard(title = "Información actual") {
            InfoRow("Servidor activo", uiState.serverUrl.ifBlank { "Sin configurar" })
            Spacer(Modifier.height(8.dp))
            InfoRow("Usuario activo", uiState.username.ifBlank { "Sin configurar" })
        }

        AdminCard(title = "Sesión") {
            OutlinedButton(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FavoriteRed),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(FavoriteRed)
                )
            ) {
                Icon(Icons.Default.Logout, null, tint = FavoriteRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}

// ── Users Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun UsersTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        // Add user button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Usuarios", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text(
                    "${uiState.users.size} usuario(s) registrado(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Button(
                onClick = viewModel::openAddUserDialog,
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Agregar", color = Color.White)
            }
        }

        HorizontalDivider(color = NetflixDivider)

        if (uiState.users.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, null, tint = TextDisabled, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No hay usuarios creados", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Toca \"Agregar\" para crear el primer usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDisabled,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.users, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        onEdit = { viewModel.openEditUserDialog(user) },
                        onDelete = { viewModel.deleteUser(user.id) },
                        onToggleActive = { viewModel.toggleUserActive(user) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: AppUser,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NetflixMediumGray, RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (user.isActive) NetflixDivider else FavoriteRed.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (user.isActive) NetflixRed else TextDisabled,
                    RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.displayName.take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (!user.isActive) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "INACTIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = FavoriteRed,
                        modifier = Modifier
                            .background(FavoriteRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "@${user.xtreamUsername}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.serverUrl.take(40),
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column {
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onToggleActive, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (user.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                    contentDescription = null,
                    tint = if (user.isActive) SuccessGreen else TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Delete, null, tint = FavoriteRed, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = NetflixMediumGray,
            title = { Text("Eliminar usuario", color = TextPrimary) },
            text = { Text("¿Eliminar a \"${user.displayName}\"? Esta acción no se puede deshacer.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FavoriteRed)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

// ── Security Tab ──────────────────────────────────────────────────────────────

@Composable
private fun SecurityTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminCard(title = "Cambiar PIN de Administrador") {
            Text(
                "El PIN protege el acceso a este panel.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.newAdminPin,
                onValueChange = viewModel::onNewAdminPinChange,
                label = { Text("Nuevo PIN (4 dígitos)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::saveAdminPin,
                enabled = uiState.newAdminPin.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar nuevo PIN", color = Color.White)
            }
            AnimatedVisibility(visible = uiState.pinSaveSuccess) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PIN actualizado", style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                }
            }
        }
    }
}

// ── User Form Dialog ──────────────────────────────────────────────────────────

@Composable
private fun UserFormDialog(uiState: AdminUiState, viewModel: AdminViewModel) {
    val isEditing = uiState.editingUser != null
    var showPassword by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = viewModel::closeUserDialog) {
        Column(
            modifier = Modifier
                .background(NetflixMediumGray, RoundedCornerShape(12.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEditing) "Editar Usuario" else "Nuevo Usuario",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            // Name
            OutlinedTextField(
                value = uiState.userFormName,
                onValueChange = viewModel::onUserFormNameChange,
                label = { Text("Nombre del usuario") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = NetflixRed) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Server URL
            OutlinedTextField(
                value = uiState.userFormServer,
                onValueChange = viewModel::onUserFormServerChange,
                label = { Text("Servidor (DNS:Puerto)") },
                leadingIcon = { Icon(Icons.Default.Dns, null, tint = NetflixRed) },
                placeholder = { Text("http://servidor.com:8080", color = TextDisabled) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Xtream Username
            OutlinedTextField(
                value = uiState.userFormUsername,
                onValueChange = viewModel::onUserFormUsernameChange,
                label = { Text("Usuario Xtream Codes") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = NetflixRed) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Xtream Password
            OutlinedTextField(
                value = uiState.userFormPassword,
                onValueChange = viewModel::onUserFormPasswordChange,
                label = { Text("Contraseña Xtream Codes") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = NetflixRed) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Access PIN
            OutlinedTextField(
                value = uiState.userFormPin,
                onValueChange = viewModel::onUserFormPinChange,
                label = { Text("PIN de acceso (4 dígitos)") },
                leadingIcon = { Icon(Icons.Default.Pin, null, tint = NetflixRed) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = iptvTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Error
            AnimatedVisibility(visible = uiState.userFormError != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = FavoriteRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(uiState.userFormError ?: "", style = MaterialTheme.typography.bodyMedium, color = FavoriteRed)
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::closeUserDialog,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("Cancelar") }
                Button(
                    onClick = viewModel::saveUser,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                ) {
                    Text(if (isEditing) "Actualizar" else "Crear", color = Color.White)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun AdminCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NetflixMediumGray, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        HorizontalDivider(color = NetflixDivider, modifier = Modifier.padding(vertical = 12.dp))
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun iptvTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NetflixRed,
    unfocusedBorderColor = NetflixDivider,
    focusedLabelColor = NetflixRed,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NetflixRed,
    focusedContainerColor = NetflixSurface,
    unfocusedContainerColor = NetflixSurface
)

private fun AdminTab.label() = when (this) {
    AdminTab.SERVER -> "Servidor"
    AdminTab.USERS -> "Usuarios"
    AdminTab.SECURITY -> "Seguridad"
}

private fun AdminTab.icon() = when (this) {
    AdminTab.SERVER -> Icons.Default.Dns
    AdminTab.USERS -> Icons.Default.Group
    AdminTab.SECURITY -> Icons.Default.Shield
}
