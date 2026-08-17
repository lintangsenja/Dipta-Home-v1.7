package com.example.ui.anak

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChildExpenseLog
import com.example.ui.util.PaycheckPeriod
import com.example.ui.util.PaycheckCycleHelper
import com.example.ui.common.PaycheckPeriodNavigatorCard
import com.example.ui.common.PaycheckCycleSettingsDialog
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import com.example.ui.theme.SoftTextMuted
import com.example.ui.util.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ItemBelanjaAnakInput(
    val namaItem: String = "",
    val hargaSatuan: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnakScreen(
    childExpenses: List<ChildExpenseLog>,
    currentPeriod: PaycheckPeriod? = null,
    onPrevPaycheckCycle: () -> Unit = {},
    onNextPaycheckCycle: () -> Unit = {},
    onResetPaycheckCycle: () -> Unit = {},
    onUpdatePaycheckStartDay: (Int) -> Unit = {},
    onAddChildExpense: (tanggal: String, modal: Double, sisa: Double, rincian: String, catatan: String) -> Unit,
    onUpdateChildExpense: (ChildExpenseLog) -> Unit,
    onDeleteChildExpense: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePeriod = currentPeriod ?: remember { PaycheckCycleHelper.calculatePeriod(25, 0) }
    var showPaycheckSettingsDialog by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLogForEdit by remember { mutableStateOf<ChildExpenseLog?>(null) }
    var deleteCandidateId by remember { mutableStateOf<Int?>(null) }

    val filteredChildExpenses = remember(childExpenses, activePeriod) {
        childExpenses.filter { activePeriod.contains(it.timestamp, it.tanggal) }
    }

    val totalPengeluaranBulanIni = remember(filteredChildExpenses) {
        filteredChildExpenses.sumOf { it.totalPengeluaran }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catat Belanja Anak",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark
                        )
                        Text(
                            text = "Pencatatan Kebutuhan & Belanja Buah Hati",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_anak")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = SoftTextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftCreamCanvas
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_anak_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Catatan Belanja Anak"
                )
            }
        },
        containerColor = SoftCreamCanvas,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Paycheck Period Navigator
            PaycheckPeriodNavigatorCard(
                currentPeriod = activePeriod,
                onPrevCycle = onPrevPaycheckCycle,
                onNextCycle = onNextPaycheckCycle,
                onResetCycle = onResetPaycheckCycle,
                onOpenSettings = { showPaycheckSettingsDialog = true },
                primaryColor = Color(0xFFE91E63),
                borderColor = Color(0xFFF8BBD0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Header Card Summary
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF8BBD0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Pengeluaran Anak (${activePeriod.shortLabel})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatRupiah(totalPengeluaranBulanIni),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFC2185B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (childExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = Color(0xFFF8BBD0),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Catatan Belanja Anak",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tekan tombol + untuk mencatat pembelian kebutuhan anak (susu, popok, baju, dll.)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp, start = 16.dp, end = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        ChildExpenseBarChartCard(childExpenses = childExpenses)
                    }

                    items(filteredChildExpenses, key = { it.id }) { log ->
                        ChildExpenseItemCard(
                            log = log,
                            onEdit = { selectedLogForEdit = log },
                            onDelete = { deleteCandidateId = log.id }
                        )
                    }
                }
            }
        }
    }

    // Paycheck Settings Dialog
    if (showPaycheckSettingsDialog) {
        PaycheckCycleSettingsDialog(
            currentStartDay = activePeriod.startDay,
            onDismiss = { showPaycheckSettingsDialog = false },
            onSave = { newDay ->
                onUpdatePaycheckStartDay(newDay)
                showPaycheckSettingsDialog = false
            }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditChildExpenseDialog(
            existingLog = null,
            onDismiss = { showAddDialog = false },
            onSave = { tgl, modal, sisa, rincian, catatan ->
                onAddChildExpense(tgl, modal, sisa, rincian, catatan)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    selectedLogForEdit?.let { log ->
        AddEditChildExpenseDialog(
            existingLog = log,
            onDismiss = { selectedLogForEdit = null },
            onSave = { tgl, modal, sisa, rincian, catatan ->
                val updated = log.copy(
                    tanggal = tgl,
                    modalAwal = modal,
                    sisaUang = sisa,
                    rincian = rincian,
                    catatan = catatan,
                    totalPengeluaran = (modal - sisa).coerceAtLeast(0.0)
                )
                onUpdateChildExpense(updated)
                selectedLogForEdit = null
            }
        )
    }

    // Delete Confirmation
    deleteCandidateId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteCandidateId = null },
            title = { Text("Hapus Catatan Belanja Anak") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan belanja ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteChildExpense(id)
                        deleteCandidateId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateId = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ChildExpenseItemCard(
    log: ChildExpenseLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8BBD0).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.tanggal,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftTextDark
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = SoftCreamCanvas, modifier = Modifier.padding(vertical = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (log.rincian.isNotBlank()) log.rincian else "Tanpa rincian item",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SoftTextDark
                    )
                    if (log.catatan.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Catatan: ${log.catatan}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatRupiah(log.totalPengeluaran),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC2185B)
                    )
                    if (log.modalAwal > 0) {
                        Text(
                            text = "Modal: ${Formatters.formatRupiah(log.modalAwal)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEditChildExpenseDialog(
    existingLog: ChildExpenseLog?,
    onDismiss: () -> Unit,
    onSave: (tanggal: String, modal: Double, sisa: Double, rincian: String, catatan: String) -> Unit
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val calNow = remember { Calendar.getInstance() }

    var tanggal by remember { mutableStateOf(existingLog?.tanggal ?: sdf.format(Date())) }
    var modalInput by remember { mutableStateOf(existingLog?.modalAwal?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
    var catatan by remember { mutableStateOf(existingLog?.catatan ?: "") }

    // Dynamic Item List for Belanja Anak
    val itemsList = remember {
        mutableStateListOf<ItemBelanjaAnakInput>().apply {
            if (existingLog != null && existingLog.rincian.isNotBlank()) {
                val parts = existingLog.rincian.split(",")
                parts.forEach { p ->
                    val clean = p.trim()
                    if (clean.isNotBlank()) {
                        if (clean.contains("(") && clean.endsWith(")")) {
                            val name = clean.substringBefore("(").trim()
                            val priceStr = clean.substringAfter("(").substringBefore(")").replace(Regex("[^0-9]"), "")
                            add(ItemBelanjaAnakInput(namaItem = name, hargaSatuan = priceStr))
                        } else {
                            add(ItemBelanjaAnakInput(namaItem = clean, hargaSatuan = ""))
                        }
                    }
                }
            }
            if (isEmpty()) {
                add(ItemBelanjaAnakInput())
            }
        }
    }

    var manualRincianText by remember { mutableStateOf(existingLog?.rincian ?: "") }
    var manualRincianTotalInput by remember { mutableStateOf(existingLog?.totalPengeluaran?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
    var useItemListMode by remember { mutableStateOf(true) }

    // Calculate sum of items dynamically on every recomposition/change
    val calculatedTotalItems = itemsList.sumOf { item ->
        item.hargaSatuan.toDoubleOrNull() ?: 0.0
    }

    val datePicker = remember(context) {
        DatePickerDialog(context, { _, y, m, d ->
            tanggal = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
        }, calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), calNow.get(Calendar.DAY_OF_MONTH))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = Color(0xFFE91E63)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingLog == null) "Tambah Belanja Anak" else "Edit Belanja Anak",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tanggal Field
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { tanggal = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    enabled = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = SageGreenPrimary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                        .testTag("input_anak_tanggal"),
                    singleLine = true
                )

                // Anggaran / Modal Awal (Optional)
                OutlinedTextField(
                    value = modalInput,
                    onValueChange = { modalInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Anggaran / Modal Awal (Rp) - Opsional") },
                    enabled = true,
                    readOnly = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_anak_modal"),
                    singleLine = true
                )

                HorizontalDivider(color = SoftCreamCanvas, modifier = Modifier.padding(vertical = 2.dp))

                // Item List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Barang Belanja Anak",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SoftTextDark
                    )

                    TextButton(onClick = { useItemListMode = !useItemListMode }) {
                        Text(if (useItemListMode) "Teks Biasa" else "Daftar Item", fontSize = 11.sp, color = SageGreenPrimary)
                    }
                }

                if (useItemListMode) {
                    itemsList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item.namaItem,
                                onValueChange = { newName ->
                                    itemsList[index] = itemsList[index].copy(namaItem = newName)
                                },
                                label = { Text("Nama Barang #${index + 1}", fontSize = 11.sp) },
                                enabled = true,
                                readOnly = false,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("input_anak_item_nama_$index"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = item.hargaSatuan,
                                onValueChange = { newPrice ->
                                    val filtered = newPrice.filter { it.isDigit() }
                                    itemsList[index] = itemsList[index].copy(hargaSatuan = filtered)
                                },
                                label = { Text("Harga (Rp)", fontSize = 11.sp) },
                                enabled = true,
                                readOnly = false,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_anak_item_harga_$index"),
                                singleLine = true
                            )

                            if (itemsList.size > 1) {
                                IconButton(
                                    onClick = { itemsList.removeAt(index) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_remove_anak_item_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus Item",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { itemsList.add(ItemBelanjaAnakInput()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_anak_item_row"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Tambah Item Barang", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Total Calculation Real-time display
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFCE4EC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Estimasi Belanja:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SoftTextDark
                            )
                            Text(
                                text = Formatters.formatRupiah(calculatedTotalItems),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFC2185B)
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = manualRincianText,
                        onValueChange = { manualRincianText = it },
                        label = { Text("Rincian Belanja") },
                        enabled = true,
                        readOnly = false,
                        modifier = Modifier.fillMaxWidth().testTag("input_anak_rincian_manual"),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = manualRincianTotalInput,
                        onValueChange = { manualRincianTotalInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Biaya Belanja (Rp)") },
                        enabled = true,
                        readOnly = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_anak_total_manual"),
                        singleLine = true
                    )
                }

                // Catatan Field
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    enabled = true,
                    readOnly = false,
                    modifier = Modifier.fillMaxWidth().testTag("input_anak_catatan"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val modal = modalInput.toDoubleOrNull() ?: 0.0
                    val (finalRincian, totalExp) = if (useItemListMode) {
                        val validItems = itemsList.filter { it.namaItem.isNotBlank() }
                        val formattedItems = validItems.map { item ->
                            val h = item.hargaSatuan.toDoubleOrNull()
                            if (h != null && h > 0) "${item.namaItem.trim()} (${Formatters.formatRupiah(h)})" else item.namaItem.trim()
                        }.joinToString(", ")
                        Pair(formattedItems, calculatedTotalItems)
                    } else {
                        val manualTotal = manualRincianTotalInput.toDoubleOrNull() ?: 0.0
                        Pair(manualRincianText.trim(), manualTotal)
                    }

                    val finalModal = if (modal > 0) modal else totalExp
                    val sisa = (finalModal - totalExp).coerceAtLeast(0.0)

                    onSave(tanggal, finalModal, sisa, finalRincian, catatan.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_anak_expense")
            ) {
                Text("Simpan Belanja Anak", fontWeight = FontWeight.Bold)
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
private fun ChildExpenseBarChartCard(
    childExpenses: List<ChildExpenseLog>
) {
    var filterMode by remember { mutableStateOf("Bulanan") } // "Bulanan" or "Mingguan"
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val chartData = remember(childExpenses, filterMode) {
        val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if (filterMode == "Bulanan") {
            val recentCycles = PaycheckCycleHelper.getRecentCycles(25, 6)
            recentCycles.map { cycle ->
                val totalInCycle = childExpenses.filter { log ->
                    cycle.contains(log.timestamp, log.tanggal)
                }.sumOf { it.totalPengeluaran }
                Pair(cycle.shortLabel, totalInCycle)
            }
        } else {
            // Mingguan (4 Minggu Terakhir)
            val weekLabels = listOf("M-3", "M-2", "M-1", "Minggu Ini")
            val nowMs = System.currentTimeMillis()
            val weekMs = 7 * 24 * 3600 * 1000L

            (3 downTo 0).mapIndexed { idx, weekOffset ->
                val endMs = nowMs - (weekOffset * weekMs)
                val startMs = endMs - weekMs
                val totalInWeek = childExpenses.filter { log ->
                    val t = if (log.timestamp > 0) log.timestamp else try {
                        sdfParse.parse(log.tanggal)?.time ?: 0L
                    } catch (_: Exception) { 0L }
                    t in startMs..endMs
                }.sumOf { it.totalPengeluaran }
                Pair(weekLabels[idx], totalInWeek)
            }
        }
    }

    val maxVal = (chartData.map { it.second }.maxOrNull() ?: 100000.0).coerceAtLeast(50000.0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF8BBD0)),
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
                        color = Color(0xFFFCE4EC),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Grafik Belanja Anak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SoftTextDark
                        )
                        Text(
                            text = if (filterMode == "Bulanan") "Perbandingan 6 bulan terakhir" else "Perbandingan 4 minggu terakhir",
                            fontSize = 11.sp,
                            color = SoftTextMuted
                        )
                    }
                }

                // Filter pills toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Bulanan", "Mingguan").forEach { mode ->
                        val isSel = filterMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFFE91E63) else Color(0xFFFCE4EC),
                            modifier = Modifier.clickable {
                                filterMode = mode
                                selectedIndex = null
                            }
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFFE91E63),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.forEachIndexed { idx, (itemLabel, amount) ->
                    val ratio = (amount / maxVal).toFloat().coerceIn(0f, 1f)
                    val barHeight = (ratio * 80).dp.coerceAtLeast(6.dp)
                    val isSelected = selectedIndex == idx

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIndex = if (selectedIndex == idx) null else idx }
                    ) {
                        if (amount > 0) {
                            Text(
                                text = if (amount >= 1_000_000) "${String.format(Locale.getDefault(), "%.1f", amount / 1_000_000.0)}Jt"
                                else if (amount >= 1_000) "${(amount / 1_000).toInt()}rb"
                                else "${amount.toInt()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFC2185B) else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isSelected) Color(0xFFC2185B)
                                    else if (amount > 0) Color(0xFFF48FB1)
                                    else Color(0xFFFCE4EC)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = itemLabel,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFC2185B) else SoftTextMuted
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
                    color = Color(0xFFFCE4EC),
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
                            text = "Pengeluaran ${activeItem.first}:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SoftTextDark
                        )
                        Text(
                            text = Formatters.formatRupiah(activeItem.second),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2185B)
                        )
                    }
                }
            }
        }
    }
}
