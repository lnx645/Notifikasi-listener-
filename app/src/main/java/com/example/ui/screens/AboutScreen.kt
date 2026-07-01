package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DuoCard
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueDark
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoOrangeDark
import com.example.ui.theme.DuoRed

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Logo and Info Center
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("about_header_section"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(DuoGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                // Playful canvas mascot or Share icon
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Notification Bridge Logo",
                    tint = DuoGreenDark,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notification Bridge",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Version 1.0.0 (Native Kotlin)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Architecture Section
        Text(
            text = "System Integration Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        ArchitectureItem(
            step = "1",
            title = "Notification Intercept",
            description = "Subscribes to standard Android notification streams using NotificationListenerService. Reads app titles, content bodies, package tags, and device properties.",
            icon = Icons.Default.RssFeed,
            tintColor = DuoBlue,
            shadowColor = DuoBlueDark
        )

        ArchitectureItem(
            step = "2",
            title = "Enforced Local Persistence",
            description = "Saves intercepted updates immediately to local Room DB. Prevents loss of events when application is closed or system restarts.",
            icon = Icons.Default.Storage,
            tintColor = DuoOrange,
            shadowColor = DuoOrangeDark
        )

        ArchitectureItem(
            step = "3",
            title = "Encrypted Preferences",
            description = "App config, URLs, and token codes are locked under 256-bit AES GCM cryptography using Jetpack Security-Crypto.",
            icon = Icons.Default.Lock,
            tintColor = DuoGreen,
            shadowColor = DuoGreenDark
        )

        ArchitectureItem(
            step = "4",
            title = "Automated Upload Queue",
            description = "Uses Android WorkManager to fire uploads in background. Re-schedules offline failures using robust exponential backoffs.",
            icon = Icons.Default.Devices,
            tintColor = DuoBlue,
            shadowColor = DuoBlueDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Technical Spec Card
        DuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = DuoGrayLight,
            shadowColor = DuoGrayLight.copy(alpha = 0.4f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Application Specifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = DuoGreen
                )

                HorizontalDivider(color = DuoGrayLight, thickness = 1.5.dp)

                SpecRow(label = "Platform", value = "Android Native 100% Kotlin")
                SpecRow(label = "UI Engine", value = "Jetpack Compose (Material 3)")
                SpecRow(label = "Database", value = "SQLite / Jetpack Room Persistence")
                SpecRow(label = "Security", value = "AES-256 SIV/GCM EncryptedPrefs")
                SpecRow(label = "Network", value = "OkHttp / Coroutine REST Client")
                SpecRow(label = "Target SDK", value = "API 36 (Android 16)")
                SpecRow(label = "Minimum SDK", value = "API 26 (Android 8.0 Oreo)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Copyright © 2026 • Build with love by Dadan Hidayat",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ArchitectureItem(
    step: String,
    title: String,
    description: String,
    icon: ImageVector,
    tintColor: Color,
    shadowColor: Color
) {
    DuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = tintColor.copy(alpha = 0.05f),
        borderColor = tintColor.copy(alpha = 0.4f),
        shadowColor = shadowColor.copy(alpha = 0.12f),
        shadowHeight = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Step $step: $title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
