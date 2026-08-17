package com.example.ui.jimpitan

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.VolunteerActivism
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.entity.SocialLog
import com.example.ui.theme.JimpitanTealPastelBg
import com.example.ui.theme.JimpitanTealPastelIcon
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JimpitanScreen(
    socialLogs: List<SocialLog>,
    onAddLog: (kategori: String, nominal: Int, keterangan: String, tipe: String, customTimestamp: Long) -> Unit,
    onUpdateLog: (log: SocialLog) -> Unit,
    onDeleteLog: (id: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<SocialLog?>(null) }
    var deleteCandidateLog by remember { mutableStateOf<SocialLog?>(null) }

    // Category filter for list view
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    // Selected Month offset (0 = current month, -1 = last month, etc.)
    var monthOffset by remember { mutableStateOf(0) }

    val calendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
    }

    val selectedYear = calendar.get(Calendar.YEAR)
    val selectedMonth = calendar.get(Calendar.MONTH) // 0-indexed

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("id", "ID")) }
    val currentMonthLabel = remember(calendar) { monthYearFormat.format(calendar.time) }

    // Filter logs for selected month
    val logsInSelectedMonth = remember(socialLogs, selectedYear, selectedMonth) {
        socialLogs.filter { log ->
            val cal = Calendar.getInstance().apply { timeInMillis = log.tanggal }
            cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
        }
    }

    // Monthly Recapitulation stats
    val totalMasuk = remember(logsInSelectedMonth) {
        logsInSelectedMonth.filter { it.tipe_transaksi != "Keluar" }.sumOf { it.nominal }
    }
    val totalKeluar = remember(logsInSelectedMonth) {
        logsInSelectedMonth.filter { it.tipe_transaksi == "Keluar" }.sumOf { it.nominal }
    }
    val saldoSisa = totalMasuk - totalKeluar

    val totalJimpitan = remember(logsInSelectedMonth) {
        logsInSelectedMonth.filter { it.kategori.contains("Jimpitan", ignoreCase = true) && it.tipe_transaksi != "Keluar" }.sumOf { it.nominal }
    }
    val totalKurban = remember(logsInSelectedMonth) {
        logsInSelectedMonth.filter { it.kategori.contains("Kurban", ignoreCase = true) && it.tipe_transaksi != "Keluar" }.sumOf { it.nominal }
    }
    val totalLainnya = remember(logsInSelectedMonth, totalMasuk, totalJimpitan, totalKurban) {
        (totalMasuk - totalJimpitan - totalKurban).coerceAtLeast(0)
    }

    // Context & Clipboard for WhatsApp Report
    val context = androidx.compose.ui.platform.LocalContext.current

    // Filtered list to show based on category chip selection
    val displayedLogs = remember(logsInSelectedMonth, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Semua") {
            logsInSelectedMonth
        } else {
            logsInSelectedMonth.filter { it.kategori.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = SoftCreamCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catatan Setoran Pribadi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Audit Mandiri Jimpitan & Tabungan Kurban",
                            fontSize = 12.sp,
                            color = SoftTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_jimpitan")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
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
                onClick = {
                    editingLog = null
                    showAddEditDialog = true
                },
                containerColor = JimpitanTealPastelIcon,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_jimpitan")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Catat Setoran Pribadi", fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. MONTH SELECTOR BAR
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { monthOffset-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Bulan Sebelumnya",
                                tint = JimpitanTealPastelIcon
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = JimpitanTealPastelIcon,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = currentMonthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { monthOffset++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Bulan Selanjutnya",
                                tint = JimpitanTealPastelIcon
                            )
                        }
                    }
                }
            }

            // 2. KARTU RINGKASAN SETORAN PRIBADI (COMPACT & CLEAN)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, JimpitanTealPastelIcon.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                        .background(JimpitanTealPastelBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = JimpitanTealPastelIcon,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Total Setoran Saya",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SoftTextMuted
                                    )
                                    Text(
                                        text = formatRupiah(totalMasuk),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JimpitanTealPastelIcon
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = JimpitanTealPastelBg
                            ) {
                                Text(
                                    text = "Audit Mandiri",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JimpitanTealPastelIcon,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Compact 3-Column Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Jimpitan
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = JimpitanTealPastelBg.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Jimpitan Saya",
                                        fontSize = 10.sp,
                                        color = SoftTextMuted
                                    )
                                    Text(
                                        text = formatRupiah(totalJimpitan),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Kurban
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SageGreenPrimaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Tabungan Kurban",
                                        fontSize = 10.sp,
                                        color = SoftTextMuted
                                    )
                                    Text(
                                        text = formatRupiah(totalKurban),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )
                                }
                            }

                            // Lainnya / Kebersihan
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF3E0),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Kebersihan/Lain",
                                        fontSize = 10.sp,
                                        color = SoftTextMuted
                                    )
                                    Text(
                                        text = formatRupiah(totalLainnya),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. KARTU PENGINGAT AUDIT MANDIRI (TRANSPARENCY REMINDER)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = JimpitanTealPastelBg,
                    border = BorderStroke(1.dp, JimpitanTealPastelIcon.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(JimpitanTealPastelIcon),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat Audit Mandiri Setoran",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JimpitanTealPastelIcon
                            )
                            Text(
                                text = "Catatan ini adalah log independen Anda. Cocokkan nilai total setoran pribadi Anda (${formatRupiah(totalMasuk)}) dengan bukti setoran / kuitansi dari Bendahara RT.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // 4. FILTER CHIPS BY CATEGORY
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = SoftTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter Kategori Setoran",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTextMuted
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Semua", "Iuran Jimpitan Warga", "Tabungan Kurban", "Iuran Kebersihan", "Lain-lain")
                        items(categories) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JimpitanTealPastelIcon,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // 5. DAFTAR RIWAYAT TRANSAKSI / DEKORATIF EMPTY STATE
            if (displayedLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Visual Graphic Banner Box
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(JimpitanTealPastelBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = JimpitanTealPastelIcon,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Belum Ada Catatan Setoran Bulan Ini",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gunakan tombol 'Catat Setoran Pribadi' di bawah untuk merekam iuran harian jimpitan, kurban, atau kebersihan mandiri Anda.",
                                fontSize = 12.sp,
                                color = SoftTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(displayedLogs, key = { it.id }) { log ->
                    SocialLogCard(
                        log = log,
                        onEdit = {
                            editingLog = log
                            showAddEditDialog = true
                        },
                        onDelete = {
                            deleteCandidateLog = log
                        }
                    )
                }
            }
        }
    }

    // ADD / EDIT DIALOG
    if (showAddEditDialog) {
        SocialLogAddEditDialog(
            existingLog = editingLog,
            onDismiss = { showAddEditDialog = false },
            onSave = { kategori, nominal, keterangan, tipe, customTs ->
                if (editingLog == null) {
                    onAddLog(kategori, nominal, keterangan, tipe, customTs)
                } else {
                    onUpdateLog(
                        editingLog!!.copy(
                            kategori = kategori,
                            nominal = nominal,
                            keterangan = keterangan,
                            tipe_transaksi = tipe,
                            tanggal = customTs
                        )
                    )
                }
                showAddEditDialog = false
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    if (deleteCandidateLog != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidateLog = null },
            title = { Text("Hapus Catatan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus catatan ${deleteCandidateLog?.kategori} sebesar ${formatRupiah(deleteCandidateLog?.nominal ?: 0)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteCandidateLog?.id?.let { onDeleteLog(it) }
                        deleteCandidateLog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteCandidateLog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SocialLogCard(
    log: SocialLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("id", "ID")) }
    val formattedDate = remember(log.tanggal) { dateFormat.format(Date(log.tanggal)) }

    val isKurban = log.kategori.contains("Kurban", ignoreCase = true)
    val badgeBg = if (isKurban) JimpitanTealPastelBg else SageGreenPrimaryContainer
    val badgeTextColor = if (isKurban) JimpitanTealPastelIcon else SageGreenPrimary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SageGreenPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_social_log_${log.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip Badge & Tipe
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (log.tipe_transaksi == "Keluar") MaterialTheme.colorScheme.errorContainer else badgeBg
                    ) {
                        Text(
                            text = if (log.tipe_transaksi == "Keluar") "📤 ${log.kategori} (Kas Keluar)" else "📥 ${log.kategori}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.tipe_transaksi == "Keluar") MaterialTheme.colorScheme.onErrorContainer else badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Date
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = SoftTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val isKeluar = log.tipe_transaksi == "Keluar"
                    Text(
                        text = "${if (isKeluar) "-" else "+"}${formatRupiah(log.nominal)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isKeluar) MaterialTheme.colorScheme.error else SageGreenPrimary
                    )

                    if (log.keterangan.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.keterangan,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons: Edit & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_edit_social_${log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Catatan",
                            tint = JimpitanTealPastelIcon,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_delete_social_${log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Catatan",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SocialLogAddEditDialog(
    existingLog: SocialLog?,
    onDismiss: () -> Unit,
    onSave: (kategori: String, nominal: Int, keterangan: String, tipe: String, customTimestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var selectedTimestamp by remember { mutableLongStateOf(existingLog?.tanggal ?: System.currentTimeMillis()) }
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

    val presetCategories = listOf(
        "Iuran Jimpitan Warga",
        "Tabungan Kurban",
        "Iuran Kebersihan",
        "Lain-lain"
    )

    var tipeInput by remember {
        mutableStateOf(existingLog?.tipe_transaksi ?: "Masuk")
    }

    var selectedCategory by remember {
        mutableStateOf(existingLog?.kategori ?: presetCategories.first())
    }
    var customCategoryText by remember {
        mutableStateOf(if (existingLog != null && !presetCategories.contains(existingLog.kategori)) existingLog.kategori else "")
    }

    var nominalInput by remember {
        mutableStateOf(existingLog?.nominal?.toString() ?: "")
    }
    var keteranganInput by remember {
        mutableStateOf(existingLog?.keterangan ?: "")
    }

    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingLog == null) "Catat Setoran Pribadi" else "Edit Catatan Setoran",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tanggal Field
                OutlinedTextField(
                    value = tanggalText,
                    onValueChange = {},
                    label = { Text("Tanggal Setoran (YYYY-MM-DD)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = JimpitanTealPastelIcon)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                        .testTag("input_jimpitan_tanggal"),
                    singleLine = true
                )

                // Tipe Transaksi Selector (Masuk vs Keluar)
                Text(
                    text = "Arah Setoran / Dana:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tipeInput == "Masuk",
                        onClick = { tipeInput = "Masuk" },
                        label = { Text("📥 Setoran Pribadi", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = tipeInput == "Keluar",
                        onClick = { tipeInput = "Keluar" },
                        label = { Text("📤 Penarikan / Penyesuaian", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category Selector
                Text(
                    text = "Pilih Kategori Setoran:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTextMuted
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presetCategories) { cat ->
                        val isSelected = (selectedCategory == cat && customCategoryText.isBlank())
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = cat
                                customCategoryText = ""
                            },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JimpitanTealPastelIcon,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                if (selectedCategory == "Lain-lain") {
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text("Kategori Kustom (opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Nominal Input
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            nominalInput = input
                            errorMessage = ""
                        }
                    },
                    label = { Text("Nominal Setoran (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nominal_social"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Keterangan Input
                OutlinedTextField(
                    value = keteranganInput,
                    onValueChange = { keteranganInput = it },
                    label = { Text("Catatan Audit / No. Kuitansi / Penerima") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_keterangan_social"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nominalVal = nominalInput.toIntOrNull() ?: 0
                    if (nominalVal <= 0) {
                        errorMessage = "Masukkan nominal transaksi yang valid (> 0)."
                        return@Button
                    }

                    val finalKategori = if (selectedCategory == "Lain-lain" && customCategoryText.isNotBlank()) {
                        customCategoryText.trim()
                    } else {
                        selectedCategory
                    }

                    onSave(finalKategori, nominalVal, keteranganInput.trim(), tipeInput, selectedTimestamp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = JimpitanTealPastelIcon),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_social")
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Batal")
            }
        }
    )
}

private fun formatRupiah(number: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(number).replace(",00", "")
}
