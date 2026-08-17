package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import com.example.ui.theme.SoftTextMuted
import com.example.ui.util.PaycheckCycleHelper
import com.example.ui.util.PaycheckPeriod

/**
 * Reusable Paycheck Cycle Navigator Card with previous/next period buttons,
 * period indicators, active cycle reset, and quick cycle start date configuration.
 */
@Composable
fun PaycheckPeriodNavigatorCard(
    currentPeriod: PaycheckPeriod,
    onPrevCycle: () -> Unit,
    onNextCycle: () -> Unit,
    onResetCycle: () -> Unit,
    onUpdateStartDay: (Int) -> Unit = {},
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    primaryColor: Color = SageGreenPrimary,
    borderColor: Color = SageGreenPrimaryContainer
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val handleOpenSettings = {
        if (onOpenSettings != null) {
            onOpenSettings()
        } else {
            showSettingsDialog = true
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("paycheck_navigator_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button
                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onPrevCycle)
                        .testTag("btn_prev_paycheck_cycle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Periode Sebelumnya",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Center Period Details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clickable { handleOpenSettings() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentPeriod.displayPeriod,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentPeriod.isCurrentCycle) primaryColor.copy(alpha = 0.15f) else Color(0xFFFFECB3),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    currentPeriod.offset == 0 -> "Siklus Gajian Aktif (Tgl ${currentPeriod.startDay})"
                                    currentPeriod.offset == -1 -> "Siklus Bulan Lalu (-1)"
                                    currentPeriod.offset < -1 -> "Siklus ${currentPeriod.offset} Bulan Lalu"
                                    currentPeriod.offset == 1 -> "Siklus Bulan Depan (+1)"
                                    else -> "Siklus +${currentPeriod.offset} Bulan"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentPeriod.isCurrentCycle) primaryColor else Color(0xFFF57F17),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Next Button
                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onNextCycle)
                        .testTag("btn_next_paycheck_cycle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Periode Selanjutnya",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick reset or configure strip
            AnimatedVisibility(
                visible = !currentPeriod.isCurrentCycle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = primaryColor.copy(alpha = 0.15f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Melihat histori periode lain",
                            fontSize = 11.sp,
                            color = SoftTextMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = primaryColor,
                            modifier = Modifier
                                .clickable(onClick = onResetCycle)
                                .testTag("btn_reset_paycheck_cycle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Kembali ke Siklus Aktif",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        PaycheckCycleSettingsDialog(
            currentStartDay = currentPeriod.startDay,
            onDismiss = { showSettingsDialog = false },
            onSave = { newStartDay ->
                onUpdateStartDay(newStartDay)
                showSettingsDialog = false
            }
        )
    }
}

/**
 * Interactive dialog allowing users to customize their monthly paycheck start date (1..31).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaycheckCycleSettingsDialog(
    currentStartDay: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(currentStartDay.coerceIn(1, 31)) }
    val previewPeriod = remember(selectedDay) {
        PaycheckCycleHelper.calculatePeriod(startDay = selectedDay, offset = 0)
    }

    val presetDays = listOf(25, 1, 20, 26, 27, 28, 30)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SageGreenPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Pengaturan Siklus Gajian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sesuaikan tanggal mulai siklus gaji",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tentukan tanggal diterimanya gaji setiap bulan. Seluruh perhitungan keuangan, ringkasan, dan grafik akan otomatis dihitung dari tanggal ini sampai 1 hari sebelum gajian berikutnya.",
                    fontSize = 12.sp,
                    color = SoftTextDark,
                    lineHeight = 16.sp
                )

                // Presets
                Column {
                    Text(
                        text = "PILIHAN CEPAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetDays.forEach { day ->
                            val isSelected = selectedDay == day
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDay = day },
                                label = {
                                    Text(
                                        text = if (day == 1) "Tgl 1 (Bulan Masehi)" else if (day == 25) "Tgl 25 (Standar)" else "Tgl $day",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenPrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Slider selector
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tanggal Mulai Siklus:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SageGreenPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "Tanggal $selectedDay",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Slider(
                        value = selectedDay.toFloat(),
                        onValueChange = { selectedDay = it.toInt().coerceIn(1, 31) },
                        valueRange = 1f..31f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = SageGreenPrimary,
                            activeTrackColor = SageGreenPrimary,
                            inactiveTrackColor = SageGreenPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("slider_paycheck_day")
                    )
                }

                // Live Preview Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCreamCanvas),
                    border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pratinjau Siklus Saat Ini:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                        }
                        Text(
                            text = previewPeriod.displayPeriod,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTextDark
                        )
                        Text(
                            text = if (selectedDay == 1) {
                                "Mencakup tanggal 1 sampai akhir bulan kalender."
                            } else {
                                "Mencakup tanggal $selectedDay bulan sebelumnya s.d. tanggal ${selectedDay - 1} bulan ini."
                            },
                            fontSize = 10.sp,
                            color = SoftTextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDay) },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_paycheck_settings")
            ) {
                Text("Terapkan Siklus", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SoftTextMuted)
            }
        }
    )
}
