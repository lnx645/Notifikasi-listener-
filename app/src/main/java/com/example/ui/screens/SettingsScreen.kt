package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoCard
import com.example.ui.components.DuoTextField
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val backendUrlState by viewModel.backendUrl.collectAsState()
    val authTokenState by viewModel.authToken.collectAsState()
    val deviceNameState by viewModel.deviceName.collectAsState()
    val syncPackagesState by viewModel.syncPackages.collectAsState()
    val retryCountState by viewModel.retryCount.collectAsState()
    val connectionTimeoutState by viewModel.connectionTimeout.collectAsState()
    val monitoringPackagesState by viewModel.monitoringPackages.collectAsState()
    val isSyncingPackages by viewModel.isSyncingPackages.collectAsState()
    val allLogsList by viewModel.allLogs.collectAsState()

    val appIdState by viewModel.applicationId.collectAsState()
    val autoRetryState by viewModel.autoRetry.collectAsState()
    val autoStartState by viewModel.autoStartAfterBoot.collectAsState()
    val debugModeState by viewModel.debugMode.collectAsState()

    // Local form states
    var backendUrl by remember { mutableStateOf("") }
    var authToken by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var syncPackages by remember { mutableStateOf(true) }
    var retryCount by remember { mutableStateOf("3") }
    var connectionTimeout by remember { mutableStateOf("15") }
    var tokenVisible by remember { mutableStateOf(false) }

    var applicationId by remember { mutableStateOf("") }
    var autoRetry by remember { mutableStateOf(true) }
    var autoStartAfterBoot by remember { mutableStateOf(true) }
    var debugMode by remember { mutableStateOf(false) }

    var newPackageInput by remember { mutableStateOf("") }

    // Dialog confirmation states
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearLogsDialog by remember { mutableStateOf(false) }

    // Synchronize local states with ViewModel values when they load
    LaunchedEffect(backendUrlState) { backendUrl = backendUrlState }
    LaunchedEffect(authTokenState) { authToken = authTokenState }
    LaunchedEffect(deviceNameState) { deviceName = deviceNameState }
    LaunchedEffect(syncPackagesState) { syncPackages = syncPackagesState }
    LaunchedEffect(retryCountState) { retryCount = retryCountState.toString() }
    LaunchedEffect(connectionTimeoutState) { connectionTimeout = connectionTimeoutState.toString() }
    LaunchedEffect(appIdState) { applicationId = appIdState }
    LaunchedEffect(autoRetryState) { autoRetry = autoRetryState }
    LaunchedEffect(autoStartState) { autoStartAfterBoot = autoStartState }
    LaunchedEffect(debugModeState) { debugMode = debugModeState }

    // --- FORM VALIDATION ---
    val isBackendUrlValid = backendUrl.trim().startsWith("https://")
    val isTimeoutValid = (connectionTimeout.toIntOrNull() ?: 0) in 1..120
    val isRetryCountValid = (retryCount.toIntOrNull() ?: 0) in 0..10
    val isAppIdValid = applicationId.trim().isNotEmpty()

    val isFormValid = isBackendUrlValid && isTimeoutValid && isRetryCountValid && isAppIdValid

    val scrollState = rememberScrollState()

    // Dialog Confirmation Reset
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Reset Konfigurasi?", fontWeight = FontWeight.Black, color = DuoRedDark)
            },
            text = {
                Text("Semua pengaturan akan dikembalikan ke setelan bawaan pabrik. Apakah Anda yakin?", fontWeight = FontWeight.Bold)
            },
            confirmButton = {
                DuoButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetConfiguration()
                    },
                    containerColor = DuoRed,
                    shadowColor = DuoRedDark,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Ya, Reset", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Black, color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Dialog Confirmation Clear Logs
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            title = {
                Text("Hapus Semua Log?", fontWeight = FontWeight.Black, color = DuoRedDark)
            },
            text = {
                Text("Tindakan ini akan menghapus seluruh catatan transaksi lokal secara permanen. Apakah Anda yakin?", fontWeight = FontWeight.Bold)
            },
            confirmButton = {
                DuoButton(
                    onClick = {
                        showClearLogsDialog = false
                        viewModel.clearLogs()
                    },
                    containerColor = DuoRed,
                    shadowColor = DuoRedDark,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Ya, Hapus", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Black, color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bridge Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )

        // API settings card
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = DuoGrayLight,
            shadowColor = DuoGrayLight.copy(alpha = 0.4f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SERVER & API SETTINGS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = DuoGreen
                )

                // Backend URL Input with HTTPS Only Validation
                Column(modifier = Modifier.fillMaxWidth()) {
                    DuoTextField(
                        value = backendUrl,
                        onValueChange = { backendUrl = it },
                        label = "Base URL API (HTTPS Only)",
                        placeholder = "https://api.example.com",
                        singleLine = true,
                        isError = !isBackendUrlValid && backendUrl.isNotEmpty(),
                        activeColor = DuoGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backend_url_input")
                    )
                    if (!isBackendUrlValid && backendUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "URL harus dimulai dengan https:// demi keamanan data.",
                            color = DuoRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                DuoTextField(
                    value = authToken,
                    onValueChange = { authToken = it },
                    label = "Authorization Token (Bearer)",
                    placeholder = "Kosongkan jika tidak memakai token",
                    singleLine = true,
                    activeColor = DuoGreen,
                    visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { tokenVisible = !tokenVisible }) {
                            Icon(
                                imageVector = if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (tokenVisible) "Hide token" else "Show token",
                                tint = DuoGreen
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_token_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DuoTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = "Device Name",
                        singleLine = true,
                        activeColor = DuoGreen,
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("device_name_input")
                    )

                    DuoTextField(
                        value = applicationId,
                        onValueChange = { applicationId = it },
                        label = "Application ID",
                        singleLine = true,
                        isError = !isAppIdValid,
                        activeColor = DuoGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("app_id_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        DuoTextField(
                            value = retryCount,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) retryCount = it },
                            label = "Max Retries",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = !isRetryCountValid,
                            activeColor = DuoGreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("retry_count_input")
                        )
                        if (!isRetryCountValid) {
                            Text(
                                text = "Batasi 0 s.d 10",
                                color = DuoRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        DuoTextField(
                            value = connectionTimeout,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) connectionTimeout = it },
                            label = "Timeout (sec)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = !isTimeoutValid,
                            activeColor = DuoGreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timeout_input")
                        )
                        if (!isTimeoutValid) {
                            Text(
                                text = "Batasi 1 s.d 120 detik",
                                color = DuoRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Feature Toggles Card (Auto Start, Auto Retry, Debug Mode)
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = DuoGrayLight,
            shadowColor = DuoGrayLight.copy(alpha = 0.4f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FEATURE & BACKGROUND OPTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = DuoOrange
                )

                // Auto Retry Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Retry", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Kirim ulang otomatis jika koneksi gagal", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = autoRetry,
                        onCheckedChange = { autoRetry = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = DuoGreen)
                    )
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f))

                // Auto Start After Boot Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Start After Boot", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Jalankan otomatis saat HP dihidupkan", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = autoStartAfterBoot,
                        onCheckedChange = { autoStartAfterBoot = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = DuoGreen)
                    )
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f))

                // Debug Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Debug Mode", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Aktifkan logging detail untuk pengembangan", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = debugMode,
                        onCheckedChange = { debugMode = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = DuoGreen)
                    )
                }
            }
        }

        // Package management section card
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = DuoGrayLight,
            shadowColor = DuoGrayLight.copy(alpha = 0.4f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "APLIKASI YANG DIPANTAU",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = DuoBlue
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DuoTextField(
                        value = newPackageInput,
                        onValueChange = { newPackageInput = it.trim() },
                        label = "Package Name Aplikasi",
                        placeholder = "com.whatsapp atau lainnya",
                        singleLine = true,
                        activeColor = DuoBlue,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_package_input")
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = {
                            if (newPackageInput.isNotEmpty() && !monitoringPackagesState.contains(newPackageInput)) {
                                val updatedList = monitoringPackagesState.toMutableList().apply { add(newPackageInput) }
                                viewModel.updateMonitoringPackages(updatedList)
                                newPackageInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(DuoBlue.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .size(54.dp)
                            .testTag("add_package_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add package icon",
                            tint = DuoBlueDark
                        )
                    }
                }

                // Display current chips of package list
                if (monitoringPackagesState.isEmpty()) {
                    Text(
                        text = "Semua aplikasi dipantau (Filter Kosong)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = DuoRed,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("packages_chips_layout"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        monitoringPackagesState.forEach { pkg ->
                            InputChip(
                                selected = true,
                                onClick = { },
                                label = {
                                    Text(
                                        text = pkg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete package",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                val updatedList = monitoringPackagesState.toMutableList().apply { remove(pkg) }
                                                viewModel.updateMonitoringPackages(updatedList)
                                            }
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = DuoBlue.copy(alpha = 0.12f),
                                    selectedLabelColor = DuoBlueDark
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Server Sync Config
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DuoBlue.copy(alpha = 0.05f),
            borderColor = DuoBlue.copy(alpha = 0.3f),
            shadowColor = DuoBlueDark.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sinkronisasi Server",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = DuoBlueDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unduh daftar aplikasi otomatis dari server",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSyncingPackages) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DuoBlue)
                } else {
                    DuoButton(
                        onClick = { viewModel.syncPackagesFromServer() },
                        containerColor = DuoBlue,
                        shadowColor = DuoBlueDark,
                        modifier = Modifier
                            .width(100.dp)
                            .testTag("sync_packages_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Sync packages icon",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unduh", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        // Save All Configuration Button (Only enabled if Form is valid)
        DuoButton(
            onClick = {
                viewModel.updateSettings(
                    backendUrl = backendUrl.trim(),
                    authToken = authToken.trim(),
                    deviceName = deviceName.trim(),
                    syncPackages = syncPackages,
                    retryCount = retryCount.toIntOrNull() ?: 3,
                    connectionTimeout = connectionTimeout.toIntOrNull() ?: 15,
                    monitoringPackages = monitoringPackagesState,
                    applicationId = applicationId.trim(),
                    autoRetry = autoRetry,
                    autoStartAfterBoot = autoStartAfterBoot,
                    debugMode = debugMode
                )
            },
            containerColor = DuoGreen,
            shadowColor = DuoGreenDark,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save configuration icon",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Konfigurasi", fontWeight = FontWeight.Black, fontSize = 15.sp)
        }

        // Export Log & Clear Cache/Logs Buttons
        Text(
            text = "DANGER ZONE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = DuoRed,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DuoRed.copy(alpha = 0.02f),
            borderColor = DuoRed.copy(alpha = 0.25f),
            shadowColor = DuoRedDark.copy(alpha = 0.05f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Export Logs via Intent Share
                DuoButton(
                    onClick = {
                        if (allLogsList.isEmpty()) {
                            // nothing to export
                        } else {
                            val shareText = "Invitnesia Bridge Logs:\n\n" + allLogsList.joinToString("\n\n") { log ->
                                "[${log.receivedAt}] App: ${log.appName} (${log.packageName})\n" +
                                "Status: ${log.status} (HTTP ${log.httpStatus ?: "N/A"})\n" +
                                "Msg: ${log.title} - ${log.message}"
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Ekspor Log Bridge"))
                        }
                    },
                    containerColor = DuoBlue,
                    shadowColor = DuoBlueDark,
                    enabled = allLogsList.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ekspor Log (${allLogsList.size} baris)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                // 2. Clear Logs Cache
                DuoButton(
                    onClick = { showClearLogsDialog = true },
                    containerColor = DuoOrange,
                    shadowColor = DuoOrangeDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bersihkan Cache Log", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                // 3. Reset Configuration to default
                DuoButton(
                    onClick = { showResetDialog = true },
                    containerColor = DuoRed,
                    shadowColor = DuoRedDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Pengaturan", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}
