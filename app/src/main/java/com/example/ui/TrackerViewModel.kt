package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.backup.BackupRestoreManager
import com.example.data.backup.BackupResult
import com.example.data.database.AppDatabase
import com.example.data.entity.AdditionalIncome
import com.example.data.entity.ChildExpenseLog
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.ElectricityLog
import com.example.data.entity.FuelLog
import com.example.data.entity.MainSalaryConfig
import com.example.data.entity.OilLog
import com.example.data.entity.RandomExpense
import com.example.data.entity.ServiceLog
import com.example.data.entity.ShoppingNoteItem
import com.example.data.entity.SocialLog
import com.example.data.entity.Vehicle
import com.example.data.entity.WarungDebt
import com.example.data.entity.WarungDebtPayment
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.firebase.SyncStatus
import com.example.data.repository.TrackerRepository
import com.example.ui.util.PaycheckCycleHelper
import com.example.ui.util.PaycheckPeriod
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthlyExpenseSummary(
    val totalBelanja: Double = 0.0,
    val totalRandom: Double = 0.0,
    val totalAnak: Double = 0.0,
    val totalBensin: Double = 0.0,
    val totalOli: Double = 0.0,
    val totalServis: Double = 0.0,
    val totalListrik: Double = 0.0,
    val grandTotal: Double = 0.0
)

data class FinancialCycleSummary(
    val mainSalary: Double = 0.0,
    val additionalIncomeTotal: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val isDeficit: Boolean = false,
    val expensePercentage: Float = 0f,
    val activeAdditionalIncomes: List<AdditionalIncome> = emptyList()
)

data class BarChartItem(
    val label: String,
    val income: Double,
    val expense: Double,
    val net: Double
)

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TrackerRepository(
        vehicleDao = db.vehicleDao(),
        fuelDao = db.fuelDao(),
        oilDao = db.oilDao(),
        electricityDao = db.electricityDao(),
        serviceDao = db.serviceDao(),
        socialDao = db.socialDao(),
        warungDao = db.warungDao(),
        recipeDao = db.recipeDao(),
        incomeDao = db.incomeDao()
    )

    private val prefs = application.getSharedPreferences("dipta_warung_prefs", Context.MODE_PRIVATE)

    // Paycheck Cycle Settings (Default starts on 25th)
    private val _paycheckStartDay = MutableStateFlow(
        prefs.getInt("paycheck_start_day", 25).coerceIn(1, 31)
    )
    val paycheckStartDay: StateFlow<Int> = _paycheckStartDay.asStateFlow()

    private val _paycheckCycleOffset = MutableStateFlow(0)
    val paycheckCycleOffset: StateFlow<Int> = _paycheckCycleOffset.asStateFlow()

    val currentPaycheckPeriod: StateFlow<PaycheckPeriod> = combine(
        _paycheckStartDay,
        _paycheckCycleOffset
    ) { startDay, offset ->
        PaycheckCycleHelper.calculatePeriod(startDay = startDay, offset = offset)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaycheckCycleHelper.calculatePeriod(25, 0)
    )

    private val _warungDebtLimit = MutableStateFlow(
        prefs.getFloat("warung_debt_limit", 500000f).toDouble()
    )
    val warungDebtLimit: StateFlow<Double> = _warungDebtLimit.asStateFlow()

    private val backupRestoreManager = BackupRestoreManager(application, repository)
    private val firestoreSyncManager = FirestoreSyncManager(application, repository)

    // Firebase state
    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            lastSyncTime = firestoreSyncManager.getLastSyncTime(),
            message = "Terhubung ke Firebase (Sinkronisasi Otomatis)"
        )
    )
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Vehicles
    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeVehicleId = MutableStateFlow(0)
    val activeVehicleId: StateFlow<Int> = _activeVehicleId.asStateFlow()

    val fuelLogs: StateFlow<List<FuelLog>> = repository.allFuelLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val oilLogs: StateFlow<List<OilLog>> = repository.allOilLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val electricityLogs: StateFlow<List<ElectricityLog>> = repository.allElectricityLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val serviceLogs: StateFlow<List<ServiceLog>> = repository.allServiceLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val socialLogs: StateFlow<List<SocialLog>> = repository.allSocialLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyGroceryLogs: StateFlow<List<DailyGroceryLog>> = repository.allDailyGroceryLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val randomExpenses: StateFlow<List<RandomExpense>> = repository.allRandomExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val childExpenses: StateFlow<List<ChildExpenseLog>> = repository.allChildExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val warungDebts: StateFlow<List<WarungDebt>> = repository.allWarungDebts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val warungDebtPayments: StateFlow<List<WarungDebtPayment>> = repository.allWarungDebtPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shoppingNoteItems: StateFlow<List<ShoppingNoteItem>> = repository.allShoppingNoteItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeRecipes: StateFlow<List<com.example.data.entity.Recipe>> = repository.activeRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deletedRecipes: StateFlow<List<com.example.data.entity.Recipe>> = repository.deletedRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mealPlanItems: StateFlow<List<com.example.data.entity.MealPlanItem>> = repository.mealPlanItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyExpenseSummary: StateFlow<MonthlyExpenseSummary> = combine(
        dailyGroceryLogs,
        randomExpenses,
        childExpenses,
        fuelLogs,
        oilLogs,
        serviceLogs,
        electricityLogs,
        currentPaycheckPeriod
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val grocery = flows[0] as List<DailyGroceryLog>
        @Suppress("UNCHECKED_CAST")
        val random = flows[1] as List<RandomExpense>
        @Suppress("UNCHECKED_CAST")
        val child = flows[2] as List<ChildExpenseLog>
        @Suppress("UNCHECKED_CAST")
        val fuel = flows[3] as List<FuelLog>
        @Suppress("UNCHECKED_CAST")
        val oil = flows[4] as List<OilLog>
        @Suppress("UNCHECKED_CAST")
        val service = flows[5] as List<ServiceLog>
        @Suppress("UNCHECKED_CAST")
        val electricity = flows[6] as List<ElectricityLog>
        val period = flows[7] as PaycheckPeriod

        val sumBelanja = grocery.filter { period.contains(it.timestamp, it.tanggal) }
            .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
        val sumRandom = random.filter { period.contains(it.timestamp, it.tanggal) }
            .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
        val sumAnak = child.filter { period.contains(it.timestamp, it.tanggal) }
            .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
        val sumBensin = fuel.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.nominal.toDouble() }
        val sumOli = oil.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.harga.toDouble() }
        val sumServis = service.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.total_biaya.toDouble() }
        val sumListrik = electricity.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.harga.toDouble() }

        val grandTotal = sumBelanja + sumRandom + sumAnak + sumBensin + sumOli + sumServis + sumListrik

        MonthlyExpenseSummary(
            totalBelanja = sumBelanja,
            totalRandom = sumRandom,
            totalAnak = sumAnak,
            totalBensin = sumBensin,
            totalOli = sumOli,
            totalServis = sumServis,
            totalListrik = sumListrik,
            grandTotal = grandTotal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyExpenseSummary()
    )

    // Income Flows
    val mainSalaryConfig: StateFlow<MainSalaryConfig?> = repository.mainSalaryConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allAdditionalIncomes: StateFlow<List<AdditionalIncome>> = repository.allAdditionalIncomes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Financial Cycle Summary (Main Salary + Active Additional Incomes - Expenses)
    val financialCycleSummary: StateFlow<FinancialCycleSummary> = combine(
        mainSalaryConfig,
        allAdditionalIncomes,
        monthlyExpenseSummary,
        currentPaycheckPeriod,
        _paycheckCycleOffset
    ) { salary, additions, expenseSummary, period, offset ->
        val baseSalary = salary?.nominal ?: 0.0
        
        // Filter additional incomes allocated to this cycle
        val activeAdditions = additions.filter { inc ->
            if (!inc.isActive) return@filter false
            if (inc.targetCycleOffset == offset) return@filter true
            if (inc.targetCycleLabel.isNotBlank() && inc.targetCycleLabel == period.label) return@filter true
            // If cycle offset not explicitly locked, match period date
            inc.targetCycleOffset == 0 && offset == 0 || period.contains(inc.timestamp, inc.tanggal)
        }

        val additionTotal = activeAdditions.sumOf { it.nominal }
        val totalIncome = baseSalary + additionTotal
        val totalExpense = expenseSummary.grandTotal
        val remaining = totalIncome - totalExpense
        val isDeficit = remaining < 0
        val percentage = when {
            totalIncome > 0 -> (totalExpense / totalIncome).toFloat().coerceIn(0f, 2.5f)
            totalExpense > 0 -> 1f
            else -> 0f
        }

        FinancialCycleSummary(
            mainSalary = baseSalary,
            additionalIncomeTotal = additionTotal,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            remainingBalance = remaining,
            isDeficit = isDeficit,
            expensePercentage = percentage,
            activeAdditionalIncomes = activeAdditions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialCycleSummary()
    )

    // Grouped Bar Chart Data (Monthly - Last 6 Cycles)
    val monthlyComparisonData: StateFlow<List<BarChartItem>> = combine(
        mainSalaryConfig,
        allAdditionalIncomes,
        dailyGroceryLogs,
        randomExpenses,
        childExpenses,
        fuelLogs,
        oilLogs,
        serviceLogs,
        electricityLogs,
        _paycheckStartDay
    ) { flows ->
        val salary = flows[0] as? MainSalaryConfig
        @Suppress("UNCHECKED_CAST")
        val additions = flows[1] as List<AdditionalIncome>
        @Suppress("UNCHECKED_CAST")
        val grocery = flows[2] as List<DailyGroceryLog>
        @Suppress("UNCHECKED_CAST")
        val random = flows[3] as List<RandomExpense>
        @Suppress("UNCHECKED_CAST")
        val child = flows[4] as List<ChildExpenseLog>
        @Suppress("UNCHECKED_CAST")
        val fuel = flows[5] as List<FuelLog>
        @Suppress("UNCHECKED_CAST")
        val oil = flows[6] as List<OilLog>
        @Suppress("UNCHECKED_CAST")
        val service = flows[7] as List<ServiceLog>
        @Suppress("UNCHECKED_CAST")
        val electricity = flows[8] as List<ElectricityLog>
        val startDay = flows[9] as Int

        val baseSalary = salary?.nominal ?: 0.0
        val list = mutableListOf<BarChartItem>()

        // Generate last 6 cycles (-5 to 0)
        val sdfShortLabel = SimpleDateFormat("MMM ''yy", Locale("id", "ID"))
        for (offset in -5..0) {
            val period = PaycheckCycleHelper.calculatePeriod(startDay = startDay, offset = offset)
            val periodAdditions = additions.filter { inc ->
                inc.isActive && (inc.targetCycleOffset == offset || period.contains(inc.timestamp, inc.tanggal))
            }.sumOf { it.nominal }

            val incTotal = baseSalary + periodAdditions

            val sumBelanja = grocery.filter { period.contains(it.timestamp, it.tanggal) }
                .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
            val sumRandom = random.filter { period.contains(it.timestamp, it.tanggal) }
                .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
            val sumAnak = child.filter { period.contains(it.timestamp, it.tanggal) }
                .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
            val sumBensin = fuel.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.nominal.toDouble() }
            val sumOli = oil.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.harga.toDouble() }
            val sumServis = service.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.total_biaya.toDouble() }
            val sumListrik = electricity.filter { period.contains(timestamp = it.tanggal) }.sumOf { it.harga.toDouble() }

            val expTotal = sumBelanja + sumRandom + sumAnak + sumBensin + sumOli + sumServis + sumListrik

            val label = sdfShortLabel.format(Date(period.startTimestamp))
            list.add(
                BarChartItem(
                    label = label,
                    income = incTotal,
                    expense = expTotal,
                    net = incTotal - expTotal
                )
            )
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val monthlyComparisonChartData: StateFlow<List<BarChartItem>> = monthlyComparisonData

    // Grouped Bar Chart Data (Yearly - Last 3 Years)
    val yearlyComparisonData: StateFlow<List<BarChartItem>> = combine(
        mainSalaryConfig,
        allAdditionalIncomes,
        dailyGroceryLogs,
        randomExpenses,
        childExpenses,
        fuelLogs,
        oilLogs,
        serviceLogs,
        electricityLogs
    ) { flows ->
        val salary = flows[0] as? MainSalaryConfig
        @Suppress("UNCHECKED_CAST")
        val additions = flows[1] as List<AdditionalIncome>
        @Suppress("UNCHECKED_CAST")
        val grocery = flows[2] as List<DailyGroceryLog>
        @Suppress("UNCHECKED_CAST")
        val random = flows[3] as List<RandomExpense>
        @Suppress("UNCHECKED_CAST")
        val child = flows[4] as List<ChildExpenseLog>
        @Suppress("UNCHECKED_CAST")
        val fuel = flows[5] as List<FuelLog>
        @Suppress("UNCHECKED_CAST")
        val oil = flows[6] as List<OilLog>
        @Suppress("UNCHECKED_CAST")
        val service = flows[7] as List<ServiceLog>
        @Suppress("UNCHECKED_CAST")
        val electricity = flows[8] as List<ElectricityLog>

        val baseSalaryPerYear = (salary?.nominal ?: 0.0) * 12
        val currentCalYear = Calendar.getInstance().get(Calendar.YEAR)
        val list = mutableListOf<BarChartItem>()
        val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())

        for (year in (currentCalYear - 2)..currentCalYear) {
            val yearStr = year.toString()

            val yearAdditions = additions.filter { inc ->
                inc.isActive && (inc.tanggal.startsWith(yearStr) || (inc.tanggal.isBlank() && sdfYear.format(Date(inc.timestamp)) == yearStr))
            }.sumOf { it.nominal }

            val incTotal = baseSalaryPerYear + yearAdditions

            val sumBelanja = grocery.filter { it.tanggal.startsWith(yearStr) }
                .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
            val sumRandom = random.filter { it.tanggal.startsWith(yearStr) }
                .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
            val sumAnak = child.filter { it.tanggal.startsWith(yearStr) }
                .sumOf { (it.modalAwal - it.sisaUang).coerceAtLeast(0.0) }
            val sumBensin = fuel.filter { sdfYear.format(Date(it.tanggal)) == yearStr }.sumOf { it.nominal.toDouble() }
            val sumOli = oil.filter { sdfYear.format(Date(it.tanggal)) == yearStr }.sumOf { it.harga.toDouble() }
            val sumServis = service.filter { sdfYear.format(Date(it.tanggal)) == yearStr }.sumOf { it.total_biaya.toDouble() }
            val sumListrik = electricity.filter { sdfYear.format(Date(it.tanggal)) == yearStr }.sumOf { it.harga.toDouble() }

            val expTotal = sumBelanja + sumRandom + sumAnak + sumBensin + sumOli + sumServis + sumListrik

            list.add(
                BarChartItem(
                    label = yearStr,
                    income = incTotal,
                    expense = expTotal,
                    net = incTotal - expTotal
                )
            )
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val yearlyComparisonChartData: StateFlow<List<BarChartItem>> = yearlyComparisonData

    // Paycheck Cycle Actions
    fun setPaycheckStartDay(day: Int) {
        val validDay = day.coerceIn(1, 31)
        _paycheckStartDay.value = validDay
        prefs.edit().putInt("paycheck_start_day", validDay).apply()
    }

    fun nextPaycheckCycle() {
        _paycheckCycleOffset.value += 1
    }

    fun prevPaycheckCycle() {
        _paycheckCycleOffset.value -= 1
    }

    fun resetPaycheckCycle() {
        _paycheckCycleOffset.value = 0
    }

    fun setPaycheckCycleOffset(offset: Int) {
        _paycheckCycleOffset.value = offset
    }

    init {
        viewModelScope.launch {
            val status = firestoreSyncManager.syncCloudToLocal()
            _syncStatus.value = status

            val seeded = repository.ensureDefaultVehiclesSeed()
            if (seeded) {
                autoSyncToCloud()
            }
        }
        viewModelScope.launch {
            vehicles.collect { list ->
                if (list.isNotEmpty()) {
                    if (_activeVehicleId.value == 0 || list.none { it.id == _activeVehicleId.value }) {
                        _activeVehicleId.value = list.first().id
                    }
                }
            }
        }
    }

    // --- ACTIONS - VEHICLES ---
    fun selectActiveVehicle(vehicleId: Int) {
        _activeVehicleId.value = vehicleId
    }

    fun addVehicle(
        nama: String,
        plat: String,
        jenis: String,
        iconType: String = "Motor",
        tanggalPajakStnk: String = "",
        catatanSparepart: String = ""
    ) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                nama_kendaraan = nama,
                nomor_plat = plat,
                jenis_kendaraan = jenis,
                icon_type = iconType,
                tanggal_pajak_stnk = tanggalPajakStnk,
                catatan_sparepart = catatanSparepart
            )
            val newId = repository.addVehicle(vehicle)
            if (newId > 0) {
                _activeVehicleId.value = newId.toInt()
            }
            autoSyncToCloud()
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
            autoSyncToCloud()
        }
    }

    fun deleteVehicle(id: Int) {
        viewModelScope.launch {
            repository.deleteVehicle(id)
            firestoreSyncManager.deleteDocumentFromCloud("vehicles", id.toString())
            val remaining = repository.getAllVehiclesList()
            if (remaining.isNotEmpty()) {
                if (_activeVehicleId.value == id) {
                    _activeVehicleId.value = remaining.first().id
                }
            } else {
                _activeVehicleId.value = 0
            }
            autoSyncToCloud()
        }
    }

    // --- ACTIONS - FUEL ---
    fun addFuelLog(
        vehicleId: Int,
        kmMotor: Int,
        nominal: Int,
        liter: Float,
        jenisBbm: String = "Pertalite",
        hargaPerLiter: Int = 0,
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addFuelLog(vehicleId, kmMotor, nominal, liter, jenisBbm, hargaPerLiter, customTimestamp)
            autoSyncToCloud()
        }
    }

    fun updateFuelLog(log: FuelLog) {
        viewModelScope.launch {
            repository.updateFuelLog(log)
            autoSyncToCloud()
        }
    }

    fun deleteFuelLog(id: Int) {
        viewModelScope.launch {
            repository.deleteFuelLog(id)
            autoSyncToCloud()
        }
    }

    fun updateVehicleOdometer(vehicleId: Int, newOdometer: Int) {
        viewModelScope.launch {
            repository.updateVehicleOdometer(vehicleId, newOdometer)
            autoSyncToCloud()
        }
    }

    // --- ACTIONS - OIL ---
    fun addOilLog(
        vehicleId: Int,
        kmMotor: Int,
        jenisOli: String,
        harga: Int,
        kapasitasMl: Int,
        intervalKm: Int = 3000,
        garansiBengkel: String = "",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addOilLog(vehicleId, kmMotor, jenisOli, harga, kapasitasMl, intervalKm, garansiBengkel, customTimestamp)
            autoSyncToCloud()
        }
    }

    fun updateOilLog(log: OilLog) {
        viewModelScope.launch {
            repository.updateOilLog(log)
            autoSyncToCloud()
        }
    }

    fun deleteOilLog(id: Int) {
        viewModelScope.launch {
            repository.deleteOilLog(id)
            autoSyncToCloud()
        }
    }

    // --- ACTIONS - ELECTRICITY ---
    fun addElectricityLog(
        harga: Int,
        jumlahKwh: Float,
        isInitial: Boolean = false,
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addElectricityLog(harga, jumlahKwh, isInitial, customTimestamp)
            autoSyncToCloud()
        }
    }

    fun updateElectricityLog(log: ElectricityLog) {
        viewModelScope.launch {
            repository.updateElectricityLog(log)
            autoSyncToCloud()
        }
    }

    fun deleteElectricityLog(id: Int) {
        viewModelScope.launch {
            repository.deleteElectricityLog(id)
            autoSyncToCloud()
        }
    }

    // --- ACTIONS - SERVICE / PERAWATAN ---
    fun addServiceLog(
        vehicleId: Int,
        kmMotor: Int,
        kategori: String,
        deskripsiItem: String,
        totalBiaya: Int,
        intervalKm: Int = 5000,
        garansiBengkel: String = "",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addServiceLog(vehicleId, kmMotor, kategori, deskripsiItem, totalBiaya, intervalKm, garansiBengkel, customTimestamp)
            autoSyncToCloud()
        }
    }

    fun updateServiceLog(log: ServiceLog) {
        viewModelScope.launch {
            repository.updateServiceLog(log)
            autoSyncToCloud()
        }
    }

    fun deleteServiceLog(id: Int) {
        viewModelScope.launch {
            repository.deleteServiceLog(id)
            autoSyncToCloud()
        }
    }

    // --- ACTIONS - KEUANGAN RT & SOSIAL (JIMPITAN & KURBAN) ---
    fun addSocialLog(
        kategori: String,
        nominal: Int,
        keterangan: String,
        tipeTransaksi: String = "Masuk",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addSocialLog(kategori, nominal, keterangan, tipeTransaksi, customTimestamp)
            autoSyncToCloud()
        }
    }

    fun updateSocialLog(log: SocialLog) {
        viewModelScope.launch {
            repository.updateSocialLog(log)
            autoSyncToCloud()
        }
    }

    fun deleteSocialLog(id: Int) {
        viewModelScope.launch {
            repository.deleteSocialLog(id)
            autoSyncToCloud()
        }
    }

    // --- WARUNG & BELANJA OPERATIONS ---
    fun updateWarungDebtLimit(newLimit: Double) {
        _warungDebtLimit.value = newLimit
        prefs.edit().putFloat("warung_debt_limit", newLimit.toFloat()).apply()
    }

    fun addDailyGroceryLog(
        tanggal: String,
        modalAwal: Double,
        sisaUang: Double,
        rincian: String,
        catatan: String
    ) {
        viewModelScope.launch {
            repository.addDailyGroceryLog(tanggal, modalAwal, sisaUang, rincian, catatan)
            autoSyncToCloud()
        }
    }

    fun updateDailyGroceryLog(log: DailyGroceryLog) {
        viewModelScope.launch {
            repository.updateDailyGroceryLog(log)
            autoSyncToCloud()
        }
    }

    fun deleteDailyGroceryLog(id: Int) {
        viewModelScope.launch {
            repository.deleteDailyGroceryLog(id)
            autoSyncToCloud()
        }
    }

    // --- RANDOM EXPENSES (TERSIER) OPERATIONS ---
    fun addRandomExpense(
        tanggal: String,
        modalAwal: Double,
        sisaUang: Double,
        rincian: String,
        catatan: String
    ) {
        viewModelScope.launch {
            repository.addRandomExpense(tanggal, modalAwal, sisaUang, rincian, catatan)
            autoSyncToCloud()
        }
    }

    fun updateRandomExpense(log: RandomExpense) {
        viewModelScope.launch {
            repository.updateRandomExpense(log)
            autoSyncToCloud()
        }
    }

    fun deleteRandomExpense(id: Int) {
        viewModelScope.launch {
            repository.deleteRandomExpense(id)
            autoSyncToCloud()
        }
    }

    // --- CHILD EXPENSES OPERATIONS ---
    fun addChildExpense(
        tanggal: String,
        modalAwal: Double,
        sisaUang: Double,
        rincian: String,
        catatan: String
    ) {
        viewModelScope.launch {
            repository.addChildExpense(tanggal, modalAwal, sisaUang, rincian, catatan)
            autoSyncToCloud()
        }
    }

    fun updateChildExpense(log: ChildExpenseLog) {
        viewModelScope.launch {
            repository.updateChildExpense(log)
            autoSyncToCloud()
        }
    }

    fun deleteChildExpense(id: Int) {
        viewModelScope.launch {
            repository.deleteChildExpense(id)
            autoSyncToCloud()
        }
    }

    fun addWarungDebt(
        tanggal: String,
        namaWarung: String,
        nominal: Double,
        alasan: String
    ) {
        viewModelScope.launch {
            repository.addWarungDebt(tanggal, namaWarung, nominal, alasan)
            autoSyncToCloud()
        }
    }

    fun updateWarungDebt(debt: WarungDebt) {
        viewModelScope.launch {
            repository.updateWarungDebt(debt)
            autoSyncToCloud()
        }
    }

    fun deleteWarungDebt(id: Int) {
        viewModelScope.launch {
            repository.deleteWarungDebt(id)
            autoSyncToCloud()
        }
    }

    fun addWarungDebtPayment(
        debtId: Int,
        tanggal: String,
        nominalBayar: Double,
        catatan: String
    ) {
        viewModelScope.launch {
            repository.addWarungDebtPayment(debtId, tanggal, nominalBayar, catatan)
            autoSyncToCloud()
        }
    }

    fun deleteWarungDebtPayment(payment: WarungDebtPayment) {
        viewModelScope.launch {
            repository.deleteWarungDebtPayment(payment)
            autoSyncToCloud()
        }
    }

    fun addShoppingNoteItem(
        namaBarang: String,
        prioritas: String,
        estimasiHarga: Double,
        catatan: String
    ) {
        viewModelScope.launch {
            repository.addShoppingNoteItem(namaBarang, prioritas, estimasiHarga, catatan)
            autoSyncToCloud()
        }
    }

    fun updateShoppingNoteItem(item: ShoppingNoteItem) {
        viewModelScope.launch {
            repository.updateShoppingNoteItem(item)
            autoSyncToCloud()
        }
    }

    fun toggleShoppingNoteDone(item: ShoppingNoteItem) {
        viewModelScope.launch {
            repository.toggleShoppingNoteDone(item)
            autoSyncToCloud()
        }
    }

    fun deleteShoppingNoteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteShoppingNoteItem(id)
            autoSyncToCloud()
        }
    }

    fun clearCompletedShoppingNotes() {
        viewModelScope.launch {
            repository.clearCompletedShoppingNotes()
            autoSyncToCloud()
        }
    }

    // --- BACKUP & RESTORE (.JSON) ---
    fun exportBackupToJson(uri: Uri, onResult: (BackupResult) -> Unit) {
        viewModelScope.launch {
            val result = backupRestoreManager.exportToUri(uri)
            onResult(result)
        }
    }

    fun restoreBackupFromJson(uri: Uri, overwrite: Boolean, onResult: (BackupResult) -> Unit) {
        viewModelScope.launch {
            val result = backupRestoreManager.restoreFromUri(uri, overwrite)
            if (result.success) {
                autoSyncToCloud()
            }
            onResult(result)
        }
    }

    // --- FIREBASE FIRESTORE SYNC & AUTH ---
    fun triggerFullManualSync() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus(isSyncing = true, message = "Memulai sinkronisasi manual 2 arah...")
            val status = firestoreSyncManager.syncTwoWay()
            _syncStatus.value = status
        }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus(isSyncing = true, message = "Menyinkronkan data ke Cloud Firestore...")
            val status = firestoreSyncManager.syncLocalToCloud()
            _syncStatus.value = status
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus(isSyncing = true, message = "Mengunduh data terbaru dari Cloud...")
            val status = firestoreSyncManager.syncCloudToLocal()
            _syncStatus.value = status
        }
    }

    fun connectFirebaseUser() {
        viewModelScope.launch {
            syncToCloud()
        }
    }

    fun signOutUser() {
        viewModelScope.launch {
            firestoreSyncManager.clearLocalCache()
            _syncStatus.value = SyncStatus(message = "Data lokal telah dibersihkan")
        }
    }

    private fun autoSyncToCloud() {
        viewModelScope.launch {
            firestoreSyncManager.syncLocalToCloud()
        }
    }

    // --- RECIPE & MEAL PLAN HANDLERS ---
    fun addRecipe(
        title: String,
        description: String,
        category: String,
        prepTime: String,
        cookTime: String,
        yields: String,
        ingredients: String,
        directions: String,
        skillRating: Int = 0,
        isFavorite: Boolean = false,
        flavorTag: String = "",
        source: String = ""
    ) {
        viewModelScope.launch {
            repository.addRecipe(
                title, description, category, prepTime, cookTime, yields, ingredients, directions,
                skillRating, isFavorite, flavorTag, source
            )
            autoSyncToCloud()
        }
    }

    fun updateRecipe(recipe: com.example.data.entity.Recipe) {
        viewModelScope.launch {
            repository.updateRecipe(recipe)
            autoSyncToCloud()
        }
    }

    fun softDeleteRecipe(id: Int) {
        viewModelScope.launch {
            repository.softDeleteRecipe(id)
            autoSyncToCloud()
        }
    }

    fun restoreRecipe(id: Int) {
        viewModelScope.launch {
            repository.restoreRecipe(id)
            autoSyncToCloud()
        }
    }

    fun hardDeleteRecipe(id: Int) {
        viewModelScope.launch {
            repository.hardDeleteRecipe(id)
            autoSyncToCloud()
        }
    }

    fun clearTrashRecipes() {
        viewModelScope.launch {
            repository.clearTrashRecipes()
            autoSyncToCloud()
        }
    }

    fun addMealPlanItem(dayOfWeek: String, recipeId: Int, recipeTitle: String, mealType: String = "Makan Siang/Malam") {
        viewModelScope.launch {
            repository.addMealPlanItem(dayOfWeek, recipeId, recipeTitle, mealType)
            autoSyncToCloud()
        }
    }

    fun deleteMealPlanItem(id: Int) {
        viewModelScope.launch {
            repository.deleteMealPlanItem(id)
            autoSyncToCloud()
        }
    }

    fun clearMealPlanForDay(dayOfWeek: String) {
        viewModelScope.launch {
            repository.clearMealPlanForDay(dayOfWeek)
            autoSyncToCloud()
        }
    }

    fun clearAllMealPlans() {
        viewModelScope.launch {
            repository.clearAllMealPlans()
            autoSyncToCloud()
        }
    }

    fun exportWeeklyMealPlanToShoppingList() {
        viewModelScope.launch {
            val plans = mealPlanItems.value
            val active = activeRecipes.value
            val recipeMap = active.associateBy { it.id }

            val allIngredientLines = mutableListOf<String>()
            plans.forEach { plan ->
                val recipe = recipeMap[plan.recipeId]
                if (recipe != null && recipe.ingredients.isNotBlank()) {
                    val lines = recipe.ingredients.split("\n", ",")
                    lines.forEach { line ->
                        val clean = line.trim()
                            .replace(Regex("^[-*•\\d.]+\\s*"), "") // remove bullets or numbers
                        if (clean.isNotBlank()) {
                            allIngredientLines.add(clean)
                        }
                    }
                }
            }

            // Distinct ingredients to avoid duplicates
            val distinctIngredients = allIngredientLines.distinctBy { it.lowercase(Locale.getDefault()) }
            if (distinctIngredients.isNotEmpty()) {
                repository.addRecipeIngredientsToShoppingList(distinctIngredients)
            }
        }
    }

    // --- INCOME SOURCES ACTIONS ---
    fun setMainSalary(nominal: Double, catatan: String = "") {
        viewModelScope.launch {
            repository.setMainSalary(nominal, catatan)
            autoSyncToCloud()
        }
    }

    fun addAdditionalIncome(
        judul: String,
        kategori: String,
        nominal: Double,
        tanggal: String,
        isActive: Boolean = true,
        targetCycleOffset: Int = 0,
        targetCycleLabel: String = "",
        catatan: String = ""
    ) {
        viewModelScope.launch {
            repository.addAdditionalIncome(
                judul = judul,
                kategori = kategori,
                nominal = nominal,
                tanggal = tanggal,
                isActive = isActive,
                targetCycleOffset = targetCycleOffset,
                targetCycleLabel = targetCycleLabel,
                catatan = catatan
            )
            autoSyncToCloud()
        }
    }

    fun updateAdditionalIncome(income: AdditionalIncome) {
        viewModelScope.launch {
            repository.updateAdditionalIncome(income)
            autoSyncToCloud()
        }
    }

    fun toggleAdditionalIncome(
        id: Int,
        isActive: Boolean,
        targetCycleOffset: Int = 0,
        targetCycleLabel: String = ""
    ) {
        viewModelScope.launch {
            repository.toggleAdditionalIncome(id, isActive, targetCycleOffset, targetCycleLabel)
            autoSyncToCloud()
        }
    }

    fun deleteAdditionalIncome(id: Int) {
        viewModelScope.launch {
            repository.deleteAdditionalIncome(id)
            autoSyncToCloud()
        }
    }

    fun resetAllMasterData(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllLocalData()
            repository.clearAllIncomes()
            _activeVehicleId.value = 0
            onSuccess()
        }
    }
}
