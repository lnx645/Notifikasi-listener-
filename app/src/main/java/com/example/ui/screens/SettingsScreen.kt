package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val backendUrlState by viewModel.backendUrl.collectAsState()
    val authTokenState by viewModel.authToken.collectAsState()
    val deviceNameState by viewModel.deviceName.collectAsState()
    val syncPackagesState by viewModel.syncPackages.collectAsState()
    val retryCountState by viewModel.retryCount.collectAsState()
    val connectionTimeoutState by viewModel.connectionTimeout.collectAsState()
    val monitoringPackagesState by viewModel.monitoringPackages.collectAsState()
    val isSyncingPackages by viewModel.isSyncingPackages.collectAsState()

    // Local form states
    var backendUrl by remember { mutableStateOf("") }
    var authToken by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var syncPackages by remember { mutableStateOf(true) }
    var retryCount by remember { mutableStateOf("3") }
    var connectionTimeout by remember { mutableStateOf("15") }
    var tokenVisible by remember { mutableStateOf(false) }

    val monitoringPackages = remember { mutableStateListOf<String>() }
    var newPackageInput by remember { mutableStateOf("") }

    // Synchronize local states with ViewModel values when they load
    LaunchedEffect(backendUrlState) { backendUrl = backendUrlState }
    LaunchedEffect(authTokenState) { authToken = authTokenState }
    LaunchedEffect(deviceNameState) { deviceName = deviceNameState }
    LaunchedEffect(syncPackagesState) { syncPackages = syncPackagesState }
    LaunchedEffect(retryCountState) { retryCount = retryCountState.toString() }
    LaunchedEffect(connectionTimeoutState) { connectionTimeout = connectionTimeoutState.toString() }
    LaunchedEffect(monitoringPackagesState) {
        monitoringPackages.clear()
        monitoringPackages.addAll(monitoringPackagesState)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bridge Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // API settings card
        OutlinedTextField(
            value = backendUrl,
            onValueChange = { backendUrl = it },
            label = { Text("Backend URL") },
            placeholder = { Text("https://api.example.com") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backend_url_input"),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = authToken,
            onValueChange = { authToken = it },
            label = { Text("Authorization Token (Bearer)") },
            placeholder = { Text("Leave empty if none") },
            singleLine = true,
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                    Icon(
                        imageVector = if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (tokenVisible) "Hide token" else "Show token"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_token_input"),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = deviceName,
            onValueChange = { deviceName = it },
            label = { Text("Device Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("device_name_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = retryCount,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) retryCount = it },
                label = { Text("Max Retries") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("retry_count_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = connectionTimeout,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) connectionTimeout = it },
                label = { Text("Timeout (sec)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("timeout_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Package management section
        Text(
            text = "Monitored Packages",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newPackageInput,
                onValueChange = { newPackageInput = it.trim() },
                label = { Text("Add Package Name") },
                placeholder = { Text("com.example.app") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("new_package_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newPackageInput.isNotEmpty() && !monitoringPackages.contains(newPackageInput)) {
                        monitoringPackages.add(newPackageInput)
                        newPackageInput = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .size(54.dp)
                    .testTag("add_package_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add package icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Display current chips of package list
        if (monitoringPackages.isEmpty()) {
            Text(
                text = "No packages set. Monitoring is disabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("packages_chips_layout"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                monitoringPackages.forEach { pkg ->
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = {
                            Text(
                                text = pkg,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete package",
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("delete_package_$pkg")
                                    .background(Color.Unspecified)
                                    .padding(2.dp)
                                    .clickable { monitoringPackages.remove(pkg) }
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Sync and Automatic flags Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Get Config From Server",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GET /config/packages to download active list",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSyncingPackages) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                ElevatedButton(
                    onClick = { viewModel.syncPackagesFromServer() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("sync_packages_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Sync packages icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Now")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save All Configuration Button
        Button(
            onClick = {
                viewModel.updateSettings(
                    backendUrl = backendUrl.trim(),
                    authToken = authToken.trim(),
                    deviceName = deviceName.trim(),
                    syncPackages = syncPackages,
                    retryCount = retryCount.toIntOrNull() ?: 3,
                    connectionTimeout = connectionTimeout.toIntOrNull() ?: 15,
                    monitoringPackages = monitoringPackages.toList()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("save_settings_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save configuration icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
