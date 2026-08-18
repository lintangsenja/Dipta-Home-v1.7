package com.example.data.backup

import android.content.Context
import android.net.Uri
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
import com.example.data.repository.TrackerRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class BackupResult(
    val success: Boolean,
    val message: String,
    val vehicleCount: Int = 0,
    val fuelCount: Int = 0,
    val oilCount: Int = 0,
    val electricityCount: Int = 0
)

class BackupRestoreManager(
    private val context: Context,
    private val trackerRepository: TrackerRepository
) {

    suspend fun createJsonBackup(): String {
        val vehicles = trackerRepository.getAllVehiclesList()
        val fuelLogs = trackerRepository.getAllFuelLogsList()
        val oilLogs = trackerRepository.getAllOilLogsList()
        val electricityLogs = trackerRepository.getAllElectricityLogsList()
        val serviceLogs = trackerRepository.getAllServiceLogsList()
        val socialLogs = trackerRepository.getAllSocialLogsList()
        val dailyGroceryLogs = trackerRepository.getAllDailyGroceryLogsList()
        val randomExpenses = trackerRepository.getAllRandomExpensesList()
        val childExpenses = trackerRepository.getAllChildExpensesList()
        val warungDebts = trackerRepository.getAllWarungDebtsList()
        val warungDebtPayments = trackerRepository.getAllWarungDebtPaymentsList()
        val shoppingNoteItems = trackerRepository.getAllShoppingNoteItemsList()
        val mainSalary = trackerRepository.getMainSalaryConfigDirect()
        val additionalIncomes = trackerRepository.getAllAdditionalIncomesList()

        val rootJson = JSONObject()
        rootJson.put("app", "Dipta Home")
        rootJson.put("version", 6)
        rootJson.put("exportedAt", System.currentTimeMillis())

        // Vehicles
        val vehicleArray = JSONArray()
        vehicles.forEach { v ->
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("nama_kendaraan", v.nama_kendaraan)
            obj.put("nomor_plat", v.nomor_plat)
            obj.put("jenis_kendaraan", v.jenis_kendaraan)
            obj.put("icon_type", v.icon_type)
            obj.put("current_odometer", v.current_odometer)
            vehicleArray.put(obj)
        }
        rootJson.put("vehicles", vehicleArray)

        // Fuel
        val fuelArray = JSONArray()
        fuelLogs.forEach { fuel ->
            val obj = JSONObject()
            obj.put("id", fuel.id)
            obj.put("vehicle_id", fuel.vehicle_id)
            obj.put("tanggal", fuel.tanggal)
            obj.put("km_motor", fuel.km_motor)
            obj.put("nominal", fuel.nominal)
            obj.put("liter", fuel.liter.toDouble())
            obj.put("jarak_tempuh", fuel.jarak_tempuh)
            obj.put("km_per_liter", fuel.km_per_liter.toDouble())
            obj.put("is_boros", fuel.is_boros)
            fuelArray.put(obj)
        }
        rootJson.put("fuel_logs", fuelArray)

        // Oil
        val oilArray = JSONArray()
        oilLogs.forEach { oil ->
            val obj = JSONObject()
            obj.put("id", oil.id)
            obj.put("vehicle_id", oil.vehicle_id)
            obj.put("tanggal", oil.tanggal)
            obj.put("km_motor", oil.km_motor)
            obj.put("jenis_oli", oil.jenis_oli)
            obj.put("harga", oil.harga)
            obj.put("kapasitas_ml", oil.kapasitas_ml)
            obj.put("target_km", oil.target_km)
            obj.put("interval_km", oil.interval_km)
            oilArray.put(obj)
        }
        rootJson.put("oil_logs", oilArray)

        // Electricity
        val elecArray = JSONArray()
        electricityLogs.forEach { elec ->
            val obj = JSONObject()
            obj.put("id", elec.id)
            obj.put("tanggal", elec.tanggal)
            obj.put("harga", elec.harga)
            obj.put("jumlah_kwh", elec.jumlah_kwh.toDouble())
            obj.put("sisa_sebelumnya", elec.sisa_sebelumnya.toDouble())
            obj.put("total_kwh_aktif", elec.total_kwh_aktif.toDouble())
            obj.put("durasi_hari", elec.durasi_hari)
            obj.put("kwh_per_hari", elec.kwh_per_hari.toDouble())
            obj.put("is_boros", elec.is_boros)
            obj.put("is_initial", elec.is_initial)
            elecArray.put(obj)
        }
        rootJson.put("electricity_logs", elecArray)

        // Service
        val serviceArray = JSONArray()
        serviceLogs.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("vehicle_id", s.vehicle_id)
            obj.put("tanggal", s.tanggal)
            obj.put("km_motor", s.km_motor)
            obj.put("kategori", s.kategori)
            obj.put("deskripsi_item", s.deskripsi_item)
            obj.put("total_biaya", s.total_biaya)
            serviceArray.put(obj)
        }
        rootJson.put("service_logs", serviceArray)

        // Social
        val socialArray = JSONArray()
        socialLogs.forEach { soc ->
            val obj = JSONObject()
            obj.put("id", soc.id)
            obj.put("tanggal", soc.tanggal)
            obj.put("kategori", soc.kategori)
            obj.put("nominal", soc.nominal)
            obj.put("keterangan", soc.keterangan)
            socialArray.put(obj)
        }
        rootJson.put("social_logs", socialArray)

        // Daily Grocery
        val groceryArray = JSONArray()
        dailyGroceryLogs.forEach { log ->
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("tanggal", log.tanggal)
            obj.put("modalAwal", log.modalAwal)
            obj.put("sisaUang", log.sisaUang)
            obj.put("totalPengeluaran", log.totalPengeluaran)
            obj.put("rincian", log.rincian)
            obj.put("catatan", log.catatan)
            obj.put("timestamp", log.timestamp)
            groceryArray.put(obj)
        }
        rootJson.put("daily_grocery_logs", groceryArray)

        // Random Expenses
        val randomArray = JSONArray()
        randomExpenses.forEach { log ->
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("tanggal", log.tanggal)
            obj.put("modalAwal", log.modalAwal)
            obj.put("sisaUang", log.sisaUang)
            obj.put("totalPengeluaran", log.totalPengeluaran)
            obj.put("rincian", log.rincian)
            obj.put("catatan", log.catatan)
            obj.put("timestamp", log.timestamp)
            randomArray.put(obj)
        }
        rootJson.put("random_expenses", randomArray)

        // Child Expenses
        val childArray = JSONArray()
        childExpenses.forEach { log ->
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("tanggal", log.tanggal)
            obj.put("modalAwal", log.modalAwal)
            obj.put("sisaUang", log.sisaUang)
            obj.put("totalPengeluaran", log.totalPengeluaran)
            obj.put("rincian", log.rincian)
            obj.put("catatan", log.catatan)
            obj.put("timestamp", log.timestamp)
            childArray.put(obj)
        }
        rootJson.put("child_expenses", childArray)

        // Warung Debts
        val debtArray = JSONArray()
        warungDebts.forEach { debt ->
            val obj = JSONObject()
            obj.put("id", debt.id)
            obj.put("tanggal", debt.tanggal)
            obj.put("namaWarung", debt.namaWarung)
            obj.put("nominal", debt.nominal)
            obj.put("alasan", debt.alasan)
            obj.put("isLunas", debt.isLunas)
            obj.put("totalDibayar", debt.totalDibayar)
            obj.put("timestamp", debt.timestamp)
            debtArray.put(obj)
        }
        rootJson.put("warung_debts", debtArray)

        // Debt Payments
        val paymentArray = JSONArray()
        warungDebtPayments.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("debtId", p.debtId)
            obj.put("tanggal", p.tanggal)
            obj.put("nominalBayar", p.nominalBayar)
            obj.put("catatan", p.catatan)
            obj.put("timestamp", p.timestamp)
            paymentArray.put(obj)
        }
        rootJson.put("warung_debt_payments", paymentArray)

        // Shopping Note Items
        val shoppingArray = JSONArray()
        shoppingNoteItems.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("namaBarang", item.namaBarang)
            obj.put("prioritas", item.prioritas)
            obj.put("isDone", item.isDone)
            obj.put("estimasiHarga", item.estimasiHarga)
            obj.put("catatan", item.catatan)
            obj.put("timestamp", item.timestamp)
            shoppingArray.put(obj)
        }
        rootJson.put("shopping_note_items", shoppingArray)

        // Main Salary Config
        if (mainSalary != null) {
            val msObj = JSONObject()
            msObj.put("id", mainSalary.id)
            msObj.put("nominal", mainSalary.nominal)
            msObj.put("catatan", mainSalary.catatan)
            msObj.put("updatedAt", mainSalary.updatedAt)
            rootJson.put("main_salary_config", msObj)
        }

        // Additional Incomes
        val incomeArray = JSONArray()
        additionalIncomes.forEach { inc ->
            val obj = JSONObject()
            obj.put("id", inc.id)
            obj.put("judul", inc.judul)
            obj.put("kategori", inc.kategori)
            obj.put("nominal", inc.nominal)
            obj.put("tanggal", inc.tanggal)
            obj.put("timestamp", inc.timestamp)
            obj.put("isActive", inc.isActive)
            obj.put("targetCycleOffset", inc.targetCycleOffset)
            obj.put("targetCycleLabel", inc.targetCycleLabel)
            obj.put("catatan", inc.catatan)
            incomeArray.put(obj)
        }
        rootJson.put("additional_incomes", incomeArray)

        return rootJson.toString(2)
    }

    suspend fun exportToUri(uri: Uri): BackupResult {
        return try {
            val jsonString = createJsonBackup()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            BackupResult(
                success = true,
                message = "Berhasil mengekspor cadangan data Dipta Home ke file .json"
            )
        } catch (e: Exception) {
            BackupResult(
                success = false,
                message = "Gagal mengekspor data: ${e.localizedMessage ?: "Terjadi kesalahan"}"
            )
        }
    }

    suspend fun restoreFromUri(uri: Uri, overwrite: Boolean = true): BackupResult {
        return try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }

            val jsonString = stringBuilder.toString()
            if (jsonString.isBlank()) {
                return BackupResult(false, "File cadangan kosong atau tidak valid.")
            }

            val rootJson = JSONObject(jsonString)

            val appName = rootJson.optString("app", "")
            if (appName.isBlank() && !rootJson.has("fuel_logs")) {
                return BackupResult(false, "Format file .json bukan cadangan Dipta Home yang valid.")
            }

            val vehicleList = mutableListOf<Vehicle>()
            if (rootJson.has("vehicles")) {
                val vArray = rootJson.getJSONArray("vehicles")
                for (i in 0 until vArray.length()) {
                    val obj = vArray.getJSONObject(i)
                    vehicleList.add(
                        Vehicle(
                            id = obj.optInt("id", 0),
                            nama_kendaraan = obj.optString("nama_kendaraan", "Kendaraan"),
                            nomor_plat = obj.optString("nomor_plat", ""),
                            jenis_kendaraan = obj.optString("jenis_kendaraan", "Motor"),
                            icon_type = obj.optString("icon_type", "Motor"),
                            current_odometer = obj.optInt("current_odometer", 0)
                        )
                    )
                }
            }

            val fuelList = mutableListOf<FuelLog>()
            if (rootJson.has("fuel_logs")) {
                val fuelArray = rootJson.getJSONArray("fuel_logs")
                for (i in 0 until fuelArray.length()) {
                    val obj = fuelArray.getJSONObject(i)
                    fuelList.add(
                        FuelLog(
                            id = obj.optInt("id", 0),
                            vehicle_id = obj.optInt("vehicle_id", 1),
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            km_motor = obj.optInt("km_motor", 0),
                            nominal = obj.optInt("nominal", 0),
                            liter = obj.optDouble("liter", 0.0).toFloat(),
                            jarak_tempuh = obj.optInt("jarak_tempuh", 0),
                            km_per_liter = obj.optDouble("km_per_liter", 0.0).toFloat(),
                            is_boros = obj.optBoolean("is_boros", false)
                        )
                    )
                }
            }

            val oilList = mutableListOf<OilLog>()
            if (rootJson.has("oil_logs")) {
                val oilArray = rootJson.getJSONArray("oil_logs")
                for (i in 0 until oilArray.length()) {
                    val obj = oilArray.getJSONObject(i)
                    oilList.add(
                        OilLog(
                            id = obj.optInt("id", 0),
                            vehicle_id = obj.optInt("vehicle_id", 1),
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            km_motor = obj.optInt("km_motor", 0),
                            jenis_oli = obj.optString("jenis_oli", "Oli Mesin"),
                            harga = obj.optInt("harga", 0),
                            kapasitas_ml = obj.optInt("kapasitas_ml", 0),
                            target_km = obj.optInt("target_km", 0),
                            interval_km = obj.optInt("interval_km", 3000)
                        )
                    )
                }
            }

            val elecList = mutableListOf<ElectricityLog>()
            if (rootJson.has("electricity_logs")) {
                val elecArray = rootJson.getJSONArray("electricity_logs")
                for (i in 0 until elecArray.length()) {
                    val obj = elecArray.getJSONObject(i)
                    elecList.add(
                        ElectricityLog(
                            id = obj.optInt("id", 0),
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            harga = obj.optInt("harga", 0),
                            jumlah_kwh = obj.optDouble("jumlah_kwh", 0.0).toFloat(),
                            sisa_sebelumnya = obj.optDouble("sisa_sebelumnya", 0.0).toFloat(),
                            total_kwh_aktif = obj.optDouble("total_kwh_aktif", 0.0).toFloat(),
                            durasi_hari = obj.optInt("durasi_hari", 0),
                            kwh_per_hari = obj.optDouble("kwh_per_hari", 0.0).toFloat(),
                            is_boros = obj.optBoolean("is_boros", false),
                            is_initial = obj.optBoolean("is_initial", false)
                        )
                    )
                }
            }

            val serviceList = mutableListOf<ServiceLog>()
            if (rootJson.has("service_logs")) {
                val serviceArray = rootJson.getJSONArray("service_logs")
                for (i in 0 until serviceArray.length()) {
                    val obj = serviceArray.getJSONObject(i)
                    serviceList.add(
                        ServiceLog(
                            id = obj.optInt("id", 0),
                            vehicle_id = obj.optInt("vehicle_id", 1),
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            km_motor = obj.optInt("km_motor", 0),
                            kategori = obj.optString("kategori", "Servis Rutin"),
                            deskripsi_item = obj.optString("deskripsi_item", ""),
                            total_biaya = obj.optInt("total_biaya", 0)
                        )
                    )
                }
            }

            val socialList = mutableListOf<SocialLog>()
            if (rootJson.has("social_logs")) {
                val socialArray = rootJson.getJSONArray("social_logs")
                for (i in 0 until socialArray.length()) {
                    val obj = socialArray.getJSONObject(i)
                    socialList.add(
                        SocialLog(
                            id = obj.optInt("id", 0),
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            kategori = obj.optString("kategori", "Iuran Jimpitan Warga"),
                            nominal = obj.optInt("nominal", 0),
                            keterangan = obj.optString("keterangan", "")
                        )
                    )
                }
            }

            val groceryList = mutableListOf<DailyGroceryLog>()
            if (rootJson.has("daily_grocery_logs")) {
                val arr = rootJson.getJSONArray("daily_grocery_logs")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    groceryList.add(
                        DailyGroceryLog(
                            id = obj.optInt("id", 0),
                            tanggal = obj.optString("tanggal", ""),
                            modalAwal = obj.optDouble("modalAwal", 0.0),
                            sisaUang = obj.optDouble("sisaUang", 0.0),
                            totalPengeluaran = obj.optDouble("totalPengeluaran", 0.0),
                            rincian = obj.optString("rincian", ""),
                            catatan = obj.optString("catatan", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val randomList = mutableListOf<RandomExpense>()
            if (rootJson.has("random_expenses")) {
                val arr = rootJson.getJSONArray("random_expenses")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    randomList.add(
                        RandomExpense(
                            id = obj.optInt("id", 0),
                            tanggal = obj.optString("tanggal", ""),
                            modalAwal = obj.optDouble("modalAwal", 0.0),
                            sisaUang = obj.optDouble("sisaUang", 0.0),
                            totalPengeluaran = obj.optDouble("totalPengeluaran", 0.0),
                            rincian = obj.optString("rincian", ""),
                            catatan = obj.optString("catatan", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val childList = mutableListOf<ChildExpenseLog>()
            if (rootJson.has("child_expenses")) {
                val arr = rootJson.getJSONArray("child_expenses")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    childList.add(
                        ChildExpenseLog(
                            id = obj.optInt("id", 0),
                            tanggal = obj.optString("tanggal", ""),
                            modalAwal = obj.optDouble("modalAwal", 0.0),
                            sisaUang = obj.optDouble("sisaUang", 0.0),
                            totalPengeluaran = obj.optDouble("totalPengeluaran", 0.0),
                            rincian = obj.optString("rincian", ""),
                            catatan = obj.optString("catatan", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val debtList = mutableListOf<WarungDebt>()
            if (rootJson.has("warung_debts")) {
                val arr = rootJson.getJSONArray("warung_debts")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    debtList.add(
                        WarungDebt(
                            id = obj.optInt("id", 0),
                            tanggal = obj.optString("tanggal", ""),
                            namaWarung = obj.optString("namaWarung", "Warung"),
                            nominal = obj.optDouble("nominal", 0.0),
                            alasan = obj.optString("alasan", ""),
                            isLunas = obj.optBoolean("isLunas", false),
                            totalDibayar = obj.optDouble("totalDibayar", 0.0),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val paymentList = mutableListOf<WarungDebtPayment>()
            if (rootJson.has("warung_debt_payments")) {
                val arr = rootJson.getJSONArray("warung_debt_payments")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    paymentList.add(
                        WarungDebtPayment(
                            id = obj.optInt("id", 0),
                            debtId = obj.optInt("debtId", 0),
                            tanggal = obj.optString("tanggal", ""),
                            nominalBayar = obj.optDouble("nominalBayar", 0.0),
                            catatan = obj.optString("catatan", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val shoppingList = mutableListOf<ShoppingNoteItem>()
            if (rootJson.has("shopping_note_items")) {
                val arr = rootJson.getJSONArray("shopping_note_items")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    shoppingList.add(
                        ShoppingNoteItem(
                            id = obj.optInt("id", 0),
                            namaBarang = obj.optString("namaBarang", ""),
                            prioritas = obj.optString("prioritas", "Sedang"),
                            isDone = obj.optBoolean("isDone", false),
                            estimasiHarga = obj.optDouble("estimasiHarga", 0.0),
                            catatan = obj.optString("catatan", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            var mainSalaryConfig: MainSalaryConfig? = null
            if (rootJson.has("main_salary_config")) {
                val obj = rootJson.getJSONObject("main_salary_config")
                mainSalaryConfig = MainSalaryConfig(
                    id = obj.optInt("id", 1),
                    nominal = obj.optDouble("nominal", 0.0),
                    catatan = obj.optString("catatan", ""),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }

            val additionalIncomeList = mutableListOf<AdditionalIncome>()
            if (rootJson.has("additional_incomes")) {
                val arr = rootJson.getJSONArray("additional_incomes")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    additionalIncomeList.add(
                        AdditionalIncome(
                            id = obj.optInt("id", 0),
                            judul = obj.optString("judul", "Penghasilan"),
                            kategori = obj.optString("kategori", "Lemburan"),
                            nominal = obj.optDouble("nominal", 0.0),
                            tanggal = obj.optString("tanggal", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isActive = obj.optBoolean("isActive", true),
                            targetCycleOffset = obj.optInt("targetCycleOffset", 0),
                            targetCycleLabel = obj.optString("targetCycleLabel", ""),
                            catatan = obj.optString("catatan", "")
                        )
                    )
                }
            }

            if (overwrite) {
                trackerRepository.restoreAllLogs(
                    vehicles = vehicleList,
                    fuelLogs = fuelList,
                    oilLogs = oilList,
                    electricityLogs = elecList,
                    serviceLogs = serviceList,
                    socialLogs = socialList,
                    dailyGroceryLogs = groceryList,
                    randomExpenses = randomList,
                    childExpenses = childList,
                    warungDebts = debtList,
                    warungDebtPayments = paymentList,
                    shoppingNoteItems = shoppingList,
                    mainSalaryConfig = mainSalaryConfig,
                    additionalIncomes = additionalIncomeList
                )
            } else {
                trackerRepository.mergeAllLogs(
                    vehicles = vehicleList,
                    fuelLogs = fuelList,
                    oilLogs = oilList,
                    electricityLogs = elecList,
                    serviceLogs = serviceList,
                    socialLogs = socialList,
                    dailyGroceryLogs = groceryList,
                    randomExpenses = randomList,
                    childExpenses = childList,
                    warungDebts = debtList,
                    warungDebtPayments = paymentList,
                    shoppingNoteItems = shoppingList,
                    mainSalaryConfig = mainSalaryConfig,
                    additionalIncomes = additionalIncomeList
                )
            }

            BackupResult(
                success = true,
                message = "Restorasi berhasil! Mengimpor ${vehicleList.size} Kendaraan, ${fuelList.size} Bensin, ${oilList.size} Oli, ${elecList.size} Listrik, ${serviceList.size} Servis, ${socialList.size} Jimpitan, ${groceryList.size} Belanja, ${additionalIncomeList.size} Sumber Penghasilan Tambahan.",
                vehicleCount = vehicleList.size,
                fuelCount = fuelList.size,
                oilCount = oilList.size,
                electricityCount = elecList.size
            )
        } catch (e: Exception) {
            BackupResult(
                success = false,
                message = "Gagal merestorasi cadangan: ${e.localizedMessage ?: "File JSON corrupt"}"
            )
        }
    }
}
