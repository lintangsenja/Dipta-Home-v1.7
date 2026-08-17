package com.example.ui.bensin

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import com.example.ui.common.DeleteConfirmationDialog
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.TrendingUp
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
import kotlin.math.roundToInt
import com.example.data.entity.FuelLog
import com.example.data.entity.Vehicle
import com.example.ui.theme.DustyRoseAccent
import com.example.ui.theme.FuelBluePastelBg
import com.example.ui.theme.FuelBluePastelIcon
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BensinScreen(
    vehicles: List<Vehicle>,
    activeVehicleId: Int,
    fuelLogs: List<FuelLog>,
    onAddLog: (vehicleId: Int, kmMotor: Int, nominal: Int, liter: Float, jenisBbm: String, hargaPerLiter: Int, customTimestamp: Long) -> Unit,
    onUpdateLog: ((FuelLog) -> Unit)? = null,
    onDeleteLog: (id: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<FuelLog?>(null) }
    var deleteCandidateLog by remember { mutableStateOf<FuelLog?>(null) }
    var selectedVehicleFilter by remember { mutableStateOf<Int?>(null) } // null = Semua

    val filteredLogs = if (selectedVehicleFilter != null) {
        fuelLogs.filter { it.vehicle_id == selectedVehicleFilter }
    } else {
        fuelLogs
    }

    val totalNominal = filteredLogs.sumOf { it.nominal }
    val totalLiter = filteredLogs.sumOf { it.liter.toDouble() }.toFloat()
    val totalJarak = filteredLogs.sumOf { it.jarak_tempuh }
    val avgKmPerLiter = if (totalLiter > 0 && totalJarak > 0) totalJarak / totalLiter else 0f
    val lastKm = filteredLogs.firstOrNull()?.km_motor ?: 0

    // Check if any log in the current filter shows boros
    val hasBorosLog = filteredLogs.any { log ->
        log.is_boros || (log.km_per_liter > 0f && avgKmPerLiter > 0f && log.km_per_liter < avgKmPerLiter * 0.82f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catat Bensin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Modul Bahan Bakar & Efisiensi",
                            fontSize = 12.sp,
                            color = FuelBluePastelIcon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("bensin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Menu Utama"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FuelBluePastelBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = DustyRoseAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_bensin_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Bensin")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Catat Bensin", fontWeight = FontWeight.Bold)
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
                            text = "Filter Kendaraan Garasi:",
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
                                    label = { Text("Semua (${fuelLogs.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                            items(vehicles, key = { it.id }) { vehicle ->
                                val count = fuelLogs.count { it.vehicle_id == vehicle.id }
                                FilterChip(
                                    selected = selectedVehicleFilter == vehicle.id,
                                    onClick = { selectedVehicleFilter = vehicle.id },
                                    label = { Text("${vehicle.nama_kendaraan} ($count)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    FuelComboChartCard(fuelLogs = filteredLogs)
                }

                // Warning Indicator Banner if Fuel Efficiency is Boros
                if (hasBorosLog) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DustyRoseAccent.copy(alpha = 0.12f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("warning_boros_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DustyRoseAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚠️ Konsumsi BBM Boros Terdeteksi",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Terdapat pengisian bensin dengan efisiensi di bawah standar rata-rata. Cek tekanan ban atau lakukan servis mesin berkala.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Pengisian Bensin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredLogs.size} Transaksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 3. List Items or Empty State
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
                                .testTag("bensin_empty_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = FuelBluePastelIcon.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum Ada Catatan Bensin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tekan tombol + Catat Bensin untuk memasukkan odometer (KM) dan jumlah liter bensin.",
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
                        val vehicleLogs = fuelLogs.filter { it.vehicle_id == log.vehicle_id && it.km_per_liter > 0f }
                        val vehicleAvg = if (vehicleLogs.isNotEmpty()) vehicleLogs.map { it.km_per_liter }.average().toFloat() else 0f

                        FuelLogItem(
                            log = log,
                            vehicle = vehicle,
                            overallAvgKmPerLiter = vehicleAvg,
                            onEdit = { editingLog = log },
                            onDelete = { deleteCandidateLog = log }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Input Dialog (Add or Edit)
    if (showDialog || editingLog != null) {
        AddFuelDialog(
            vehicles = vehicles,
            defaultVehicleId = selectedVehicleFilter ?: activeVehicleId,
            fuelLogs = fuelLogs,
            editingLog = editingLog,
            onDismiss = {
                showDialog = false
                editingLog = null
            },
            onConfirm = { vehicleId, km, nominal, liter, jenisBbm, hargaPerLiter, customTs ->
                if (editingLog != null) {
                    onUpdateLog?.invoke(
                        editingLog!!.copy(
                            vehicle_id = vehicleId,
                            km_motor = km,
                            nominal = nominal,
                            liter = liter,
                            jenis_bbm = jenisBbm,
                            harga_per_liter = hargaPerLiter,
                            tanggal = customTs
                        )
                    )
                } else {
                    onAddLog(vehicleId, km, nominal, liter, jenisBbm, hargaPerLiter, customTs)
                }
                showDialog = false
                editingLog = null
            }
        )
    }

    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = deleteCandidateLog != null,
        message = "Apakah Anda yakin ingin menghapus data pengisian bensin sebesar ${Formatters.formatRupiah(deleteCandidateLog?.nominal ?: 0)}?",
        onDismiss = { deleteCandidateLog = null },
        onConfirm = {
            deleteCandidateLog?.let { onDeleteLog(it.id) }
            deleteCandidateLog = null
        }
    )
}

@Composable
fun MetricItem(label: String, value: String) {
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
fun FuelLogItem(
    log: FuelLog,
    vehicle: Vehicle?,
    overallAvgKmPerLiter: Float,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBoros = log.is_boros || (log.km_per_liter > 0f && overallAvgKmPerLiter > 0f && log.km_per_liter < overallAvgKmPerLiter * 0.82f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, SageGreenPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bensin_log_item_${log.id}")
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
                        val vehicleIcon = if (v.jenis_kendaraan.equals("Mobil", ignoreCase = true)) {
                            Icons.Default.DirectionsCar
                        } else {
                            Icons.Default.TwoWheeler
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FuelBluePastelBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = vehicleIcon,
                                    contentDescription = null,
                                    tint = FuelBluePastelIcon,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${v.nama_kendaraan}${if (v.nomor_plat.isNotBlank()) " • ${v.nomor_plat}" else ""}",
                                    color = FuelBluePastelIcon,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SageGreenPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = log.jenis_bbm,
                            color = SageGreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = Formatters.formatDate(log.tanggal),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.width(4.dp))

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
                        .background(FuelBluePastelBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = FuelBluePastelIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Odometer: ${Formatters.formatNumber(log.km_motor)} km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val hargaPerLiterText = if (log.harga_per_liter > 0) " @ ${Formatters.formatRupiah(log.harga_per_liter.toDouble())}/L" else ""
                    Text(
                        text = "${Formatters.formatRupiah(log.nominal)} • ${String.format("%.1f", log.liter)} Liter$hargaPerLiterText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (log.jarak_tempuh > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SageGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "+${Formatters.formatNumber(log.jarak_tempuh)} km",
                                color = SageGreenPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%.1f", log.km_per_liter)} km/L",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBoros) MaterialTheme.colorScheme.error else SageGreenPrimary
                        )
                    } else {
                        Text(
                            text = "Catatan Awal",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Efficiency Status Badge
            if (log.jarak_tempuh > 0 && log.km_per_liter > 0f) {
                Spacer(modifier = Modifier.height(10.dp))
                if (isBoros) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DustyRoseAccent.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = DustyRoseAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚠️ Konsumsi BBM Boros (Efisiensi Turun)",
                                color = DustyRoseAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SageGreenPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌱 Efisiensi BBM Normal (${String.format("%.1f", log.km_per_liter)} km/L)",
                                color = SageGreenPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFuelDialog(
    vehicles: List<Vehicle>,
    defaultVehicleId: Int,
    fuelLogs: List<FuelLog>,
    editingLog: FuelLog? = null,
    onDismiss: () -> Unit,
    onConfirm: (vehicleId: Int, km: Int, nominal: Int, liter: Float, jenisBbm: String, hargaPerLiter: Int, customTimestamp: Long) -> Unit
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

    var selectedVehicleId by remember(editingLog) { mutableStateOf(editingLog?.vehicle_id ?: (defaultVehicleId.takeIf { id -> vehicles.any { it.id == id } } ?: (vehicles.firstOrNull()?.id ?: 1))) }

    val fuelPresets = remember {
        listOf(
            "Pertalite" to 10000,
            "Pertamax" to 12100,
            "Pertamax Turbo" to 13200,
            "Solar" to 6800,
            "Dexlite" to 14500,
            "Pertamina Dex" to 15100
        )
    }

    var selectedJenisBbm by remember(editingLog) { mutableStateOf(editingLog?.jenis_bbm ?: "Pertalite") }
    var hargaPerLiterInput by remember(editingLog) { mutableStateOf(editingLog?.harga_per_liter?.toString() ?: "10000") }
    var literInput by remember(editingLog) { mutableStateOf(editingLog?.liter?.toString() ?: "5.0") }
    var nominalInput by remember(editingLog) { mutableStateOf(editingLog?.nominal?.toString() ?: "50000") }

    val vehicleLogs = remember(selectedVehicleId, fuelLogs) {
        fuelLogs.filter { it.vehicle_id == selectedVehicleId }
    }

    val lastOdometerForVehicle = remember(vehicleLogs) {
        vehicleLogs.maxOfOrNull { it.km_motor } ?: 0
    }

    val vehicleHistoricalAvg = remember(vehicleLogs) {
        val valid = vehicleLogs.filter { it.km_per_liter > 0f }
        if (valid.isNotEmpty()) valid.map { it.km_per_liter }.average().toFloat() else 0f
    }

    var kmInput by remember(editingLog) { mutableStateOf(editingLog?.km_motor?.toString() ?: if (lastOdometerForVehicle > 0) (lastOdometerForVehicle + 100).toString() else "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live Efficiency Estimation
    val kmInt = kmInput.toIntOrNull() ?: 0
    val literFloat = literInput.toFloatOrNull() ?: 0f
    val estJarak = if (kmInt > lastOdometerForVehicle && lastOdometerForVehicle > 0) kmInt - lastOdometerForVehicle else 0
    val estKmLiter = if (estJarak > 0 && literFloat > 0f) estJarak / literFloat else 0f

    val isEstBoros = if (estKmLiter > 0f) {
        if (vehicleHistoricalAvg > 0f) {
            estKmLiter < (vehicleHistoricalAvg * 0.82f)
        } else {
            val selectedV = vehicles.find { it.id == selectedVehicleId }
            val isMobil = selectedV?.jenis_kendaraan.equals("Mobil", ignoreCase = true)
            if (isMobil) estKmLiter < 8.5f else estKmLiter < 28f
        }
    } else false

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = FuelBluePastelIcon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (editingLog == null) "Catat Bensin Baru" else "Edit Pengisian Bensin", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = FuelBluePastelIcon)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                        .testTag("input_bensin_tanggal"),
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
                        FilterChip(
                            selected = selectedVehicleId == vehicle.id,
                            onClick = {
                                selectedVehicleId = vehicle.id
                                val newLastKm = fuelLogs.filter { it.vehicle_id == vehicle.id }.maxOfOrNull { it.km_motor } ?: 0
                                kmInput = if (newLastKm > 0) (newLastKm + 100).toString() else ""
                            },
                            label = { Text(vehicle.nama_kendaraan) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = SageGreenPrimary
                            )
                        )
                    }
                }

                if (lastOdometerForVehicle > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Odometer Terakhir:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${Formatters.formatNumber(lastOdometerForVehicle)} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = kmInput,
                    onValueChange = { kmInput = it; errorMessage = null },
                    label = { Text("Posisi KM Odometer Saat Ini") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_km_motor")
                )

                // 1. Pilihan Jenis BBM (Chips)
                Column {
                    Text(
                        text = "Jenis Bahan Bakar (BBM):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(fuelPresets, key = { it.first }) { (jenis, hargaPreset) ->
                            FilterChip(
                                selected = selectedJenisBbm == jenis,
                                onClick = {
                                    selectedJenisBbm = jenis
                                    hargaPerLiterInput = hargaPreset.toString()
                                    val l = literInput.toFloatOrNull() ?: 0f
                                    if (l > 0f) {
                                        nominalInput = (l * hargaPreset).roundToInt().toString()
                                    }
                                    errorMessage = null
                                },
                                label = { Text("$jenis (${Formatters.formatNumber(hargaPreset)})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FuelBluePastelIcon.copy(alpha = 0.2f),
                                    selectedLabelColor = FuelBluePastelIcon
                                )
                            )
                        }
                    }
                }

                // 2. Harga per Liter Input (Editable)
                OutlinedTextField(
                    value = hargaPerLiterInput,
                    onValueChange = { newHargaStr ->
                        hargaPerLiterInput = newHargaStr
                        errorMessage = null
                        val hargaVal = newHargaStr.toIntOrNull() ?: 0
                        val literVal = literInput.toFloatOrNull() ?: 0f
                        if (hargaVal > 0 && literVal > 0f) {
                            nominalInput = (literVal * hargaVal).roundToInt().toString()
                        }
                    },
                    label = { Text("Harga per Liter (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_harga_per_liter")
                )

                // 3. Jumlah Liter Bensin (L) Input (Editable)
                OutlinedTextField(
                    value = literInput,
                    onValueChange = { newLiterStr ->
                        literInput = newLiterStr
                        errorMessage = null
                        val literVal = newLiterStr.toFloatOrNull() ?: 0f
                        val hargaVal = hargaPerLiterInput.toIntOrNull() ?: 0
                        if (literVal > 0f && hargaVal > 0) {
                            nominalInput = (literVal * hargaVal).roundToInt().toString()
                        }
                    },
                    label = { Text("Jumlah Liter Bensin (L)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_liter_bensin")
                )

                // 4. Total Biaya Pengeluaran (Rp) Input (Calculated / Editable for manual override)
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { newNominalStr ->
                        nominalInput = newNominalStr
                        errorMessage = null
                        val nominalVal = newNominalStr.toIntOrNull() ?: 0
                        val hargaVal = hargaPerLiterInput.toIntOrNull() ?: 0
                        if (nominalVal > 0 && hargaVal > 0) {
                            val l = nominalVal.toFloat() / hargaVal
                            literInput = String.format(Locale.US, "%.2f", l)
                        }
                    },
                    label = { Text("Total Biaya Pengeluaran (Rp)") },
                    supportingText = {
                        Text(
                            text = "Dihitung otomatis (${literInput}L × Rp${hargaPerLiterInput}). Anda tetap bisa mengubah total biaya manual jika perlu.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nominal_bensin")
                )

                // Live Efficiency Calculation Preview
                if (estKmLiter > 0f) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isEstBoros) DustyRoseAccent.copy(alpha = 0.15f) else SageGreenPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isEstBoros) "⚠️ Estimasi Konsumsi BBM Boros!" else "🌱 Estimasi Efisiensi Baik",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEstBoros) DustyRoseAccent else SageGreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Jarak: +${Formatters.formatNumber(estJarak)} km • ${String.format("%.1f", estKmLiter)} km/L",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                    val nominal = nominalInput.toIntOrNull()
                    val liter = literInput.toFloatOrNull()
                    val hargaPerLiter = hargaPerLiterInput.toIntOrNull() ?: 0

                    if (km == null || km <= 0) {
                        errorMessage = "Masukkan angka KM kendaraan yang valid"
                        return@Button
                    }
                    if (nominal == null || nominal <= 0) {
                        errorMessage = "Masukkan total biaya bensin yang valid"
                        return@Button
                    }
                    if (liter == null || liter <= 0f) {
                        errorMessage = "Masukkan jumlah liter bensin yang valid"
                        return@Button
                    }

                    onConfirm(selectedVehicleId, km, nominal, liter, selectedJenisBbm, hargaPerLiter, selectedTimestamp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DustyRoseAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_bensin_button")
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
private fun FuelComboChartCard(
    fuelLogs: List<FuelLog>
) {
    var filterMode by remember { mutableStateOf("Bulanan") } // "Bulanan" or "Mingguan"
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val chartData = remember(fuelLogs, filterMode) {
        if (filterMode == "Bulanan") {
            val sdfMonth = SimpleDateFormat("MMM", Locale("id", "ID"))
            val sdfKey = SimpleDateFormat("yyyy-MM", Locale.getDefault())

            val monthsList = mutableListOf<Triple<String, String, Long>>()
            for (i in 5 downTo 0) {
                val c = Calendar.getInstance()
                c.add(Calendar.MONTH, -i)
                val monthLabel = sdfMonth.format(c.time)
                val key = sdfKey.format(c.time)
                monthsList.add(Triple(monthLabel, key, c.timeInMillis))
            }

            monthsList.map { (label, key, _) ->
                val logsInMonth = fuelLogs.filter { log ->
                    val logDate = Date(log.tanggal)
                    sdfKey.format(logDate) == key
                }
                val totalLiters = logsInMonth.sumOf { it.liter.toDouble() }.toFloat()
                val totalNominal = logsInMonth.sumOf { it.nominal.toLong() }
                Triple(label, totalLiters, totalNominal)
            }
        } else {
            // Mingguan (4 Minggu Terakhir)
            val weekLabels = listOf("M-3", "M-2", "M-1", "Minggu Ini")
            val nowMs = System.currentTimeMillis()
            val weekMs = 7 * 24 * 3600 * 1000L

            (3 downTo 0).mapIndexed { idx, weekOffset ->
                val endMs = nowMs - (weekOffset * weekMs)
                val startMs = endMs - weekMs
                val logsInWeek = fuelLogs.filter { log ->
                    log.tanggal in startMs..endMs
                }
                val totalLiters = logsInWeek.sumOf { it.liter.toDouble() }.toFloat()
                val totalNominal = logsInWeek.sumOf { it.nominal.toLong() }
                Triple(weekLabels[idx], totalLiters, totalNominal)
            }
        }
    }

    val maxLiters = (chartData.map { it.second }.maxOrNull() ?: 10f).coerceAtLeast(5f)
    val maxNominal = (chartData.map { it.third.toFloat() }.maxOrNull() ?: 50000f).coerceAtLeast(10000f)

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
                        color = FuelBluePastelBg,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = FuelBluePastelIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Grafik Bensin (Volume & Biaya)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (filterMode == "Bulanan") "Tren 6 bulan terakhir" else "Perbandingan 4 minggu terakhir",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Filter mode toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(FuelBluePastelBg)
                        .padding(2.dp)
                ) {
                    listOf("Bulanan", "Mingguan").forEach { mode ->
                        val isSel = filterMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) FuelBluePastelIcon else Color.Transparent)
                                .clickable {
                                    filterMode = mode
                                    selectedIndex = null
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else FuelBluePastelIcon
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(FuelBluePastelIcon.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Liter (Vol)", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(DustyRoseAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Biaya (Rp)", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp, top = 12.dp, start = 8.dp, end = 8.dp)
                        .pointerInput(chartData) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (chartData.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, chartData.size - 1)
                                selectedIndex = if (selectedIndex == index) null else index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (chartData.size - 1).coerceAtLeast(1)

                    val gridColor = Color.LightGray.copy(alpha = 0.3f)
                    for (i in 0..2) {
                        val y = height * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }

                    val barWidthPx = 18.dp.toPx()
                    chartData.forEachIndexed { idx, item ->
                        val x = idx * stepX
                        val ratioLiter = (item.second / maxLiters).coerceIn(0f, 1f)
                        val barHeightPx = ratioLiter * height
                        val isSelected = selectedIndex == idx

                        drawRoundRect(
                            color = if (isSelected) FuelBluePastelIcon else FuelBluePastelIcon.copy(alpha = 0.5f),
                            topLeft = androidx.compose.ui.geometry.Offset(x - barWidthPx / 2f, height - barHeightPx),
                            size = androidx.compose.ui.geometry.Size(barWidthPx, barHeightPx.coerceAtLeast(4f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    val linePoints = chartData.mapIndexed { idx, item ->
                        val x = idx * stepX
                        val ratioCost = (item.third.toFloat() / maxNominal).coerceIn(0f, 1f)
                        val y = height - (ratioCost * height)
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    if (linePoints.isNotEmpty()) {
                        val linePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(linePoints.first().x, linePoints.first().y)
                            for (i in 0 until linePoints.size - 1) {
                                val p1 = linePoints[i]
                                val p2 = linePoints[i + 1]
                                val cx1 = p1.x + (p2.x - p1.x) / 2f
                                val cy1 = p1.y
                                val cx2 = p1.x + (p2.x - p1.x) / 2f
                                val cy2 = p2.y
                                cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
                            }
                        }

                        drawPath(
                            path = linePath,
                            color = DustyRoseAccent,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )

                        linePoints.forEachIndexed { idx, pt ->
                            val isSel = selectedIndex == idx
                            drawCircle(
                                color = Color.White,
                                radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = DustyRoseAccent,
                                radius = if (isSel) 4.5.dp.toPx() else 2.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    chartData.forEachIndexed { idx, item ->
                        val isSel = selectedIndex == idx
                        Text(
                            text = item.first,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) FuelBluePastelIcon else Color.Gray
                        )
                    }
                }
            }

            val activeIdx = selectedIndex ?: (chartData.size - 1)
            val activeItem = chartData.getOrNull(activeIdx)
            if (activeItem != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FuelBluePastelBg,
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
                            text = "Pengisian ${activeItem.first}:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", activeItem.second)} Liter  |  ${Formatters.formatRupiah(activeItem.third.toDouble())}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuelBluePastelIcon
                        )
                    }
                }
            }
        }
    }
}
