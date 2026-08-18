package com.example.ui.penghasilan

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AdditionalIncome
import com.example.data.entity.MainSalaryConfig
import com.example.ui.BarChartItem
import com.example.ui.FinancialCycleSummary
import com.example.ui.common.PaycheckPeriodNavigatorCard
import com.example.ui.theme.DustyRoseAccent
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import com.example.ui.theme.SoftTextMuted
import com.example.ui.util.Formatters
import com.example.ui.util.PaycheckCycleHelper
import com.example.ui.util.PaycheckPeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenghasilanScreen(
    mainSalaryConfig: MainSalaryConfig?,
    additionalIncomes: List<AdditionalIncome>,
    financialSummary: FinancialCycleSummary? = null,
    monthlyChartData: List<BarChartItem> = emptyList(),
    yearlyChartData: List<BarChartItem> = emptyList(),
    currentPeriod: PaycheckPeriod? = null,
    paycheckStartDay: Int = 25,
    onPrevCycle: () -> Unit = {},
    onNextCycle: () -> Unit = {},
    onResetCycle: () -> Unit = {},
    onUpdatePaycheckStartDay: (Int) -> Unit = {},
    onSetMainSalary: (nominal: Double, catatan: String) -> Unit = { _, _ -> },
    onAddAdditionalIncome: (
        judul: String,
        kategori: String,
        nominal: Double,
        tanggal: String,
        isActive: Boolean,
        targetCycleOffset: Int,
        targetCycleLabel: String,
        catatan: String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onUpdateAdditionalIncome: (AdditionalIncome) -> Unit = {},
    onToggleAdditionalIncome: (id: Int, isActive: Boolean, targetCycleOffset: Int, targetCycleLabel: String) -> Unit = { _, _, _, _ -> },
    onDeleteAdditionalIncome: (id: Int) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var chartMode by remember { mutableStateOf("monthly") } // "monthly" or "yearly"

    val activePeriod = currentPeriod ?: remember(paycheckStartDay) {
        PaycheckCycleHelper.calculatePeriod(paycheckStartDay, 0)
    }

    val activeFinancialSummary = financialSummary ?: remember(mainSalaryConfig, additionalIncomes) {
        val base = mainSalaryConfig?.nominal ?: 0.0
        val add = additionalIncomes.filter { it.isActive }.sumOf { it.nominal }
        FinancialCycleSummary(
            mainSalary = base,
            additionalIncomeTotal = add,
            totalIncome = base + add,
            totalExpense = 0.0,
            remainingBalance = base + add,
            isDeficit = false,
            expensePercentage = 0f
        )
    }

    // Dialog states
    var showEditSalaryDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var editingIncome by remember { mutableStateOf<AdditionalIncome?>(null) }
    var togglingIncomeTarget by remember { mutableStateOf<AdditionalIncome?>(null) }
    var deleteCandidateIncome by remember { mutableStateOf<AdditionalIncome?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sumber Penghasilan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTextDark
                        )
                        Text(
                            text = "Siklus ${activePeriod.displayPeriod}",
                            fontSize = 11.sp,
                            color = SageGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_penghasilan")
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
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddIncomeDialog = true },
                    containerColor = SageGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_additional_income")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Penghasilan Tambahan"
                    )
                }
            }
        },
        containerColor = SoftCreamCanvas,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = SageGreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SageGreenPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Gaji Pokok & Grafik",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_gaji_utama")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Penghasilan Tambahan",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (additionalIncomes.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedTab == 1) SageGreenPrimary else Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = additionalIncomes.size.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_penghasilan_tambahan")
                )
            }

            // Tab Content
            if (selectedTab == 0) {
                GajiUtamaTabContent(
                    mainSalaryConfig = mainSalaryConfig,
                    financialSummary = activeFinancialSummary,
                    currentPeriod = activePeriod,
                    paycheckStartDay = paycheckStartDay,
                    chartMode = chartMode,
                    monthlyChartData = monthlyChartData,
                    yearlyChartData = yearlyChartData,
                    onPrevCycle = onPrevCycle,
                    onNextCycle = onNextCycle,
                    onResetCycle = onResetCycle,
                    onChangeChartMode = { chartMode = it },
                    onEditSalaryClick = { showEditSalaryDialog = true }
                )
            } else {
                PenghasilanTambahanTabContent(
                    additionalIncomes = additionalIncomes,
                    currentPeriod = activePeriod,
                    onToggle = { income ->
                        if (!income.isActive) {
                            // Turning ON -> Show popup dialog to choose cycle allocation
                            togglingIncomeTarget = income
                        } else {
                            // Turning OFF -> directly deactivate
                            onToggleAdditionalIncome(income.id, false, 0, "")
                            Toast.makeText(context, "${income.judul} dinonaktifkan dari siklus ini", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onEdit = { income -> editingIncome = income },
                    onDelete = { income -> deleteCandidateIncome = income },
                    onAddNew = { showAddIncomeDialog = true }
                )
            }
        }
    }

    // --- DIALOGS ---

    // 1. Edit Main Salary Dialog
    if (showEditSalaryDialog) {
        EditMainSalaryDialog(
            currentNominal = mainSalaryConfig?.nominal ?: 0.0,
            currentNotes = mainSalaryConfig?.catatan ?: "",
            startDay = paycheckStartDay,
            onDismiss = { showEditSalaryDialog = false },
            onSave = { nominal, notes ->
                onSetMainSalary(nominal, notes)
                showEditSalaryDialog = false
                Toast.makeText(context, "Gaji Pokok berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Add / Edit Additional Income Dialog
    if (showAddIncomeDialog || editingIncome != null) {
        AddEditAdditionalIncomeDialog(
            income = editingIncome,
            currentPeriod = activePeriod,
            onDismiss = {
                showAddIncomeDialog = false
                editingIncome = null
            },
            onSave = { judul, kategori, nominal, tanggal, isActive, offset, label, catatan ->
                if (editingIncome != null) {
                    val updated = editingIncome!!.copy(
                        judul = judul,
                        kategori = kategori,
                        nominal = nominal,
                        tanggal = tanggal,
                        isActive = isActive,
                        targetCycleOffset = offset,
                        targetCycleLabel = label,
                        catatan = catatan
                    )
                    onUpdateAdditionalIncome(updated)
                    Toast.makeText(context, "Penghasilan tambahan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    onAddAdditionalIncome(judul, kategori, nominal, tanggal, isActive, offset, label, catatan)
                    Toast.makeText(context, "Penghasilan tambahan berhasil dicatat", Toast.LENGTH_SHORT).show()
                }
                showAddIncomeDialog = false
                editingIncome = null
            }
        )
    }

    // 3. Toggle Allocation Popup Dialog
    togglingIncomeTarget?.let { income ->
        val nextPeriod = remember(paycheckStartDay) {
            PaycheckCycleHelper.calculatePeriod(startDay = paycheckStartDay, offset = 1)
        }
        ToggleAllocationDialog(
            income = income,
            currentPeriod = activePeriod,
            nextPeriod = nextPeriod,
            onDismiss = { togglingIncomeTarget = null },
            onSelectAllocation = { offset, label ->
                onToggleAdditionalIncome(income.id, true, offset, label)
                togglingIncomeTarget = null
                Toast.makeText(context, "${income.judul} diaktifkan untuk $label", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 4. Delete Confirmation Dialog
    deleteCandidateIncome?.let { income ->
        AlertDialog(
            onDismissRequest = { deleteCandidateIncome = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Hapus Penghasilan Tambahan?") },
            text = { Text("Apakah Anda yakin ingin menghapus '${income.judul}' (${Formatters.formatRupiah(income.nominal)})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAdditionalIncome(income.id)
                        deleteCandidateIncome = null
                        Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateIncome = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ==========================================
// SUB-COMPONENT: Gaji Utama Tab Content
// ==========================================
@Composable
private fun GajiUtamaTabContent(
    mainSalaryConfig: MainSalaryConfig?,
    financialSummary: FinancialCycleSummary,
    currentPeriod: PaycheckPeriod,
    paycheckStartDay: Int,
    chartMode: String,
    monthlyChartData: List<BarChartItem>,
    yearlyChartData: List<BarChartItem>,
    onPrevCycle: () -> Unit,
    onNextCycle: () -> Unit,
    onResetCycle: () -> Unit,
    onChangeChartMode: (String) -> Unit,
    onEditSalaryClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Paycheck Cycle Navigator
        item {
            PaycheckPeriodNavigatorCard(
                currentPeriod = currentPeriod,
                onPrevCycle = onPrevCycle,
                onNextCycle = onNextCycle,
                onResetCycle = onResetCycle,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Gaji Pokok Active Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, SageGreenPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_main_salary")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SageGreenPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gaji Pokok Bulanan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Siklus Gajian Tgl $paycheckStartDay",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onEditSalaryClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_edit_main_salary")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Gaji",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (mainSalaryConfig != null && mainSalaryConfig.nominal > 0) "Ubah" else "Atur Gaji",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Nominal Display
                    val nominal = mainSalaryConfig?.nominal ?: 0.0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SageGreenPrimary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "Nominal Gaji Pokok Terdaftar",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatRupiah(nominal),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SageGreenPrimary
                            )
                            if (!mainSalaryConfig?.catatan.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Catatan: ${mainSalaryConfig?.catatan}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = "ℹ️ Gaji pokok ini otomatis berlaku untuk setiap siklus baru (mulai tgl $paycheckStartDay) tanpa perlu diinput berulang kali.",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 3. Status Keuangan & Sisa Gaji Card (Deficit indicator)
        item {
            FinancialCycleStatusCard(financialSummary = financialSummary)
        }

        // 4. Grouped Bar Chart (Grafik Batang Ganda Pendapatan vs Pengeluaran)
        item {
            GroupedBarChartSection(
                chartMode = chartMode,
                monthlyChartData = monthlyChartData,
                yearlyChartData = yearlyChartData,
                onChangeChartMode = onChangeChartMode
            )
        }
    }
}

// ==========================================
// SUB-COMPONENT: Status Keuangan & Sisa Gaji Card
// ==========================================
@Composable
fun FinancialCycleStatusCard(
    financialSummary: FinancialCycleSummary,
    modifier: Modifier = Modifier
) {
    val isDeficit = financialSummary.isDeficit
    val cardBgColor = if (isDeficit) Color(0xFFFFEBEE) else Color.White
    val cardBorderColor = if (isDeficit) Color(0xFFFFCDD2) else SageGreenPrimaryContainer
    val statusColor = if (isDeficit) Color(0xFFD32F2F) else SageGreenPrimary

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.2.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_financial_status")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDeficit) Icons.Default.Warning else Icons.Default.Savings,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Status Keuangan Siklus Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isDeficit) "⚠️ Terjadi Defisit Anggaran" else "✓ Keuangan Terkendali (Surplus)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isDeficit) "DEFISIT" else "SURPLUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Big Balance Indicator (Sisa Gaji / Minus)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDeficit) Color(0xFFFFCDD2).copy(alpha = 0.4f) else SoftCreamCanvas.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isDeficit) "Defisit Keuangan (Minus)" else "Sisa Anggaran / Gaji",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val remainingText = if (isDeficit) {
                            "- " + Formatters.formatRupiah(Math.abs(financialSummary.remainingBalance))
                        } else {
                            Formatters.formatRupiah(financialSummary.remainingBalance)
                        }
                        Text(
                            text = remainingText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }

                    Icon(
                        imageVector = if (isDeficit) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Progress Bar of Expense vs Income
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pemakaian Anggaran",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val percentStr = (financialSummary.expensePercentage * 100).toInt()
                    Text(
                        text = "$percentStr%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                LinearProgressIndicator(
                    progress = { financialSummary.expensePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = Color.LightGray.copy(alpha = 0.4f)
                )
            }

            HorizontalDivider(color = cardBorderColor)

            // Income Breakdown vs Total Expenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Income Column
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Pendapatan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = Formatters.formatRupiah(financialSummary.totalIncome),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pokok: ${Formatters.formatRupiah(financialSummary.mainSalary)}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (financialSummary.additionalIncomeTotal > 0) {
                            Text(
                                text = "+ Tambahan: ${Formatters.formatRupiah(financialSummary.additionalIncomeTotal)}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = SageGreenPrimary
                            )
                        }
                    }
                }

                // Total Expense Column
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, DustyRoseAccent.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Pengeluaran", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = Formatters.formatRupiah(financialSummary.totalExpense),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DustyRoseAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Semua Modul Dipta Home",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-COMPONENT: Grouped Bar Chart (Batang Ganda)
// ==========================================
@Composable
fun GroupedBarChartSection(
    chartMode: String, // "monthly" or "yearly"
    monthlyChartData: List<BarChartItem>,
    yearlyChartData: List<BarChartItem>,
    onChangeChartMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = if (chartMode == "monthly") monthlyChartData else yearlyChartData

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, SageGreenPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_grouped_bar_chart")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title & Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SageGreenPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Grafik Batang Ganda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Pendapatan vs Pengeluaran",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Filter Buttons: Bulanan vs Tahunan
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartMode == "monthly",
                        onClick = { onChangeChartMode("monthly") },
                        label = { Text("Bulanan", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("btn_chart_monthly")
                    )
                    FilterChip(
                        selected = chartMode == "yearly",
                        onClick = { onChangeChartMode("yearly") },
                        label = { Text("Tahunan", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("btn_chart_yearly")
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SageGreenPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pendapatan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SageGreenPrimary)

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(DustyRoseAccent)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pengeluaran", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DustyRoseAccent)
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data riwayat perbandingan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Grouped Bar Canvas
                GroupedBarChartCanvas(
                    items = items,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Quick Scannable Summary Table
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items.takeLast(3).forEach { item ->
                        val isSurplus = item.net >= 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftCreamCanvas.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTextDark
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "In: ${Formatters.formatRupiah(item.income)}",
                                        fontSize = 10.sp,
                                        color = SageGreenPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Out: ${Formatters.formatRupiah(item.expense)}",
                                        fontSize = 10.sp,
                                        color = DustyRoseAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = (if (isSurplus) "+ " else "- ") + Formatters.formatRupiah(Math.abs(item.net)),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSurplus) SageGreenPrimary else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Draw Canvas for Grouped Bar Chart
@Composable
private fun GroupedBarChartCanvas(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    val maxVal = remember(items) {
        val highest = items.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
        if (highest <= 0.0) 100000.0 else highest * 1.15
    }

    val incomeColor = SageGreenPrimary
    val expenseColor = DustyRoseAccent

    Canvas(modifier = modifier.padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 8.dp)) {
        val width = size.width
        val height = size.height
        val groupCount = items.size
        if (groupCount == 0) return@Canvas

        val groupWidth = width / groupCount
        val barWidth = (groupWidth * 0.35f).coerceAtMost(28f)
        val barSpacing = 4f

        items.forEachIndexed { index, item ->
            val groupCenterX = (index * groupWidth) + (groupWidth / 2)

            val incomeHeight = ((item.income / maxVal) * height).toFloat().coerceIn(4f, height)
            val expenseHeight = ((item.expense / maxVal) * height).toFloat().coerceIn(4f, height)

            val incomeLeft = groupCenterX - barWidth - (barSpacing / 2)
            val expenseLeft = groupCenterX + (barSpacing / 2)

            // Draw Income Bar (Left bar of group)
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(incomeLeft, height - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Draw Expense Bar (Right bar of group)
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(expenseLeft, height - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Draw Bottom Label
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(
                    item.label,
                    groupCenterX,
                    height + 32f,
                    paint
                )
            }
        }
    }
}

// ==========================================
// SUB-COMPONENT: Penghasilan Tambahan Tab Content
// ==========================================
@Composable
private fun PenghasilanTambahanTabContent(
    additionalIncomes: List<AdditionalIncome>,
    currentPeriod: PaycheckPeriod,
    onToggle: (AdditionalIncome) -> Unit,
    onEdit: (AdditionalIncome) -> Unit,
    onDelete: (AdditionalIncome) -> Unit,
    onAddNew: () -> Unit
) {
    if (additionalIncomes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SageGreenPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Belum Ada Penghasilan Tambahan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Catat pemasukan ekstra seperti lemburan, bonus, tunjangan, atau uang kaget untuk menambah daya beli siklus gajian Anda.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_empty_add_income")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Penghasilan Tambahan")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Sumber Tambahan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gunakan tombol ON/OFF untuk memasukkan ke siklus gajian",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${additionalIncomes.count { it.isActive }} Aktif",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageGreenPrimary
                    )
                }
            }

            items(additionalIncomes, key = { it.id }) { income ->
                AdditionalIncomeItemCard(
                    income = income,
                    onToggle = { onToggle(income) },
                    onEdit = { onEdit(income) },
                    onDelete = { onDelete(income) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // Space for FAB
            }
        }
    }
}

// Item Card for Additional Income with interactive ON/OFF Toggle
@Composable
private fun AdditionalIncomeItemCard(
    income: AdditionalIncome,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryIcon = when (income.kategori.lowercase()) {
        "lemburan" -> Icons.Default.Schedule
        "bonus" -> Icons.Default.CardGiftcard
        "freelance" -> Icons.Default.Work
        "thr", "hadiah", "uang kaget" -> Icons.Default.MonetizationOn
        else -> Icons.Default.Savings
    }

    val activeColor = SageGreenPrimary
    val isItemActive = income.isActive

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isItemActive) Color.White else Color.White.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = if (isItemActive) 1.2.dp else 0.8.dp,
            color = if (isItemActive) SageGreenPrimaryContainer else Color.LightGray.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isItemActive) 1.5.dp else 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("income_item_${income.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isItemActive) SageGreenPrimaryContainer else Color.LightGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = if (isItemActive) SageGreenPrimary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = income.judul,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isItemActive) SoftTextDark else Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isItemActive) SageGreenPrimary.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = income.kategori,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isItemActive) SageGreenPrimary else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (income.tanggal.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = income.tanggal,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Interactive ON/OFF Switch
                Switch(
                    checked = isItemActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SageGreenPrimary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("switch_toggle_income_${income.id}")
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Formatters.formatRupiah(income.nominal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isItemActive) SageGreenPrimary else Color.Gray
                    )
                    // Cycle Target Label
                    val cycleText = when {
                        !isItemActive -> "Status: Nonaktif (Tidak dialokasikan)"
                        income.targetCycleLabel.isNotBlank() -> "Alokasi: ${income.targetCycleLabel}"
                        income.targetCycleOffset == 1 -> "Alokasi: Siklus Bulan Depan"
                        income.targetCycleOffset == 0 -> "Alokasi: Siklus Berjalan"
                        else -> "Alokasi: Cadangan"
                    }
                    Text(
                        text = cycleText,
                        fontSize = 10.sp,
                        fontWeight = if (isItemActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isItemActive) SageGreenPrimary else Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("btn_edit_income_${income.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("btn_delete_income_${income.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (income.catatan.isNotBlank()) {
                Text(
                    text = "Catatan: ${income.catatan}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// DIALOG: Edit Main Salary
// ==========================================
@Composable
private fun EditMainSalaryDialog(
    currentNominal: Double,
    currentNotes: String,
    startDay: Int,
    onDismiss: () -> Unit,
    onSave: (nominal: Double, notes: String) -> Unit
) {
    var nominalText by remember {
        mutableStateOf(if (currentNominal > 0) currentNominal.toLong().toString() else "")
    }
    var notesText by remember { mutableStateOf(currentNotes) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = SageGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atur Gaji Pokok Bulanan")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Gaji pokok akan otomatis aktif dan mengikuti siklus gajian (Mulai tanggal $startDay setiap bulan).",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = {
                        nominalText = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("Nominal Gaji Pokok (Rp)") },
                    placeholder = { Text("Contoh: 7500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage.isNotBlank(),
                    supportingText = if (errorMessage.isNotBlank()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("input_main_salary_nominal")
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Misal: Gaji Pokok PT Maju Bersama") },
                    modifier = Modifier.fillMaxWidth().testTag("input_main_salary_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nominal = nominalText.toDoubleOrNull() ?: 0.0
                    if (nominal <= 0) {
                        errorMessage = "Nominal gaji pokok harus lebih dari 0"
                        return@Button
                    }
                    onSave(nominal, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                modifier = Modifier.testTag("btn_save_main_salary")
            ) {
                Text("Simpan Gaji", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// DIALOG: Add / Edit Additional Income
// ==========================================
@Composable
private fun AddEditAdditionalIncomeDialog(
    income: AdditionalIncome?,
    currentPeriod: PaycheckPeriod,
    onDismiss: () -> Unit,
    onSave: (
        judul: String,
        kategori: String,
        nominal: Double,
        tanggal: String,
        isActive: Boolean,
        targetCycleOffset: Int,
        targetCycleLabel: String,
        catatan: String
    ) -> Unit
) {
    val isEdit = income != null
    var judul by remember { mutableStateOf(income?.judul ?: "") }
    var kategori by remember { mutableStateOf(income?.kategori ?: "Lemburan") }
    var nominalText by remember {
        mutableStateOf(if (income != null && income.nominal > 0) income.nominal.toLong().toString() else "")
    }
    var tanggal by remember {
        mutableStateOf(income?.tanggal ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var isActive by remember { mutableStateOf(income?.isActive ?: true) }
    var targetOffset by remember { mutableIntStateOf(income?.targetCycleOffset ?: 0) }
    var catatan by remember { mutableStateOf(income?.catatan ?: "") }
    var errorJudul by remember { mutableStateOf(false) }
    var errorNominal by remember { mutableStateOf(false) }

    val categories = listOf("Lemburan", "Bonus", "Freelance", "THR", "Hadiah", "Uang Kaget", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEdit) "Ubah Penghasilan Tambahan" else "Catat Penghasilan Tambahan")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = judul,
                        onValueChange = {
                            judul = it
                            errorJudul = false
                        },
                        label = { Text("Judul / Sumber Pemasukan *") },
                        placeholder = { Text("Misal: Lemburan Project X, Bonus Tahunan") },
                        isError = errorJudul,
                        supportingText = if (errorJudul) {
                            { Text("Judul wajib diisi", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth().testTag("input_income_judul")
                    )
                }

                item {
                    Text("Kategori:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(4).forEach { cat ->
                            FilterChip(
                                selected = kategori == cat,
                                onClick = { kategori = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.drop(4).forEach { cat ->
                            FilterChip(
                                selected = kategori == cat,
                                onClick = { kategori = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = nominalText,
                        onValueChange = {
                            nominalText = it.filter { char -> char.isDigit() }
                            errorNominal = false
                        },
                        label = { Text("Nominal Pemasukan (Rp) *") },
                        placeholder = { Text("Contoh: 500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorNominal,
                        supportingText = if (errorNominal) {
                            { Text("Nominal harus lebih dari 0", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth().testTag("input_income_nominal")
                    )
                }

                item {
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = { tanggal = it },
                        label = { Text("Tanggal Penerimaan (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_income_tanggal")
                    )
                }

                item {
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_income_catatan")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (judul.isBlank()) {
                        errorJudul = true
                        return@Button
                    }
                    val nominal = nominalText.toDoubleOrNull() ?: 0.0
                    if (nominal <= 0) {
                        errorNominal = true
                        return@Button
                    }
                    onSave(
                        judul.trim(),
                        kategori,
                        nominal,
                        tanggal,
                        isActive,
                        targetOffset,
                        currentPeriod.label,
                        catatan.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                modifier = Modifier.testTag("btn_save_income")
            ) {
                Text(if (isEdit) "Simpan Perubahan" else "Tambah Pemasukan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// DIALOG: Toggle Allocation Option Selector
// ==========================================
@Composable
private fun ToggleAllocationDialog(
    income: AdditionalIncome,
    currentPeriod: PaycheckPeriod,
    nextPeriod: PaycheckPeriod,
    onDismiss: () -> Unit,
    onSelectAllocation: (offset: Int, label: String) -> Unit
) {
    var selectedOption by remember { mutableIntStateOf(0) } // 0 = Current Cycle, 1 = Next Cycle, 2 = Saldo Cadangan

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = SageGreenPrimary
            )
        },
        title = { Text("Alokasi Penghasilan Tambahan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pemasukan '${income.judul}' (${Formatters.formatRupiah(income.nominal)}) akan dialokasikan ke siklus mana?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Current Cycle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedOption == 0) SageGreenPrimary.copy(alpha = 0.12f) else Color.White,
                    border = BorderStroke(1.dp, if (selectedOption == 0) SageGreenPrimary else Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOption = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == 0,
                            onClick = { selectedOption = 0 }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Siklus Berjalan (${currentPeriod.displayPeriod})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Menambah budget/daya beli siklus bulan ini",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Next Cycle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedOption == 1) SageGreenPrimary.copy(alpha = 0.12f) else Color.White,
                    border = BorderStroke(1.dp, if (selectedOption == 1) SageGreenPrimary else Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOption = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == 1,
                            onClick = { selectedOption = 1 }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Siklus Bulan Depan (${nextPeriod.displayPeriod})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Disimpan untuk anggaran belanja bulan berikutnya",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 3: Saldo Cadangan
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedOption == 2) SageGreenPrimary.copy(alpha = 0.12f) else Color.White,
                    border = BorderStroke(1.dp, if (selectedOption == 2) SageGreenPrimary else Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOption = 2 }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == 2,
                            onClick = { selectedOption = 2 }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Saldo Cadangan / Tabungan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Disimpan sebagai dana darurat / tabungan ekstra",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedOption) {
                        0 -> onSelectAllocation(0, currentPeriod.label)
                        1 -> onSelectAllocation(1, nextPeriod.label)
                        else -> onSelectAllocation(99, "Saldo Cadangan")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                modifier = Modifier.testTag("btn_confirm_allocation")
            ) {
                Text("Terapkan Alokasi", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
