package com.example.ui.warung

import android.app.DatePickerDialog
import com.example.ui.common.DateRangeFilterDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.RandomExpense
import com.example.data.entity.ShoppingNoteItem
import com.example.data.entity.WarungDebt
import com.example.data.entity.WarungDebtPayment
import com.example.ui.util.PaycheckPeriod
import com.example.ui.util.PaycheckCycleHelper
import com.example.ui.common.PaycheckPeriodNavigatorCard
import com.example.ui.common.PaycheckCycleSettingsDialog
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import com.example.ui.theme.SoftTextMuted
import com.example.ui.theme.WarungGreenPastelBg
import com.example.ui.theme.WarungGreenPastelIcon
import com.example.ui.theme.WarungOrangePastelBg
import com.example.ui.theme.WarungOrangePastelText
import com.example.ui.theme.WarungRedPastelBg
import com.example.ui.theme.WarungRedPastelText
import com.example.ui.util.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarungScreen(
    dailyGroceryLogs: List<DailyGroceryLog>,
    randomExpenses: List<RandomExpense> = emptyList(),
    warungDebts: List<WarungDebt>,
    warungDebtPayments: List<WarungDebtPayment>,
    shoppingNoteItems: List<ShoppingNoteItem>,
    warungDebtLimit: Double,
    currentPeriod: PaycheckPeriod? = null,
    onPrevPaycheckCycle: () -> Unit = {},
    onNextPaycheckCycle: () -> Unit = {},
    onResetPaycheckCycle: () -> Unit = {},
    onUpdatePaycheckStartDay: (Int) -> Unit = {},
    onAddDailyGroceryLog: (tanggal: String, modalAwal: Double, sisaUang: Double, rincian: String, catatan: String) -> Unit,
    onUpdateDailyGroceryLog: (DailyGroceryLog) -> Unit,
    onDeleteDailyGroceryLog: (Int) -> Unit,
    onAddRandomExpense: (tanggal: String, modalAwal: Double, sisaUang: Double, rincian: String, catatan: String) -> Unit = { _, _, _, _, _ -> },
    onUpdateRandomExpense: (RandomExpense) -> Unit = {},
    onDeleteRandomExpense: (Int) -> Unit = {},
    onAddWarungDebt: (tanggal: String, namaWarung: String, nominal: Double, alasan: String) -> Unit,
    onUpdateWarungDebt: (WarungDebt) -> Unit,
    onDeleteWarungDebt: (Int) -> Unit,
    onAddWarungDebtPayment: (debtId: Int, tanggal: String, nominalBayar: Double, catatan: String) -> Unit,
    onDeleteWarungDebtPayment: (WarungDebtPayment) -> Unit,
    onAddShoppingNoteItem: (namaBarang: String, prioritas: String, estimasiHarga: Double, catatan: String) -> Unit,
    onUpdateShoppingNoteItem: (ShoppingNoteItem) -> Unit,
    onToggleShoppingNoteDone: (ShoppingNoteItem) -> Unit,
    onDeleteShoppingNoteItem: (Int) -> Unit,
    onClearCompletedShoppingNotes: () -> Unit,
    onUpdateWarungDebtLimit: (Double) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activePeriod = currentPeriod ?: remember { PaycheckCycleHelper.calculatePeriod(25, 0) }
    var showPaycheckSettingsDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ringkasan", "Belanja", "Random", "Hutang Warung", "Pelunasan", "Note Belanja")

    // Filter & Search states
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua Status") }
    var selectedTimeFilter by remember { mutableStateOf("Semua Waktu") }
    var selectedPriorityFilter by remember { mutableStateOf("Semua Prioritas") }
    var selectedNoteStatusFilter by remember { mutableStateOf("Semua Item") }

    // Dialog States
    var showUniversalAddDialog by remember { mutableStateOf(false) }
    var initialDialogType by remember { mutableIntStateOf(0) } // 0: Belanja, 1: Random, 2: Debt, 3: Note

    var editingGroceryLog by remember { mutableStateOf<DailyGroceryLog?>(null) }
    var editingRandomLog by remember { mutableStateOf<RandomExpense?>(null) }
    var editingDebt by remember { mutableStateOf<WarungDebt?>(null) }
    var selectedDebtToPay by remember { mutableStateOf<WarungDebt?>(null) }
    var editingNoteItem by remember { mutableStateOf<ShoppingNoteItem?>(null) }

    var showPayDebtDialog by remember { mutableStateOf(false) }
    var showLimitSettingsDialog by remember { mutableStateOf(false) }

    // Date Range Filter States
    var showWarungDateRangeDialog by remember { mutableStateOf(false) }
    var isWarungDateRangeActive by remember { mutableStateOf(false) }
    var warungStartDate by remember { mutableStateOf("") }
    var warungEndDate by remember { mutableStateOf("") }

    // Warning limit check
    val totalUnpaidDebt = warungDebts.filter { !it.isLunas }.sumOf { (it.nominal - it.totalDibayar).coerceAtLeast(0.0) }
    val isNearOrExceedingLimit = totalUnpaidDebt >= (warungDebtLimit * 0.8)

    // Financial Overview Calculations (Filtered dynamically based on Paycheck Cycle or Custom Date Range)
    fun isWarungDateInRange(dateStr: String): Boolean {
        if (isWarungDateRangeActive && warungStartDate.isNotBlank() && warungEndDate.isNotBlank()) {
            if (dateStr.isBlank()) return false
            return dateStr >= warungStartDate && dateStr <= warungEndDate
        }
        return activePeriod.contains(dateStr = dateStr)
    }

    val filteredGroceryLogs = remember(dailyGroceryLogs, isWarungDateRangeActive, warungStartDate, warungEndDate, activePeriod) {
        dailyGroceryLogs.filter { isWarungDateInRange(it.tanggal) }
    }

    val filteredRandomExpenses = remember(randomExpenses, isWarungDateRangeActive, warungStartDate, warungEndDate, activePeriod) {
        randomExpenses.filter { isWarungDateInRange(it.tanggal) }
    }

    val totalModal = filteredGroceryLogs.sumOf { it.modalAwal } + filteredRandomExpenses.sumOf { it.modalAwal }
    val totalPengeluaran = filteredGroceryLogs.sumOf { it.totalPengeluaran } + filteredRandomExpenses.sumOf { it.totalPengeluaran }
    val totalSisaUang = filteredGroceryLogs.sumOf { it.sisaUang } + filteredRandomExpenses.sumOf { it.sisaUang }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Catat Warung & Keuangan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SoftTextDark
                            )
                        }
                        Text(
                            text = "Manajemen transaksi, belanja harian, & hutang warung",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("warung_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = SoftTextDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLimitSettingsDialog = true },
                        modifier = Modifier.testTag("warung_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Limit Hutang",
                            tint = SageGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftCreamCanvas)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingGroceryLog = null
                    editingRandomLog = null
                    editingDebt = null
                    editingNoteItem = null
                    initialDialogType = when (selectedTab) {
                        1 -> 0 // Belanja
                        2 -> 1 // Random
                        3 -> 2 // Hutang
                        5 -> 3 // Note
                        else -> 0
                    }
                    showUniversalAddDialog = true
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Catatan"
                    )
                },
                text = {
                    Text(
                        text = "Catat Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                containerColor = SageGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("warung_fab_add")
            )
        },
        containerColor = SoftCreamCanvas
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Warning limit banner at top if near or exceeding limit
            if (isNearOrExceedingLimit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = WarungRedPastelBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan Limit",
                            tint = WarungRedPastelText,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (totalUnpaidDebt >= warungDebtLimit) "❌ MELEBIHI LIMIT HUTANG WARUNG!" else "⚠️ MENDEKATI LIMIT HUTANG!",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = WarungRedPastelText
                            )
                            Text(
                                text = "Total hutang aktif: ${Formatters.formatRupiah(totalUnpaidDebt)} dari limit ${Formatters.formatRupiah(warungDebtLimit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarungRedPastelText
                            )
                        }
                    }
                }
            }

            // Paycheck Period Navigator
            PaycheckPeriodNavigatorCard(
                currentPeriod = activePeriod,
                onPrevCycle = onPrevPaycheckCycle,
                onNextCycle = onNextPaycheckCycle,
                onResetCycle = onResetPaycheckCycle,
                onOpenSettings = { showPaycheckSettingsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Tabs Header Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SoftCreamCanvas,
                contentColor = SageGreenPrimary,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        },
                        selectedContentColor = SageGreenPrimary,
                        unselectedContentColor = SoftTextMuted
                    )
                }
            }

            // Sub-menu views
            when (selectedTab) {
                0 -> WarungDashboardView(
                    dailyGroceryLogs = filteredGroceryLogs,
                    randomExpenses = filteredRandomExpenses,
                    warungDebts = warungDebts,
                    warungDebtPayments = warungDebtPayments,
                    shoppingNoteItems = shoppingNoteItems,
                    warungDebtLimit = warungDebtLimit,
                    isDateRangeActive = isWarungDateRangeActive,
                    startDateStr = warungStartDate,
                    endDateStr = warungEndDate,
                    onTriggerDateRangePicker = { showWarungDateRangeDialog = true },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    statusFilter = selectedStatusFilter,
                    onStatusFilterChange = { selectedStatusFilter = it },
                    timeFilter = selectedTimeFilter,
                    onTimeFilterChange = { selectedTimeFilter = it },
                    onNavigateToTab = { selectedTab = it },
                    onEditGrocery = { log ->
                        editingGroceryLog = log
                        initialDialogType = 0
                        showUniversalAddDialog = true
                    },
                    onDeleteGrocery = onDeleteDailyGroceryLog,
                    onEditDebt = { debt ->
                        editingDebt = debt
                        initialDialogType = 2
                        showUniversalAddDialog = true
                    },
                    onDeleteDebt = onDeleteWarungDebt,
                    onPayDebt = { debt ->
                        selectedDebtToPay = debt
                        showPayDebtDialog = true
                    }
                )
                1 -> BelanjaHarianView(
                    logs = filterGroceryLogs(dailyGroceryLogs, searchQuery, selectedStatusFilter, selectedTimeFilter),
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedStatus = selectedStatusFilter,
                    onStatusChange = { selectedStatusFilter = it },
                    selectedTime = selectedTimeFilter,
                    onTimeChange = { selectedTimeFilter = it },
                    onEdit = { log ->
                        editingGroceryLog = log
                        initialDialogType = 0
                        showUniversalAddDialog = true
                    },
                    onDelete = onDeleteDailyGroceryLog
                )
                2 -> RandomExpenseView(
                    logs = filterRandomExpenses(randomExpenses, searchQuery, selectedTimeFilter),
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedTime = selectedTimeFilter,
                    onTimeChange = { selectedTimeFilter = it },
                    onEdit = { log ->
                        editingRandomLog = log
                        initialDialogType = 1
                        showUniversalAddDialog = true
                    },
                    onDelete = onDeleteRandomExpense
                )
                3 -> HutangWarungView(
                    debts = filterDebts(warungDebts, searchQuery, selectedStatusFilter, selectedTimeFilter),
                    debtLimit = warungDebtLimit,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedStatus = selectedStatusFilter,
                    onStatusChange = { selectedStatusFilter = it },
                    selectedTime = selectedTimeFilter,
                    onTimeChange = { selectedTimeFilter = it },
                    onEdit = { debt ->
                        editingDebt = debt
                        initialDialogType = 2
                        showUniversalAddDialog = true
                    },
                    onDelete = onDeleteWarungDebt,
                    onPayDirect = { debt ->
                        selectedDebtToPay = debt
                        showPayDebtDialog = true
                    }
                )
                4 -> BayarHutangView(
                    debts = warungDebts,
                    payments = filterPayments(warungDebtPayments, warungDebts, searchQuery, selectedTimeFilter),
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedTime = selectedTimeFilter,
                    onTimeChange = { selectedTimeFilter = it },
                    onPayDebt = { debt ->
                        selectedDebtToPay = debt
                        showPayDebtDialog = true
                    },
                    onDeletePayment = onDeleteWarungDebtPayment
                )
                5 -> NoteBelanjaView(
                    noteItems = shoppingNoteItems,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedPriority = selectedPriorityFilter,
                    onPriorityChange = { selectedPriorityFilter = it },
                    selectedNoteStatus = selectedNoteStatusFilter,
                    onNoteStatusChange = { selectedNoteStatusFilter = it },
                    onToggleDone = onToggleShoppingNoteDone,
                    onEdit = { item ->
                        editingNoteItem = item
                        initialDialogType = 3
                        showUniversalAddDialog = true
                    },
                    onDelete = onDeleteShoppingNoteItem,
                    onClearCompleted = onClearCompletedShoppingNotes
                )
            }
        }
    }

    // --- DIALOGS ---

    // Universal Transaction Dialog (Multi-tab/Responsive)
    if (showUniversalAddDialog) {
        UniversalWarungTransactionDialog(
            initialType = initialDialogType,
            editingGroceryLog = editingGroceryLog,
            editingRandomLog = editingRandomLog,
            editingDebt = editingDebt,
            editingNoteItem = editingNoteItem,
            warungDebts = warungDebts,
            suggestedModal = (dailyGroceryLogs.firstOrNull()?.modalAwal ?: 50000.0),
            onDismiss = {
                showUniversalAddDialog = false
                editingGroceryLog = null
                editingRandomLog = null
                editingDebt = null
                editingNoteItem = null
            },
            onSaveGrocery = { tanggal, modal, sisa, rincian, catatan ->
                if (editingGroceryLog != null) {
                    val updated = editingGroceryLog!!.copy(
                        tanggal = tanggal,
                        modalAwal = modal,
                        sisaUang = sisa,
                        totalPengeluaran = (modal - sisa).coerceAtLeast(0.0),
                        rincian = rincian,
                        catatan = catatan
                    )
                    onUpdateDailyGroceryLog(updated)
                } else {
                    onAddDailyGroceryLog(tanggal, modal, sisa, rincian, catatan)
                }
                showUniversalAddDialog = false
            },
            onSaveRandom = { tanggal, modal, sisa, rincian, catatan ->
                if (editingRandomLog != null) {
                    val updated = editingRandomLog!!.copy(
                        tanggal = tanggal,
                        modalAwal = modal,
                        sisaUang = sisa,
                        totalPengeluaran = (modal - sisa).coerceAtLeast(0.0),
                        rincian = rincian,
                        catatan = catatan
                    )
                    onUpdateRandomExpense(updated)
                } else {
                    onAddRandomExpense(tanggal, modal, sisa, rincian, catatan)
                }
                showUniversalAddDialog = false
            },
            onSaveDebt = { tanggal, namaWarung, nominal, alasan ->
                if (editingDebt != null) {
                    val updated = editingDebt!!.copy(
                        tanggal = tanggal,
                        namaWarung = namaWarung,
                        nominal = nominal,
                        alasan = alasan
                    )
                    onUpdateWarungDebt(updated)
                } else {
                    onAddWarungDebt(tanggal, namaWarung, nominal, alasan)
                }
                showUniversalAddDialog = false
            },
            onSaveNote = { nama, prioritas, estimasi, catatan ->
                if (editingNoteItem != null) {
                    val updated = editingNoteItem!!.copy(
                        namaBarang = nama,
                        prioritas = prioritas,
                        estimasiHarga = estimasi,
                        catatan = catatan
                    )
                    onUpdateShoppingNoteItem(updated)
                } else {
                    onAddShoppingNoteItem(nama, prioritas, estimasi, catatan)
                }
                showUniversalAddDialog = false
            }
        )
    }

    // Pay Debt Dialog
    if (showPayDebtDialog && selectedDebtToPay != null) {
        PayDebtDialog(
            debt = selectedDebtToPay!!,
            onDismiss = { showPayDebtDialog = false },
            onSave = { debtId, tanggal, nominalBayar, catatan ->
                onAddWarungDebtPayment(debtId, tanggal, nominalBayar, catatan)
                showPayDebtDialog = false
            }
        )
    }

    // Debt Limit Settings Dialog
    if (showLimitSettingsDialog) {
        LimitSettingsDialog(
            currentLimit = warungDebtLimit,
            onDismiss = { showLimitSettingsDialog = false },
            onSave = { newLimit ->
                onUpdateWarungDebtLimit(newLimit)
                showLimitSettingsDialog = false
            }
        )
    }

    // Date Range Filter Dialog
    if (showWarungDateRangeDialog) {
        DateRangeFilterDialog(
            showDialog = showWarungDateRangeDialog,
            initialStartDate = warungStartDate,
            initialEndDate = warungEndDate,
            onDismissRequest = { showWarungDateRangeDialog = false },
            onApplyDateRange = { start, end ->
                warungStartDate = start
                warungEndDate = end
                isWarungDateRangeActive = true
                showWarungDateRangeDialog = false
            },
            onResetDateRange = {
                warungStartDate = ""
                warungEndDate = ""
                isWarungDateRangeActive = false
                showWarungDateRangeDialog = false
            }
        )
    }

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
}

// ==========================================
// 1. DASHBOARD & RINGKASAN KEUANGAN VIEW
// ==========================================
@Composable
private fun WarungDashboardView(
    dailyGroceryLogs: List<DailyGroceryLog>,
    randomExpenses: List<RandomExpense>,
    warungDebts: List<WarungDebt>,
    warungDebtPayments: List<WarungDebtPayment>,
    shoppingNoteItems: List<ShoppingNoteItem>,
    warungDebtLimit: Double,
    isDateRangeActive: Boolean = false,
    startDateStr: String = "",
    endDateStr: String = "",
    onTriggerDateRangePicker: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    timeFilter: String,
    onTimeFilterChange: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onEditGrocery: (DailyGroceryLog) -> Unit,
    onDeleteGrocery: (Int) -> Unit,
    onEditDebt: (WarungDebt) -> Unit,
    onDeleteDebt: (Int) -> Unit,
    onPayDebt: (WarungDebt) -> Unit
) {
    val totalModal = dailyGroceryLogs.sumOf { it.modalAwal } + randomExpenses.sumOf { it.modalAwal }
    val totalPengeluaran = dailyGroceryLogs.sumOf { it.totalPengeluaran } + randomExpenses.sumOf { it.totalPengeluaran }
    val totalSisaUang = dailyGroceryLogs.sumOf { it.sisaUang } + randomExpenses.sumOf { it.sisaUang }
    val totalUnpaidDebt = warungDebts.filter { !it.isLunas }.sumOf { (it.nominal - it.totalDibayar).coerceAtLeast(0.0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ringkasan Keuangan Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageGreenPrimary),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ringkasan Keuangan Warung",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDateRangeActive) Color.White else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clickable { onTriggerDateRangePicker() }
                                .testTag("btn_trigger_warung_date_range_picker")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Filter Rentang Tanggal",
                                    tint = if (isDateRangeActive) SageGreenPrimary else Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDateRangeActive) "$startDateStr s.d. $endDateStr" else "Filter Tanggal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDateRangeActive) SageGreenPrimary else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardMetricBox(
                            label = "Total Modal",
                            value = Formatters.formatRupiah(totalModal),
                            subtitle = "Anggaran",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricBox(
                            label = "Pengeluaran",
                            value = Formatters.formatRupiah(totalPengeluaran),
                            subtitle = "Belanja",
                            valueColor = Color(0xFFFFEB3B),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricBox(
                            label = "Hutang Aktif",
                            value = Formatters.formatRupiah(totalUnpaidDebt),
                            subtitle = "Di Warung",
                            valueColor = if (totalUnpaidDebt > 0) Color(0xFFFF8A80) else Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Kartu Grafik Analitik Interaktif (Harian, Mingguan, Bulanan)
        item {
            WarungAnalyticsChartCard(
                dailyGroceryLogs = dailyGroceryLogs,
                randomExpenses = randomExpenses,
                warungDebts = warungDebts
            )
        }

        // 3. Pencarian & Filter Cepat
        item {
            WarungSearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                activeTab = 0,
                selectedStatusFilter = statusFilter,
                onStatusFilterChange = onStatusFilterChange,
                selectedTimeFilter = timeFilter,
                onTimeFilterChange = onTimeFilterChange,
                placeholderText = "Cari barang, warung, atau rincian..."
            )
        }

        // 4. Daftar Kartu Transaksi Terbaru (Card-Based List)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Transaksi Belanja & Catatan Terbaru",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTextDark
                )

                TextButton(onClick = { onNavigateToTab(1) }) {
                    Text(
                        text = "Lihat Semua",
                        fontSize = 12.sp,
                        color = SageGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val filteredGrocery = filterGroceryLogs(dailyGroceryLogs, searchQuery, statusFilter, timeFilter)
        val filteredDebts = filterDebts(warungDebts, searchQuery, statusFilter, timeFilter)

        if (filteredGrocery.isEmpty() && filteredDebts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = SoftTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada catatan transaksi ditemukan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTextMuted
                        )
                        Text(
                            text = "Gunakan tombol + di kanan bawah untuk menambah catatan baru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                }
            }
        } else {
            // Recent Grocery Cards
            items(filteredGrocery.take(5), key = { "grocery_${it.id}" }) { log ->
                ModernGroceryCard(
                    log = log,
                    onEdit = { onEditGrocery(log) },
                    onDelete = { onDeleteGrocery(log.id) }
                )
            }

            // Recent Debt Cards
            items(filteredDebts.take(5), key = { "debt_${it.id}" }) { debt ->
                ModernWarungDebtCard(
                    debt = debt,
                    onEdit = { onEditDebt(debt) },
                    onDelete = { onDeleteDebt(debt.id) },
                    onPay = { onPayDebt(debt) }
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricBox(
    label: String,
    value: String,
    subtitle: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

// ==========================================
// GRAFIK ANALITIK MINI & TREN
// ==========================================
@Composable
private fun WarungAnalyticsChartCard(
    dailyGroceryLogs: List<DailyGroceryLog>,
    randomExpenses: List<RandomExpense>,
    warungDebts: List<WarungDebt>
) {
    var selectedFilter by remember { mutableStateOf("Harian") } // "Harian", "Mingguan", "Bulanan"
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header + Filter Chip Toggles
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
                        color = SageGreenPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Analisis Pengeluaran Warung",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SoftTextDark
                        )
                        Text(
                            text = when (selectedFilter) {
                                "Harian" -> "Pengeluaran harian minggu ini (Grafik Batang)"
                                "Mingguan" -> "Perbandingan 4 minggu terakhir (Grafik Batang)"
                                else -> "Tren akumulasi 6 bulan terakhir (Grafik Garis)"
                            },
                            fontSize = 11.sp,
                            color = SoftTextMuted
                        )
                    }
                }

                // Filter Chip Selector (Harian, Mingguan, Bulanan)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Harian", "Mingguan", "Bulanan").forEach { mode ->
                        val isSelected = selectedFilter == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SageGreenPrimary else SageGreenPrimaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.clickable {
                                selectedFilter = mode
                                selectedIndex = null
                            }
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SageGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedFilter) {
                "Harian" -> {
                    val daysShort = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    val daysFull = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

                    val expensesPerDay = daysFull.map { dayName ->
                        dailyGroceryLogs.filter { matchesDayOfWeek(it.tanggal, dayName) }.sumOf { it.totalPengeluaran } +
                        randomExpenses.filter { matchesDayOfWeek(it.tanggal, dayName) }.sumOf { it.totalPengeluaran }
                    }

                    val maxVal = expensesPerDay.maxOrNull()?.coerceAtLeast(10000.0) ?: 10000.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        expensesPerDay.forEachIndexed { idx, valAmount ->
                            val ratio = (valAmount / maxVal).toFloat()
                            val barHeightDp = (ratio * 80).dp.coerceAtLeast(6.dp)
                            val isPeak = valAmount == expensesPerDay.maxOrNull() && valAmount > 0
                            val isSelected = selectedIndex == idx

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedIndex = if (selectedIndex == idx) null else idx }
                            ) {
                                Text(
                                    text = if (valAmount > 0) "${(valAmount / 1000).toInt()}k" else "",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || isPeak) SageGreenPrimary else Color.Gray
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SoftCreamCanvas),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeightDp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) Color(0xFF2E7D32)
                                                else if (isPeak) SageGreenPrimary
                                                else SageGreenPrimary.copy(alpha = 0.5f)
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = daysShort[idx],
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || isPeak) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected || isPeak) SoftTextDark else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeIdx = selectedIndex ?: expensesPerDay.indexOfMaxOrNull()
                    val activeDay = if (activeIdx != null && expensesPerDay[activeIdx] > 0) daysFull[activeIdx] else null
                    val activeAmount = if (activeIdx != null) expensesPerDay[activeIdx] else 0.0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftCreamCanvas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (activeDay != null)
                                    "${if (selectedIndex != null) "Detail" else "Hari Terboros"}: $activeDay (${Formatters.formatRupiah(activeAmount)})"
                                else
                                    "Sistem siap mencatat tren belanja harian warung Anda secara otomatis.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftTextDark
                            )
                        }
                    }
                }

                "Mingguan" -> {
                    val weekLabels = listOf("Minggu 1", "Minggu 2", "Minggu 3", "Minggu Ini")
                    val nowMs = System.currentTimeMillis()
                    val weekMs = 7 * 24 * 3600 * 1000L

                    val weeklyExpenses = (3 downTo 0).map { weekOffset ->
                        val endMs = nowMs - (weekOffset * weekMs)
                        val startMs = endMs - weekMs
                        val g = dailyGroceryLogs.filter { log ->
                            val t = if (log.timestamp > 0) log.timestamp else parseDateToMs(log.tanggal)
                            t in startMs..endMs
                        }.sumOf { it.totalPengeluaran }

                        val r = randomExpenses.filter { exp ->
                            val t = if (exp.timestamp > 0) exp.timestamp else parseDateToMs(exp.tanggal)
                            t in startMs..endMs
                        }.sumOf { it.totalPengeluaran }
                        g + r
                    }

                    val maxWeeklyVal = weeklyExpenses.maxOrNull()?.coerceAtLeast(50000.0) ?: 50000.0
                    val thisWeek = weeklyExpenses.getOrElse(3) { 0.0 }
                    val lastWeek = weeklyExpenses.getOrElse(2) { 0.0 }
                    val diffPct = if (lastWeek > 0) ((thisWeek - lastWeek) / lastWeek) * 100.0 else 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyExpenses.forEachIndexed { idx, valAmount ->
                            val ratio = (valAmount / maxWeeklyVal).toFloat()
                            val barHeightDp = (ratio * 80).dp.coerceAtLeast(6.dp)
                            val isThisWeek = idx == 3
                            val isSelected = selectedIndex == idx

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedIndex = if (selectedIndex == idx) null else idx }
                            ) {
                                Text(
                                    text = if (valAmount > 0) "${(valAmount / 1000).toInt()}k" else "-",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || isThisWeek) SageGreenPrimary else Color.Gray
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SoftCreamCanvas),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeightDp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color(0xFF2E7D32)
                                                else if (isThisWeek) SageGreenPrimary
                                                else SageGreenPrimary.copy(alpha = 0.5f)
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = weekLabels[idx],
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected || isThisWeek) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected || isThisWeek) SoftTextDark else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeIdx = selectedIndex ?: 3
                    val activeWeekLabel = weekLabels.getOrElse(activeIdx) { "Minggu Ini" }
                    val activeWeekAmount = weeklyExpenses.getOrElse(activeIdx) { 0.0 }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (thisWeek > lastWeek && lastWeek > 0) Color(0xFFFFF3E0) else SoftCreamCanvas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (thisWeek <= lastWeek) Icons.Default.CheckCircle else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (thisWeek <= lastWeek) SageGreenPrimary else Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (selectedIndex != null) {
                                    "$activeWeekLabel: Total Belanja ${Formatters.formatRupiah(activeWeekAmount)}"
                                } else when {
                                    lastWeek == 0.0 && thisWeek == 0.0 -> "Belum ada catatan transaksi belanja minggu ini."
                                    lastWeek == 0.0 -> "Minggu ini belanja: ${Formatters.formatRupiah(thisWeek)}"
                                    thisWeek > lastWeek -> "Minggu ini ${Formatters.formatRupiah(thisWeek)} (${String.format(Locale.US, "+%.0f%%", diffPct)} lebih boros dari minggu lalu)"
                                    else -> "Minggu ini ${Formatters.formatRupiah(thisWeek)} (${String.format(Locale.US, "%.0f%%", diffPct)} lebih hemat dari minggu lalu)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftTextDark
                            )
                        }
                    }
                }

                "Bulanan" -> {
                    // Line Chart (Grafik Garis) for Paycheck Cycle Cumulative Expenses
                    val recentCycles = PaycheckCycleHelper.getRecentCycles(25, 6)
                    val monthsList = recentCycles.map { cycle ->
                        val g = dailyGroceryLogs.filter { log ->
                            cycle.contains(log.timestamp, log.tanggal)
                        }.sumOf { it.totalPengeluaran }

                        val r = randomExpenses.filter { exp ->
                            cycle.contains(exp.timestamp, exp.tanggal)
                        }.sumOf { it.totalPengeluaran }

                        Pair(cycle.label, g + r)
                    }

                    val maxVal = monthsList.map { it.second }.maxOrNull()?.coerceAtLeast(100000.0) ?: 100000.0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 22.dp, top = 12.dp, start = 12.dp, end = 12.dp)
                                .pointerInput(monthsList) {
                                    detectTapGestures { offset ->
                                        val stepX = size.width / (monthsList.size - 1).coerceAtLeast(1)
                                        val index = (offset.x / stepX).roundToInt().coerceIn(0, monthsList.size - 1)
                                        selectedIndex = if (selectedIndex == index) null else index
                                    }
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            val stepX = width / (monthsList.size - 1).coerceAtLeast(1)

                            // Grid lines
                            val gridColor = Color.LightGray.copy(alpha = 0.3f)
                            for (i in 0..2) {
                                val y = height * (i / 2f)
                                drawLine(
                                    color = gridColor,
                                    start = androidx.compose.ui.geometry.Offset(0f, y),
                                    end = androidx.compose.ui.geometry.Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Compute line points
                            val linePoints = monthsList.mapIndexed { idx, item ->
                                val x = idx * stepX
                                val ratio = (item.second / maxVal).coerceIn(0.0, 1.0).toFloat()
                                val y = height - (ratio * height)
                                androidx.compose.ui.geometry.Offset(x, y)
                            }

                            // Draw filled area under line
                            if (linePoints.isNotEmpty()) {
                                val pathFill = Path().apply {
                                    moveTo(linePoints.first().x, height)
                                    linePoints.forEach { lineTo(it.x, it.y) }
                                    lineTo(linePoints.last().x, height)
                                    close()
                                }
                                drawPath(
                                    path = pathFill,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            SageGreenPrimary.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )

                                // Draw line
                                val pathLine = Path().apply {
                                    moveTo(linePoints.first().x, linePoints.first().y)
                                    for (i in 1 until linePoints.size) {
                                        lineTo(linePoints[i].x, linePoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = pathLine,
                                    color = SageGreenPrimary,
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Draw dot nodes
                                linePoints.forEachIndexed { idx, pt ->
                                    val isSelected = selectedIndex == idx
                                    val radiusPx = if (isSelected) 7.dp.toPx() else 4.dp.toPx()
                                    drawCircle(
                                        color = if (isSelected) Color(0xFF1B5E20) else SageGreenPrimary,
                                        radius = radiusPx,
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = radiusPx / 2f,
                                        center = pt
                                    )
                                }
                            }
                        }

                        // Labels along bottom
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            monthsList.forEachIndexed { idx, item ->
                                val isSel = selectedIndex == idx
                                Text(
                                    text = item.first,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) SageGreenPrimary else SoftTextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeIdx = selectedIndex ?: (monthsList.size - 1)
                    val activeItem = monthsList.getOrNull(activeIdx)
                    val peakMonthPair = monthsList.maxByOrNull { it.second }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftCreamCanvas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (selectedIndex != null && activeItem != null) {
                                    "Bulan ${activeItem.first}: Total ${Formatters.formatRupiah(activeItem.second)}"
                                } else if (peakMonthPair != null && peakMonthPair.second > 0) {
                                    "Bulan Terboros: ${peakMonthPair.first} (${Formatters.formatRupiah(peakMonthPair.second)})"
                                } else {
                                    "Sistem siap merekapitulasi pengeluaran bulanan warung Anda."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftTextDark
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseDateToMs(dateStr: String): Long {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: 0L
    } catch (_: Exception) { 0L }
}

private fun matchesDayOfWeek(dateStr: String, targetDayName: String): Boolean {
    if (dateStr.isBlank()) return false
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return false
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfWeekInt = cal.get(Calendar.DAY_OF_WEEK)
        val dayName = when (dayOfWeekInt) {
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            Calendar.SUNDAY -> "Minggu"
            else -> ""
        }
        return dayName.equals(targetDayName, ignoreCase = true)
    } catch (_: Exception) {
        return false
    }
}

private fun List<Double>.indexOfMaxOrNull(): Int? {
    if (isEmpty()) return null
    var maxIdx = 0
    var maxVal = this[0]
    for (i in 1 until size) {
        if (this[i] > maxVal) {
            maxVal = this[i]
            maxIdx = i
        }
    }
    return maxIdx
}

// Filter logic helpers
private fun filterGroceryLogs(
    logs: List<DailyGroceryLog>,
    query: String,
    status: String,
    timeRange: String
): List<DailyGroceryLog> {
    return logs.filter { log ->
        val matchesQuery = query.isBlank() ||
                log.rincian.contains(query, ignoreCase = true) ||
                log.catatan.contains(query, ignoreCase = true) ||
                log.tanggal.contains(query, ignoreCase = true)

        val matchesTime = matchesTimeFilter(log.tanggal, timeRange)
        matchesQuery && matchesTime
    }
}

private fun filterRandomExpenses(
    logs: List<RandomExpense>,
    query: String,
    timeRange: String
): List<RandomExpense> {
    return logs.filter { log ->
        val matchesQuery = query.isBlank() ||
                log.rincian.contains(query, ignoreCase = true) ||
                log.catatan.contains(query, ignoreCase = true) ||
                log.tanggal.contains(query, ignoreCase = true)

        val matchesTime = matchesTimeFilter(log.tanggal, timeRange)
        matchesQuery && matchesTime
    }
}

private fun filterDebts(
    debts: List<WarungDebt>,
    query: String,
    status: String,
    timeRange: String
): List<WarungDebt> {
    return debts.filter { debt ->
        val matchesQuery = query.isBlank() ||
                debt.namaWarung.contains(query, ignoreCase = true) ||
                debt.alasan.contains(query, ignoreCase = true) ||
                debt.tanggal.contains(query, ignoreCase = true)

        val matchesStatus = when (status) {
            "Lunas" -> debt.isLunas
            "Utang" -> !debt.isLunas
            else -> true
        }

        val matchesTime = matchesTimeFilter(debt.tanggal, timeRange)
        matchesQuery && matchesStatus && matchesTime
    }
}

private fun filterShoppingNotes(
    notes: List<ShoppingNoteItem>,
    query: String,
    priority: String = "Semua Prioritas",
    statusItem: String = "Semua Item"
): List<ShoppingNoteItem> {
    return notes.filter { item ->
        val matchesQuery = query.isBlank() ||
                item.namaBarang.contains(query, ignoreCase = true) ||
                item.catatan.contains(query, ignoreCase = true)

        val matchesPriority = if (priority == "Semua Prioritas" || priority == "Semua") true else item.prioritas.equals(priority, ignoreCase = true)

        val matchesStatus = when (statusItem) {
            "Belum Dibeli" -> !item.isDone
            "Sudah Dibeli" -> item.isDone
            else -> true
        }

        matchesQuery && matchesPriority && matchesStatus
    }
}

private fun filterPayments(
    payments: List<WarungDebtPayment>,
    debts: List<WarungDebt>,
    query: String,
    timeRange: String
): List<WarungDebtPayment> {
    return payments.filter { p ->
        val parentDebt = debts.find { it.id == p.debtId }
        val matchesQuery = query.isBlank() ||
                (parentDebt?.namaWarung?.contains(query, ignoreCase = true) == true) ||
                p.catatan.contains(query, ignoreCase = true) ||
                p.tanggal.contains(query, ignoreCase = true)

        val matchesTime = matchesTimeFilter(p.tanggal, timeRange)
        matchesQuery && matchesTime
    }
}

private fun matchesTimeFilter(dateStr: String, filter: String): Boolean {
    if (filter == "Semua Waktu" || dateStr.isBlank()) return true
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return true
        val calDate = Calendar.getInstance().apply { time = date }
        val now = Calendar.getInstance()

        return when (filter) {
            "Hari Ini" -> {
                calDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        calDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            }
            "7 Hari Terakhir" -> {
                val diff = now.timeInMillis - calDate.timeInMillis
                diff in 0..(7 * 24 * 60 * 60 * 1000L)
            }
            "Bulan Ini" -> {
                calDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        calDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
            else -> true
        }
    } catch (_: Exception) {
        return true
    }
}

// ==========================================
// SUB-MENU 1: BELANJA HARIAN VIEW
// ==========================================
@Composable
fun BelanjaHarianView(
    logs: List<DailyGroceryLog>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    onEdit: (DailyGroceryLog) -> Unit,
    onDelete: (Int) -> Unit
) {
    val totalExpenseThisMonth = logs.sumOf { it.totalPengeluaran }
    val avgExpensePerLog = if (logs.isNotEmpty()) totalExpenseThisMonth / logs.size else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Pengeluaran Belanja Harian",
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftTextMuted
                        )
                        Text(
                            text = Formatters.formatRupiah(totalExpenseThisMonth),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark
                        )
                        Text(
                            text = "Rata-rata: ${Formatters.formatRupiah(avgExpensePerLog)} / belanja",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WarungGreenPastelBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = WarungGreenPastelIcon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Search Bar Inside View
        item {
            WarungSearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchChange,
                activeTab = 1,
                selectedStatusFilter = selectedStatus,
                onStatusFilterChange = onStatusChange,
                selectedTimeFilter = selectedTime,
                onTimeFilterChange = onTimeChange,
                placeholderText = "Cari rincian belanja atau tanggal..."
            )
        }

        item {
            Text(
                text = "Riwayat Belanja Harian (${logs.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftTextDark,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = SoftTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada catatan belanja harian.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTextMuted
                        )
                        Text(
                            text = "Gunakan tombol + di bawah untuk mencatat modal & sisa uang belanja.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                ModernGroceryCard(
                    log = log,
                    onEdit = { onEdit(log) },
                    onDelete = { onDelete(log.id) }
                )
            }
        }
    }
}

@Composable
fun ModernGroceryCard(
    log: DailyGroceryLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WarungGreenPastelBg,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = WarungGreenPastelIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = log.tanggal,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark
                        )
                        Text(
                            text = "Modal: ${Formatters.formatRupiah(log.modalAwal)}  |  Sisa: ${Formatters.formatRupiah(log.sisaUang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SageGreenPrimaryContainer
                ) {
                    Text(
                        text = Formatters.formatRupiah(log.totalPengeluaran),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SageGreenPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (log.rincian.isNotBlank() || log.catatan.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SoftCreamCanvas)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Sembunyikan Struk Rincian" else "Lihat Struk Rincian Belanja",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SageGreenPrimary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        if (log.rincian.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftCreamCanvas,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Rincian Items Struk:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SoftTextMuted
                                    )
                                    Text(
                                        text = log.rincian,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTextDark,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        if (log.catatan.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Catatan Warung: ${log.catatan}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = WarungRedPastelText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SUB-MENU 2: RANDOM EXPENSE VIEW
// ==========================================
@Composable
fun RandomExpenseView(
    logs: List<RandomExpense>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    onEdit: (RandomExpense) -> Unit,
    onDelete: (Int) -> Unit
) {
    val totalExpense = logs.sumOf { it.totalPengeluaran }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WarungOrangePastelBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pengeluaran Random / Darurat Warung",
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftTextMuted
                        )
                        Text(
                            text = Formatters.formatRupiah(totalExpense),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = WarungOrangePastelText
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = WarungOrangePastelText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            WarungSearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchChange,
                activeTab = 2,
                selectedTimeFilter = selectedTime,
                onTimeFilterChange = onTimeChange,
                placeholderText = "Cari pengeluaran random..."
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Belum ada pengeluaran random warung.", color = SoftTextMuted)
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.tanggal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Modal: ${Formatters.formatRupiah(log.modalAwal)} | Sisa: ${Formatters.formatRupiah(log.sisaUang)}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                Formatters.formatRupiah(log.totalPengeluaran),
                                fontWeight = FontWeight.Bold,
                                color = WarungOrangePastelText
                            )
                        }
                        if (log.rincian.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Rincian: ${log.rincian}", fontSize = 12.sp, color = SoftTextDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { onEdit(log) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDelete(log.id) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WarungRedPastelText, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-MENU 3: HUTANG WARUNG VIEW
// ==========================================
@Composable
fun HutangWarungView(
    debts: List<WarungDebt>,
    debtLimit: Double,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    onEdit: (WarungDebt) -> Unit,
    onDelete: (Int) -> Unit,
    onPayDirect: (WarungDebt) -> Unit
) {
    val totalUnpaid = debts.filter { !it.isLunas }.sumOf { (it.nominal - it.totalDibayar).coerceAtLeast(0.0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Debt Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (totalUnpaid >= debtLimit) WarungRedPastelBg else WarungOrangePastelBg
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Sisa Hutang Warung",
                                style = MaterialTheme.typography.labelMedium,
                                color = SoftTextMuted
                            )
                            Text(
                                text = Formatters.formatRupiah(totalUnpaid),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (totalUnpaid >= debtLimit) WarungRedPastelText else WarungOrangePastelText
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "Limit: ${Formatters.formatRupiah(debtLimit)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SoftTextDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progressRatio = (totalUnpaid / debtLimit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (totalUnpaid >= debtLimit) WarungRedPastelText else WarungOrangePastelText,
                        trackColor = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Search Field
        item {
            WarungSearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchChange,
                activeTab = 3,
                selectedStatusFilter = selectedStatus,
                onStatusFilterChange = onStatusChange,
                selectedTimeFilter = selectedTime,
                onTimeFilterChange = onTimeChange,
                placeholderText = "Cari nama warung / keterangan hutang..."
            )
        }

        if (debts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoneyOff,
                            contentDescription = null,
                            tint = SoftTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada catatan hutang warung.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTextMuted
                        )
                    }
                }
            }
        } else {
            items(debts, key = { it.id }) { debt ->
                ModernWarungDebtCard(
                    debt = debt,
                    onEdit = { onEdit(debt) },
                    onDelete = { onDelete(debt.id) },
                    onPay = { onPayDirect(debt) }
                )
            }
        }
    }
}

@Composable
fun ModernWarungDebtCard(
    debt: WarungDebt,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPay: () -> Unit
) {
    val remaining = (debt.nominal - debt.totalDibayar).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = debt.namaWarung,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftTextDark
                    )
                    Text(
                        text = "Tanggal: ${debt.tanggal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (debt.isLunas) WarungGreenPastelBg else WarungOrangePastelBg
                ) {
                    Text(
                        text = if (debt.isLunas) "LUNAS" else "BELUM LUNAS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (debt.isLunas) WarungGreenPastelIcon else WarungOrangePastelText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Nominal:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTextMuted
                )
                Text(
                    text = Formatters.formatRupiah(debt.nominal),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SoftTextDark
                )
            }

            if (!debt.isLunas) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sisa Tagihan:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextMuted
                    )
                    Text(
                        text = Formatters.formatRupiah(remaining),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = WarungOrangePastelText
                    )
                }
            }

            if (debt.alasan.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keterangan: ${debt.alasan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!debt.isLunas) {
                    Button(
                        onClick = onPay,
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bayar / Cicil", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = WarungRedPastelText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-MENU 4: BAYAR HUTANG VIEW
// ==========================================
@Composable
fun BayarHutangView(
    debts: List<WarungDebt>,
    payments: List<WarungDebtPayment>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    onPayDebt: (WarungDebt) -> Unit,
    onDeletePayment: (WarungDebtPayment) -> Unit
) {
    val activeDebts = debts.filter { !it.isLunas }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pelunasan & Cicilan Hutang Warung",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftTextDark
                    )
                    Text(
                        text = "Bayar hutang warung secara bertahap (cicilan) atau langsung lunas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextMuted
                    )
                }
            }
        }

        item {
            WarungSearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchChange,
                activeTab = 4,
                selectedTimeFilter = selectedTime,
                onTimeFilterChange = onTimeChange,
                placeholderText = "Cari warung atau rincian pelunasan..."
            )
        }

        item {
            Text(
                text = "Daftar Hutang Aktif (${activeDebts.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftTextDark
            )
        }

        if (activeDebts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WarungGreenPastelIcon,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Semua hutang warung sudah LUNAS! 🎉",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark
                        )
                    }
                }
            }
        } else {
            items(activeDebts, key = { it.id }) { debt ->
                val progress = (debt.totalDibayar / debt.nominal.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                val remaining = (debt.nominal - debt.totalDibayar).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = debt.namaWarung,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SoftTextDark
                            )
                            Button(
                                onClick = { onPayDebt(debt) },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Bayar", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = SageGreenPrimary,
                            trackColor = SageGreenPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Dibayar: ${Formatters.formatRupiah(debt.totalDibayar)} / ${Formatters.formatRupiah(debt.nominal)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTextMuted
                            )
                            Text(
                                text = "Sisa: ${Formatters.formatRupiah(remaining)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = WarungOrangePastelText
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Riwayat Pembayaran Cicilan (${payments.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftTextDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (payments.isEmpty()) {
            item {
                Text("Belum ada riwayat pembayaran.", style = MaterialTheme.typography.bodySmall, color = SoftTextMuted)
            }
        } else {
            items(payments, key = { it.id }) { p ->
                val parentDebt = debts.find { it.id == p.debtId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = parentDebt?.namaWarung ?: "Warung",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SoftTextDark
                            )
                            Text(
                                text = "${p.tanggal} ${if (p.catatan.isNotBlank()) "- ${p.catatan}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTextMuted
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${Formatters.formatRupiah(p.nominalBayar)}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = WarungGreenPastelIcon
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onDeletePayment(p) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Pembayaran",
                                    tint = WarungRedPastelText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-MENU 5: NOTE BELANJA VIEW
// ==========================================
@Composable
fun NoteBelanjaView(
    noteItems: List<ShoppingNoteItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedPriority: String,
    onPriorityChange: (String) -> Unit,
    selectedNoteStatus: String,
    onNoteStatusChange: (String) -> Unit,
    onToggleDone: (ShoppingNoteItem) -> Unit,
    onEdit: (ShoppingNoteItem) -> Unit,
    onDelete: (Int) -> Unit,
    onClearCompleted: () -> Unit
) {
    val filteredItems = filterShoppingNotes(noteItems, searchQuery, selectedPriority, selectedNoteStatus)

    val uncheckedItems = noteItems.filter { !it.isDone }
    val totalEstUnchecked = uncheckedItems.sumOf { it.estimasiHarga }
    val completedCount = noteItems.count { it.isDone }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimasi Total Belanjaan",
                            style = MaterialTheme.typography.labelMedium,
                            color = SoftTextMuted
                        )
                        Text(
                            text = Formatters.formatRupiah(totalEstUnchecked),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SoftTextDark
                        )
                        Text(
                            text = "$completedCount dari ${noteItems.size} barang sudah dibeli",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTextMuted
                        )
                    }

                    if (completedCount > 0) {
                        OutlinedButton(
                            onClick = onClearCompleted,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarungRedPastelText),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Hapus Selesai", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            WarungSearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchChange,
                activeTab = 5,
                selectedPriorityFilter = selectedPriority,
                onPriorityFilterChange = onPriorityChange,
                selectedNoteStatusFilter = selectedNoteStatus,
                onNoteStatusFilterChange = onNoteStatusChange,
                placeholderText = "Cari item belanjaan atau catatan..."
            )
        }

        if (filteredItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            tint = SoftTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Catatan belanjaan kosong.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                ShoppingNoteCard(
                    item = item,
                    onToggleDone = { onToggleDone(item) },
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item.id) }
                )
            }
        }
    }
}

@Composable
fun ShoppingNoteCard(
    item: ShoppingNoteItem,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone) SoftCreamCanvas else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isDone) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isDone,
                onCheckedChange = { onToggleDone() },
                colors = CheckboxDefaults.colors(checkedColor = SageGreenPrimary)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.namaBarang,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (item.isDone) SoftTextMuted else SoftTextDark
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val (badgeBg, badgeText) = when (item.prioritas) {
                        "Tinggi" -> WarungRedPastelBg to WarungRedPastelText
                        "Sedang" -> WarungOrangePastelBg to WarungOrangePastelText
                        else -> WarungGreenPastelBg to WarungGreenPastelIcon
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = item.prioritas,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (item.estimasiHarga > 0 || item.catatan.isNotBlank()) {
                    Text(
                        text = "${if (item.estimasiHarga > 0) Formatters.formatRupiah(item.estimasiHarga) else ""} ${if (item.catatan.isNotBlank()) "(${item.catatan})" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextMuted
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = WarungRedPastelText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// FORM INPUT RESPONSIF & UNIVERSAL DIALOGS
// ==========================================

data class ReceiptItemInput(val name: String = "", val priceStr: String = "")

fun parseRincianToItems(rincian: String): List<ReceiptItemInput> {
    if (rincian.isBlank()) return listOf(ReceiptItemInput("", ""))
    val lines = rincian.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    val result = mutableListOf<ReceiptItemInput>()
    for (line in lines) {
        val regex = Regex("""^(.*?)\s*[\(:–-]\s*(?:Rp\.?\s*)?([\d\.,]+)\)?$""", RegexOption.IGNORE_CASE)
        val match = regex.find(line)
        if (match != null) {
            val name = match.groupValues[1].trim()
            val priceClean = match.groupValues[2].replace(".", "").replace(",", "").trim()
            result.add(ReceiptItemInput(name, priceClean))
        } else {
            result.add(ReceiptItemInput(line, ""))
        }
    }
    return if (result.isEmpty()) listOf(ReceiptItemInput("", "")) else result
}

fun formatItemsToRincian(items: List<ReceiptItemInput>): String {
    return items
        .filter { it.name.isNotBlank() || it.priceStr.isNotBlank() }
        .joinToString("\n") { item ->
            val priceVal = item.priceStr.toDoubleOrNull()
            if (priceVal != null && priceVal > 0) {
                val formattedPrice = Formatters.formatRupiah(priceVal)
                "${item.name.ifBlank { "Item" }} ($formattedPrice)"
            } else {
                item.name
            }
        }
}

@Composable
fun UniversalWarungTransactionDialog(
    initialType: Int, // 0: Belanja Harian, 1: Random, 2: Debt, 3: Note
    editingGroceryLog: DailyGroceryLog?,
    editingRandomLog: RandomExpense?,
    editingDebt: WarungDebt?,
    editingNoteItem: ShoppingNoteItem?,
    warungDebts: List<WarungDebt>,
    suggestedModal: Double,
    onDismiss: () -> Unit,
    onSaveGrocery: (tanggal: String, modal: Double, sisa: Double, rincian: String, catatan: String) -> Unit,
    onSaveRandom: (tanggal: String, modal: Double, sisa: Double, rincian: String, catatan: String) -> Unit,
    onSaveDebt: (tanggal: String, namaWarung: String, nominal: Double, alasan: String) -> Unit,
    onSaveNote: (namaBarang: String, prioritas: String, estimasiHarga: Double, catatan: String) -> Unit
) {
    var selectedType by remember { mutableIntStateOf(initialType) }
    val types = listOf("Belanja", "Random", "Hutang", "Note")

    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Form States
    var tanggal by remember { mutableStateOf(editingGroceryLog?.tanggal ?: editingRandomLog?.tanggal ?: editingDebt?.tanggal ?: sdf.format(Date())) }
    var modalStr by remember { mutableStateOf(editingGroceryLog?.modalAwal?.toInt()?.toString() ?: editingRandomLog?.modalAwal?.toInt()?.toString() ?: suggestedModal.toInt().toString()) }
    var catatan by remember { mutableStateOf(editingGroceryLog?.catatan ?: editingRandomLog?.catatan ?: editingNoteItem?.catatan ?: "") }

    // Debt specific
    var namaWarung by remember { mutableStateOf(editingDebt?.namaWarung ?: "") }
    var nominalDebtStr by remember { mutableStateOf(editingDebt?.nominal?.toInt()?.toString() ?: "") }
    var alasanDebt by remember { mutableStateOf(editingDebt?.alasan ?: "") }

    // Shopping Note specific
    var namaBarangNote by remember { mutableStateOf(editingNoteItem?.namaBarang ?: "") }
    var prioritasNote by remember { mutableStateOf(editingNoteItem?.prioritas ?: "Sedang") }
    var estimasiNoteStr by remember { mutableStateOf(editingNoteItem?.estimasiHarga?.toInt()?.toString() ?: "") }

    val items = remember {
        mutableStateListOf<ReceiptItemInput>().apply {
            val initialRincian = editingGroceryLog?.rincian ?: editingRandomLog?.rincian ?: ""
            addAll(parseRincianToItems(initialRincian))
        }
    }

    val calendar = remember(tanggal) {
        Calendar.getInstance().apply {
            try {
                val parts = tanggal.split("-")
                if (parts.size == 3) {
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            } catch (_: Exception) {}
        }
    }

    val datePickerDialog = remember(context, calendar) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                tanggal = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val modalVal = modalStr.toDoubleOrNull() ?: 0.0
    val totalBelanjaCalculated = items.sumOf { it.priceStr.toDoubleOrNull() ?: 0.0 }
    val sisaUangCalculated = modalVal - totalBelanjaCalculated

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Form Input Transaksi Warung",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SoftTextDark
                )

                // Type selector tabs inside dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEachIndexed { idx, tName ->
                        val isSel = selectedType == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) SageGreenPrimary else SoftCreamCanvas,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = idx }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = tName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else SoftTextDark
                                )
                            }
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Common Date picker
                if (selectedType != 3) {
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = { tanggal = it },
                        label = { Text("Tanggal (YYYY-MM-DD)") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Kalender",
                                    tint = SageGreenPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        singleLine = true
                    )
                }

                when (selectedType) {
                    0, 1 -> { // Belanja Harian & Random
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = modalStr,
                                onValueChange = { modalStr = it },
                                label = { Text("Modal Awal (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            if (suggestedModal > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(onClick = { modalStr = suggestedModal.toInt().toString() }) {
                                    Text("Isi Modal", fontSize = 11.sp, color = SageGreenPrimary)
                                }
                            }
                        }

                        HorizontalDivider(color = SoftCreamCanvas)

                        Text(
                            text = "Rincian Barang & Kalkulator Struk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SoftTextDark
                        )

                        items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { newName -> items[index] = item.copy(name = newName) },
                                    label = { Text("Barang") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = item.priceStr,
                                    onValueChange = { newPrice -> items[index] = item.copy(priceStr = newPrice) },
                                    label = { Text("Harga (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        if (items.size > 1) items.removeAt(index)
                                        else items[0] = ReceiptItemInput("", "")
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = WarungRedPastelText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { items.add(ReceiptItemInput("", "")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tambah Item Struk", fontSize = 12.sp)
                        }

                        // Auto calculation card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Belanja:", fontSize = 12.sp, color = SoftTextDark)
                                    Text(Formatters.formatRupiah(totalBelanjaCalculated), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sisa Uang (Otomatis):", fontSize = 12.sp, color = SoftTextDark)
                                    Text(
                                        Formatters.formatRupiah(sisaUangCalculated),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sisaUangCalculated < 0) WarungRedPastelText else SageGreenPrimary
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = catatan,
                            onValueChange = { catatan = it },
                            label = { Text("Catatan / Lokasi Warung (Opsional)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    2 -> { // Debt
                        OutlinedTextField(
                            value = namaWarung,
                            onValueChange = { namaWarung = it },
                            label = { Text("Nama Warung / Toko") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = nominalDebtStr,
                            onValueChange = { nominalDebtStr = it },
                            label = { Text("Nominal Hutang (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = alasanDebt,
                            onValueChange = { alasanDebt = it },
                            label = { Text("Alasan / Barang Dibeli") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    3 -> { // Note
                        OutlinedTextField(
                            value = namaBarangNote,
                            onValueChange = { namaBarangNote = it },
                            label = { Text("Nama Barang / Logistik") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Tinggi", "Sedang", "Rendah").forEach { p ->
                                val isSel = prioritasNote == p
                                FilterChip(
                                    selected = isSel,
                                    onClick = { prioritasNote = p },
                                    label = { Text(p, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = estimasiNoteStr,
                            onValueChange = { estimasiNoteStr = it },
                            label = { Text("Estimasi Harga (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = catatan,
                            onValueChange = { catatan = it },
                            label = { Text("Catatan Pilihan Toko / Merek") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedType) {
                        0 -> {
                            val rincianFormatted = formatItemsToRincian(items)
                            onSaveGrocery(tanggal, modalVal, sisaUangCalculated, rincianFormatted, catatan)
                        }
                        1 -> {
                            val rincianFormatted = formatItemsToRincian(items)
                            onSaveRandom(tanggal, modalVal, sisaUangCalculated, rincianFormatted, catatan)
                        }
                        2 -> {
                            val nom = nominalDebtStr.toDoubleOrNull() ?: 0.0
                            onSaveDebt(tanggal, namaWarung, nom, alasanDebt)
                        }
                        3 -> {
                            val est = estimasiNoteStr.toDoubleOrNull() ?: 0.0
                            onSaveNote(namaBarangNote, prioritasNote, est, catatan)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Simpan Data", fontWeight = FontWeight.Bold)
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
fun PayDebtDialog(
    debt: WarungDebt,
    onDismiss: () -> Unit,
    onSave: (debtId: Int, tanggal: String, nominalBayar: Double, catatan: String) -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var tanggal by remember { mutableStateOf(sdf.format(Date())) }
    var nominalStr by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    val remaining = (debt.nominal - debt.totalDibayar).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bayar / Cicil Hutang ${debt.namaWarung}",
                fontWeight = FontWeight.Bold,
                color = SoftTextDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SageGreenPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Sisa Tagihan Belum Lunas:", fontSize = 11.sp, color = SoftTextMuted)
                        Text(Formatters.formatRupiah(remaining), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SageGreenPrimary)
                    }
                }

                OutlinedTextField(
                    value = nominalStr,
                    onValueChange = { nominalStr = it },
                    label = { Text("Nominal Bayar (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextButton(onClick = { nominalStr = remaining.toInt().toString() }) {
                    Text("Pelunasan Langsung (Sesuai Sisa)", fontSize = 11.sp, color = SageGreenPrimary)
                }

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan Pembayaran (Opsional)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nom = nominalStr.toDoubleOrNull() ?: 0.0
                    if (nom > 0) {
                        onSave(debt.id, tanggal, nom, catatan)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Simpan Pembayaran", fontWeight = FontWeight.Bold)
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
fun LimitSettingsDialog(
    currentLimit: Double,
    onDismiss: () -> Unit,
    onSave: (newLimit: Double) -> Unit
) {
    var limitStr by remember { mutableStateOf(currentLimit.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Pengaturan Limit Hutang Warung", fontWeight = FontWeight.Bold, color = SoftTextDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Batas aman total hutang warung sebelum mendapat peringatan:", fontSize = 12.sp, color = SoftTextMuted)
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Limit Hutang Maksimal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newLim = limitStr.toDoubleOrNull() ?: currentLimit
                    onSave(newLim)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Simpan Limit")
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
fun WarungSearchBarWithFilter(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeTab: Int,
    selectedStatusFilter: String = "Semua Status",
    onStatusFilterChange: (String) -> Unit = {},
    selectedTimeFilter: String = "Semua Waktu",
    onTimeFilterChange: (String) -> Unit = {},
    selectedPriorityFilter: String = "Semua Prioritas",
    onPriorityFilterChange: (String) -> Unit = {},
    selectedNoteStatusFilter: String = "Semua Item",
    onNoteStatusFilterChange: (String) -> Unit = {},
    placeholderText: String = "Cari barang, warung, atau rincian...",
    modifier: Modifier = Modifier
) {
    var showFilterDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(placeholderText, fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pencarian",
                        tint = SageGreenPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Hapus Pencarian",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SageGreenPrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("warung_search_input")
            )

            Box {
                val hasActiveFilter = (selectedStatusFilter != "Semua Status") ||
                        (selectedTimeFilter != "Semua Waktu") ||
                        (selectedPriorityFilter != "Semua Prioritas") ||
                        (selectedNoteStatusFilter != "Semua Item")

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (hasActiveFilter) SageGreenPrimary else Color.White,
                    border = BorderStroke(
                        1.dp,
                        if (hasActiveFilter) SageGreenPrimary else Color.LightGray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    IconButton(
                        onClick = { showFilterDropdown = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("warung_filter_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (hasActiveFilter) Color.White else SageGreenPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showFilterDropdown,
                    onDismissRequest = { showFilterDropdown = false },
                    modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .width(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ Filter Transaksi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTextDark
                            )
                            IconButton(
                                onClick = { showFilterDropdown = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = SoftCreamCanvas)

                        when (activeTab) {
                            0 -> {
                                StandardStatusFilterSection(selectedStatusFilter, onStatusFilterChange)
                                StandardTimeFilterSection(selectedTimeFilter, onTimeFilterChange)
                            }
                            1, 2, 4 -> {
                                StandardTimeFilterSection(selectedTimeFilter, onTimeFilterChange)
                            }
                            3 -> {
                                DebtStatusFilterSection(selectedStatusFilter, onStatusFilterChange)
                                StandardTimeFilterSection(selectedTimeFilter, onTimeFilterChange)
                            }
                            5 -> {
                                PriorityFilterSection(selectedPriorityFilter, onPriorityFilterChange)
                                NoteStatusFilterSection(selectedNoteStatusFilter, onNoteStatusFilterChange)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                onStatusFilterChange("Semua Status")
                                onTimeFilterChange("Semua Waktu")
                                onPriorityFilterChange("Semua Prioritas")
                                onNoteStatusFilterChange("Semua Item")
                                showFilterDropdown = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Reset Semua Filter",
                                fontSize = 12.sp,
                                color = WarungRedPastelText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Filter Chips Row
        val activeChips = mutableListOf<Pair<String, () -> Unit>>()
        if (selectedStatusFilter != "Semua Status") {
            activeChips.add("Status: $selectedStatusFilter" to { onStatusFilterChange("Semua Status") })
        }
        if (selectedTimeFilter != "Semua Waktu") {
            activeChips.add("Waktu: $selectedTimeFilter" to { onTimeFilterChange("Semua Waktu") })
        }
        if (selectedPriorityFilter != "Semua Prioritas") {
            activeChips.add("Prioritas: $selectedPriorityFilter" to { onPriorityFilterChange("Semua Prioritas") })
        }
        if (selectedNoteStatusFilter != "Semua Item") {
            activeChips.add("Item: $selectedNoteStatusFilter" to { onNoteStatusFilterChange("Semua Item") })
        }

        if (activeChips.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Filter aktif:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SoftTextMuted
                    )
                }
                items(activeChips) { (label, onClear) ->
                    ActiveFilterChip(label = label, onClear = onClear)
                }
            }
        }
    }
}

@Composable
private fun StandardStatusFilterSection(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SoftTextMuted)
        listOf("Semua Status", "Lunas", "Utang").forEach { option ->
            FilterDropdownOptionRow(
                label = option,
                isSelected = selected == option,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun DebtStatusFilterSection(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Status Hutang", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SoftTextMuted)
        listOf("Semua Status", "Utang", "Lunas").forEach { option ->
            FilterDropdownOptionRow(
                label = option,
                isSelected = selected == option,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun StandardTimeFilterSection(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Rentang Waktu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SoftTextMuted)
        listOf("Semua Waktu", "Hari Ini", "7 Hari Terakhir", "Bulan Ini").forEach { option ->
            FilterDropdownOptionRow(
                label = option,
                isSelected = selected == option,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun PriorityFilterSection(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Prioritas Item", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SoftTextMuted)
        listOf("Semua Prioritas", "Tinggi", "Sedang", "Rendah").forEach { option ->
            FilterDropdownOptionRow(
                label = option,
                isSelected = selected == option,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun NoteStatusFilterSection(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Status Pembelian", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SoftTextMuted)
        listOf("Semua Item", "Belum Dibeli", "Sudah Dibeli").forEach { option ->
            FilterDropdownOptionRow(
                label = option,
                isSelected = selected == option,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun FilterDropdownOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) WarungGreenPastelBg else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) SageGreenPrimary else SoftTextDark,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SageGreenPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = WarungGreenPastelBg,
        border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SageGreenPrimary
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(SageGreenPrimary)
                    .clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus filter",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
