package com.iptvapp.player.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Admin Panel",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }

        HorizontalDivider(color = NetflixDivider)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Server Info Card
            AdminCard(title = "Server Information") {
                InfoRow(label = "Server URL", value = uiState.serverUrl)
                Spacer(Modifier.height(8.dp))
                InfoRow(label = "Username", value = uiState.username)
            }

            // PIN Settings Card
            AdminCard(title = "Security Settings") {
                Text(
                    text = "Change Admin PIN",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.newPin,
                    onValueChange = viewModel::onNewPinChange,
                    label = { Text("New 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetflixRed,
                        unfocusedBorderColor = NetflixDivider,
                        focusedLabelColor = NetflixRed,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NetflixRed,
                        focusedContainerColor = NetflixMediumGray,
                        unfocusedContainerColor = NetflixMediumGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::saveNewPin,
                    enabled = uiState.newPin.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save PIN", color = TextPrimary)
                }
                AnimatedVisibility(visible = uiState.saveSuccess) {
                    Text(
                        text = "PIN saved successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Danger Zone
            AdminCard(title = "Account") {
                OutlinedButton(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FavoriteRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(FavoriteRed)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = FavoriteRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }
}

@Composable
private fun AdminCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NetflixMediumGray, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        HorizontalDivider(
            color = NetflixDivider,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
