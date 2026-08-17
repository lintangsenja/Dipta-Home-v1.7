package com.example.ui.listrik

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import com.example.ui.common.DeleteConfirmationDialog
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableLongStateOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.TrendingUp
import kotlin.math.roundToInt
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
import com.example.data.entity.ElectricityLog
import com.example.ui.theme.DustyRoseAccent
import com.example.ui.theme.ElectricityOrangePastelBg
import com.example.ui.theme.ElectricityOrangePastelIcon
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListrikScreen(
    electricityLogs: List<ElectricityLog>,
    onAddLog: (harga: Int, jumlahKwh: Float, isInitial: Boolean, customTimestamp: Long) -> Unit,
    onUpdateLog: ((ElectricityLog) -> Unit)? = null,
    onDeleteLog: (id: Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<ElectricityLog?>(null) }
    var deleteCandidateLog by remember { mutableStateOf<ElectricityLog?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf("Semua") } // "Semua", "Token", "Inisialisasi"

    val isFirstLog = electricityLogs.isEmpty()
    val latestLog = electricityLogs.firstOrNull()
    val totalHarga = electricityLogs.sumOf { it.harga }
    val totalKwhAktif = latestLog?.let {
        if (it.total_kwh_aktif > 0f) it.total_kwh_aktif else it.jumlah_kwh
    } ?: 0f

    val validLogs = electricityLogs.filter { it.kwh_per_hari > 0f }
    val avgKwhPerHari = if (validLogs.isNotEmpty()) {
        validLogs.map { it.kwh_per_hari }.average().toFloat()
    } else 0f

    val estimatedDaysRemaining = if (avgKwhPerHari > 0f && totalKwhAktif > 0f) {
        (totalKwhAktif / avgKwhPerHari).toInt()
    } else 0

    val filteredLogs = remember(electricityLogs, searchQuery, selectedFilterType) {
        electricityLogs.filter { log ->
            val matchesSearch = searchQuery.isBlank() ||
                    log.harga.toString().contains(searchQuery) ||
                    log.jumlah_kwh.toString().contains(searchQuery)
            val matchesFilter = when (selectedFilterType) {
                "Token" -> !log.is_initial
                "Inisialisasi" -> log.is_initial
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val hasBorosLog = electricityLogs.any { it.is_boros }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catat kWh Listrik",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Akurasi Sisa kWh & Monitoring Pemakaian",
                            fontSize = 12.sp,
                            color = ElectricityOrangePastelIcon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("listrik_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Menu Utama"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ElectricityOrangePastelBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = DustyRoseAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_listrik_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Catat kWh Listrik")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFirstLog) "Catat Sisa Meteran" else "Catat Token Listrik",
                        fontWeight = FontWeight.Bold
                    )
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

                item {
                    ElectricityTrendLineChartCard(electricityLogs = electricityLogs)
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
                            placeholder = { Text("Cari catatan listrik...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_listrik_input")
                        )

                        Box {
                            IconButton(
                                onClick = { showFilterMenu = !showFilterMenu },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ElectricityOrangePastelBg, RoundedCornerShape(14.dp))
                                    .testTag("btn_filter_listrik")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = "Filter Listrik",
                                    tint = ElectricityOrangePastelIcon
                                )
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Semua Catatan") },
                                    onClick = { selectedFilterType = "Semua"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Pembelian Token") },
                                    onClick = { selectedFilterType = "Token"; showFilterMenu = false }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Inisialisasi Meteran") },
                                    onClick = { selectedFilterType = "Inisialisasi"; showFilterMenu = false }
                                )
                            }
                        }
                    }
                }

                // Active Filter Indicator Chip
                if (selectedFilterType != "Semua") {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { selectedFilterType = "Semua" },
                            label = { Text("Filter: $selectedFilterType ✕", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricityOrangePastelBg,
                                selectedLabelColor = ElectricityOrangePastelIcon
                            )
                        )
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
                            text = "Riwayat Catatan Listrik",
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
                                .padding(vertical = 12.dp)
                                .testTag("listrik_empty_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = ElectricityOrangePastelIcon.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Mulai dengan Catat Sisa Meteran",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Untuk pertama kali, masukkan sisa kWh di meteran Anda saat ini. Pada pembelian berikutnya, sisa kWh akan otomatis terakumulasi dengan kWh baru dari struk PLN.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredLogs, key = { it.id }) { log ->
                        ElectricityLogItem(
                            log = log,
                            onEdit = { editingLog = log },
                            onDelete = { deleteCandidateLog = log }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Input / Edit Dialog
    if (showDialog || editingLog != null) {
        AddElectricityDialog(
            isFirstLog = editingLog?.is_initial ?: isFirstLog,
            previousTotalKwh = totalKwhAktif,
            editingLog = editingLog,
            onDismiss = {
                showDialog = false
                editingLog = null
            },
            onConfirm = { harga, kwh, isInitial, customTs ->
                if (editingLog != null) {
                    onUpdateLog?.invoke(
                        editingLog!!.copy(
                            harga = harga,
                            jumlah_kwh = kwh,
                            is_initial = isInitial,
                            tanggal = customTs
                        )
                    )
                } else {
                    onAddLog(harga, kwh, isInitial, customTs)
                }
                showDialog = false
                editingLog = null
            }
        )
    }

    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = deleteCandidateLog != null,
        message = "Apakah Anda yakin ingin menghapus catatan listrik ini (${if (deleteCandidateLog?.is_initial == true) "Meteran Awal" else Formatters.formatRupiah(deleteCandidateLog?.harga ?: 0)})?",
        onDismiss = { deleteCandidateLog = null },
        onConfirm = {
            deleteCandidateLog?.let { onDeleteLog(it.id) }
            deleteCandidateLog = null
        }
    )
}

@Composable
fun ElectricityMetric(label: String, value: String) {
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
fun ElectricityLogItem(
    log: ElectricityLog,
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
            .testTag("listrik_log_item_${log.id}")
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
                    Text(
                        text = Formatters.formatDate(log.tanggal),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.is_initial) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SageGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Pencatatan Pertama",
                                color = SageGreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Log",
                            tint = ElectricityOrangePastelIcon,
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (log.is_boros) DustyRoseAccent.copy(alpha = 0.15f) else ElectricityOrangePastelBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (log.is_boros) Icons.Default.Warning else Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = if (log.is_boros) DustyRoseAccent else ElectricityOrangePastelIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (log.is_initial && log.harga == 0) "Sisa Awal Meteran" else Formatters.formatRupiah(log.harga),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (log.is_initial) {
                            "${String.format("%.1f", log.jumlah_kwh)} kWh (Meteran)"
                        } else {
                            "+${String.format("%.1f", log.jumlah_kwh)} kWh (Struk)" +
                                    if (log.sisa_sebelumnya > 0) " • Sisa: ${String.format("%.1f", log.sisa_sebelumnya)} kWh" else ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (log.is_boros) DustyRoseAccent.copy(alpha = 0.15f) else ElectricityOrangePastelBg
                    ) {
                        Text(
                            text = if (log.is_initial) {
                                "Total: ${String.format("%.1f", log.total_kwh_aktif)} kWh"
                            } else {
                                "Total Aktif: ${String.format("%.1f", log.total_kwh_aktif)} kWh"
                            },
                            color = if (log.is_boros) DustyRoseAccent else ElectricityOrangePastelIcon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (log.durasi_hari > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Siklus ${log.durasi_hari} hari (${String.format("%.1f", log.kwh_per_hari)} kWh/hari)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (log.is_boros) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DustyRoseAccent.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DustyRoseAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Indikasi Pemakaian Boros! Durasi habis lebih cepat (${String.format("%.1f", log.kwh_per_hari)} kWh/hari).",
                            fontSize = 11.sp,
                            color = DustyRoseAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddElectricityDialog(
    isFirstLog: Boolean,
    previousTotalKwh: Float,
    editingLog: ElectricityLog? = null,
    onDismiss: () -> Unit,
    onConfirm: (harga: Int, jumlahKwh: Float, isInitial: Boolean, customTimestamp: Long) -> Unit
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

    var hargaInput by remember(editingLog) { mutableStateOf(editingLog?.harga?.toString() ?: (if (isFirstLog) "0" else "100000")) }
    var kwhInput by remember(editingLog) { mutableStateOf(editingLog?.jumlah_kwh?.toString() ?: (if (isFirstLog) "15.0" else "66.2")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val newKwhValue = kwhInput.toFloatOrNull() ?: 0f
    val previewTotalKwh = if (isFirstLog) newKwhValue else previousTotalKwh + newKwhValue

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = ElectricityOrangePastelIcon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingLog != null) "Edit Catatan Listrik" else if (isFirstLog) "Pencatatan Pertama Meteran" else "Catat Token Listrik Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tanggal Field
                OutlinedTextField(
                    value = tanggalText,
                    onValueChange = {},
                    label = { Text("Tanggal Transaksi (YYYY-MM-DD)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = ElectricityOrangePastelIcon)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                        .testTag("input_listrik_tanggal"),
                    singleLine = true
                )

                // Informational Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SageGreenPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFirstLog) {
                                "Catat sisa kWh meteran rumah Anda saat ini. Nominal Beli (Rp) bersifat opsional."
                            } else {
                                "Sisa kWh sebelumnya (${String.format("%.1f", previousTotalKwh)} kWh) akan otomatis diakumulasikan dengan pembelian baru."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (isFirstLog) {
                    OutlinedTextField(
                        value = kwhInput,
                        onValueChange = { kwhInput = it; errorMessage = null },
                        label = { Text("Sisa kWh di Meteran Saat Ini") },
                        placeholder = { Text("Contoh: 15.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_kwh_listrik")
                    )

                    OutlinedTextField(
                        value = hargaInput,
                        onValueChange = { hargaInput = it; errorMessage = null },
                        label = { Text("Nominal Pembelian (Rp) [Opsional]") },
                        placeholder = { Text("0 jika lupa/hanya catat meteran") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_harga_listrik")
                    )
                } else {
                    OutlinedTextField(
                        value = hargaInput,
                        onValueChange = { hargaInput = it; errorMessage = null },
                        label = { Text("Nominal Token Listrik (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_harga_listrik")
                    )

                    OutlinedTextField(
                        value = kwhInput,
                        onValueChange = { kwhInput = it; errorMessage = null },
                        label = { Text("Jumlah kWh Baru (Dari Struk PLN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_kwh_listrik")
                    )

                    // Nominal Quick Selection Chips
                    Text("Pilih Nominal Cepat:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { hargaInput = "50000"; kwhInput = "33.1" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("50rb", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { hargaInput = "100000"; kwhInput = "66.2" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("100rb", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { hargaInput = "200000"; kwhInput = "132.4" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("200rb", fontSize = 10.sp)
                        }
                    }

                    // Calculation Preview Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricityOrangePastelBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total kWh Aktif Baru:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format("%.1f", previewTotalKwh)} kWh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricityOrangePastelIcon
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
                    val harga = hargaInput.toIntOrNull() ?: 0
                    val kwh = kwhInput.toFloatOrNull()

                    if (!isFirstLog && (hargaInput.toIntOrNull() == null || harga <= 0)) {
                        errorMessage = "Masukkan nominal token yang valid"
                        return@Button
                    }
                    if (kwh == null || kwh <= 0f) {
                        errorMessage = "Masukkan jumlah kWh yang valid"
                        return@Button
                    }

                    onConfirm(harga, kwh, isFirstLog, selectedTimestamp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DustyRoseAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_listrik_button")
            ) {
                Text(
                    text = if (isFirstLog) "Simpan Sisa Awal" else "Simpan Token",
                    fontWeight = FontWeight.Bold
                )
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
private fun ElectricityTrendLineChartCard(
    electricityLogs: List<ElectricityLog>
) {
    var viewMode by remember { mutableStateOf("Biaya") } // "Biaya" or "kWh"
    var filterMode by remember { mutableStateOf("Bulanan") } // "Bulanan" or "Mingguan"
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val chartData = remember(electricityLogs, filterMode) {
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
                val logsInMonth = electricityLogs.filter { log ->
                    val logDate = Date(log.tanggal)
                    sdfKey.format(logDate) == key
                }
                val totalRp = logsInMonth.sumOf { it.harga.toLong() }
                val totalKwh = logsInMonth.sumOf { it.jumlah_kwh.toDouble() }.toFloat()
                Triple(label, totalRp, totalKwh)
            }
        } else {
            // Mingguan (4 Minggu Terakhir)
            val weekLabels = listOf("M-3", "M-2", "M-1", "Minggu Ini")
            val nowMs = System.currentTimeMillis()
            val weekMs = 7 * 24 * 3600 * 1000L

            (3 downTo 0).mapIndexed { idx, weekOffset ->
                val endMs = nowMs - (weekOffset * weekMs)
                val startMs = endMs - weekMs
                val logsInWeek = electricityLogs.filter { log ->
                    log.tanggal in startMs..endMs
                }
                val totalRp = logsInWeek.sumOf { it.harga.toLong() }
                val totalKwh = logsInWeek.sumOf { it.jumlah_kwh.toDouble() }.toFloat()
                Triple(weekLabels[idx], totalRp, totalKwh)
            }
        }
    }

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
                        color = ElectricityOrangePastelBg,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = ElectricityOrangePastelIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Grafik Tren Pembelian Listrik",
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time range filter (Bulanan vs Mingguan)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricityOrangePastelBg)
                            .padding(2.dp)
                    ) {
                        listOf("Bulanan", "Mingguan").forEach { mode ->
                            val isSel = filterMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElectricityOrangePastelIcon else Color.Transparent)
                                    .clickable {
                                        filterMode = mode
                                        selectedPointIndex = null
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else ElectricityOrangePastelIcon
                                )
                            }
                        }
                    }

                    // View Mode toggle (Biaya vs kWh)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricityOrangePastelBg)
                            .padding(2.dp)
                    ) {
                        listOf("Biaya", "kWh").forEach { mode ->
                            val isSel = viewMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElectricityOrangePastelIcon else Color.Transparent)
                                    .clickable { viewMode = mode }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else ElectricityOrangePastelIcon
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val values = chartData.map { if (viewMode == "Biaya") it.second.toFloat() else it.third }
            val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(if (viewMode == "Biaya") 50000f else 10f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp, top = 16.dp, start = 8.dp, end = 8.dp)
                        .pointerInput(chartData, viewMode) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (chartData.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, chartData.size - 1)
                                selectedPointIndex = if (selectedPointIndex == index) null else index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (chartData.size - 1).coerceAtLeast(1)

                    val gridColor = Color.LightGray.copy(alpha = 0.4f)
                    for (i in 0..2) {
                        val y = height * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    }

                    val points = values.mapIndexed { idx, valAmount ->
                        val x = idx * stepX
                        val normalizedY = (valAmount / maxVal).coerceIn(0f, 1f)
                        val y = height - (normalizedY * height)
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val strokePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val controlX1 = p1.x + (p2.x - p1.x) / 2f
                                val controlY1 = p1.y
                                val controlX2 = p1.x + (p2.x - p1.x) / 2f
                                val controlY2 = p2.y
                                cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                            }
                        }

                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(strokePath)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    ElectricityOrangePastelIcon.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )

                        drawPath(
                            path = strokePath,
                            color = ElectricityOrangePastelIcon,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )

                        points.forEachIndexed { idx, pt ->
                            val isSelected = selectedPointIndex == idx
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = ElectricityOrangePastelIcon,
                                radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
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
                        val isSel = selectedPointIndex == idx
                        Text(
                            text = item.first,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) ElectricityOrangePastelIcon else Color.Gray
                        )
                    }
                }
            }

            val activeIdx = selectedPointIndex ?: (chartData.size - 1)
            val activeItem = chartData.getOrNull(activeIdx)
            if (activeItem != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricityOrangePastelBg,
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
                            text = "Total ${activeItem.first}:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (viewMode == "Biaya") Formatters.formatRupiah(activeItem.second) else "${String.format(Locale.getDefault(), "%.1f", activeItem.third)} kWh",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricityOrangePastelIcon
                        )
                    }
                }
            }
        }
    }
}
