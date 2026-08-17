package com.example.data.repository

import com.example.data.dao.ElectricityDao
import com.example.data.dao.FuelDao
import com.example.data.dao.OilDao
import com.example.data.dao.RecipeDao
import com.example.data.dao.ServiceDao
import com.example.data.dao.SocialDao
import com.example.data.dao.VehicleDao
import com.example.data.dao.WarungDao
import com.example.data.entity.ChildExpenseLog
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.ElectricityLog
import com.example.data.entity.FuelLog
import com.example.data.entity.MealPlanItem
import com.example.data.entity.OilLog
import com.example.data.entity.RandomExpense
import com.example.data.entity.Recipe
import com.example.data.entity.ServiceLog
import com.example.data.entity.ShoppingNoteItem
import com.example.data.entity.SocialLog
import com.example.data.entity.Vehicle
import com.example.data.entity.WarungDebt
import com.example.data.entity.WarungDebtPayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class TrackerRepository(
    private val vehicleDao: VehicleDao,
    private val fuelDao: FuelDao,
    private val oilDao: OilDao,
    private val electricityDao: ElectricityDao,
    private val serviceDao: ServiceDao,
    private val socialDao: SocialDao,
    private val warungDao: WarungDao,
    private val recipeDao: RecipeDao
) {
    // Flows for reactive UI updates
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    val allFuelLogs: Flow<List<FuelLog>> = fuelDao.getAllLogs()
    val allOilLogs: Flow<List<OilLog>> = oilDao.getAllLogs()
    val allElectricityLogs: Flow<List<ElectricityLog>> = electricityDao.getAllLogs()
        .map { logs -> recalculateElectricityLogs(logs) }
    val allServiceLogs: Flow<List<ServiceLog>> = serviceDao.getAllLogs()
    val allSocialLogs: Flow<List<SocialLog>> = socialDao.getAllLogs()

    val allDailyGroceryLogs: Flow<List<DailyGroceryLog>> = warungDao.getAllDailyGroceryLogs()
    val allRandomExpenses: Flow<List<RandomExpense>> = warungDao.getAllRandomExpenses()
    val allChildExpenses: Flow<List<ChildExpenseLog>> = warungDao.getAllChildExpenses()
    val allWarungDebts: Flow<List<WarungDebt>> = warungDao.getAllWarungDebts()
    val allWarungDebtPayments: Flow<List<WarungDebtPayment>> = warungDao.getAllWarungDebtPayments()
    val allShoppingNoteItems: Flow<List<ShoppingNoteItem>> = warungDao.getAllShoppingNoteItems()

    val activeRecipes: Flow<List<Recipe>> = recipeDao.getAllActiveRecipes()
    val deletedRecipes: Flow<List<Recipe>> = recipeDao.getDeletedRecipes()
    val mealPlanItems: Flow<List<MealPlanItem>> = recipeDao.getAllMealPlanItems()

    // --- VEHICLE LOGIC ---
    suspend fun getAllVehiclesList(): List<Vehicle> = withContext(Dispatchers.IO) {
        vehicleDao.getAllVehiclesList()
    }

    suspend fun addVehicle(vehicle: Vehicle): Long = withContext(Dispatchers.IO) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun addVehicle(nama: String, plat: String, jenis: String, iconType: String = "Motor"): Long = withContext(Dispatchers.IO) {
        val vehicle = Vehicle(
            nama_kendaraan = nama,
            nomor_plat = plat,
            jenis_kendaraan = jenis,
            icon_type = iconType
        )
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: Vehicle) = withContext(Dispatchers.IO) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(id: Int) = withContext(Dispatchers.IO) {
        vehicleDao.deleteVehicleById(id)
    }

    suspend fun ensureDefaultVehicleExists(): Vehicle? = withContext(Dispatchers.IO) {
        val list = vehicleDao.getAllVehiclesList()
        list.firstOrNull()
    }

    suspend fun ensureDefaultVehiclesSeed(): Boolean = withContext(Dispatchers.IO) {
        // Safe implementation: never delete user-created vehicles from Room database
        false
    }

    // --- BENSIN LOGIC ---
    suspend fun getLatestFuelLog(): FuelLog? = fuelDao.getLatestLog()

    suspend fun getLatestFuelLogByVehicle(vehicleId: Int): FuelLog? = fuelDao.getLatestLogByVehicle(vehicleId)

    suspend fun addFuelLog(
        vehicleId: Int,
        kmMotor: Int,
        nominal: Int,
        liter: Float,
        jenisBbm: String = "Pertalite",
        hargaPerLiter: Int = 0,
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        val previousLog = fuelDao.getLatestLogByVehicle(vehicleId)
        val jarakTempuh = if (previousLog != null && kmMotor > previousLog.km_motor) {
            kmMotor - previousLog.km_motor
        } else {
            0
        }
        val kmPerLiter = if (liter > 0f && jarakTempuh > 0) {
            jarakTempuh / liter
        } else {
            0f
        }

        val allPreviousLogs = fuelDao.getAllLogsList().filter { it.vehicle_id == vehicleId && it.km_per_liter > 0f }
        val avgHistorical = if (allPreviousLogs.isNotEmpty()) {
            allPreviousLogs.map { it.km_per_liter }.average().toFloat()
        } else {
            0f
        }

        val vehicle = vehicleDao.getAllVehiclesList().find { it.id == vehicleId }
        val isMobil = vehicle?.jenis_kendaraan.equals("Mobil", ignoreCase = true)

        val isBoros = if (kmPerLiter > 0f) {
            if (avgHistorical > 0f) {
                kmPerLiter < (avgHistorical * 0.82f) || (if (isMobil) kmPerLiter < 8.5f else kmPerLiter < 28f)
            } else {
                if (isMobil) kmPerLiter < 8.5f else kmPerLiter < 28f
            }
        } else {
            false
        }

        val log = FuelLog(
            vehicle_id = vehicleId,
            tanggal = customTimestamp,
            km_motor = kmMotor,
            nominal = nominal,
            liter = liter,
            jarak_tempuh = jarakTempuh,
            km_per_liter = kmPerLiter,
            is_boros = isBoros,
            jenis_bbm = jenisBbm,
            harga_per_liter = hargaPerLiter
        )
        fuelDao.insertLog(log)

        if (vehicle != null && kmMotor > vehicle.current_odometer) {
            vehicleDao.updateVehicle(vehicle.copy(current_odometer = kmMotor))
        }
    }

    suspend fun updateFuelLog(log: FuelLog) = fuelDao.insertLog(log)

    suspend fun deleteFuelLog(id: Int) = fuelDao.deleteLogById(id)

    suspend fun updateVehicleOdometer(vehicleId: Int, newOdometer: Int) = withContext(Dispatchers.IO) {
        val vehicle = vehicleDao.getVehicleById(vehicleId) ?: return@withContext
        if (newOdometer > 0) {
            val updated = vehicle.copy(current_odometer = newOdometer)
            vehicleDao.updateVehicle(updated)
        }
    }

    // --- OLI LOGIC ---
    suspend fun getLatestOilLog(jenis: String? = null): OilLog? {
        return if (jenis != null) {
            oilDao.getLatestLogByJenis(jenis)
        } else {
            oilDao.getLatestLog()
        }
    }

    suspend fun getLatestOilLogByVehicle(vehicleId: Int): OilLog? = oilDao.getLatestLogByVehicle(vehicleId)

    suspend fun addOilLog(
        vehicleId: Int,
        kmMotor: Int,
        jenisOli: String,
        harga: Int,
        kapasitasMl: Int,
        intervalKm: Int = 3000,
        garansiBengkel: String = "",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        val targetKm = kmMotor + intervalKm
        val log = OilLog(
            vehicle_id = vehicleId,
            tanggal = customTimestamp,
            km_motor = kmMotor,
            jenis_oli = jenisOli,
            harga = harga,
            kapasitas_ml = kapasitasMl,
            target_km = targetKm,
            interval_km = intervalKm,
            garansi_bengkel = garansiBengkel
        )
        oilDao.insertLog(log)

        // Update vehicle odometer if higher
        val vehicle = vehicleDao.getVehicleById(vehicleId)
        if (vehicle != null && kmMotor > vehicle.current_odometer) {
            vehicleDao.updateVehicle(vehicle.copy(current_odometer = kmMotor))
        }
    }

    suspend fun updateOilLog(log: OilLog) = oilDao.insertLog(log)

    suspend fun deleteOilLog(id: Int) = oilDao.deleteLogById(id)

    // --- LISTRIK LOGIC ---
    suspend fun getLatestElectricityLog(): ElectricityLog? = electricityDao.getLatestLog()

    fun recalculateElectricityLogs(logs: List<ElectricityLog>): List<ElectricityLog> {
        if (logs.isEmpty()) return emptyList()

        val sorted = logs.sortedWith(compareBy<ElectricityLog> { it.tanggal }.thenBy { it.id })
        val result = mutableListOf<ElectricityLog>()

        for (i in sorted.indices) {
            val curr = sorted[i]
            if (i == 0) {
                val isInit = curr.is_initial || curr.harga == 0
                val totalAktif = curr.jumlah_kwh
                result.add(
                    curr.copy(
                        is_initial = isInit,
                        sisa_sebelumnya = 0f,
                        total_kwh_aktif = totalAktif,
                        durasi_hari = 0,
                        kwh_per_hari = 0f,
                        is_boros = false
                    )
                )
            } else {
                val prev = result[i - 1]
                val sisaPrev = prev.total_kwh_aktif
                val totalAktif = sisaPrev + curr.jumlah_kwh

                if (curr.is_initial) {
                    result.add(
                        curr.copy(
                            sisa_sebelumnya = sisaPrev,
                            total_kwh_aktif = totalAktif,
                            durasi_hari = 0,
                            kwh_per_hari = 0f,
                            is_boros = false
                        )
                    )
                } else {
                    if (prev.is_initial) {
                        // First token purchase after initial meter balance entry.
                        // Token adds to baseline; no usage cycle completed yet.
                        result.add(
                            curr.copy(
                                sisa_sebelumnya = sisaPrev,
                                total_kwh_aktif = totalAktif,
                                durasi_hari = 0,
                                kwh_per_hari = 0f,
                                is_boros = false
                            )
                        )
                    } else {
                        val diffMs = curr.tanggal - prev.tanggal
                        val days = if (diffMs > 0) TimeUnit.MILLISECONDS.toDays(diffMs).toInt() else 0

                        if (days < 1) {
                            // Same day purchase or <24 hrs difference
                            result.add(
                                curr.copy(
                                    sisa_sebelumnya = sisaPrev,
                                    total_kwh_aktif = totalAktif,
                                    durasi_hari = 0,
                                    kwh_per_hari = 0f,
                                    is_boros = false
                                )
                            )
                        } else {
                            val baseKwh = if (prev.total_kwh_aktif > 0f) prev.total_kwh_aktif else prev.jumlah_kwh
                            val kwhPerHari = baseKwh / days.toFloat()

                            val validHistoricalRates = result
                                .filter { !it.is_initial && it.kwh_per_hari > 0f }
                                .map { it.kwh_per_hari }

                            val avgKwhPerHari = if (validHistoricalRates.isNotEmpty()) {
                                validHistoricalRates.average().toFloat()
                            } else {
                                12.0f
                            }

                            val isBoros = kwhPerHari > (avgKwhPerHari * 1.35f) && kwhPerHari > 18.0f

                            result.add(
                                curr.copy(
                                    sisa_sebelumnya = sisaPrev,
                                    total_kwh_aktif = totalAktif,
                                    durasi_hari = days,
                                    kwh_per_hari = kwhPerHari,
                                    is_boros = isBoros
                                )
                            )
                        }
                    }
                }
            }
        }

        return result.sortedWith(compareByDescending<ElectricityLog> { it.tanggal }.thenByDescending { it.id })
    }

    suspend fun addElectricityLog(
        harga: Int,
        jumlahKwh: Float,
        isInitial: Boolean = false,
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        val existingLogs = electricityDao.getAllLogsList()
        val actualIsInitial = isInitial || (existingLogs.isEmpty() && harga == 0)

        val newLog = ElectricityLog(
            tanggal = customTimestamp,
            harga = harga,
            jumlah_kwh = jumlahKwh,
            is_initial = actualIsInitial
        )

        val allList = existingLogs + newLog
        val recalculated = recalculateElectricityLogs(allList)

        electricityDao.insertAll(recalculated)
    }

    suspend fun updateElectricityLog(log: ElectricityLog) {
        electricityDao.insertLog(log)
        val remaining = electricityDao.getAllLogsList()
        if (remaining.isNotEmpty()) {
            val recalculated = recalculateElectricityLogs(remaining)
            electricityDao.insertAll(recalculated)
        }
    }

    suspend fun deleteElectricityLog(id: Int) {
        electricityDao.deleteLogById(id)
        val remaining = electricityDao.getAllLogsList()
        if (remaining.isNotEmpty()) {
            val recalculated = recalculateElectricityLogs(remaining)
            electricityDao.insertAll(recalculated)
        }
    }

    // --- SERVICE / PERAWATAN LOGIC ---
    suspend fun addServiceLog(
        vehicleId: Int,
        kmMotor: Int,
        kategori: String,
        deskripsiItem: String,
        totalBiaya: Int,
        intervalKm: Int = 5000,
        garansiBengkel: String = "",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        val targetKmNext = kmMotor + intervalKm
        val log = ServiceLog(
            vehicle_id = vehicleId,
            tanggal = customTimestamp,
            km_motor = kmMotor,
            kategori = kategori,
            deskripsi_item = deskripsiItem,
            total_biaya = totalBiaya,
            target_km_next = targetKmNext,
            interval_km = intervalKm,
            garansi_bengkel = garansiBengkel
        )
        serviceDao.insertLog(log)

        // Update vehicle odometer if higher
        val vehicle = vehicleDao.getVehicleById(vehicleId)
        if (vehicle != null && kmMotor > vehicle.current_odometer) {
            vehicleDao.updateVehicle(vehicle.copy(current_odometer = kmMotor))
        }
    }

    suspend fun updateServiceLog(log: ServiceLog) = serviceDao.insertLog(log)

    suspend fun deleteServiceLog(id: Int) = serviceDao.deleteLogById(id)

    // --- KEUANGAN RT & SOSIAL (JIMPITAN & KURBAN) LOGIC ---
    suspend fun addSocialLog(
        kategori: String,
        nominal: Int,
        keterangan: String,
        tipeTransaksi: String = "Masuk",
        customTimestamp: Long = System.currentTimeMillis()
    ) {
        val log = SocialLog(
            tanggal = customTimestamp,
            kategori = kategori,
            nominal = nominal,
            keterangan = keterangan,
            tipe_transaksi = tipeTransaksi
        )
        socialDao.insertLog(log)
    }

    suspend fun updateSocialLog(log: SocialLog) {
        socialDao.updateLog(log)
    }

    suspend fun deleteSocialLog(id: Int) = socialDao.deleteLogById(id)

    // --- WARUNG & BELANJA LOGIC ---
    suspend fun getAllDailyGroceryLogsList(): List<DailyGroceryLog> = warungDao.getAllDailyGroceryLogsList()

    suspend fun addDailyGroceryLog(
        tanggal: String,
        modalAwal: Double,
        sisaUang: Double,
        rincian: String = "",
        catatan: String = ""
    ): Long {
        val totalPengeluaran = (modalAwal - sisaUang).coerceAtLeast(0.0)
        val log = DailyGroceryLog(
            tanggal = tanggal,
            modalAwal = modalAwal,
            sisaUang = sisaUang,
            totalPengeluaran = totalPengeluaran,
            rincian = rincian,
            catatan = catatan
        )
        return warungDao.insertDailyGroceryLog(log)
    }

    suspend fun updateDailyGroceryLog(log: DailyGroceryLog) {
        warungDao.updateDailyGroceryLog(log)
    }

    suspend fun deleteDailyGroceryLog(id: Int) = warungDao.deleteDailyGroceryLogById(id)

    // --- RANDOM EXPENSES (TERSIER) LOGIC ---
    suspend fun getAllRandomExpensesList(): List<RandomExpense> = warungDao.getAllRandomExpensesList()

    suspend fun addRandomExpense(
        tanggal: String,
        modalAwal: Double,
        sisaUang: Double,
        rincian: String = "",
        catatan: String = ""
    ): Long {
        val totalPengeluaran = (modalAwal - sisaUang).coerceAtLeast(0.0)
        val log = RandomExpense(
            tanggal = tanggal,
            modalAwal = modalAwal,
            sisaUang = sisaUang,
            totalPengeluaran = totalPengeluaran,
            rincian = rincian,
            catatan = catatan
        )
        return warungDao.insertRandomExpense(log)
    }

    suspend fun updateRandomExpense(log: RandomExpense) {
        warungDao.updateRandomExpense(log)
    }

    suspend fun deleteRandomExpense(id: Int) = warungDao.deleteRandomExpenseById(id)

    // --- CHILD EXPENSES (ANAK) LOGIC ---
    suspend fun getAllChildExpensesList(): List<ChildExpenseLog> = warungDao.getAllChildExpensesList()

    suspend fun addChildExpense(
        tanggal: String,
        modalAwal: Double,
        sisaUang: Double,
        rincian: String = "",
        catatan: String = ""
    ): Long {
        val totalPengeluaran = (modalAwal - sisaUang).coerceAtLeast(0.0)
        val log = ChildExpenseLog(
            tanggal = tanggal,
            modalAwal = modalAwal,
            sisaUang = sisaUang,
            totalPengeluaran = totalPengeluaran,
            rincian = rincian,
            catatan = catatan
        )
        return warungDao.insertChildExpense(log)
    }

    suspend fun updateChildExpense(log: ChildExpenseLog) {
        warungDao.updateChildExpense(log)
    }

    suspend fun deleteChildExpense(id: Int) = warungDao.deleteChildExpenseById(id)

    suspend fun getAllWarungDebtsList(): List<WarungDebt> = warungDao.getAllWarungDebtsList()

    suspend fun addWarungDebt(
        tanggal: String,
        namaWarung: String,
        nominal: Double,
        alasan: String = ""
    ): Long {
        val debt = WarungDebt(
            tanggal = tanggal,
            namaWarung = if (namaWarung.isBlank()) "Warung" else namaWarung,
            nominal = nominal,
            alasan = alasan,
            isLunas = false,
            totalDibayar = 0.0
        )
        return warungDao.insertWarungDebt(debt)
    }

    suspend fun updateWarungDebt(debt: WarungDebt) {
        warungDao.updateWarungDebt(debt)
    }

    suspend fun deleteWarungDebt(id: Int) = warungDao.deleteWarungDebtById(id)

    suspend fun getAllWarungDebtPaymentsList(): List<WarungDebtPayment> = warungDao.getAllWarungDebtPaymentsList()

    suspend fun addWarungDebtPayment(
        debtId: Int,
        tanggal: String,
        nominalBayar: Double,
        catatan: String = ""
    ) {
        val payment = WarungDebtPayment(
            debtId = debtId,
            tanggal = tanggal,
            nominalBayar = nominalBayar,
            catatan = catatan
        )
        warungDao.insertWarungDebtPayment(payment)

        // Update debt total paid & lunas status
        val debt = warungDao.getWarungDebtById(debtId)
        if (debt != null) {
            val newTotalPaid = debt.totalDibayar + nominalBayar
            val updatedDebt = debt.copy(
                totalDibayar = newTotalPaid,
                isLunas = newTotalPaid >= debt.nominal
            )
            warungDao.updateWarungDebt(updatedDebt)
        }
    }

    suspend fun deleteWarungDebtPayment(payment: WarungDebtPayment) {
        warungDao.deleteWarungDebtPaymentById(payment.id)
        val debt = warungDao.getWarungDebtById(payment.debtId)
        if (debt != null) {
            val newTotalPaid = (debt.totalDibayar - payment.nominalBayar).coerceAtLeast(0.0)
            val updatedDebt = debt.copy(
                totalDibayar = newTotalPaid,
                isLunas = newTotalPaid >= debt.nominal
            )
            warungDao.updateWarungDebt(updatedDebt)
        }
    }

    suspend fun getAllShoppingNoteItemsList(): List<ShoppingNoteItem> = warungDao.getAllShoppingNoteItemsList()

    suspend fun addShoppingNoteItem(
        namaBarang: String,
        prioritas: String = "Sedang",
        estimasiHarga: Double = 0.0,
        catatan: String = ""
    ): Long {
        val item = ShoppingNoteItem(
            namaBarang = namaBarang,
            prioritas = prioritas,
            isDone = false,
            estimasiHarga = estimasiHarga,
            catatan = catatan
        )
        return warungDao.insertShoppingNoteItem(item)
    }

    suspend fun updateShoppingNoteItem(item: ShoppingNoteItem) {
        warungDao.updateShoppingNoteItem(item)
    }

    suspend fun toggleShoppingNoteDone(item: ShoppingNoteItem) {
        warungDao.updateShoppingNoteItem(item.copy(isDone = !item.isDone))
    }

    suspend fun deleteShoppingNoteItem(id: Int) = warungDao.deleteShoppingNoteItemById(id)

    suspend fun clearCompletedShoppingNotes() = warungDao.deleteCompletedShoppingNotes()


    suspend fun clearAllLocalData() {
        vehicleDao.clearAll()
        fuelDao.clearAll()
        oilDao.clearAll()
        electricityDao.clearAll()
        serviceDao.deleteAll()
        socialDao.deleteAll()
        warungDao.deleteAllDailyGroceryLogs()
        warungDao.deleteAllRandomExpenses()
        warungDao.deleteAllChildExpenses()
        warungDao.deleteAllWarungDebts()
        warungDao.deleteAllWarungDebtPayments()
        warungDao.deleteAllShoppingNoteItems()
        recipeDao.clearAllRecipes()
        recipeDao.clearAllMealPlans()
    }

    // --- BULK FETCH & BACKUP / RESTORE ---
    suspend fun getAllFuelLogsList(): List<FuelLog> = fuelDao.getAllLogsList()
    suspend fun getAllOilLogsList(): List<OilLog> = oilDao.getAllLogsList()
    suspend fun getAllElectricityLogsList(): List<ElectricityLog> = electricityDao.getAllLogsList()
    suspend fun getAllServiceLogsList(): List<ServiceLog> = serviceDao.getAllLogsList()
    suspend fun getAllSocialLogsList(): List<SocialLog> = socialDao.getAllLogsList()
    suspend fun getAllRecipesList(): List<Recipe> = recipeDao.getAllActiveRecipesList()
    suspend fun getAllMealPlanItemsList(): List<MealPlanItem> = recipeDao.getAllMealPlanItemsList()

    suspend fun restoreAllLogs(
        vehicles: List<Vehicle>,
        fuelLogs: List<FuelLog>,
        oilLogs: List<OilLog>,
        electricityLogs: List<ElectricityLog>,
        serviceLogs: List<ServiceLog> = emptyList(),
        socialLogs: List<SocialLog> = emptyList(),
        dailyGroceryLogs: List<DailyGroceryLog> = emptyList(),
        randomExpenses: List<RandomExpense> = emptyList(),
        childExpenses: List<ChildExpenseLog> = emptyList(),
        warungDebts: List<WarungDebt> = emptyList(),
        warungDebtPayments: List<WarungDebtPayment> = emptyList(),
        shoppingNoteItems: List<ShoppingNoteItem> = emptyList()
    ) {
        vehicleDao.clearAll()
        fuelDao.clearAll()
        oilDao.clearAll()
        electricityDao.clearAll()
        serviceDao.deleteAll()
        socialDao.deleteAll()
        warungDao.deleteAllDailyGroceryLogs()
        warungDao.deleteAllRandomExpenses()
        warungDao.deleteAllChildExpenses()
        warungDao.deleteAllWarungDebts()
        warungDao.deleteAllWarungDebtPayments()
        warungDao.deleteAllShoppingNoteItems()

        if (vehicles.isNotEmpty()) {
            vehicleDao.insertAll(vehicles)
        }
        fuelDao.insertAll(fuelLogs)
        oilDao.insertAll(oilLogs)
        electricityDao.insertAll(electricityLogs)
        if (serviceLogs.isNotEmpty()) {
            serviceDao.insertAll(serviceLogs)
        }
        if (socialLogs.isNotEmpty()) {
            socialDao.insertAll(socialLogs)
        }
        dailyGroceryLogs.forEach { warungDao.insertDailyGroceryLog(it) }
        randomExpenses.forEach { warungDao.insertRandomExpense(it) }
        childExpenses.forEach { warungDao.insertChildExpense(it) }
        warungDebts.forEach { warungDao.insertWarungDebt(it) }
        warungDebtPayments.forEach { warungDao.insertWarungDebtPayment(it) }
        shoppingNoteItems.forEach { warungDao.insertShoppingNoteItem(it) }
    }

    suspend fun mergeAllLogs(
        vehicles: List<Vehicle>,
        fuelLogs: List<FuelLog>,
        oilLogs: List<OilLog>,
        electricityLogs: List<ElectricityLog>,
        serviceLogs: List<ServiceLog> = emptyList(),
        socialLogs: List<SocialLog> = emptyList(),
        dailyGroceryLogs: List<DailyGroceryLog> = emptyList(),
        randomExpenses: List<RandomExpense> = emptyList(),
        childExpenses: List<ChildExpenseLog> = emptyList(),
        warungDebts: List<WarungDebt> = emptyList(),
        warungDebtPayments: List<WarungDebtPayment> = emptyList(),
        shoppingNoteItems: List<ShoppingNoteItem> = emptyList(),
        recipes: List<Recipe> = emptyList(),
        mealPlanItems: List<MealPlanItem> = emptyList()
    ) {
        if (vehicles.isNotEmpty()) {
            vehicleDao.insertAll(vehicles)
        }
        fuelDao.insertAll(fuelLogs)
        oilDao.insertAll(oilLogs)
        electricityDao.insertAll(electricityLogs)
        if (serviceLogs.isNotEmpty()) {
            serviceDao.insertAll(serviceLogs)
        }
        if (socialLogs.isNotEmpty()) {
            socialDao.insertAll(socialLogs)
        }
        dailyGroceryLogs.forEach { warungDao.insertDailyGroceryLog(it) }
        randomExpenses.forEach { warungDao.insertRandomExpense(it) }
        childExpenses.forEach { warungDao.insertChildExpense(it) }
        warungDebts.forEach { warungDao.insertWarungDebt(it) }
        warungDebtPayments.forEach { warungDao.insertWarungDebtPayment(it) }
        shoppingNoteItems.forEach { warungDao.insertShoppingNoteItem(it) }
        recipes.forEach { recipeDao.insertRecipe(it) }
        mealPlanItems.forEach { recipeDao.insertMealPlanItem(it) }
    }

    // --- RECIPE & MEAL PLAN METHODS ---
    suspend fun addRecipe(
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
    ): Long {
        val recipe = Recipe(
            title = title,
            description = description,
            category = category,
            prepTime = prepTime,
            cookTime = cookTime,
            yields = yields,
            ingredients = ingredients,
            directions = directions,
            skillRating = skillRating,
            isFavorite = isFavorite,
            flavorTag = flavorTag,
            source = source
        )
        return recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun softDeleteRecipe(id: Int) {
        recipeDao.softDeleteRecipe(id)
    }

    suspend fun restoreRecipe(id: Int) {
        recipeDao.restoreRecipe(id)
    }

    suspend fun hardDeleteRecipe(id: Int) {
        recipeDao.hardDeleteRecipe(id)
    }

    suspend fun clearTrashRecipes() {
        recipeDao.clearTrash()
    }

    suspend fun addMealPlanItem(dayOfWeek: String, recipeId: Int, recipeTitle: String, mealType: String = "Makan Siang/Malam"): Long {
        val item = MealPlanItem(
            dayOfWeek = dayOfWeek,
            recipeId = recipeId,
            recipeTitle = recipeTitle,
            mealType = mealType
        )
        return recipeDao.insertMealPlanItem(item)
    }

    suspend fun deleteMealPlanItem(id: Int) {
        recipeDao.deleteMealPlanItem(id)
    }

    suspend fun clearMealPlanForDay(dayOfWeek: String) {
        recipeDao.clearMealPlanForDay(dayOfWeek)
    }

    suspend fun clearAllMealPlans() {
        recipeDao.clearAllMealPlans()
    }

    suspend fun addRecipeIngredientsToShoppingList(ingredients: List<String>) {
        ingredients.forEach { line ->
            val clean = line.trim()
            if (clean.isNotBlank()) {
                val item = ShoppingNoteItem(
                    namaBarang = clean,
                    prioritas = "Sedang",
                    isDone = false,
                    estimasiHarga = 0.0,
                    catatan = "Daftar Belanja Resep Mingguan"
                )
                warungDao.insertShoppingNoteItem(item)
            }
        }
    }
}
