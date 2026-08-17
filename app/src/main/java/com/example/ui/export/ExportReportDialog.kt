package com.example.ui.export

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import com.example.ui.theme.SoftTextMuted
import com.example.util.ExportPeriodType
import com.example.util.ReportExportOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExportReportDialog(
    onDismiss: () -> Unit,
    onExportExcel: (ReportExportOptions) -> Unit,
    onExportPdf: (ReportExportOptions) -> Unit
) {
    val context = LocalContext.current
    val calNow = remember { Calendar.getInstance() }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var periodType by remember { mutableStateOf(ExportPeriodType.MONTHLY) }
    var selectedYear by remember { mutableIntStateOf(calNow.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calNow.get(Calendar.MONTH)) }

    // Start of current month default
    val defaultStart = remember {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        sdf.format(c.time)
    }
    val defaultEnd = remember { sdf.format(Date()) }

    var startDate by remember { mutableStateOf(defaultStart) }
    var endDate by remember { mutableStateOf(defaultEnd) }

    // Checkboxes
    var incBelanja by remember { mutableStateOf(true) }
    var incRandom by remember { mutableStateOf(true) }
    var incAnak by remember { mutableStateOf(true) }
    var incBensin by remember { mutableStateOf(true) }
    var incOli by remember { mutableStateOf(true) }
    var incServis by remember { mutableStateOf(true) }
    var incListrik by remember { mutableStateOf(true) }
    var incJimpitan by remember { mutableStateOf(true) }
    var incHutang by remember { mutableStateOf(true) }

    val allChecked = incBelanja && incRandom && incAnak && incBensin && incOli && incServis && incListrik && incJimpitan && incHutang

    fun toggleAll(check: Boolean) {
        incBelanja = check
        incRandom = check
        incAnak = check
        incBensin = check
        incOli = check
        incServis = check
        incListrik = check
        incJimpitan = check
        incHutang = check
    }

    val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = (2024..2030).toList()

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    // Date Picker Dialogs for Start and End dates
    val startDatePicker = remember(context) {
        DatePickerDialog(context, { _, y, m, d ->
            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
        }, calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), 1)
    }

    val endDatePicker = remember(context) {
        DatePickerDialog(context, { _, y, m, d ->
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
        }, calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), calNow.get(Calendar.DAY_OF_MONTH))
    }

    val currentOptions = remember(periodType, selectedYear, selectedMonth, startDate, endDate, incBelanja, incRandom, incAnak, incBensin, incOli, incServis, incListrik, incJimpitan, incHutang) {
        ReportExportOptions(
            periodType = periodType,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            startDate = startDate,
            endDate = endDate,
            includeBelanja = incBelanja,
            includeRandom = incRandom,
            includeAnak = incAnak,
            includeBensin = incBensin,
            includeOli = incOli,
            includeServis = incServis,
            includeListrik = incListrik,
            includeJimpitan = incJimpitan,
            includeHutang = incHutang
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SageGreenPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Export & Cetak Laporan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftTextDark
                    )
                    Text(
                        text = "Unduh format Excel (.xlsx) & PDF",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Periode Laporan
                Text(
                    text = "1. Periode Laporan",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = SoftTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { periodType = ExportPeriodType.MONTHLY },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = periodType == ExportPeriodType.MONTHLY,
                            onClick = { periodType = ExportPeriodType.MONTHLY }
                        )
                        Text("Bulanan", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { periodType = ExportPeriodType.CUSTOM_RANGE },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = periodType == ExportPeriodType.CUSTOM_RANGE,
                            onClick = { periodType = ExportPeriodType.CUSTOM_RANGE }
                        )
                        Text("Custom Tanggal", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (periodType == ExportPeriodType.MONTHLY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Dropdown
                        Box(modifier = Modifier.weight(1.4f)) {
                            OutlinedButton(
                                onClick = { monthDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(monthNames[selectedMonth], style = MaterialTheme.typography.bodySmall)
                            }
                            DropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false }
                            ) {
                                monthNames.forEachIndexed { idx, name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            selectedMonth = idx
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { yearDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(selectedYear.toString(), style = MaterialTheme.typography.bodySmall)
                            }
                            DropdownMenu(
                                expanded = yearDropdownExpanded,
                                onDismissRequest = { yearDropdownExpanded = false }
                            ) {
                                years.forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text(yr.toString()) },
                                        onClick = {
                                            selectedYear = yr
                                            yearDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Dari (Mulai)", style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                IconButton(onClick = { startDatePicker.show() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("Sampai (Akhir)", style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                IconButton(onClick = { endDatePicker.show() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                HorizontalDivider(color = SoftCreamCanvas, modifier = Modifier.padding(vertical = 4.dp))

                // 2. Kategori Modul Checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Kategori Data Laporan",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SoftTextDark
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { toggleAll(!allChecked) }
                    ) {
                        Checkbox(
                            checked = allChecked,
                            onCheckedChange = { toggleAll(it) },
                            colors = CheckboxDefaults.colors(checkedColor = SageGreenPrimary)
                        )
                        Text("Pilih Semua", style = MaterialTheme.typography.bodySmall, color = SoftTextMuted)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        CategoryCheckboxItem("Belanja Harian (Warung)", incBelanja, "chk_belanja") { incBelanja = it }
                        CategoryCheckboxItem("Random / Tersier", incRandom, "chk_random") { incRandom = it }
                        CategoryCheckboxItem("Belanja & Kebutuhan Anak", incAnak, "chk_anak") { incAnak = it }
                        CategoryCheckboxItem("Isi Bensin Motor", incBensin, "chk_bensin") { incBensin = it }
                        CategoryCheckboxItem("Oli Motor", incOli, "chk_oli") { incOli = it }
                        CategoryCheckboxItem("Servis & Perawatan", incServis, "chk_servis") { incServis = it }
                        CategoryCheckboxItem("Catatan kWh Listrik", incListrik, "chk_listrik") { incListrik = it }
                        CategoryCheckboxItem("Jimpitan & Sosial", incJimpitan, "chk_jimpitan") { incJimpitan = it }
                        CategoryCheckboxItem("Hutang Warung", incHutang, "chk_hutang") { incHutang = it }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Choice of Action Buttons
                Text(
                    text = "3. Pilih Format Export",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = SoftTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onExportExcel(currentOptions) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_export_excel"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Excel (.xlsx)", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { onExportPdf(currentOptions) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_export_pdf"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_export_cancel")
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun CategoryCheckboxItem(
    label: String,
    checked: Boolean,
    testTagKey: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
            .testTag(testTagKey),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = SageGreenPrimary),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SoftTextDark
        )
    }
}
