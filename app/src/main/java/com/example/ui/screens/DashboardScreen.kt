package com.example.ui.screens

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoCard
import com.example.ui.theme.*

@Composable
fun DuoMascotDrawing(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Draw body (Duolingo Green circle)
        drawCircle(
            color = Color(0xFF58CC02),
            radius = w * 0.45f
        )

        // Draw eye outer white circles
        drawCircle(
            color = Color.White,
            radius = w * 0.16f,
            center = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.45f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.16f,
            center = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.45f)
        )

        // Draw pupils (vibrant dark gray circles)
        drawCircle(
            color = Color(0xFF4B4B4B),
            radius = w * 0.07f,
            center = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.45f)
        )
        drawCircle(
            color = Color(0xFF4B4B4B),
            radius = w * 0.07f,
            center = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.45f)
        )

        // Draw pupil highlights
        drawCircle(
            color = Color.White,
            radius = w * 0.025f,
            center = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.43f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.025f,
            center = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.43f)
        )

        // Draw beak (orange triangle/curved path)
        val beakPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.52f)
            lineTo(w * 0.44f, h * 0.60f)
            lineTo(w * 0.56f, h * 0.60f)
            close()
        }
        drawPath(
            path = beakPath,
            color = Color(0xFFFF9600)
        )

        // Cute rosy cheeks
        drawCircle(
            color = Color(0xFFFF4B4B).copy(alpha = 0.3f),
            radius = w * 0.05f,
            center = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.58f)
        )
        drawCircle(
            color = Color(0xFFFF4B4B).copy(alpha = 0.3f),
            radius = w * 0.05f,
            center = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.58f)
        )
    }
}

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()
    
    // Stats flows
    val totalCount by viewModel.totalLogsCount.collectAsState()
    val successCount by viewModel.successLogsCount.collectAsState()
    val failedCount by viewModel.failedLogsCount.collectAsState()
    val pendingCount by viewModel.pendingLogsCount.collectAsState()
    
    val todayTotal by viewModel.todayTotalLogsCount.collectAsState()
    val todaySuccess by viewModel.todaySuccessLogsCount.collectAsState()
    val todayFailed by viewModel.todayFailedLogsCount.collectAsState()
    
    val latestLog by viewModel.latestLog.collectAsState()
    val serverConnectionStatus by viewModel.serverConnectionStatus.collectAsState()
    val isTestingApi by viewModel.isTestingApi.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val authToken by viewModel.authToken.collectAsState()

    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    // Dialog untuk menjelaskan pentingnya Akses Notifikasi
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Akses Notifikasi Diperlukan",
                    fontWeight = FontWeight.Black,
                    color = DuoOrangeDark,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Aplikasi ini mendengarkan pemberitahuan pembayaran e-wallet (DANA, GoPay, dll.) untuk dijembatani ke server Anda.\n\n" +
                           "Izin 'Akses Notifikasi' diperlukan sistem agar proses pemantauan berjalan stabil di background.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                DuoButton(
                    onClick = {
                        showPermissionDialog = false
                        viewModel.openNotificationAccessSettings(context)
                    },
                    containerColor = DuoGreen,
                    shadowColor = DuoGreenDark,
                    modifier = Modifier.width(130.dp)
                ) {
                    Text("Buka Setelan", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Black, color = DuoOrangeDark)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Refresh rotation animation
    var refreshTrigger by remember { mutableStateOf(0) }
    val rotationAngle by animateFloatAsState(
        targetValue = refreshTrigger * 360f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(key1 = true) {
        viewModel.checkServiceStatus()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Playful Duo Mascot Banner
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                DuoMascotDrawing(
                    modifier = Modifier
                        .size(76.dp)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invitnesia Notif!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Jembatan notifikasi e-wallet instan dan aman.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- HEALTH & STATUS ROW ---
        val healthColor = if (!isServiceEnabled) DuoRed else if (!isMonitoringEnabled) DuoOrange else DuoGreen
        val healthBorder = if (!isServiceEnabled) DuoRedDark else if (!isMonitoringEnabled) DuoOrangeDark else DuoGreenDark
        val healthLabel = if (!isServiceEnabled) "TINDAKAN DIPERLUKAN" else if (!isMonitoringEnabled) "PEMANTAUAN MATI" else "SISTEM SEHAT"
        val healthDesc = if (!isServiceEnabled) "Izin Akses Notifikasi mati" else if (!isMonitoringEnabled) "Mengabaikan pemberitahuan" else "Aktif mendengarkan"

        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = healthColor.copy(alpha = 0.06f),
            borderColor = healthColor.copy(alpha = 0.4f),
            shadowColor = healthBorder.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(healthColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (!isServiceEnabled) Icons.Default.Cancel else if (!isMonitoringEnabled) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = "Health status icon",
                            tint = healthBorder,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = healthLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = healthBorder
                        )
                        Text(
                            text = healthDesc,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                IconButton(
                    onClick = {
                        refreshTrigger++
                        viewModel.checkServiceStatus()
                    },
                    modifier = Modifier
                        .background(DuoGrayLight.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            }
                    )
                }
            }
        }

        // --- MASTER MONITORING SWITCH CARD ---
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (isMonitoringEnabled) DuoGreen.copy(alpha = 0.05f) else DuoGrayLight.copy(alpha = 0.1f),
            borderColor = if (isMonitoringEnabled) DuoGreen.copy(alpha = 0.3f) else DuoGrayLight,
            shadowColor = if (isMonitoringEnabled) DuoGreenDark.copy(alpha = 0.1f) else DuoGrayLight.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isMonitoringEnabled) DuoGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMonitoringEnabled) "Monitoring Aktif" else "Monitoring Nonaktif",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (isMonitoringEnabled) DuoGreenDark else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isMonitoringEnabled) "Menjembatani notifikasi ke server" else "Abaikan semua notifikasi masuk",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isMonitoringEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked && !isServiceEnabled) {
                            showPermissionDialog = true
                        } else {
                            viewModel.setMonitoringEnabled(isChecked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DuoGreen,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = DuoGrayLight
                    ),
                    modifier = Modifier.testTag("monitoring_switch")
                )
            }
        }

        // --- INDIKATOR STATUS DETAIL ---
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = DuoGrayLight,
            shadowColor = DuoGrayLight.copy(alpha = 0.4f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "STATUS DIAGNOSTIK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = DuoBlue
                )

                // 1. Izin Akses Notifikasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (isServiceEnabled) DuoGreen else DuoRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Akses Notifikasi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = if (isServiceEnabled) "Diberikan" else "Ditolak",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (isServiceEnabled) DuoGreenDark else DuoRed
                    )
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f), thickness = 1.dp)

                // 2. Koneksi Server
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (serverConnectionStatus == "CONNECTED") DuoGreen else if (serverConnectionStatus == "DISCONNECTED") DuoRed else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Koneksi Server", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = serverConnectionStatus,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (serverConnectionStatus == "CONNECTED") DuoGreenDark else if (serverConnectionStatus == "DISCONNECTED") DuoRed else Color.Gray
                    )
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f), thickness = 1.dp)

                // 3. Status Otorisasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (authToken.isNotEmpty()) DuoGreen else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Otorisasi Bearer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = if (authToken.isNotEmpty()) "TEROTORISASI" else "ANONIM (No Token)",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (authToken.isNotEmpty()) DuoGreenDark else Color.Gray
                    )
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f), thickness = 1.dp)

                // 4. Device ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = DuoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device ID", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(deviceId))
                        }
                    ) {
                        Text(
                            text = deviceId.take(12) + "...",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = DuoBlueDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = DuoBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f), thickness = 1.dp)

                // 5. Antrean Retry & Last Sync
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (pendingCount > 0) DuoOrange else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Queue", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = "$pendingCount tertunda",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (pendingCount > 0) DuoOrangeDark else Color.Gray
                    )
                }

                Divider(color = DuoGrayLight.copy(alpha = 0.5f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = DuoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sinkronisasi Paket", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = lastSyncTime,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = DuoBlueDark
                    )
                }
            }
        }

        // --- STATISTIK ---
        Text(
            text = "Statistik Hari Ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DuoStatCard(
                title = "Total",
                count = todayTotal,
                icon = Icons.Default.NotificationsActive,
                color = DuoBlue,
                borderColor = DuoBlue,
                shadowColor = DuoBlueDark,
                modifier = Modifier.weight(1f)
            )

            DuoStatCard(
                title = "Sukses",
                count = todaySuccess,
                icon = Icons.Default.CloudSync,
                color = DuoGreen,
                borderColor = DuoGreen,
                shadowColor = DuoGreenDark,
                modifier = Modifier.weight(1f)
            )

            DuoStatCard(
                title = "Gagal",
                count = todayFailed,
                icon = Icons.Default.ErrorOutline,
                color = DuoRed,
                borderColor = DuoRed,
                shadowColor = DuoRedDark,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Total Sepanjang Masa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DuoStatCard(
                title = "Total",
                count = totalCount,
                icon = Icons.Default.NotificationsActive,
                color = Color.Gray,
                borderColor = Color.Gray,
                shadowColor = Color.DarkGray,
                modifier = Modifier.weight(1f)
            )

            DuoStatCard(
                title = "Sukses",
                count = successCount,
                icon = Icons.Default.CloudSync,
                color = DuoGreen.copy(alpha = 0.8f),
                borderColor = DuoGreen.copy(alpha = 0.8f),
                shadowColor = DuoGreenDark.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )

            DuoStatCard(
                title = "Gagal",
                count = failedCount,
                icon = Icons.Default.ErrorOutline,
                color = DuoRed.copy(alpha = 0.8f),
                borderColor = DuoRed.copy(alpha = 0.8f),
                shadowColor = DuoRedDark.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
        }

        // --- LAST NOTIFICATION CARD ---
        Text(
            text = "Notifikasi Terakhir",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        if (latestLog != null) {
            val log = latestLog!!
            DuoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                borderColor = DuoGrayLight,
                shadowColor = DuoGrayLight.copy(alpha = 0.4f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.appName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoBlueDark
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (log.status == "SUCCESS") DuoGreen.copy(alpha = 0.15f) else DuoRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = log.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (log.status == "SUCCESS") DuoGreenDark else DuoRedDark
                            )
                        }
                    }

                    Text(
                        text = log.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = log.message,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.packageName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = log.receivedAt.substringAfter("T").take(5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            DuoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DuoGrayLight.copy(alpha = 0.12f),
                borderColor = DuoGrayLight,
                shadowColor = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No notifications icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Belum ada notifikasi yang ditangkap",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray
                    )
                }
            }
        }

        // --- ACTIONS / KONTROL CEPAT ---
        Text(
            text = "Kontrol Cepat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        // Playful Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DuoButton(
                onClick = { viewModel.testApiConnection() },
                containerColor = DuoBlue,
                shadowColor = DuoBlueDark,
                enabled = !isTestingApi,
                modifier = Modifier
                    .weight(1f)
                    .testTag("test_api_button")
            ) {
                if (isTestingApi) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Test API icon",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test API", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            DuoButton(
                onClick = { viewModel.triggerManualRetry() },
                containerColor = DuoOrange,
                shadowColor = DuoOrangeDark,
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry icon",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Send", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Invitnesia Notif • Versi Produksi v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun DuoStatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    borderColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier
) {
    DuoCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.08f),
        borderColor = borderColor.copy(alpha = 0.4f),
        shadowColor = shadowColor.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
