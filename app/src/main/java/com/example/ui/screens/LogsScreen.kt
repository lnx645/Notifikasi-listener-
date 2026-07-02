package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationLog
import com.example.ui.MainViewModel
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoCard
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueDark
import com.example.ui.theme.DuoGray
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedDark

@Composable
fun LogsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allLogs.collectAsState()
    var currentPage by remember { mutableStateOf(1) }
    var itemsPerPage by remember { mutableStateOf(10) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Logs Subheader with Clear Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Activity Logs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${logs.size} log(s) captured",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (logs.isNotEmpty()) {
                DuoButton(
                    onClick = { 
                        viewModel.clearLogs()
                        currentPage = 1
                    },
                    containerColor = DuoRed,
                    shadowColor = DuoRedDark,
                    modifier = Modifier
                        .width(110.dp)
                        .testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs icon",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Playful Canvas drawing or design-focused Icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(DuoBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No activity",
                            tint = DuoBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Logs masih kosong",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Setelah aplikasi yang didaftarkan menerima notifikasi, riwayat transmisi akan muncul di sini.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            val totalPages = maxOf(1, kotlin.math.ceil(logs.size.toDouble() / itemsPerPage).toInt())
            if (currentPage > totalPages) {
                currentPage = totalPages
            }
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, logs.size)
            val paginatedLogs = logs.subList(startIndex, endIndex)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("logs_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paginatedLogs, key = { it.id }) { log ->
                    LogItemCard(log = log)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pagination Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Items per page selector or label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Tampilkan:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Chips to select items per page
                    listOf(5, 10, 25).forEach { limit ->
                        val isSelected = itemsPerPage == limit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DuoBlue else DuoGrayLight.copy(alpha = 0.5f))
                                .clickable {
                                    itemsPerPage = limit
                                    currentPage = 1 // reset to first page
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = limit.toString(),
                                color = if (isSelected) Color.White else DuoGray,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Right side: Prev / Next with page info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DuoButton(
                        onClick = { if (currentPage > 1) currentPage-- },
                        enabled = currentPage > 1,
                        containerColor = DuoBlue,
                        shadowColor = DuoBlueDark,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Halaman Sebelumnya",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "$currentPage / $totalPages",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    DuoButton(
                        onClick = { if (currentPage < totalPages) currentPage++ },
                        enabled = currentPage < totalPages,
                        containerColor = DuoBlue,
                        shadowColor = DuoBlueDark,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Halaman Berikutnya",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemCard(log: NotificationLog) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (log.status) {
        "SUCCESS" -> DuoGreen
        "FAILED" -> DuoRed
        else -> DuoOrange
    }

    val statusIcon = when (log.status) {
        "SUCCESS" -> Icons.Default.CheckCircle
        "FAILED" -> Icons.Default.Error
        else -> Icons.Default.HourglassEmpty
    }

    DuoCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
            .testTag("log_item_${log.id}"),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = DuoGrayLight,
        shadowColor = DuoGrayLight.copy(alpha = 0.4f),
        shadowHeight = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: App Label, Timestamp and Expand indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = log.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = log.receivedAt.substringAfter("T").substringBefore("+"),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body contents: Title & Text Message
            Column(modifier = Modifier.padding(start = 20.dp)) {
                if (log.title.isNotEmpty()) {
                    Text(
                        text = log.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row: status text & package ID
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (log.status == "PENDING_RETRY") "Queued (Retry ${log.retryCount})" else log.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }

            // Expanded details block
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DuoGrayLight)

                    if (!log.bigText.isNullOrEmpty()) {
                        DetailRow(label = "Big Text", value = log.bigText)
                    }
                    if (!log.subText.isNullOrEmpty()) {
                        DetailRow(label = "Sub Text", value = log.subText)
                    }

                    DetailRow(label = "ISO Received Time", value = log.receivedAt)
                    DetailRow(label = "HTTP Code", value = log.httpStatus?.toString() ?: "N/A (Pending)")
                    if (!log.errorMessage.isNullOrEmpty()) {
                        DetailRow(label = "Error Detail", value = log.errorMessage)
                    }
                    DetailRow(label = "Android Version", value = log.androidVersion)
                    DetailRow(label = "Device Brand", value = log.deviceBrand)
                    DetailRow(label = "Device Model", value = log.deviceModel)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = DuoBlue
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}
