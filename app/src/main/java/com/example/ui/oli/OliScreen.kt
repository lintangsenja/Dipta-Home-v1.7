package com.example.ui.oli

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteOutline
import com.example.ui.common.DeleteConfirmationDialog
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WarningAmber
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableLongStateOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OilLog
import com.example.data.entity.Vehicle
import com.example.ui.theme.DustyRoseAccent
import com.example.ui.theme.OilYellowPastelBg
import com.example.ui.theme.OilYellowPastelIcon
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OliScreen(
    vehicles: List<Vehicle>,
    activeVehicleId: Int,
    oilLogs: List<OilLog>,
    onAddLog: (vehicleId: Int, kmMotor: Int, jenisOli: String, harga: Int, kapasitasMl: Int, intervalKm: Int, garansiBengkel: String, customTimestamp: Long) -> Unit,
    onUpdateLog: ((OilLog) -> Unit)? = null,
    onUpdateOdometer: (vehicleId: Int, newOdometer: Int) -> Unit,
    onDeleteLog: (id: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateKmDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<OilLog?>(null) }
    var deleteCandidateLog by remember { mutableStateOf<OilLog?>(null) }
    var selectedVehicleFilter by remember { mutableStateOf<Int?>(null) } // null = Semua
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedJenisOliFilter by remember { mutableStateOf("Semua") }

    val selectedVehicle = if (selectedVehicleFilter != null) {
        vehicles.find { it.id == selectedVehicleFilter }
    } else {
        vehicles.find { it.id == activeVehicleId } ?: vehicles.firstOrNull()
    }

    val filteredLogs = remember(oilLogs, selectedVehicleFilter, searchQuery, selectedJenisOliFilter) {
        oilLogs.filter { log ->
            val matchesVehicle = selectedVehicleFilter == null || log.vehicle_id == selectedVehicleFilter
            val matchesSearch = searchQuery.isBlank() ||
                    log.jenis_oli.contains(searchQuery, ignoreCase = true) ||
                    log.garansi_bengkel.contains(searchQuery, ignoreCase = true) ||
                    log.km_motor.toString().contains(searchQuery)
            val matchesJenis = selectedJenisOliFilter == "Semua" || log.jenis_oli.equals(selectedJenisOliFilter, ignoreCase = true)
            matchesVehicle && matchesSearch && matchesJenis
        }
    }

    // Calculation per vehicle or overall
    val totalBiayaMesin = filteredLogs.filter { it.jenis_oli == "Oli Mesin" }.sumOf { it.harga }
    val totalBiayaGardan = filteredLogs.filter { it.jenis_oli == "Oli Gardan" }.sumOf { it.harga }
    val totalBiayaSemua = totalBiayaMesin + totalBiayaGardan

    val latestMesin = filteredLogs.firstOrNull { it.jenis_oli == "Oli Mesin" }
    val latestGardan = filteredLogs.firstOrNull { it.jenis_oli == "Oli Gardan" }

    // Vehicle's Odometer
    val currentOdometer = maxOf(
        selectedVehicle?.current_odometer ?: 0,
        filteredLogs.maxOfOrNull { it.km_motor } ?: 0
    )

    val isMatic = selectedVehicle?.jenis_kendaraan?.contains("Matic", ignoreCase = true) == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catat Oli",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Servis Oli Mesin, Gardan & Reminder KM",
                            fontSize = 12.sp,
                            color = OilYellowPastelIcon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("oli_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Menu Utama"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OilYellowPastelBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DustyRoseAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_oli_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Catatan Oli")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Catat Ganti Oli", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Filter Vehicle Chips
                item {
                    Column {
                        Text(
                            text = "Pilih Kendaraan Garasi:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedVehicleFilter == null,
                                    onClick = { selectedVehicleFilter = null },
                                    label = { Text("Semua Kendaraan (${oilLogs.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                            items(vehicles, key = { it.id }) { vehicle ->
                                val count = oilLogs.count { it.vehicle_id == vehicle.id }
                                val vIsMatic = vehicle.jenis_kendaraan.contains("Matic", ignoreCase = true)
                                FilterChip(
                                    selected = selectedVehicleFilter == vehicle.id,
                                    onClick = { selectedVehicleFilter = vehicle.id },
                                    label = {
                                        Text("${vehicle.nama_kendaraan} ${if (vIsMatic) "[Matic]" else ""} ($count)")
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // 1. Odometer & Standalone Update Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.5.dp, SageGreenPrimaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("odometer_summary_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
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
                                            .background(OilYellowPastelBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = OilYellowPastelIcon,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedVehicle?.nama_kendaraan ?: "Odometer Kendaraan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Posisi KM Odometer Terkini",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showUpdateKmDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SageGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("update_km_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Update KM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Posisi KM Odometer saat ini:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (currentOdometer > 0) "${Formatters.formatNumber(currentOdometer)} KM" else "Belum Diisi",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isMatic) OilYellowPastelBg else SageGreenPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (isMatic) "Trans: Motor Matic" else "Trans: Bebek/Sport/Manual",
                                            color = if (isMatic) OilYellowPastelIcon else SageGreenPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Real-Time Oil Reminder Status Cards (Mesin & Gardan)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Oli Mesin Reminder
                        OilReminderProgressCard(
                            title = "Status Oli Mesin",
                            latestLog = latestMesin,
                            currentOdometer = currentOdometer,
                            defaultIntervalKm = 3000
                        )

                        // Oli Gardan Reminder (If Matic or has Gardan history)
                        if (isMatic || latestGardan != null) {
                            OilReminderProgressCard(
                                title = "Status Oli Gardan",
                                latestLog = latestGardan,
                                currentOdometer = currentOdometer,
                                defaultIntervalKm = 6000
                            )
                        }
                    }
                }

                // 3. Rekapitulasi Biaya Maintenance
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rekap_biaya_oli_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Rekapitulasi Biaya Oli",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricItem(
                                    label = "Oli Mesin",
                                    value = Formatters.formatRupiah(totalBiayaMesin)
                                )
                                MetricItem(
                                    label = "Oli Gardan",
                                    value = Formatters.formatRupiah(totalBiayaGardan)
                                )
                                MetricItem(
                                    label = "Total Perawatan",
                                    value = Formatters.formatRupiah(totalBiayaSemua)
                                )
                            }
                        }
                    }
                }

                // Search Bar & Filter Lines Icon
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari jenis oli / KM / garansi...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_oli_input")
                        )

                        Box {
                            IconButton(
                                onClick = { showFilterMenu = !showFilterMenu },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(OilYellowPastelBg, RoundedCornerShape(14.dp))
                                    .testTag("btn_filter_oli")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = "Filter Oli",
                                    tint = OilYellowPastelIcon
                                )
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Semua Jenis Oli") },
                                    onClick = { selectedJenisOliFilter = "Semua"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Oli Mesin") },
                                    onClick = { selectedJenisOliFilter = "Oli Mesin"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Oli Gardan") },
                                    onClick = { selectedJenisOliFilter = "Oli Gardan"; showFilterMenu = false }
                                )
                            }
                        }
                    }
                }

                if (selectedJenisOliFilter != "Semua") {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { selectedJenisOliFilter = "Semua" },
                            label = { Text("Filter: $selectedJenisOliFilter ✕", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OilYellowPastelBg,
                                selectedLabelColor = OilYellowPastelIcon
                            )
                        )
                    }
                }

                // 4. Section Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Servis Oli",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredLogs.size} Catatan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 5. List Items or Empty State
                if (filteredLogs.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                                .testTag("oli_empty_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OilBarrel,
                                    contentDescription = null,
                                    tint = OilYellowPastelIcon.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum Ada Catatan Ganti Oli",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tekan tombol + Catat Ganti Oli untuk menyimpan riwayat penggantian Oli Mesin atau Oli Gardan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredLogs, key = { it.id }) { log ->
                        val vehicle = vehicles.find { it.id == log.vehicle_id }
                        OilLogItem(
                            log = log,
                            vehicle = vehicle,
                            currentOdometer = currentOdometer,
                            onEdit = { editingLog = log },
                            onDelete = { deleteCandidateLog = log }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Add Oil Dialog (Supports Matic Dual Form & Target Interval KM)
    if (showAddDialog) {
        AddOilDialog(
            vehicles = vehicles,
            defaultVehicleId = selectedVehicleFilter ?: activeVehicleId,
            oilLogs = oilLogs,
            onDismiss = { showAddDialog = false },
            onConfirmLogs = { vehicleId, km, logsToAdd, customTs ->
                logsToAdd.forEach { (jenis, harga, kap, interval) ->
                    onAddLog(vehicleId, km, jenis, harga, kap, interval, "", customTs)
                }
                showAddDialog = false
            }
        )
    }

    // Edit Single Oil Dialog
    if (editingLog != null) {
        EditSingleOilDialog(
            log = editingLog!!,
            vehicles = vehicles,
            onDismiss = { editingLog = null },
            onConfirm = { updatedLog ->
                onUpdateLog?.invoke(updatedLog)
                editingLog = null
            }
        )
    }

    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = deleteCandidateLog != null,
        message = "Apakah Anda yakin ingin menghapus catatan penggantian ${deleteCandidateLog?.jenis_oli ?: "oli"} ini?",
        onDismiss = { deleteCandidateLog = null },
        onConfirm = {
            deleteCandidateLog?.let { onDeleteLog(it.id) }
            deleteCandidateLog = null
        }
    )

    // Standalone Update Odometer Dialog
    if (showUpdateKmDialog) {
        UpdateOdometerDialog(
            vehicles = vehicles,
            defaultVehicleId = selectedVehicleFilter ?: activeVehicleId,
            currentOdometer = currentOdometer,
            onDismiss = { showUpdateKmDialog = false },
            onConfirm = { vehicleId, newKm ->
                onUpdateOdometer(vehicleId, newKm)
                showUpdateKmDialog = false
            }
        )
    }
}

@Composable
fun OilReminderProgressCard(
    title: String,
    latestLog: OilLog?,
    currentOdometer: Int,
    defaultIntervalKm: Int
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, SageGreenPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (latestLog != null) {
                    Text(
                        text = "Ganti: ${Formatters.formatNumber(latestLog.km_motor)} KM",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (latestLog == null) {
                Text(
                    text = "Belum ada catatan $title. Tambahkan catatan ganti oli untuk mengaktifkan pemantauan target.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val startKm = latestLog.km_motor
                val intervalKm = if (latestLog.interval_km > 0) latestLog.interval_km else defaultIntervalKm
                val targetKm = if (latestLog.target_km > 0) latestLog.target_km else (startKm + intervalKm)

                val effectiveCurrent = maxOf(currentOdometer, startKm)
                val usedKm = maxOf(0, effectiveCurrent - startKm)
                val totalInterval = maxOf(1, targetKm - startKm)
                val remainingKm = targetKm - effectiveCurrent

                val progress = (usedKm.toFloat() / totalInterval.toFloat()).coerceIn(0f, 1f)

                val (statusColor, statusText, badgeLabel) = when {
                    remainingKm <= 0 -> Triple(
                        DustyRoseAccent,
                        "🚨 MELEWATI TARGET! (Lewat ${Formatters.formatNumber(-remainingKm)} KM) Segera ganti $title!",
                        "Overdue"
                    )
                    remainingKm <= 500 -> Triple(
                        Color(0xFFE6A100), // Pastel Yellow/Orange
                        "⚠️ SEGERA GANTI OLI! Sisa ${Formatters.formatNumber(remainingKm)} KM lagi menuju $targetKm KM.",
                        "Segera Ganti"
                    )
                    else -> Triple(
                        SageGreenPrimary,
                        "🌱 Condition Safe: Sisa ${Formatters.formatNumber(remainingKm)} KM lagi (Target: ${Formatters.formatNumber(targetKm)} KM).",
                        "Aman"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target penggantian: ${Formatters.formatNumber(targetKm)} KM (+${Formatters.formatNumber(intervalKm)} KM)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun OilLogItem(
    log: OilLog,
    vehicle: Vehicle?,
    currentOdometer: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, SageGreenPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("oli_log_item_${log.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    vehicle?.let { v ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OilYellowPastelBg
                        ) {
                            Text(
                                text = "${v.nama_kendaraan}${if (v.nomor_plat.isNotBlank()) " • ${v.nomor_plat}" else ""}",
                                color = OilYellowPastelIcon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (log.jenis_oli == "Oli Mesin") OilYellowPastelBg.copy(alpha = 0.5f) else SageGreenPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = log.jenis_oli,
                            color = if (log.jenis_oli == "Oli Mesin") OilYellowPastelIcon else SageGreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Formatters.formatDate(log.tanggal),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Log",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Log",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OilYellowPastelBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = OilYellowPastelIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Diganti: ${Formatters.formatNumber(log.km_motor)} KM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kapasitas: ${log.kapasitas_ml} ml • Target Berikutnya: ${if (log.target_km > 0) Formatters.formatNumber(log.target_km) else Formatters.formatNumber(log.km_motor + log.interval_km)} KM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.garansi_bengkel.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SageGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🛡️ Garansi: ${log.garansi_bengkel}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = Formatters.formatRupiah(log.harga),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Data holder for logs inside dialog
data class PendingOilEntry(
    val jenisOli: String,
    val harga: Int,
    val kapasitasMl: Int,
    val intervalKm: Int
)

@Composable
fun AddOilDialog(
    vehicles: List<Vehicle>,
    defaultVehicleId: Int,
    oilLogs: List<OilLog>,
    onDismiss: () -> Unit,
    onConfirmLogs: (vehicleId: Int, km: Int, logs: List<PendingOilEntry>, customTimestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var tanggalText by remember { mutableStateOf(sdf.format(Date(selectedTimestamp))) }

    val datePicker = remember(context) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(y, m, d, 12, 0, 0)
                selectedTimestamp = cal.timeInMillis
                tanggalText = sdf.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    var selectedVehicleId by remember {
        mutableStateOf(defaultVehicleId.takeIf { id -> vehicles.any { it.id == id } } ?: (vehicles.firstOrNull()?.id ?: 1))
    }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId }
    val isMatic = selectedVehicle?.jenis_kendaraan?.contains("Matic", ignoreCase = true) == true

    val lastOdometerForVehicle = remember(selectedVehicleId, oilLogs) {
        val maxLog = oilLogs.filter { it.vehicle_id == selectedVehicleId }.maxOfOrNull { it.km_motor } ?: 0
        maxOf(selectedVehicle?.current_odometer ?: 0, maxLog)
    }

    var kmInput by remember { mutableStateOf(if (lastOdometerForVehicle > 0) lastOdometerForVehicle.toString() else "") }

    // Toggle for dual recording on Matic
    var includeOliMesin by remember { mutableStateOf(true) }
    var includeOliGardan by remember { mutableStateOf(isMatic) }

    // Engine Oil Form Fields
    var hargaMesinInput by remember { mutableStateOf("55000") }
    var kapMesinInput by remember { mutableStateOf("800") }
    var intervalMesinInput by remember { mutableStateOf("3000") }

    // Gear Oil Form Fields
    var hargaGardanInput by remember { mutableStateOf("25000") }
    var kapGardanInput by remember { mutableStateOf("120") }
    var intervalGardanInput by remember { mutableStateOf("6000") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.OilBarrel,
                    contentDescription = null,
                    tint = OilYellowPastelIcon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Catat Ganti Oli Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tanggal Field
                OutlinedTextField(
                    value = tanggalText,
                    onValueChange = {},
                    label = { Text("Tanggal Transaksi (YYYY-MM-DD)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = OilYellowPastelIcon)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                        .testTag("input_oli_tanggal"),
                    singleLine = true
                )

                Text(
                    text = "Pilih Kendaraan Garasi:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        val vIsMatic = vehicle.jenis_kendaraan.contains("Matic", ignoreCase = true)
                        FilterChip(
                            selected = selectedVehicleId == vehicle.id,
                            onClick = {
                                selectedVehicleId = vehicle.id
                                val vLogMax = oilLogs.filter { it.vehicle_id == vehicle.id }.maxOfOrNull { it.km_motor } ?: 0
                                val vOdoMax = maxOf(vehicle.current_odometer, vLogMax)
                                kmInput = if (vOdoMax > 0) vOdoMax.toString() else ""
                                includeOliGardan = vIsMatic
                            },
                            label = { Text("${vehicle.nama_kendaraan}${if (vIsMatic) " [Matic]" else ""}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = SageGreenPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = kmInput,
                    onValueChange = { kmInput = it; errorMessage = null },
                    label = { Text("Posisi Odometer Saat Ini (KM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_km_oli")
                )

                // Branching Form: Matic vs Bebek/Sport
                if (isMatic) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OilYellowPastelBg.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🛵 Kendaraan Matic - Opsi Pencatatan Ganda:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OilYellowPastelIcon
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = includeOliMesin,
                                    onCheckedChange = { includeOliMesin = it },
                                    colors = CheckboxDefaults.colors(checkedColor = SageGreenPrimary)
                                )
                                Text("Oli Mesin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Checkbox(
                                    checked = includeOliGardan,
                                    onCheckedChange = { includeOliGardan = it },
                                    colors = CheckboxDefaults.colors(checkedColor = SageGreenPrimary)
                                )
                                Text("Oli Gardan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // --- SECTION 1: OLI MESIN ---
                if (includeOliMesin || !isMatic) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🛢️ Details Oli Mesin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = hargaMesinInput,
                                    onValueChange = { hargaMesinInput = it; errorMessage = null },
                                    label = { Text("Harga (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("input_harga_oli_mesin")
                                )
                                OutlinedTextField(
                                    value = kapMesinInput,
                                    onValueChange = { kapMesinInput = it; errorMessage = null },
                                    label = { Text("Kapasitas (ml)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("input_kapasitas_oli_mesin")
                                )
                            }

                            OutlinedTextField(
                                value = intervalMesinInput,
                                onValueChange = { intervalMesinInput = it; errorMessage = null },
                                label = { Text("Interval Penambahan Target (+KM)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_interval_oli_mesin")
                            )

                            val kmVal = kmInput.toIntOrNull() ?: 0
                            val intVal = intervalMesinInput.toIntOrNull() ?: 3000
                            if (kmVal > 0) {
                                Text(
                                    text = "Target Ganti Oli Mesin: ${Formatters.formatNumber(kmVal + intVal)} KM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )
                            }
                        }
                    }
                }

                // --- SECTION 2: OLI GARDAN (Hidden for non-Matic automatically) ---
                if (isMatic && includeOliGardan) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = OilYellowPastelBg.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚙️ Details Oli Gardan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OilYellowPastelIcon
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = hargaGardanInput,
                                    onValueChange = { hargaGardanInput = it; errorMessage = null },
                                    label = { Text("Harga Gardan (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("input_harga_oli_gardan")
                                )
                                OutlinedTextField(
                                    value = kapGardanInput,
                                    onValueChange = { kapGardanInput = it; errorMessage = null },
                                    label = { Text("Kapasitas (ml)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("input_kapasitas_oli_gardan")
                                )
                            }

                            OutlinedTextField(
                                value = intervalGardanInput,
                                onValueChange = { intervalGardanInput = it; errorMessage = null },
                                label = { Text("Interval Penambahan Target (+KM)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_interval_oli_gardan")
                            )

                            val kmVal = kmInput.toIntOrNull() ?: 0
                            val intVal = intervalGardanInput.toIntOrNull() ?: 6000
                            if (kmVal > 0) {
                                Text(
                                    text = "Target Ganti Oli Gardan: ${Formatters.formatNumber(kmVal + intVal)} KM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OilYellowPastelIcon
                                )
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmInput.toIntOrNull()
                    if (km == null || km <= 0) {
                        errorMessage = "Masukkan angka KM odometer yang valid"
                        return@Button
                    }

                    val entries = mutableListOf<PendingOilEntry>()

                    if (includeOliMesin || !isMatic) {
                        val h = hargaMesinInput.toIntOrNull()
                        val kap = kapMesinInput.toIntOrNull()
                        val intval = intervalMesinInput.toIntOrNull() ?: 3000
                        if (h == null || h <= 0) {
                            errorMessage = "Masukkan harga Oli Mesin yang valid"
                            return@Button
                        }
                        if (kap == null || kap <= 0) {
                            errorMessage = "Masukkan kapasitas Oli Mesin (ml)"
                            return@Button
                        }
                        entries.add(PendingOilEntry("Oli Mesin", h, kap, intval))
                    }

                    if (isMatic && includeOliGardan) {
                        val h = hargaGardanInput.toIntOrNull()
                        val kap = kapGardanInput.toIntOrNull()
                        val intval = intervalGardanInput.toIntOrNull() ?: 6000
                        if (h == null || h <= 0) {
                            errorMessage = "Masukkan harga Oli Gardan yang valid"
                            return@Button
                        }
                        if (kap == null || kap <= 0) {
                            errorMessage = "Masukkan kapasitas Oli Gardan (ml)"
                            return@Button
                        }
                        entries.add(PendingOilEntry("Oli Gardan", h, kap, intval))
                    }

                    if (entries.isEmpty()) {
                        errorMessage = "Pilih minimal satu opsi oli untuk dicatat"
                        return@Button
                    }

                    onConfirmLogs(selectedVehicleId, km, entries, selectedTimestamp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DustyRoseAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_oli_button")
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun UpdateOdometerDialog(
    vehicles: List<Vehicle>,
    defaultVehicleId: Int,
    currentOdometer: Int,
    onDismiss: () -> Unit,
    onConfirm: (vehicleId: Int, newKm: Int) -> Unit
) {
    var selectedVehicleId by remember {
        mutableStateOf(defaultVehicleId.takeIf { id -> vehicles.any { it.id == id } } ?: (vehicles.firstOrNull()?.id ?: 1))
    }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId }
    val initialKm = maxOf(selectedVehicle?.current_odometer ?: 0, currentOdometer)

    var kmInput by remember { mutableStateOf(if (initialKm > 0) initialKm.toString() else "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = SageGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Update Odometer Mandiri", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Perbarui posisi kilometer motor Anda kapan saja. Sistem akan langsung memperbarui status sisa target ganti oli.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Pilih Kendaraan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        FilterChip(
                            selected = selectedVehicleId == vehicle.id,
                            onClick = {
                                selectedVehicleId = vehicle.id
                                kmInput = if (vehicle.current_odometer > 0) vehicle.current_odometer.toString() else ""
                            },
                            label = { Text(vehicle.nama_kendaraan) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = SageGreenPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = kmInput,
                    onValueChange = { kmInput = it; errorMessage = null },
                    label = { Text("Posisi Odometer Terkini (KM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_update_odometer")
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmInput.toIntOrNull()
                    if (km == null || km <= 0) {
                        errorMessage = "Masukkan posisi kilometer yang valid"
                        return@Button
                    }
                    onConfirm(selectedVehicleId, km)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_update_odometer_button")
            ) {
                Text("Simpan Update KM", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EditSingleOilDialog(
    log: OilLog,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onConfirm: (OilLog) -> Unit
) {
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var selectedTimestamp by remember(log) { mutableLongStateOf(log.tanggal) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var tanggalText by remember(log) { mutableStateOf(sdf.format(Date(selectedTimestamp))) }

    val datePicker = remember(context) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(y, m, d, 12, 0, 0)
                selectedTimestamp = cal.timeInMillis
                tanggalText = sdf.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    var selectedVehicleId by remember(log) { mutableStateOf(log.vehicle_id) }
    var jenisOli by remember(log) { mutableStateOf(log.jenis_oli) }
    var kmInput by remember(log) { mutableStateOf(log.km_motor.toString()) }
    var hargaInput by remember(log) { mutableStateOf(log.harga.toString()) }
    var kapasitasInput by remember(log) { mutableStateOf(log.kapasitas_ml.toString()) }
    var intervalInput by remember(log) { mutableStateOf(log.interval_km.toString()) }
    var garansiInput by remember(log) { mutableStateOf(log.garansi_bengkel) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.OilBarrel,
                    contentDescription = null,
                    tint = OilYellowPastelIcon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Edit Catatan Ganti Oli", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tanggal Field
                OutlinedTextField(
                    value = tanggalText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal Penggantian") },
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { datePicker.show() }
                )

                // Kendaraan Selector
                if (vehicles.size > 1) {
                    Column {
                        Text("Pilih Kendaraan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(vehicles, key = { it.id }) { v ->
                                FilterChip(
                                    selected = selectedVehicleId == v.id,
                                    onClick = { selectedVehicleId = v.id },
                                    label = { Text(v.nama_kendaraan, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                // Jenis Oli Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = jenisOli == "Oli Mesin",
                        onClick = { jenisOli = "Oli Mesin" },
                        label = { Text("Oli Mesin") }
                    )
                    FilterChip(
                        selected = jenisOli == "Oli Gardan",
                        onClick = { jenisOli = "Oli Gardan" },
                        label = { Text("Oli Gardan") }
                    )
                }

                OutlinedTextField(
                    value = kmInput,
                    onValueChange = { kmInput = it; errorMessage = null },
                    label = { Text("Posisi KM Odometer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hargaInput,
                        onValueChange = { hargaInput = it; errorMessage = null },
                        label = { Text("Harga (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = kapasitasInput,
                        onValueChange = { kapasitasInput = it; errorMessage = null },
                        label = { Text("Kapasitas (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = intervalInput,
                    onValueChange = { intervalInput = it; errorMessage = null },
                    label = { Text("Interval Ganti Berikutnya (KM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = garansiInput,
                    onValueChange = { garansiInput = it },
                    label = { Text("Garansi Bengkel (Opsional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmInput.toIntOrNull()
                    val harga = hargaInput.toIntOrNull()
                    val kap = kapasitasInput.toIntOrNull()
                    val interval = intervalInput.toIntOrNull() ?: 3000

                    if (km == null || km <= 0) {
                        errorMessage = "Masukkan posisi KM yang valid"
                        return@Button
                    }
                    if (harga == null || harga < 0) {
                        errorMessage = "Masukkan harga oli yang valid"
                        return@Button
                    }
                    if (kap == null || kap <= 0) {
                        errorMessage = "Masukkan kapasitas oli dalam ml"
                        return@Button
                    }

                    onConfirm(
                        log.copy(
                            vehicle_id = selectedVehicleId,
                            tanggal = selectedTimestamp,
                            km_motor = km,
                            jenis_oli = jenisOli,
                            harga = harga,
                            kapasitas_ml = kap,
                            interval_km = interval,
                            target_km = km + interval,
                            garansi_bengkel = garansiInput
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
