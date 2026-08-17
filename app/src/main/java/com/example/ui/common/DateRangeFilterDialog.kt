package com.example.ui.common

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateRangeFilterDialog(
    showDialog: Boolean,
    initialStartDate: String = "",
    initialEndDate: String = "",
    onDismissRequest: () -> Unit,
    onApplyDateRange: (startDate: String, endDate: String) -> Unit,
    onResetDateRange: () -> Unit
) {
    if (!showDialog) return

    val context = LocalContext.current
    val calNow = Calendar.getInstance()
    val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val todayStr = remember { sdfIso.format(calNow.time) }

    var startDate by remember {
        mutableStateOf(if (initialStartDate.isNotBlank()) initialStartDate else todayStr)
    }
    var endDate by remember {
        mutableStateOf(if (initialEndDate.isNotBlank()) initialEndDate else todayStr)
    }
    var errorMessage by remember { mutableStateOf("") }

    // Convert YYYY-MM-DD to "D MMM YYYY"
    fun formatDisplayDate(dateStr: String): String {
        if (dateStr.isBlank()) return "Pilih Tanggal"
        return try {
            val date = sdfIso.parse(dateStr)
            if (date != null) {
                SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(date)
            } else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    // DatePicker for Start Date
    val startDatePicker = remember(context, startDate) {
        val cal = Calendar.getInstance()
        try {
            val d = sdfIso.parse(startDate)
            if (d != null) cal.time = d
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                startDate = sdfIso.format(selectedCal.time)
                errorMessage = ""
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    // DatePicker for End Date
    val endDatePicker = remember(context, endDate) {
        val cal = Calendar.getInstance()
        try {
            val d = sdfIso.parse(endDate)
            if (d != null) cal.time = d
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                endDate = sdfIso.format(selectedCal.time)
                errorMessage = ""
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SageGreenPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Filter Rentang Tanggal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kustomisasi periode rekap keuangan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Presets Chips
                Text(
                    text = "Preset Cepat:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageGreenPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Hari Ini
                    FilterChip(
                        selected = (startDate == todayStr && endDate == todayStr),
                        onClick = {
                            startDate = todayStr
                            endDate = todayStr
                            errorMessage = ""
                        },
                        label = { Text("Hari Ini", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    // 7 Hari Terakhir
                    FilterChip(
                        selected = false,
                        onClick = {
                            val calStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -6) }
                            startDate = sdfIso.format(calStart.time)
                            endDate = todayStr
                            errorMessage = ""
                        },
                        label = { Text("7 Hari Terakhir", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    // 30 Hari Terakhir
                    FilterChip(
                        selected = false,
                        onClick = {
                            val calStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -29) }
                            startDate = sdfIso.format(calStart.time)
                            endDate = todayStr
                            errorMessage = ""
                        },
                        label = { Text("30 Hari Terakhir", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    // Bulan Ini
                    FilterChip(
                        selected = false,
                        onClick = {
                            val calStart = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_MONTH, 1)
                            }
                            startDate = sdfIso.format(calStart.time)
                            endDate = todayStr
                            errorMessage = ""
                        },
                        label = { Text("Awal Bulan s.d. Sekarang", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Custom Range Input Cards
                Text(
                    text = "Pilih Manual Tanggal Mulai & Selesai:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { startDatePicker.show() }
                            .testTag("btn_select_start_date")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Tanggal Mulai",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = formatDisplayDate(startDate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // End Date Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { endDatePicker.show() }
                            .testTag("btn_select_end_date")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Tanggal Selesai",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = formatDisplayDate(endDate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Error / Info Badge
                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SageGreenPrimaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Rentang: ${formatDisplayDate(startDate)} — ${formatDisplayDate(endDate)}",
                                fontSize = 11.sp,
                                color = SageGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startDate > endDate) {
                        errorMessage = "Tanggal mulai tidak boleh melebihi tanggal selesai"
                    } else {
                        onApplyDateRange(startDate, endDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_apply_date_filter")
            ) {
                Text("Terapkan Filter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onResetDateRange()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SageGreenPrimary),
                modifier = Modifier.testTag("btn_reset_date_filter")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = SageGreenPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Bulan Ini", color = SageGreenPrimary, fontSize = 12.sp)
            }
        }
    )
}
