package com.example.ui.hub

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import com.example.ui.common.DeleteConfirmationDialog
import com.example.ui.common.PaycheckCycleSettingsDialog
import com.example.ui.common.PaycheckPeriodNavigatorCard
import com.example.ui.util.PaycheckCycleHelper
import com.example.ui.util.PaycheckPeriod
import com.example.ui.theme.SoftTextDark
import com.example.ui.theme.SoftTextMuted
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.common.BackupRestoreDialog
import com.example.ui.common.DateRangeFilterDialog
import com.example.data.entity.AdditionalIncome
import com.example.data.entity.MainSalaryConfig
import com.example.ui.BarChartItem
import com.example.ui.FinancialCycleSummary
import com.example.ui.penghasilan.FinancialCycleStatusCard
import com.example.ui.penghasilan.GroupedBarChartSection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.OtherHouses
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.VolunteerActivism
import com.example.R
import com.example.data.backup.BackupResult
import com.example.data.entity.ChildExpenseLog
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.ElectricityLog
import com.example.data.entity.FuelLog
import com.example.data.entity.OilLog
import com.example.data.entity.ServiceLog
import com.example.data.entity.SocialLog
import com.example.data.entity.Vehicle
import com.example.data.firebase.SyncStatus
import com.example.ui.MonthlyExpenseSummary
import com.example.ui.theme.DustyRoseAccent
import com.example.ui.theme.ElectricityOrangePastelBg
import com.example.ui.theme.ElectricityOrangePastelIcon
import com.example.ui.theme.FuelBluePastelBg
import com.example.ui.theme.FuelBluePastelIcon
import com.example.ui.theme.JimpitanTealPastelBg
import com.example.ui.theme.JimpitanTealPastelIcon
import com.example.ui.theme.OilYellowPastelBg
import com.example.ui.theme.OilYellowPastelIcon
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.ServisPurplePastelBg
import com.example.ui.theme.ServisPurplePastelIcon
import com.example.ui.theme.WarungGreenPastelBg
import com.example.ui.theme.WarungGreenPastelIcon
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.TableChart
import com.example.data.entity.RandomExpense
import com.example.data.entity.WarungDebt
import com.example.ui.export.ExportReportDialog
import com.example.util.ExportReportManager
import com.example.ui.util.Formatters
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun formatSyncTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Belum pernah sinkron"

    val nowCalendar = Calendar.getInstance()
    val syncCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }

    val timeFormat = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID"))
    val timeStr = timeFormat.format(Date(timestamp))

    val isToday = nowCalendar.get(Calendar.YEAR) == syncCalendar.get(Calendar.YEAR) &&
            nowCalendar.get(Calendar.DAY_OF_YEAR) == syncCalendar.get(Calendar.DAY_OF_YEAR)

    val isYesterday = nowCalendar.get(Calendar.YEAR) == syncCalendar.get(Calendar.YEAR) &&
            nowCalendar.get(Calendar.DAY_OF_YEAR) - syncCalendar.get(Calendar.DAY_OF_YEAR) == 1

    return when {
        isToday -> "Hari ini, $timeStr"
        isYesterday -> "Kemarin, $timeStr"
        else -> {
            val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
            dateFormat.format(Date(timestamp))
        }
    }
}

data class RecentActivityItem(
    val id: String,
    val category: String,
    val title: String,
    val amount: Double,
    val dateText: String,
    val timestamp: Long,
    val color: Color,
    val icon: ImageVector
)

private fun parseDateToMillis(dateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.parse(dateStr)?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}

private fun formatDateFromMillis(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "-"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    vehicles: List<Vehicle>,
    activeVehicleId: Int,
    latestFuel: FuelLog?,
    latestOil: OilLog?,
    latestElectricity: ElectricityLog?,
    latestService: ServiceLog? = null,
    latestSocial: SocialLog? = null,
    fuelLogs: List<FuelLog> = emptyList(),
    oilLogs: List<OilLog> = emptyList(),
    electricityLogs: List<ElectricityLog> = emptyList(),
    serviceLogs: List<ServiceLog> = emptyList(),
    socialLogs: List<SocialLog> = emptyList(),
    dailyGroceryLogs: List<DailyGroceryLog> = emptyList(),
    randomExpenses: List<RandomExpense> = emptyList(),
    childExpenses: List<ChildExpenseLog> = emptyList(),
    warungDebts: List<WarungDebt> = emptyList(),
    monthlyExpenseSummary: MonthlyExpenseSummary? = null,
    financialSummary: FinancialCycleSummary? = null,
    mainSalaryConfig: MainSalaryConfig? = null,
    additionalIncomes: List<AdditionalIncome> = emptyList(),
    monthlyChartData: List<BarChartItem> = emptyList(),
    yearlyChartData: List<BarChartItem> = emptyList(),
    currentPaycheckPeriod: PaycheckPeriod? = null,
    syncStatus: SyncStatus,
    currentUser: FirebaseUser?,
    onSelectVehicle: (Int) -> Unit,
    onAddVehicle: (nama: String, plat: String, jenis: String, icon: String) -> Unit,
    onDeleteVehicle: (Int) -> Unit,
    onPrevPaycheckCycle: () -> Unit = {},
    onNextPaycheckCycle: () -> Unit = {},
    onResetPaycheckCycle: () -> Unit = {},
    onUpdatePaycheckStartDay: (Int) -> Unit = {},
    onNavigateToPenghasilan: () -> Unit = {},
    onNavigateToBensin: () -> Unit,
    onNavigateToOli: () -> Unit,
    onNavigateToListrik: () -> Unit,
    onNavigateToServis: () -> Unit = {},
    onNavigateToJimpitan: () -> Unit = {},
    onNavigateToWarung: () -> Unit = {},
    onNavigateToAnak: () -> Unit = {},
    onNavigateToResep: () -> Unit = {},
    onSyncToCloud: () -> Unit,
    onSyncFromCloud: () -> Unit,
    onManualSync: () -> Unit = {},
    onConnectFirebase: () -> Unit,
    onSignOutFirebase: () -> Unit,
    onExportBackup: (Uri, (BackupResult) -> Unit) -> Unit,
    onRestoreBackup: (Uri, Boolean, (BackupResult) -> Unit) -> Unit,
    onResetAllData: ((() -> Unit) -> Unit) = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val activePeriod = currentPaycheckPeriod ?: remember { PaycheckCycleHelper.calculatePeriod(25, 0) }

    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var overwriteOption by remember { mutableStateOf(true) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var deleteCandidateVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showPaycheckSettingsDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var isCustomDateRangeActive by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }
    var wasSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(syncStatus.isSyncing) {
        if (wasSyncing && !syncStatus.isSyncing && syncStatus.message.isNotBlank()) {
            Toast.makeText(context, syncStatus.message, Toast.LENGTH_LONG).show()
        }
        wasSyncing = syncStatus.isSyncing
    }

    // Active vehicle object
    val activeVehicle = vehicles.find { it.id == activeVehicleId } ?: vehicles.firstOrNull()

    // Aggregate Recent Activities (Top 10)
    val recentActivities = remember(
        dailyGroceryLogs, randomExpenses, childExpenses, fuelLogs, oilLogs,
        serviceLogs, electricityLogs, socialLogs, warungDebts
    ) {
        val list = mutableListOf<RecentActivityItem>()

        dailyGroceryLogs.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "grocery_${log.id}",
                    category = "Belanja Harian",
                    title = log.rincian.ifBlank { "Belanja Dapur" },
                    amount = log.totalPengeluaran,
                    dateText = log.tanggal,
                    timestamp = log.timestamp,
                    color = DustyRoseAccent,
                    icon = Icons.Default.ShoppingCart
                )
            )
        }

        randomExpenses.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "random_${log.id}",
                    category = "Random",
                    title = log.rincian.ifBlank { log.catatan.ifBlank { "Pengeluaran Random" } },
                    amount = log.totalPengeluaran,
                    dateText = log.tanggal,
                    timestamp = log.timestamp,
                    color = Color(0xFF8E24AA),
                    icon = Icons.Default.ReceiptLong
                )
            )
        }

        childExpenses.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "child_${log.id}",
                    category = "Belanja Anak",
                    title = log.rincian.ifBlank { "Kebutuhan Anak" },
                    amount = log.totalPengeluaran,
                    dateText = log.tanggal,
                    timestamp = log.timestamp,
                    color = Color(0xFFE91E63),
                    icon = Icons.Default.ChildCare
                )
            )
        }

        fuelLogs.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "fuel_${log.id}",
                    category = "Isi Bensin",
                    title = "Isi Bensin ${log.jenis_bbm}",
                    amount = log.nominal.toDouble(),
                    dateText = formatDateFromMillis(log.tanggal),
                    timestamp = log.tanggal,
                    color = FuelBluePastelIcon,
                    icon = Icons.Default.LocalGasStation
                )
            )
        }

        oilLogs.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "oil_${log.id}",
                    category = "Ganti Oli",
                    title = "Ganti ${log.jenis_oli}",
                    amount = log.harga.toDouble(),
                    dateText = formatDateFromMillis(log.tanggal),
                    timestamp = log.tanggal,
                    color = OilYellowPastelIcon,
                    icon = Icons.Default.OilBarrel
                )
            )
        }

        serviceLogs.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "service_${log.id}",
                    category = "Servis",
                    title = log.deskripsi_item.ifBlank { log.kategori },
                    amount = log.total_biaya.toDouble(),
                    dateText = formatDateFromMillis(log.tanggal),
                    timestamp = log.tanggal,
                    color = ServisPurplePastelIcon,
                    icon = Icons.Default.Build
                )
            )
        }

        electricityLogs.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "elec_${log.id}",
                    category = "Listrik",
                    title = "Token Listrik ${log.jumlah_kwh} kWh",
                    amount = log.harga.toDouble(),
                    dateText = formatDateFromMillis(log.tanggal),
                    timestamp = log.tanggal,
                    color = ElectricityOrangePastelIcon,
                    icon = Icons.Default.ElectricBolt
                )
            )
        }

        socialLogs.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "social_${log.id}",
                    category = "Sosial & Jimpitan",
                    title = log.keterangan.ifBlank { log.kategori },
                    amount = log.nominal.toDouble(),
                    dateText = formatDateFromMillis(log.tanggal),
                    timestamp = log.tanggal,
                    color = JimpitanTealPastelIcon,
                    icon = Icons.Default.VolunteerActivism
                )
            )
        }

        warungDebts.forEach { log ->
            list.add(
                RecentActivityItem(
                    id = "warung_${log.id}",
                    category = "Hutang Warung",
                    title = "Hutang: ${log.namaWarung}",
                    amount = log.nominal,
                    dateText = log.tanggal,
                    timestamp = log.timestamp,
                    color = WarungGreenPastelIcon,
                    icon = Icons.Default.Storefront
                )
            )
        }

        list.sortedByDescending { if (it.timestamp > 0) it.timestamp else parseDateToMillis(it.dateText) }.take(10)
    }

    // SAF Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportBackup(uri) { result ->
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showRestoreDialog = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(320.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Drawer Header
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SoftCreamCanvas
                            ),
                            border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(SageGreenPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OtherHouses,
                                            contentDescription = null,
                                            tint = SageGreenPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Dipta Home",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary
                                        )
                                        Text(
                                            text = "Pengaturan & Sistem Central",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (currentUser != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, SageGreenPrimaryContainer.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = SageGreenPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = currentUser.email ?: "Terhubung",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                            TextButton(
                                                onClick = onSignOutFirebase,
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Keluar", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 1: MODUL RUMAH TANGGA
                    item {
                        Text(
                            text = "MODUL RUMAH TANGGA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 1. Sumber Penghasilan
                            DrawerNavRow(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Sumber Penghasilan",
                                subtitle = "Gaji pokok & pemasukan tambahan",
                                tint = SageGreenPrimary,
                                testTag = "drawer_nav_penghasilan",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToPenghasilan()
                                }
                            )

                            // 2. Catat Warung & Belanja
                            DrawerNavRow(
                                icon = Icons.Default.ShoppingCart,
                                label = "Catat Warung & Belanja",
                                subtitle = "Belanja harian, hutang & cicilan",
                                tint = WarungGreenPastelIcon,
                                testTag = "drawer_nav_warung",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToWarung()
                                }
                            )

                            // 3. Resep & Rencana Masak
                            DrawerNavRow(
                                icon = Icons.Default.ReceiptLong,
                                label = "Resep & Rencana Masak",
                                subtitle = "Koleksi resep & menu mingguan",
                                tint = SageGreenPrimary,
                                testTag = "drawer_nav_resep",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToResep()
                                }
                            )

                            // 4. Belanja Anak
                            DrawerNavRow(
                                icon = Icons.Default.ChildCare,
                                label = "Belanja Anak",
                                subtitle = "Kebutuhan & catatan anak",
                                tint = Color(0xFFE91E63),
                                testTag = "drawer_nav_anak",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToAnak()
                                }
                            )

                            // 5. Catat Bensin
                            DrawerNavRow(
                                icon = Icons.Default.LocalGasStation,
                                label = "Catat Bensin",
                                subtitle = "Bahan bakar & odometer",
                                tint = FuelBluePastelIcon,
                                testTag = "drawer_nav_bensin",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToBensin()
                                }
                            )

                            // 6. Catat Oli
                            DrawerNavRow(
                                icon = Icons.Default.OilBarrel,
                                label = "Catat Oli",
                                subtitle = "Oli mesin & gardan",
                                tint = OilYellowPastelIcon,
                                testTag = "drawer_nav_oli",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToOli()
                                }
                            )

                            // 7. Servis & Perawatan
                            DrawerNavRow(
                                icon = Icons.Default.Build,
                                label = "Servis & Perawatan",
                                subtitle = "Perbaikan & suku cadang",
                                tint = ServisPurplePastelIcon,
                                testTag = "drawer_nav_servis",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToServis()
                                }
                            )

                            // 8. Catat kWh Listrik
                            DrawerNavRow(
                                icon = Icons.Default.ElectricBolt,
                                label = "Catat kWh Listrik",
                                subtitle = "Token & meteran listrik",
                                tint = ElectricityOrangePastelIcon,
                                testTag = "drawer_nav_listrik",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToListrik()
                                }
                            )

                            // 9. Setoran Jimpitan & Kurban
                            DrawerNavRow(
                                icon = Icons.Default.VolunteerActivism,
                                label = "Setoran Jimpitan & Kurban",
                                subtitle = "Audit setoran warga & sosial",
                                tint = JimpitanTealPastelIcon,
                                testTag = "drawer_nav_jimpitan",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToJimpitan()
                                }
                            )
                        }
                    }

                    // Section 2: SISTEM & CADANGAN
                    item {
                        Text(
                            text = "SISTEM & CADANGAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Unified Backup & Restore in Drawer
                            DrawerNavRow(
                                icon = Icons.Default.CloudSync,
                                label = "Backup & Restore",
                                subtitle = "Cloud Firebase & Cadangan .json",
                                tint = SageGreenPrimary,
                                badge = if (syncStatus.isSyncing) "Syncing" else if (syncStatus.isSuccess) "Aktif" else "Lokal",
                                testTag = "drawer_nav_backup_restore",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    showBackupRestoreDialog = true
                                }
                            )

                            // Cetak & Export Laporan
                            DrawerNavRow(
                                icon = Icons.Default.Summarize,
                                label = "Cetak & Export Laporan",
                                subtitle = "Format PDF Formal & Excel (.xlsx)",
                                tint = SageGreenPrimary,
                                testTag = "drawer_nav_export",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    showExportDialog = true
                                }
                            )

                            // Pengaturan Siklus Gaji
                            DrawerNavRow(
                                icon = Icons.Default.Schedule,
                                label = "Siklus Gajian (Tgl ${activePeriod.startDay})",
                                subtitle = activePeriod.displayPeriod,
                                tint = SageGreenPrimary,
                                testTag = "drawer_nav_paycheck_settings",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    showPaycheckSettingsDialog = true
                                }
                            )

                            // Garasi & Tambah Kendaraan
                            DrawerNavRow(
                                icon = Icons.Default.Garage,
                                label = "Garasi Kendaraan (${vehicles.size})",
                                subtitle = activeVehicle?.let { "Aktif: ${it.nama_kendaraan} (${it.nomor_plat})" } ?: "Kelola & tambah kendaraan",
                                tint = SageGreenPrimary,
                                testTag = "drawer_nav_garage",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    showAddVehicleDialog = true
                                }
                            )
                        }
                    }

                    // Section 3: PEMELIHARAAN SISTEM
                    item {
                        Text(
                            text = "PEMELIHARAAN SISTEM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Compact Icon Menu 1: Reset Catatan
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFEBEE),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        showResetDialog = true
                                    }
                                    .testTag("btn_reset_all_data")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD32F2F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Bersihkan Catatan",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Reset Data",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                        Text(
                                            text = "Bersihkan Catatan",
                                            fontSize = 9.sp,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }

                            // Compact Icon Menu 2: Status Sistem / Database
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftCreamCanvas,
                                border = BorderStroke(1.dp, SageGreenPrimaryContainer),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SageGreenPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Status Sistem OK",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Sistem Normal",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary
                                        )
                                        Text(
                                            text = "Database Aktif",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Drawer Footer
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Dipta Home v1.1 • Offline First + Cloud Backup",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) {
        Scaffold { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Full-width Image Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                                .testTag("hub_header_card")
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hub_header_1785230264470),
                                contentDescription = "Header Dipta Home",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.4f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.15f)
                                            )
                                        )
                                    )
                            )

                            // Hamburger Menu Button (Garis 3) di sudut kiri atas banner
                            Surface(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(start = 16.dp, top = 16.dp)
                                    .align(Alignment.TopStart),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.9f),
                                shadowElevation = 4.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch { drawerState.open() }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("hamburger_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu Navigasi",
                                        tint = SoftTextDark
                                    )
                                }
                            }
                        }
                    }

                    // 2. Ringkasan Keuangan (Dashboard Summary with Paycheck Cycle Filter)
                    item {
                        fun isDateInActiveRange(timestamp: Long = 0L, dateStr: String = ""): Boolean {
                            val dateToTest = if (dateStr.isNotBlank()) {
                                dateStr
                            } else if (timestamp > 0L) {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
                            } else ""

                            if (isCustomDateRangeActive && customStartDate.isNotBlank() && customEndDate.isNotBlank()) {
                                if (dateToTest.isBlank()) return false
                                return dateToTest >= customStartDate && dateToTest <= customEndDate
                            } else {
                                return activePeriod.contains(timestamp = timestamp, dateStr = dateStr)
                            }
                        }

                        val totalBelanja = if (isCustomDateRangeActive) {
                            remember(dailyGroceryLogs, customStartDate, customEndDate) {
                                dailyGroceryLogs.filter { isDateInActiveRange(it.timestamp, it.tanggal) }
                                    .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
                            }
                        } else {
                            monthlyExpenseSummary?.totalBelanja ?: remember(dailyGroceryLogs, activePeriod) {
                                dailyGroceryLogs.filter { activePeriod.contains(it.timestamp, it.tanggal) }
                                    .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
                            }
                        }

                        val totalRandom = if (isCustomDateRangeActive) {
                            remember(randomExpenses, customStartDate, customEndDate) {
                                randomExpenses.filter { isDateInActiveRange(it.timestamp, it.tanggal) }
                                    .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
                            }
                        } else {
                            monthlyExpenseSummary?.totalRandom ?: remember(randomExpenses, activePeriod) {
                                randomExpenses.filter { activePeriod.contains(it.timestamp, it.tanggal) }
                                    .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
                            }
                        }

                        val totalAnak = if (isCustomDateRangeActive) {
                            remember(childExpenses, customStartDate, customEndDate) {
                                childExpenses.filter { isDateInActiveRange(it.timestamp, it.tanggal) }
                                    .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
                            }
                        } else {
                            monthlyExpenseSummary?.totalAnak ?: remember(childExpenses, activePeriod) {
                                childExpenses.filter { activePeriod.contains(it.timestamp, it.tanggal) }
                                    .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
                            }
                        }

                        val totalBensin = if (isCustomDateRangeActive) {
                            remember(fuelLogs, customStartDate, customEndDate) {
                                fuelLogs.filter { isDateInActiveRange(it.tanggal) }.sumOf { it.nominal.toDouble() }
                            }
                        } else {
                            monthlyExpenseSummary?.totalBensin ?: remember(fuelLogs, activePeriod) {
                                fuelLogs.filter { activePeriod.contains(timestamp = it.tanggal) }.sumOf { it.nominal.toDouble() }
                            }
                        }

                        val totalOli = if (isCustomDateRangeActive) {
                            remember(oilLogs, customStartDate, customEndDate) {
                                oilLogs.filter { isDateInActiveRange(it.tanggal) }.sumOf { it.harga.toDouble() }
                            }
                        } else {
                            monthlyExpenseSummary?.totalOli ?: remember(oilLogs, activePeriod) {
                                oilLogs.filter { activePeriod.contains(timestamp = it.tanggal) }.sumOf { it.harga.toDouble() }
                            }
                        }

                        val totalServis = if (isCustomDateRangeActive) {
                            remember(serviceLogs, customStartDate, customEndDate) {
                                serviceLogs.filter { isDateInActiveRange(it.tanggal) }.sumOf { it.total_biaya.toDouble() }
                            }
                        } else {
                            monthlyExpenseSummary?.totalServis ?: remember(serviceLogs, activePeriod) {
                                serviceLogs.filter { activePeriod.contains(timestamp = it.tanggal) }.sumOf { it.total_biaya.toDouble() }
                            }
                        }

                        val totalListrik = if (isCustomDateRangeActive) {
                            remember(electricityLogs, customStartDate, customEndDate) {
                                electricityLogs.filter { isDateInActiveRange(it.tanggal) }.sumOf { it.harga.toDouble() }
                            }
                        } else {
                            monthlyExpenseSummary?.totalListrik ?: remember(electricityLogs, activePeriod) {
                                electricityLogs.filter { activePeriod.contains(timestamp = it.tanggal) }.sumOf { it.harga.toDouble() }
                            }
                        }

                        val grandTotal = totalBelanja + totalRandom + totalAnak + totalBensin + totalOli + totalServis + totalListrik

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Paycheck Period Navigator
                            PaycheckPeriodNavigatorCard(
                                currentPeriod = activePeriod,
                                onPrevCycle = onPrevPaycheckCycle,
                                onNextCycle = onNextPaycheckCycle,
                                onResetCycle = onResetPaycheckCycle,
                                onOpenSettings = { showPaycheckSettingsDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Status Keuangan & Sisa Gaji Card (Surplus / Deficit Indicator)
                            val activeFinancialSummary = financialSummary ?: remember(mainSalaryConfig, additionalIncomes, grandTotal) {
                                val baseSalary = mainSalaryConfig?.nominal ?: 0.0
                                val activeAdd = additionalIncomes.filter { it.isActive }.sumOf { it.nominal }
                                val totalInc = baseSalary + activeAdd
                                val rem = totalInc - grandTotal
                                FinancialCycleSummary(
                                    mainSalary = baseSalary,
                                    additionalIncomeTotal = activeAdd,
                                    totalIncome = totalInc,
                                    totalExpense = grandTotal,
                                    remainingBalance = rem,
                                    isDeficit = rem < 0,
                                    expensePercentage = if (totalInc > 0) (grandTotal / totalInc).toFloat() else 0f
                                )
                            }
                            FinancialCycleStatusCard(
                                financialSummary = activeFinancialSummary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                border = BorderStroke(1.2.dp, SageGreenPrimaryContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("monthly_summary_card")
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
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(SageGreenPrimaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = null,
                                                    tint = SageGreenPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Ringkasan Keuangan",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = SageGreenPrimary
                                                )
                                                Text(
                                                    text = if (isCustomDateRangeActive) "Rentang: $customStartDate s.d. $customEndDate" else "Siklus Gajian (${activePeriod.displayPeriod})",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isCustomDateRangeActive) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCustomDateRangeActive) SageGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Interactive Calendar Date Range Picker Trigger
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCustomDateRangeActive) SageGreenPrimary else SageGreenPrimaryContainer,
                                            modifier = Modifier
                                                .clickable { showDateRangeDialog = true }
                                                .testTag("btn_trigger_date_range_picker")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DateRange,
                                                    contentDescription = "Filter Rentang Tanggal",
                                                    tint = if (isCustomDateRangeActive) Color.White else SageGreenPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isCustomDateRangeActive) "Filter Aktif" else "Filter Tanggal",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCustomDateRangeActive) Color.White else SageGreenPrimary
                                                )
                                            }
                                        }
                                    }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftCreamCanvas.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Total Pengeluaran Dipta Home",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = Formatters.formatRupiah(grandTotal),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Category Breakdown Grid (Compact 3-Column Grid)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SummaryMiniBadge(
                                            title = "Belanja",
                                            amount = totalBelanja,
                                            color = DustyRoseAccent,
                                            bgColor = DustyRoseAccent.copy(alpha = 0.12f),
                                            modifier = Modifier.weight(1f)
                                        )
                                        SummaryMiniBadge(
                                            title = "Random",
                                            amount = totalRandom,
                                            color = Color(0xFF8E24AA),
                                            bgColor = Color(0xFFF3E5F5),
                                            modifier = Modifier.weight(1f)
                                        )
                                        SummaryMiniBadge(
                                            title = "Anak",
                                            amount = totalAnak,
                                            color = Color(0xFFE91E63),
                                            bgColor = Color(0xFFFCE4EC),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SummaryMiniBadge(
                                            title = "Bensin",
                                            amount = totalBensin,
                                            color = FuelBluePastelIcon,
                                            bgColor = FuelBluePastelBg,
                                            modifier = Modifier.weight(1f)
                                        )
                                        SummaryMiniBadge(
                                            title = "Oli",
                                            amount = totalOli,
                                            color = OilYellowPastelIcon,
                                            bgColor = OilYellowPastelBg,
                                            modifier = Modifier.weight(1f)
                                        )
                                        SummaryMiniBadge(
                                            title = "Servis",
                                            amount = totalServis,
                                            color = ServisPurplePastelIcon,
                                            bgColor = ServisPurplePastelBg,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SummaryMiniBadge(
                                            title = "Listrik",
                                            amount = totalListrik,
                                            color = ElectricityOrangePastelIcon,
                                            bgColor = ElectricityOrangePastelBg,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SageGreenPrimaryContainer.copy(alpha = 0.5f),
                                            border = BorderStroke(0.8.dp, SageGreenPrimary.copy(alpha = 0.25f)),
                                            modifier = Modifier.weight(2f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "7 Kategori Rumah Tangga",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageGreenPrimary
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SageGreenPrimary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Grouped Bar Chart Section (Perbandingan Pendapatan vs Pengeluaran) - Diletakkan di bawah Ringkasan Keuangan
                        if (monthlyChartData.isNotEmpty() || yearlyChartData.isNotEmpty()) {
                            var hubChartMode by remember { mutableStateOf("monthly") }
                            GroupedBarChartSection(
                                chartMode = hubChartMode,
                                monthlyChartData = monthlyChartData,
                                yearlyChartData = yearlyChartData,
                                onChangeChartMode = { hubChartMode = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                    // 3. Recent Activities Section
                    item {
                        RecentActivityCard(
                            activities = recentActivities,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        AddVehicleDialog(
            onDismiss = { showAddVehicleDialog = false },
            onConfirm = { nama, plat, jenis ->
                onAddVehicle(nama, plat, jenis, jenis)
                showAddVehicleDialog = false
            }
        )
    }

    // Delete Vehicle Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = deleteCandidateVehicle != null,
        title = "Hapus Kendaraan",
        message = "Apakah Anda yakin ingin menghapus '${deleteCandidateVehicle?.nama_kendaraan}' (${deleteCandidateVehicle?.nomor_plat}) dari Garasi Kendaraan?",
        onDismiss = { deleteCandidateVehicle = null },
        onConfirm = {
            deleteCandidateVehicle?.let { onDeleteVehicle(it.id) }
            deleteCandidateVehicle = null
        }
    )

    // Restore Confirmation Dialog
    if (showRestoreDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text("Restorasi Cadangan Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Pilih metode pemulihan data dari file .json:",
                        fontSize = 13.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { overwriteOption = true }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = overwriteOption,
                            onClick = { overwriteOption = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Timpa Seluruh Data (Overwrite)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Menghapus data lokal dan mengganti dengan isi file .json", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { overwriteOption = false }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = !overwriteOption,
                            onClick = { overwriteOption = false }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Gabungkan Data (Merge)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Menambahkan data dari file .json tanpa menghapus data lokal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedRestoreUri ?: return@Button
                        showRestoreDialog = false
                        onRestoreBackup(uri, overwriteOption) { result ->
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DustyRoseAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mulai Restorasi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Master Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Bersihkan Semua Catatan?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PERINGATAN MASTER RESET:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Tindakan ini akan menghapus SELURUH catatan & transaksi dari semua modul secara permanen:\n\n" +
                                "• Belanja Warung, Dapur & Pengeluaran Lain\n" +
                                "• Catatan Kendaraan Garasi, Bensin & Oli\n" +
                                "• Riwayat Servis & Jimpitan Warga\n" +
                                "• Pembelian Token/Tagihan Listrik\n" +
                                "• Catatan Belanja Anak & Hutang\n" +
                                "• Daftar Resep & Rencana Menu\n\n" +
                                "Seluruh tampilan dan grafik akan dikosongkan dan aplikasi kembali bersih seperti baru. Apakah Anda yakin ingin melanjutkan?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetAllData {
                            Toast.makeText(
                                context,
                                "Seluruh data catatan berhasil dibersihkan! Aplikasi kembali bersih.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Bersihkan Semua", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal")
                }
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

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            syncStatus = syncStatus,
            currentUser = currentUser,
            onDismiss = { showBackupRestoreDialog = false },
            onManualSync = onManualSync,
            onSyncToCloud = onSyncToCloud,
            onSyncFromCloud = onSyncFromCloud,
            onSignOutFirebase = onSignOutFirebase,
            onExportJson = {
                showBackupRestoreDialog = false
                val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                exportLauncher.launch("DiptaHome_Backup_$dateStr.json")
            },
            onImportJson = {
                showBackupRestoreDialog = false
                importLauncher.launch(arrayOf("application/json", "*/*"))
            }
        )
    }

    if (showExportDialog) {
        ExportReportDialog(
            onDismiss = { showExportDialog = false },
            onExportExcel = { options ->
                val data = ExportReportManager.prepareData(
                    options = options,
                    dailyGroceryLogs = dailyGroceryLogs,
                    randomExpenses = randomExpenses,
                    childExpenses = childExpenses,
                    fuelLogs = fuelLogs,
                    oilLogs = oilLogs,
                    serviceLogs = serviceLogs,
                    electricityLogs = electricityLogs,
                    socialLogs = socialLogs,
                    warungDebts = warungDebts
                )
                ExportReportManager.exportToExcel(context, data)
                showExportDialog = false
            },
            onExportPdf = { options ->
                val data = ExportReportManager.prepareData(
                    options = options,
                    dailyGroceryLogs = dailyGroceryLogs,
                    randomExpenses = randomExpenses,
                    childExpenses = childExpenses,
                    fuelLogs = fuelLogs,
                    oilLogs = oilLogs,
                    serviceLogs = serviceLogs,
                    electricityLogs = electricityLogs,
                    socialLogs = socialLogs,
                    warungDebts = warungDebts
                )
                ExportReportManager.exportToPdf(context, data)
                showExportDialog = false
            }
        )
    }

    if (showDateRangeDialog) {
        DateRangeFilterDialog(
            showDialog = showDateRangeDialog,
            initialStartDate = customStartDate,
            initialEndDate = customEndDate,
            onDismissRequest = { showDateRangeDialog = false },
            onApplyDateRange = { start, end ->
                customStartDate = start
                customEndDate = end
                isCustomDateRangeActive = true
                showDateRangeDialog = false
            },
            onResetDateRange = {
                customStartDate = ""
                customEndDate = ""
                isCustomDateRangeActive = false
                showDateRangeDialog = false
            }
        )
    }
}

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onConfirm: (nama: String, plat: String, jenis: String) -> Unit
) {
    var namaInput by remember { mutableStateOf("") }
    var platInput by remember { mutableStateOf("") }
    var jenisInput by remember { mutableStateOf("Motor") } // "Motor" or "Mobil"
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                Text(text = "Tambah Kendaraan Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = namaInput,
                    onValueChange = { namaInput = it; errorMessage = null },
                    label = { Text("Nama Kendaraan (ex: Honda Vario 125)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nama_kendaraan")
                )

                OutlinedTextField(
                    value = platInput,
                    onValueChange = { platInput = it; errorMessage = null },
                    label = { Text("Nomor Plat / Polisi (ex: B 1234 ABC)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_plat_kendaraan")
                )

                Text(
                    text = "Jenis Kendaraan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = jenisInput == "Motor",
                        onClick = { jenisInput = "Motor" },
                        label = { Text("🛵 Motor") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = SageGreenPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_jenis_motor")
                    )

                    FilterChip(
                        selected = jenisInput == "Mobil",
                        onClick = { jenisInput = "Mobil" },
                        label = { Text("🚗 Mobil") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = SageGreenPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_jenis_mobil")
                    )
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
                    if (namaInput.isBlank()) {
                        errorMessage = "Masukkan nama kendaraan"
                        return@Button
                    }
                    onConfirm(namaInput.trim(), platInput.trim().uppercase(), jenisInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_vehicle_button")
            ) {
                Text("Simpan Kendaraan", fontWeight = FontWeight.Bold)
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
fun ModuleCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    cardBackgroundColor: Color,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, cardBackgroundColor.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftTextDark
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = cardBackgroundColor
                    ) {
                        Text(
                            text = badgeText,
                            color = iconColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SummaryMiniBadge(
    title: String,
    amount: Number,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (amount.toDouble() > 0) Formatters.formatRupiah(amount.toDouble()) else "Rp 0",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SoftTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentActivityCard(
    activities: List<RecentActivityItem>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val visibleItems = if (isExpanded) activities else activities.take(4)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.2.dp, SageGreenPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SageGreenPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Aktivitas Terbaru",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SageGreenPrimary
                        )
                        Text(
                            text = "Riwayat 10 transaksi terakhir",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SageGreenPrimaryContainer
                ) {
                    Text(
                        text = "${activities.size} Log",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageGreenPrimary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activities.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Belum ada riwayat transaksi yang tercatat.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    visibleItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(item.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        color = item.color,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.dateText,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (item.amount > 0) Formatters.formatRupiah(item.amount) else "Rp 0",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SoftTextDark
                            )
                        }

                        if (index < visibleItems.size - 1) {
                            HorizontalDivider(
                                color = SoftCreamCanvas,
                                thickness = 1.dp
                            )
                        }
                    }
                }

                if (activities.size > 4) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = SageGreenPrimaryContainer, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_toggle_recent_activities"),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Tampilkan Ringkas" else "Lihat Selengkapnya (${activities.size} Transaksi)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SoftCreamCanvas,
        border = BorderStroke(0.8.dp, SageGreenPrimaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTextDark
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tint.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = tint,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
