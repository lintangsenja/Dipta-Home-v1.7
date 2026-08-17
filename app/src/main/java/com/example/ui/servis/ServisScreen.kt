package com.example.ui.servis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Edit
import com.example.ui.common.DeleteConfirmationDialog
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableLongStateOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.entity.ServiceLog
import com.example.data.entity.Vehicle
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.ServisPurplePastelBg
import com.example.ui.theme.ServisPurplePastelIcon
import com.example.ui.util.Formatters
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServisScreen(
    vehicles: List<Vehicle>,
    activeVehicleId: Int,
    serviceLogs: List<ServiceLog>,
    onAddLog: (vehicleId: Int, km: Int, kategori: String, deskripsi: String, totalBiaya: Int, intervalKm: Int, garansiBengkel: String, customTimestamp: Long) -> Unit,
    onUpdateLog: ((ServiceLog) -> Unit)? = null,
    onUpdateOdometer: (vehicleId: Int, newOdometer: Int) -> Unit,
    onDeleteLog: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter vehicle ID: 0 means "Semua Kendaraan"
    var selectedFilterVehicleId by remember { mutableIntStateOf(if (activeVehicleId > 0) activeVehicleId else 0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showOdometerDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<ServiceLog?>(null) }
    var logToDelete by remember { mutableStateOf<ServiceLog?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedKategoriFilter by remember { mutableStateOf("Semua") }

    // Filter logs
    val filteredLogs = remember(serviceLogs, selectedFilterVehicleId, searchQuery, selectedKategoriFilter) {
        serviceLogs.filter { log ->
            val matchesVehicle = selectedFilterVehicleId == 0 || log.vehicle_id == selectedFilterVehicleId
            val matchesSearch = searchQuery.isBlank() ||
                    log.kategori.contains(searchQuery, ignoreCase = true) ||
                    log.deskripsi_item.contains(searchQuery, ignoreCase = true) ||
                    log.garansi_bengkel.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedKategoriFilter == "Semua" || log.kategori.equals(selectedKategoriFilter, ignoreCase = true)
            matchesVehicle && matchesSearch && matchesCategory
        }
    }

    // Active vehicle details
    val activeVehicle = remember(vehicles, selectedFilterVehicleId) {
        if (selectedFilterVehicleId != 0) {
            vehicles.find { it.id == selectedFilterVehicleId }
        } else {
            vehicles.find { it.id == activeVehicleId } ?: vehicles.firstOrNull()
        }
    }

    val totalServiceExpense = remember(filteredLogs) {
        filteredLogs.sumOf { it.total_biaya }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Servis & Perawatan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Perbaikan & Suku Cadang",
                            fontSize = 11.sp,
                            color = ServisPurplePastelIcon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_servis")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = SageGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ServisPurplePastelBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ServisPurplePastelIcon,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_service")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Servis")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catat Servis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Vehicle Selector Filter Bar
                item {
                    Column {
                        Text(
                            text = "Pilih Kendaraan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedFilterVehicleId == 0,
                                    onClick = { selectedFilterVehicleId = 0 },
                                    label = { Text("Semua Kendaraan", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ServisPurplePastelBg,
                                        selectedLabelColor = ServisPurplePastelIcon
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("vehicle_chip_all")
                                )
                            }
                            items(vehicles, key = { it.id }) { vehicle ->
                                val isSelected = vehicle.id == selectedFilterVehicleId
                                val icon = if (vehicle.jenis_kendaraan.equals("Mobil", ignoreCase = true)) {
                                    Icons.Default.DirectionsCar
                                } else {
                                    Icons.Default.TwoWheeler
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilterVehicleId = vehicle.id },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(vehicle.nama_kendaraan, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ServisPurplePastelBg,
                                        selectedLabelColor = ServisPurplePastelIcon
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("vehicle_chip_filter_${vehicle.id}")
                                )
                            }
                        }
                    }
                }

                item {
                    ServiceDonutChartCard(serviceLogs = filteredLogs)
                }

                // 3. Search Bar & Filter Lines Icon
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari rincian / suku cadang / garansi...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_servis_input")
                        )

                        Box {
                            IconButton(
                                onClick = { showFilterMenu = !showFilterMenu },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ServisPurplePastelBg, RoundedCornerShape(14.dp))
                                    .testTag("btn_filter_servis")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = "Filter Servis",
                                    tint = ServisPurplePastelIcon
                                )
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Semua Kategori") },
                                    onClick = { selectedKategoriFilter = "Semua"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Servis Rutin") },
                                    onClick = { selectedKategoriFilter = "Servis Rutin"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Ganti Suku Cadang") },
                                    onClick = { selectedKategoriFilter = "Ganti Suku Cadang"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Perbaikan") },
                                    onClick = { selectedKategoriFilter = "Perbaikan"; showFilterMenu = false }
                                )
                            }
                        }
                    }
                }

                if (selectedKategoriFilter != "Semua") {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { selectedKategoriFilter = "Semua" },
                            label = { Text("Filter: $selectedKategoriFilter ✕", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ServisPurplePastelBg,
                                selectedLabelColor = ServisPurplePastelIcon
                            )
                        )
                    }
                }

                // Section Title: Riwayat Servis
                item {
                    Text(
                        text = "Riwayat Perbaikan & Suku Cadang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                    )
                }

                // 4. Empty State or Log List
                if (filteredLogs.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Belum Ada Catatan Servis",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tekan tombol + Catat Servis untuk menambahkan riwayat servis rutin atau ganti suku cadang.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredLogs, key = { it.id }) { log ->
                        val vehicle = vehicles.find { it.id == log.vehicle_id }
                        val dateFormatted = remember(log.tanggal) {
                            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date(log.tanggal))
                        }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("service_item_${log.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ServisPurplePastelBg
                                        ) {
                                            Text(
                                                text = log.kategori,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ServisPurplePastelIcon,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        if (vehicle != null && selectedFilterVehicleId == 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = vehicle.nama_kendaraan,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { editingLog = log },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Log",
                                                tint = ServisPurplePastelIcon,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = { logToDelete = log },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Hapus Log",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                if (log.deskripsi_item.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = log.deskripsi_item,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (log.garansi_bengkel.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SageGreenPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "🛡️ Garansi Bengkel: ${log.garansi_bengkel}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }



                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Odometer: ${Formatters.formatNumber(log.km_motor)} KM",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SageGreenPrimary
                                        )
                                        Text(
                                            text = dateFormatted,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = Formatters.formatRupiah(log.total_biaya),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- DIALOG: ADD / EDIT SERVICE LOG ---
    if (showAddDialog || editingLog != null) {
        AddServiceLogDialog(
            vehicles = vehicles,
            initialVehicleId = editingLog?.vehicle_id ?: (if (selectedFilterVehicleId != 0) selectedFilterVehicleId else activeVehicleId),
            editingLog = editingLog,
            onDismiss = {
                showAddDialog = false
                editingLog = null
            },
            onConfirm = { vehicleId, km, kategori, deskripsi, biaya, garansi, customTs ->
                if (editingLog != null) {
                    onUpdateLog?.invoke(
                        editingLog!!.copy(
                            vehicle_id = vehicleId,
                            km_motor = km,
                            kategori = kategori,
                            deskripsi_item = deskripsi,
                            total_biaya = biaya,
                            garansi_bengkel = garansi,
                            tanggal = customTs
                        )
                    )
                } else {
                    onAddLog(vehicleId, km, kategori, deskripsi, biaya, 0, garansi, customTs)
                }
                showAddDialog = false
                editingLog = null
            }
        )
    }

    // --- DIALOG: UPDATE ODOMETER ---
    if (showOdometerDialog && activeVehicle != null) {
        UpdateOdometerDialogServis(
            vehicleName = activeVehicle.nama_kendaraan,
            currentKm = activeVehicle.current_odometer,
            onDismiss = { showOdometerDialog = false },
            onConfirm = { newOdometer ->
                onUpdateOdometer(activeVehicle.id, newOdometer)
                showOdometerDialog = false
            }
        )
    }

    // --- DIALOG: DELETE CONFIRMATION ---
    DeleteConfirmationDialog(
        showDialog = logToDelete != null,
        message = "Riwayat servis \"${logToDelete?.kategori}\" sebesar ${Formatters.formatRupiah(logToDelete?.total_biaya ?: 0)} akan dihapus permanen.",
        onDismiss = { logToDelete = null },
        onConfirm = {
            logToDelete?.let { onDeleteLog(it.id) }
            logToDelete = null
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceLogDialog(
    vehicles: List<Vehicle>,
    initialVehicleId: Int,
    editingLog: ServiceLog? = null,
    onDismiss: () -> Unit,
    onConfirm: (vehicleId: Int, km: Int, kategori: String, deskripsi: String, totalBiaya: Int, garansi: String, customTimestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var selectedTimestamp by remember(editingLog) { mutableLongStateOf(editingLog?.tanggal ?: System.currentTimeMillis()) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var tanggalText by remember(editingLog) { mutableStateOf(sdf.format(Date(selectedTimestamp))) }

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

    var selectedVehicleId by remember(editingLog) { mutableIntStateOf(editingLog?.vehicle_id ?: (if (vehicles.any { it.id == initialVehicleId }) initialVehicleId else (vehicles.firstOrNull()?.id ?: 1))) }
    val currentVehicle = vehicles.find { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    var kmText by remember(editingLog, selectedVehicleId) {
        mutableStateOf(editingLog?.km_motor?.toString() ?: currentVehicle?.current_odometer?.takeIf { it > 0 }?.toString() ?: "")
    }

    val categories = listOf("Servis Rutin", "Ganti Suku Cadang", "Perbaikan")
    var selectedCategory by remember(editingLog) { mutableStateOf(editingLog?.kategori ?: categories[0]) }
    var deskripsiText by remember(editingLog) { mutableStateOf(editingLog?.deskripsi_item ?: "") }
    var biayaText by remember(editingLog) { mutableStateOf(editingLog?.total_biaya?.toString() ?: "") }
    var garansiText by remember(editingLog) { mutableStateOf(editingLog?.garansi_bengkel ?: "") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = ServisPurplePastelIcon,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editingLog == null) "Catat Servis / Perawatan" else "Edit Catatan Servis", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tanggal Field
                OutlinedTextField(
                    value = tanggalText,
                    onValueChange = {},
                    label = { Text("Tanggal Transaksi (YYYY-MM-DD)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = ServisPurplePastelIcon)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                        .testTag("input_servis_tanggal"),
                    singleLine = true
                )

                // Vehicle selection row
                Text("Kendaraan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        val isSel = vehicle.id == selectedVehicleId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ServisPurplePastelBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .border(
                                    width = if (isSel) 1.5.dp else 0.dp,
                                    color = if (isSel) ServisPurplePastelIcon else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedVehicleId = vehicle.id }
                        ) {
                            Text(
                                text = vehicle.nama_kendaraan,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) ServisPurplePastelIcon else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // KM Saat Servis
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it.filter { char -> char.isDigit() } },
                    label = { Text("KM Saat Servis (Odometer)") },
                    placeholder = { Text("Contoh: 8500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_km_servis")
                )

                // Category selection chips
                Text("Kategori Servis:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        val isCatSel = category == selectedCategory
                        FilterChip(
                            selected = isCatSel,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ServisPurplePastelBg,
                                selectedLabelColor = ServisPurplePastelIcon
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // EditText for item description
                OutlinedTextField(
                    value = deskripsiText,
                    onValueChange = { deskripsiText = it },
                    label = { Text("Rincian / Suku Cadang") },
                    placeholder = { Text("Contoh: Ganti ban, rantai, gir, dan kampas rem") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_deskripsi_servis")
                )

                // Total Biaya
                OutlinedTextField(
                    value = biayaText,
                    onValueChange = { biayaText = it.filter { char -> char.isDigit() } },
                    label = { Text("Total Biaya (Rp)") },
                    placeholder = { Text("Contoh: 150000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_biaya_servis")
                )

                // Garansi Bengkel (opsional)
                OutlinedTextField(
                    value = garansiText,
                    onValueChange = { garansiText = it },
                    label = { Text("Catatan Garansi Bengkel (Opsional)") },
                    placeholder = { Text("Contoh: 1 Bulan / 1.000 KM") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_garansi_servis")
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmText.toIntOrNull() ?: 0
                    val biaya = biayaText.toIntOrNull() ?: 0
                    if (km <= 0) {
                        errorMsg = "Masukkan KM Odometer yang valid"
                        return@Button
                    }
                    if (biaya < 0) {
                        errorMsg = "Masukkan nominal biaya yang valid"
                        return@Button
                    }
                    onConfirm(selectedVehicleId, km, selectedCategory, deskripsiText, biaya, garansiText, selectedTimestamp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ServisPurplePastelIcon),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_add_service")
            ) {
                Text("Simpan")
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
fun UpdateOdometerDialogServis(
    vehicleName: String,
    currentKm: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var newKmText by remember { mutableStateOf(if (currentKm > 0) currentKm.toString() else "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SageGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Odometer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Perbarui kilometer posisi $vehicleName saat ini:", fontSize = 12.sp)
                OutlinedTextField(
                    value = newKmText,
                    onValueChange = { newKmText = it.filter { char -> char.isDigit() } },
                    label = { Text("KM Odometer Saat Ini") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newKm = newKmText.toIntOrNull() ?: 0
                    if (newKm <= 0) {
                        errorMsg = "KM Odometer tidak valid"
                        return@Button
                    }
                    onConfirm(newKm)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Perbarui")
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
private fun ServiceDonutChartCard(
    serviceLogs: List<ServiceLog>
) {
    var filterMode by remember { mutableStateOf("Bulanan") } // "Bulanan" or "Mingguan"
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }

    val categories = listOf("Servis Rutin", "Ganti Suku Cadang", "Perbaikan", "Lainnya")
    val categoryColors = listOf(
        ServisPurplePastelIcon,
        Color(0xFF00897B),
        Color(0xFFE53935),
        Color(0xFFFB8C00)
    )

    val filteredLogs = remember(serviceLogs, filterMode) {
        val nowMs = System.currentTimeMillis()
        val weekMs = 7 * 24 * 3600 * 1000L
        val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if (filterMode == "Bulanan") {
            serviceLogs
        } else {
            // Mingguan (4 minggu terakhir)
            serviceLogs.filter { log ->
                log.tanggal >= (nowMs - 4 * weekMs)
            }
        }
    }

    val categoryData = remember(filteredLogs) {
        val totalAll = filteredLogs.sumOf { it.total_biaya.toLong() }.coerceAtLeast(1L)
        categories.mapIndexed { idx, catName ->
            val sumCat = filteredLogs.filter {
                if (catName == "Lainnya") {
                    !it.kategori.equals("Servis Rutin", ignoreCase = true) &&
                    !it.kategori.equals("Ganti Suku Cadang", ignoreCase = true) &&
                    !it.kategori.equals("Perbaikan", ignoreCase = true)
                } else {
                    it.kategori.equals(catName, ignoreCase = true)
                }
            }.sumOf { it.total_biaya.toLong() }
            val percentage = (sumCat.toFloat() / totalAll.toFloat()) * 100f
            Triple(catName, sumCat, percentage)
        }
    }

    val totalCost = categoryData.sumOf { it.second }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ServisPurplePastelBg,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = ServisPurplePastelIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Proporsi Biaya Perawatan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (filterMode == "Bulanan") "Kategori servis & suku cadang (Semua)" else "Kategori 4 minggu terakhir",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Filter mode toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ServisPurplePastelBg)
                        .padding(2.dp)
                ) {
                    listOf("Bulanan", "Mingguan").forEach { mode ->
                        val isSel = filterMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) ServisPurplePastelIcon else Color.Transparent)
                                .clickable {
                                    filterMode = mode
                                    selectedCategoryIndex = null
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else ServisPurplePastelIcon
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(115.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeWidth = 18.dp.toPx()
                        var startAngle = -90f

                        if (totalCost == 0L) {
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                            )
                        } else {
                            categoryData.forEachIndexed { idx, item ->
                                val sweepAngle = (item.third / 100f) * 360f
                                if (sweepAngle > 0f) {
                                    drawArc(
                                        color = categoryColors[idx],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = if (selectedCategoryIndex == idx) strokeWidth + 6f else strokeWidth,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Butt
                                        )
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = if (totalCost >= 1_000_000) "${String.format(Locale.getDefault(), "%.1f", totalCost / 1_000_000.0)}Jt"
                            else if (totalCost >= 1_000) "${(totalCost / 1_000)}rb"
                            else "$totalCost",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ServisPurplePastelIcon
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryData.forEachIndexed { idx, (catName, _, pct) ->
                        val isSelected = selectedCategoryIndex == idx
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ServisPurplePastelBg else Color.Transparent)
                                .clickable {
                                    selectedCategoryIndex = if (selectedCategoryIndex == idx) null else idx
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(categoryColors[idx], CircleShape)
                                )
                                Text(
                                    text = catName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${pct.roundToInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColors[idx]
                            )
                        }
                    }
                }
            }

            val activeIdx = selectedCategoryIndex
            if (activeIdx != null) {
                val activeCat = categoryData.getOrNull(activeIdx)
                if (activeCat != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ServisPurplePastelBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total ${activeCat.first}:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Formatters.formatRupiah(activeCat.second),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColors[activeIdx]
                            )
                        }
                    }
                }
            }
        }
    }
}
