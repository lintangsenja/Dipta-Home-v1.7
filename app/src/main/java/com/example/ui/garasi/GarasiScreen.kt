package com.example.ui.garasi

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FuelLog
import com.example.data.entity.OilLog
import com.example.data.entity.ServiceLog
import com.example.data.entity.Vehicle
import com.example.ui.theme.FuelBluePastelIcon
import com.example.ui.theme.OilYellowPastelIcon
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.ServisPurplePastelIcon
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import com.example.ui.util.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarasiScreen(
    vehicles: List<Vehicle>,
    activeVehicleId: Int,
    fuelLogs: List<FuelLog> = emptyList(),
    oilLogs: List<OilLog> = emptyList(),
    serviceLogs: List<ServiceLog> = emptyList(),
    onSelectActiveVehicle: (Int) -> Unit = {},
    onAddVehicle: (nama: String, plat: String, jenis: String, icon: String, pajak: String, sparepart: String) -> Unit,
    onUpdateVehicle: (Vehicle) -> Unit = {},
    onDeleteVehicle: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var deletingVehicle by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Garasi Kendaraan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dashboard & Manajemen Kendaraan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_garasi")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = SageGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddVehicleDialog = true },
                containerColor = SageGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_vehicle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Kendaraan Baru"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tambah Kendaraan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Header Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCreamCanvas),
                    border = BorderStroke(1.2.dp, SageGreenPrimaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SageGreenPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Garage,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daftar Kendaraan Keluarga",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTextDark
                            )
                            Text(
                                text = "Total ${vehicles.size} unit terdaftar di sistem Dipta Home",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (vehicles.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Garage,
                                contentDescription = null,
                                tint = SageGreenPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Belum Ada Kendaraan Terdaftar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tekan tombol '+' di bawah untuk menambahkan kendaraan pertama Anda.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { showAddVehicleDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Kendaraan Sekarang")
                            }
                        }
                    }
                }
            } else {
                items(vehicles, key = { it.id }) { vehicle ->
                    val isActive = vehicle.id == activeVehicleId || (activeVehicleId == 0 && vehicle == vehicles.firstOrNull())
                    val vehicleFuelLogs = fuelLogs.filter { it.vehicle_id == vehicle.id }
                    val vehicleOilLogs = oilLogs.filter { it.vehicle_id == vehicle.id }
                    val vehicleServiceLogs = serviceLogs.filter { it.vehicle_id == vehicle.id }

                    val maxOdometer = maxOf(
                        vehicle.current_odometer,
                        vehicleFuelLogs.maxOfOrNull { it.km_motor } ?: 0,
                        vehicleOilLogs.maxOfOrNull { it.km_motor } ?: 0,
                        vehicleServiceLogs.maxOfOrNull { it.km_motor } ?: 0
                    )

                    VehicleCardItem(
                        vehicle = vehicle,
                        isActive = isActive,
                        maxOdometer = maxOdometer,
                        fuelCount = vehicleFuelLogs.size,
                        serviceCount = vehicleServiceLogs.size,
                        onSelectActive = { onSelectActiveVehicle(vehicle.id) },
                        onEdit = { editingVehicle = vehicle },
                        onDelete = { deletingVehicle = vehicle }
                    )
                }
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        VehicleFormDialog(
            vehicle = null,
            onDismiss = { showAddVehicleDialog = false },
            onSave = { nama, plat, jenis, icon, pajak, sparepart ->
                onAddVehicle(nama, plat, jenis, icon, pajak, sparepart)
                showAddVehicleDialog = false
            }
        )
    }

    // Edit Vehicle Dialog
    editingVehicle?.let { vehicle ->
        VehicleFormDialog(
            vehicle = vehicle,
            onDismiss = { editingVehicle = null },
            onSave = { nama, plat, jenis, icon, pajak, sparepart ->
                onUpdateVehicle(
                    vehicle.copy(
                        nama_kendaraan = nama,
                        nomor_plat = plat,
                        jenis_kendaraan = jenis,
                        icon_type = icon,
                        tanggal_pajak_stnk = pajak,
                        catatan_sparepart = sparepart
                    )
                )
                editingVehicle = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingVehicle?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { deletingVehicle = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Kendaraan?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus kendaraan '${vehicle.nama_kendaraan}' (${vehicle.nomor_plat})?",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Peringatan: Riwayat servis dan pengisian terkait tetap aman di database lokal.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVehicle(vehicle.id)
                        deletingVehicle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_vehicle")
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingVehicle = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun VehicleCardItem(
    vehicle: Vehicle,
    isActive: Boolean,
    maxOdometer: Int,
    fuelCount: Int,
    serviceCount: Int,
    onSelectActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isMobil = vehicle.jenis_kendaraan.equals("Mobil", ignoreCase = true) || vehicle.icon_type.equals("Mobil", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            if (isActive) 1.8.dp else 1.dp,
            if (isActive) SageGreenPrimary else SageGreenPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vehicle_card_${vehicle.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Icon, Name, Plat, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) SageGreenPrimaryContainer else SoftCreamCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMobil) Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = if (isActive) SageGreenPrimary else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = vehicle.nama_kendaraan,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SageGreenPrimary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Aktif",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF263238),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = vehicle.nomor_plat.ifBlank { "PLAT --" },
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = vehicle.jenis_kendaraan,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit & Delete Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_edit_vehicle_${vehicle.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kendaraan",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_vehicle_${vehicle.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Kendaraan",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = SoftCreamCanvas, thickness = 1.dp)

            // Metrics / Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Odometer Terkini", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${Formatters.formatNumber(maxOdometer)} km",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTextDark
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = FuelBluePastelIcon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fuelCount Isi BBM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = ServisPurplePastelIcon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$serviceCount Servis",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Optional Info: Pajak STNK & Catatan
            if (vehicle.tanggal_pajak_stnk.isNotBlank() || vehicle.catatan_sparepart.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftCreamCanvas.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (vehicle.tanggal_pajak_stnk.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jatuh Tempo Pajak STNK: ${vehicle.tanggal_pajak_stnk}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftTextDark
                                )
                            }
                        }

                        if (vehicle.catatan_sparepart.isNotBlank()) {
                            Text(
                                text = "Catatan: ${vehicle.catatan_sparepart}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Button to set as active vehicle if not active
            if (!isActive) {
                OutlinedButton(
                    onClick = onSelectActive,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary),
                    border = BorderStroke(1.dp, SageGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_select_active_${vehicle.id}"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pilih Sebagai Kendaraan Aktif",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleFormDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (nama: String, plat: String, jenis: String, icon: String, pajak: String, sparepart: String) -> Unit
) {
    val isEdit = vehicle != null
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val cal = remember { Calendar.getInstance() }

    var nama by remember { mutableStateOf(vehicle?.nama_kendaraan ?: "") }
    var plat by remember { mutableStateOf(vehicle?.nomor_plat ?: "") }
    var jenis by remember { mutableStateOf(vehicle?.jenis_kendaraan ?: "Motor") }
    var iconType by remember { mutableStateOf(vehicle?.icon_type ?: "Motor") }
    var tanggalPajak by remember { mutableStateOf(vehicle?.tanggal_pajak_stnk ?: "") }
    var catatanSparepart by remember { mutableStateOf(vehicle?.catatan_sparepart ?: "") }

    var errorNama by remember { mutableStateOf(false) }

    LaunchedEffect(vehicle) {
        if (vehicle != null && vehicle.tanggal_pajak_stnk.isNotBlank()) {
            try {
                val parsed = sdf.parse(vehicle.tanggal_pajak_stnk)
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) {}
        }
    }

    val datePicker = remember(context) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                tanggalPajak = sdf.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val vehicleTypes = listOf("Motor", "Mobil", "Motor Matic", "Motor Bebek", "Mobil MPV", "Mobil SUV")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Garage,
                    contentDescription = null,
                    tint = SageGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Ubah Data Kendaraan" else "Tambah Kendaraan Baru")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = nama,
                        onValueChange = {
                            nama = it
                            errorNama = false
                        },
                        label = { Text("Nama Kendaraan *") },
                        placeholder = { Text("Contoh: Supra GTR, Avanza") },
                        isError = errorNama,
                        supportingText = if (errorNama) {
                            { Text("Nama kendaraan wajib diisi", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vehicle_nama")
                    )
                }

                item {
                    OutlinedTextField(
                        value = plat,
                        onValueChange = { plat = it.uppercase() },
                        label = { Text("Nomor Plat Polisi") },
                        placeholder = { Text("Contoh: B 1234 XYZ") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vehicle_plat")
                    )
                }

                item {
                    Text("Tipe Kendaraan:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = iconType == "Motor",
                            onClick = {
                                iconType = "Motor"
                                jenis = "Motor"
                            },
                            label = { Text("Sepeda Motor", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = iconType == "Mobil",
                            onClick = {
                                iconType = "Mobil"
                                jenis = "Mobil"
                            },
                            label = { Text("Mobil", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() }
                    ) {
                        OutlinedTextField(
                            value = tanggalPajak,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jatuh Tempo Pajak STNK (Opsional)") },
                            placeholder = { Text("Pilih tanggal pajak") },
                            trailingIcon = {
                                IconButton(onClick = { datePicker.show() }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Pilih Tanggal Pajak",
                                        tint = SageGreenPrimary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_vehicle_pajak")
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { datePicker.show() }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = catatanSparepart,
                        onValueChange = { catatanSparepart = it },
                        label = { Text("Catatan / Riwayat Sparepart (Opsional)") },
                        placeholder = { Text("Misal: Aki baru diganti Jan 2025") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vehicle_catatan")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isBlank()) {
                        errorNama = true
                        return@Button
                    }
                    onSave(
                        nama.trim(),
                        plat.trim(),
                        jenis,
                        iconType,
                        tanggalPajak,
                        catatanSparepart.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                modifier = Modifier.testTag("btn_save_vehicle")
            ) {
                Text(if (isEdit) "Simpan Perubahan" else "Tambah Kendaraan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
