package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.entity.AdditionalIncome
import com.example.data.entity.ChildExpenseLog
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.ElectricityLog
import com.example.data.entity.FuelLog
import com.example.data.entity.MainSalaryConfig
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
import com.example.data.repository.TrackerRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val isSuccess: Boolean = true,
    val message: String = "Belum terhubung ke Cloud"
)

class FirestoreSyncManager(
    private val context: Context,
    private val trackerRepository: TrackerRepository
) {
    private var lastErrorDetail: String? = null
    private val prefs = context.getSharedPreferences("dipta_sync_prefs", Context.MODE_PRIVATE)

    fun getLastSyncTime(): Long = prefs.getLong("last_sync_time", 0L)

    private fun saveLastSyncTime(timestamp: Long) {
        prefs.edit().putLong("last_sync_time", timestamp).apply()
    }

    fun isPlayServicesAvailable(): Boolean {
        return try {
            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            val result = availability.isGooglePlayServicesAvailable(context)
            result == com.google.android.gms.common.ConnectionResult.SUCCESS
        } catch (e: Throwable) {
            false
        }
    }

    private fun getFirebaseInitError(): String? {
        if (!isPlayServicesAvailable()) {
            return "Layanan Google Play Services tidak tersedia di perangkat ini. Menggunakan database lokal."
        }
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val app = FirebaseApp.initializeApp(context)
                if (app == null) {
                    "File google-services.json tidak ditemukan atau konfigurasi Firebase tidak valid."
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Firebase initialization failed", e)
            "Inisialisasi Firebase gagal: ${e.localizedMessage ?: "File google-services.json belum sesuai"}"
        }
    }

    private fun isFirebaseAvailable(): Boolean {
        return isPlayServicesAvailable() && getFirebaseInitError() == null
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isFirebaseAvailable()) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                lastErrorDetail = "Gagal memuat FirebaseFirestore: ${e.localizedMessage}"
                null
            }
        } else null
    }

    fun getLastAuthError(): String? = lastErrorDetail

    private fun parseFirebaseException(e: Exception): String {
        val message = e.localizedMessage ?: e.message ?: "Terjadi kesalahan koneksi"
        return when {
            message.contains("network", ignoreCase = true) || message.contains("timeout", ignoreCase = true) || message.contains("unreachable", ignoreCase = true) || message.contains("UNAVAILABLE", ignoreCase = true) ->
                "Kendala koneksi internet. Periksa koneksi data/Wi-Fi Anda."
            message.contains("PERMISSION_DENIED", ignoreCase = true) || message.contains("permission-denied", ignoreCase = true) ->
                "Aturan Firestore Security Rules memblokir akses read/write. Pastikan Security Rules diset 'allow read, write: if true;' di Firebase Console."
            message.contains("API key", ignoreCase = true) || message.contains("apikey", ignoreCase = true) ->
                "API Key Firebase tidak valid di google-services.json."
            else -> message
        }
    }

    suspend fun clearLocalCache() {
        trackerRepository.clearAllLocalData()
        lastErrorDetail = null
    }

    suspend fun deleteDocumentFromCloud(collectionName: String, documentId: String) {
        val fs = firestore ?: return
        try {
            fs.collection(collectionName).document(documentId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error deleting document $documentId from $collectionName", e)
        }
    }

    // Helper to commit batched writes cleanly without hitting the 500 operation limit
    private suspend fun commitBatchedWrites(
        fs: FirebaseFirestore,
        operations: List<Pair<DocumentReference, Map<String, Any?>>>
    ) {
        if (operations.isEmpty()) return
        val chunkedOps = operations.chunked(400)
        for (chunk in chunkedOps) {
            val batch = fs.batch()
            for ((ref, data) in chunk) {
                batch.set(ref, data, SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    // Sync local Room logs directly to Cloud Firestore top-level public collections
    suspend fun syncLocalToCloud(): SyncStatus {
        val initErr = getFirebaseInitError()
        if (initErr != null) {
            return SyncStatus(
                isSyncing = false,
                lastSyncTime = getLastSyncTime(),
                isSuccess = false,
                message = initErr
            )
        }

        val fs = firestore
        if (fs == null) {
            val errDetail = lastErrorDetail ?: "Inisialisasi Firebase belum lengkap atau file google-services.json bermasalah."
            return SyncStatus(
                isSyncing = false,
                lastSyncTime = getLastSyncTime(),
                isSuccess = false,
                message = errDetail
            )
        }

        return try {
            val vehicles = trackerRepository.getAllVehiclesList()
            val fuelLogs = trackerRepository.getAllFuelLogsList()
            val oilLogs = trackerRepository.getAllOilLogsList()
            val electricityLogs = trackerRepository.getAllElectricityLogsList()
            val serviceLogs = trackerRepository.getAllServiceLogsList()
            val socialLogs = trackerRepository.getAllSocialLogsList()
            val groceryLogs = trackerRepository.getAllDailyGroceryLogsList()
            val randomExpenses = trackerRepository.getAllRandomExpensesList()
            val childExpenses = trackerRepository.getAllChildExpensesList()
            val warungDebts = trackerRepository.getAllWarungDebtsList()
            val debtPayments = trackerRepository.getAllWarungDebtPaymentsList()
            val shoppingItems = trackerRepository.getAllShoppingNoteItemsList()
            val recipes = trackerRepository.getAllRecipesList()
            val mealPlanItems = trackerRepository.getAllMealPlanItemsList()
            val mainSalary = trackerRepository.getMainSalaryConfigDirect()
            val additionalIncomes = trackerRepository.getAllAdditionalIncomesList()

            val ops = mutableListOf<Pair<DocumentReference, Map<String, Any?>>>()

            // 0. Sync Vehicles
            vehicles.forEach { v ->
                val topRef = fs.collection("vehicles").document(v.id.toString())
                val data = mapOf(
                    "id" to v.id,
                    "nama_kendaraan" to v.nama_kendaraan,
                    "nomor_plat" to v.nomor_plat,
                    "jenis_kendaraan" to v.jenis_kendaraan,
                    "icon_type" to v.icon_type,
                    "current_odometer" to v.current_odometer,
                    "tanggal_pajak_stnk" to v.tanggal_pajak_stnk,
                    "catatan_sparepart" to v.catatan_sparepart,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 1. Sync Fuel Logs
            fuelLogs.forEach { log ->
                val topRef = fs.collection("fuel_logs").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "vehicle_id" to log.vehicle_id,
                    "tanggal" to log.tanggal,
                    "km_motor" to log.km_motor,
                    "nominal" to log.nominal,
                    "liter" to log.liter,
                    "jarak_tempuh" to log.jarak_tempuh,
                    "km_per_liter" to log.km_per_liter,
                    "is_boros" to log.is_boros,
                    "jenis_bbm" to log.jenis_bbm,
                    "harga_per_liter" to log.harga_per_liter,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 2. Sync Oil Logs
            oilLogs.forEach { log ->
                val topRef = fs.collection("oil_logs").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "vehicle_id" to log.vehicle_id,
                    "tanggal" to log.tanggal,
                    "km_motor" to log.km_motor,
                    "jenis_oli" to log.jenis_oli,
                    "harga" to log.harga,
                    "kapasitas_ml" to log.kapasitas_ml,
                    "target_km" to log.target_km,
                    "interval_km" to log.interval_km,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 3. Sync Electricity Logs
            electricityLogs.forEach { log ->
                val topRef = fs.collection("electricity_logs").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "tanggal" to log.tanggal,
                    "harga" to log.harga,
                    "jumlah_kwh" to log.jumlah_kwh,
                    "sisa_sebelumnya" to log.sisa_sebelumnya,
                    "total_kwh_aktif" to log.total_kwh_aktif,
                    "durasi_hari" to log.durasi_hari,
                    "kwh_per_hari" to log.kwh_per_hari,
                    "is_boros" to log.is_boros,
                    "is_initial" to log.is_initial,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 4. Sync Service Logs
            serviceLogs.forEach { log ->
                val topRef = fs.collection("service_logs").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "vehicle_id" to log.vehicle_id,
                    "tanggal" to log.tanggal,
                    "km_motor" to log.km_motor,
                    "kategori" to log.kategori,
                    "deskripsi_item" to log.deskripsi_item,
                    "total_biaya" to log.total_biaya,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 5. Sync Social Logs
            socialLogs.forEach { log ->
                val topRef = fs.collection("social_logs").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "tanggal" to log.tanggal,
                    "kategori" to log.kategori,
                    "nominal" to log.nominal,
                    "keterangan" to log.keterangan,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 6. Sync Grocery Logs
            groceryLogs.forEach { log ->
                val topRef = fs.collection("daily_grocery_logs").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "tanggal" to log.tanggal,
                    "modalAwal" to log.modalAwal,
                    "sisaUang" to log.sisaUang,
                    "totalPengeluaran" to log.totalPengeluaran,
                    "rincian" to log.rincian,
                    "catatan" to log.catatan,
                    "timestamp" to log.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 6b. Sync Random Expenses
            randomExpenses.forEach { log ->
                val topRef = fs.collection("random_expenses").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "tanggal" to log.tanggal,
                    "modalAwal" to log.modalAwal,
                    "sisaUang" to log.sisaUang,
                    "totalPengeluaran" to log.totalPengeluaran,
                    "rincian" to log.rincian,
                    "catatan" to log.catatan,
                    "timestamp" to log.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 6c. Sync Child Expenses
            childExpenses.forEach { log ->
                val topRef = fs.collection("child_expenses").document(log.id.toString())
                val data = mapOf(
                    "id" to log.id,
                    "tanggal" to log.tanggal,
                    "modalAwal" to log.modalAwal,
                    "sisaUang" to log.sisaUang,
                    "totalPengeluaran" to log.totalPengeluaran,
                    "rincian" to log.rincian,
                    "catatan" to log.catatan,
                    "timestamp" to log.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 7. Sync Warung Debts
            warungDebts.forEach { debt ->
                val topRef = fs.collection("warung_debts").document(debt.id.toString())
                val data = mapOf(
                    "id" to debt.id,
                    "tanggal" to debt.tanggal,
                    "namaWarung" to debt.namaWarung,
                    "nominal" to debt.nominal,
                    "alasan" to debt.alasan,
                    "isLunas" to debt.isLunas,
                    "totalDibayar" to debt.totalDibayar,
                    "timestamp" to debt.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 8. Sync Debt Payments
            debtPayments.forEach { p ->
                val topRef = fs.collection("warung_debt_payments").document(p.id.toString())
                val data = mapOf(
                    "id" to p.id,
                    "debtId" to p.debtId,
                    "tanggal" to p.tanggal,
                    "nominalBayar" to p.nominalBayar,
                    "catatan" to p.catatan,
                    "timestamp" to p.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 9. Sync Shopping Notes
            shoppingItems.forEach { item ->
                val topRef = fs.collection("shopping_note_items").document(item.id.toString())
                val data = mapOf(
                    "id" to item.id,
                    "namaBarang" to item.namaBarang,
                    "prioritas" to item.prioritas,
                    "isDone" to item.isDone,
                    "estimasiHarga" to item.estimasiHarga,
                    "catatan" to item.catatan,
                    "timestamp" to item.timestamp,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 10. Sync Recipes
            recipes.forEach { r ->
                val topRef = fs.collection("recipes").document(r.id.toString())
                val data = mapOf(
                    "id" to r.id,
                    "title" to r.title,
                    "description" to r.description,
                    "category" to r.category,
                    "prepTime" to r.prepTime,
                    "cookTime" to r.cookTime,
                    "yields" to r.yields,
                    "ingredients" to r.ingredients,
                    "directions" to r.directions,
                    "skillRating" to r.skillRating,
                    "isFavorite" to r.isFavorite,
                    "flavorTag" to r.flavorTag,
                    "source" to r.source,
                    "isDeleted" to r.isDeleted,
                    "deletedAt" to r.deletedAt,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 11. Sync Meal Plan Items
            mealPlanItems.forEach { m ->
                val topRef = fs.collection("meal_plan_items").document(m.id.toString())
                val data = mapOf(
                    "id" to m.id,
                    "dayOfWeek" to m.dayOfWeek,
                    "recipeId" to m.recipeId,
                    "recipeTitle" to m.recipeTitle,
                    "mealType" to m.mealType,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            // 12. Sync Main Salary Config
            if (mainSalary != null) {
                val topRef = fs.collection("main_salary_config").document("primary")
                val data = mapOf(
                    "id" to mainSalary.id,
                    "nominal" to mainSalary.nominal,
                    "catatan" to mainSalary.catatan,
                    "updatedAt" to mainSalary.updatedAt
                )
                ops.add(topRef to data)
            }

            // 13. Sync Additional Incomes
            additionalIncomes.forEach { inc ->
                val topRef = fs.collection("additional_incomes").document(inc.id.toString())
                val data = mapOf(
                    "id" to inc.id,
                    "judul" to inc.judul,
                    "kategori" to inc.kategori,
                    "nominal" to inc.nominal,
                    "tanggal" to inc.tanggal,
                    "timestamp" to inc.timestamp,
                    "isActive" to inc.isActive,
                    "targetCycleOffset" to inc.targetCycleOffset,
                    "targetCycleLabel" to inc.targetCycleLabel,
                    "catatan" to inc.catatan,
                    "updatedAt" to System.currentTimeMillis()
                )
                ops.add(topRef to data)
            }

            commitBatchedWrites(fs, ops)

            val totalUploaded = vehicles.size + fuelLogs.size + oilLogs.size +
                    electricityLogs.size + serviceLogs.size + socialLogs.size +
                    groceryLogs.size + randomExpenses.size + childExpenses.size +
                    warungDebts.size + debtPayments.size + shoppingItems.size +
                    recipes.size + mealPlanItems.size + (if (mainSalary != null) 1 else 0) +
                    additionalIncomes.size

            val now = System.currentTimeMillis()
            saveLastSyncTime(now)

            SyncStatus(
                isSyncing = false,
                lastSyncTime = now,
                isSuccess = true,
                message = "Berhasil mengunggah $totalUploaded data ke Cloud Firestore."
            )
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error syncing to Firestore", e)
            val parsedMsg = parseFirebaseException(e)
            SyncStatus(
                isSyncing = false,
                lastSyncTime = getLastSyncTime(),
                isSuccess = false,
                message = "Gagal Upload Cloud: $parsedMsg"
            )
        }
    }

    // Document parsing helpers with full null safety and format conversion
    private fun DocumentSnapshot.getSafeInt(vararg keys: String, default: Int = 0): Int {
        for (key in keys) {
            if (!contains(key)) continue
            val raw = get(key) ?: continue
            when (raw) {
                is Number -> return raw.toInt()
                is String -> raw.toIntOrNull()?.let { return it }
                is Boolean -> return if (raw) 1 else 0
            }
        }
        return default
    }

    private fun DocumentSnapshot.getSafeLong(vararg keys: String, default: Long = 0L): Long {
        for (key in keys) {
            if (!contains(key)) continue
            val raw = get(key) ?: continue
            when (raw) {
                is Number -> return raw.toLong()
                is String -> raw.toLongOrNull()?.let { return it }
            }
        }
        return default
    }

    private fun DocumentSnapshot.getSafeDouble(vararg keys: String, default: Double = 0.0): Double {
        for (key in keys) {
            if (!contains(key)) continue
            val raw = get(key) ?: continue
            when (raw) {
                is Number -> return raw.toDouble()
                is String -> raw.toDoubleOrNull()?.let { return it }
            }
        }
        return default
    }

    private fun DocumentSnapshot.getSafeFloat(vararg keys: String, default: Float = 0f): Float {
        for (key in keys) {
            if (!contains(key)) continue
            val raw = get(key) ?: continue
            when (raw) {
                is Number -> return raw.toFloat()
                is String -> raw.toFloatOrNull()?.let { return it }
            }
        }
        return default
    }

    private fun DocumentSnapshot.getSafeString(vararg keys: String, default: String = ""): String {
        for (key in keys) {
            if (!contains(key)) continue
            val raw = get(key) ?: continue
            val str = raw.toString().trim()
            if (str.isNotBlank() && str != "null") return str
        }
        return default
    }

    private fun DocumentSnapshot.getSafeBoolean(vararg keys: String, default: Boolean = false): Boolean {
        for (key in keys) {
            if (!contains(key)) continue
            val raw = get(key) ?: continue
            when (raw) {
                is Boolean -> return raw
                is String -> return raw.equals("true", ignoreCase = true) || raw == "1"
                is Number -> return raw.toInt() != 0
            }
        }
        return default
    }

    private suspend fun <T> safeFetchCollection(
        fs: FirebaseFirestore,
        collectionName: String,
        idSelector: (T) -> Int,
        transform: (DocumentSnapshot) -> T?
    ): List<T> {
        val itemsMap = LinkedHashMap<Int, T>()

        suspend fun fetchFromQuery(query: com.google.firebase.firestore.Query) {
            try {
                val snapshot = query.get().await()
                for (doc in snapshot.documents) {
                    try {
                        val item = transform(doc)
                        if (item != null) {
                            val itemId = idSelector(item)
                            if (itemId != 0 && !itemsMap.containsKey(itemId)) {
                                itemsMap[itemId] = item
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("FirestoreSyncManager", "Doc parse error in $collectionName: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w("FirestoreSyncManager", "Query failed for $collectionName: ${e.message}")
            }
        }

        // 1. Fetch from shared top-level collection
        fetchFromQuery(fs.collection(collectionName))

        // 2. Fetch from collectionGroup(collectionName) across all subcollections (if any)
        try {
            fetchFromQuery(fs.collectionGroup(collectionName))
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "collectionGroup query for $collectionName failed: ${e.message}")
        }

        return itemsMap.values.toList()
    }

    // Sync Cloud Firestore logs directly to local Room
    suspend fun syncCloudToLocal(): SyncStatus {
        val initErr = getFirebaseInitError()
        if (initErr != null) {
            return SyncStatus(
                isSyncing = false,
                isSuccess = false,
                message = initErr
            )
        }

        val fs = firestore
        if (fs == null) {
            val errDetail = lastErrorDetail ?: "Inisialisasi Firebase belum lengkap. Silakan periksa koneksi internet Anda."
            return SyncStatus(
                isSyncing = false,
                isSuccess = false,
                message = errDetail
            )
        }

        return try {
            val vehicleList = safeFetchCollection(fs, "vehicles", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val nama = doc.getSafeString("nama_kendaraan", "namaBarang", "nama", "name", default = "Kendaraan")
                val plat = doc.getSafeString("nomor_plat", "plat", "nopol", default = "")
                val jenis = doc.getSafeString("jenis_kendaraan", "jenis", "type", default = "Motor")
                val icon = doc.getSafeString("icon_type", "icon", default = "Motor")
                val currOdo = doc.getSafeInt("current_odometer", "odometer", "km", default = 0)
                val stnk = doc.getSafeString("tanggal_pajak_stnk", "stnk", default = "")
                val sparepart = doc.getSafeString("catatan_sparepart", "sparepart", default = "")
                Vehicle(id, nama, plat, jenis, icon, currOdo, stnk, sparepart)
            }

            val fuelList = safeFetchCollection(fs, "fuel_logs", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val vId = doc.getSafeInt("vehicle_id", "vehicleId", "vId", default = 1)
                val tanggal = doc.getSafeLong("tanggal", "date", "timestamp", default = System.currentTimeMillis())
                val km = doc.getSafeInt("km_motor", "km", "odometer", default = 0)
                val nominal = doc.getSafeInt("nominal", "harga", "total", default = 0)
                val liter = doc.getSafeFloat("liter", "jumlah_liter", default = 0f)
                val jarak = doc.getSafeInt("jarak_tempuh", "jarak", default = 0)
                val kmLiter = doc.getSafeFloat("km_per_liter", "kmLiter", default = 0f)
                val isBoros = doc.getSafeBoolean("is_boros", "isBoros", default = false)
                val jenisBbm = doc.getSafeString("jenis_bbm", "jenisBbm", "jenis", default = "Pertalite")
                val hargaPerLiter = doc.getSafeInt("harga_per_liter", "hargaPerLiter", default = 0)
                FuelLog(id, vId, tanggal, km, nominal, liter, jarak, kmLiter, isBoros, jenisBbm, hargaPerLiter)
            }

            val oilList = safeFetchCollection(fs, "oil_logs", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val vId = doc.getSafeInt("vehicle_id", "vehicleId", default = 1)
                val tanggal = doc.getSafeLong("tanggal", "date", "timestamp", default = System.currentTimeMillis())
                val km = doc.getSafeInt("km_motor", "km", default = 0)
                val jenis = doc.getSafeString("jenis_oli", "jenis", "kategori", default = "Oli Mesin")
                val harga = doc.getSafeInt("harga", "nominal", "totalBiaya", default = 0)
                val kap = doc.getSafeInt("kapasitas_ml", "kapasitas", default = 0)
                val targetKm = doc.getSafeInt("target_km", "targetKm", default = 0)
                val intervalKm = doc.getSafeInt("interval_km", "intervalKm", default = 3000)
                OilLog(id, vId, tanggal, km, jenis, harga, kap, targetKm, intervalKm)
            }

            val elecList = safeFetchCollection(fs, "electricity_logs", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val tanggal = doc.getSafeLong("tanggal", "date", "timestamp", default = System.currentTimeMillis())
                val harga = doc.getSafeInt("harga", "nominal", default = 0)
                val kwh = doc.getSafeFloat("jumlah_kwh", "kwh", default = 0f)
                val sisa = doc.getSafeFloat("sisa_sebelumnya", "sisa", default = 0f)
                val totalAktif = doc.getSafeFloat("total_kwh_aktif", "totalKwh", default = 0f)
                val durasi = doc.getSafeInt("durasi_hari", "durasi", default = 0)
                val kwhHari = doc.getSafeFloat("kwh_per_hari", "kwhPerHari", default = 0f)
                val isBoros = doc.getSafeBoolean("is_boros", "isBoros", default = false)
                val isInitial = doc.getSafeBoolean("is_initial", "isInitial", default = false)
                ElectricityLog(id, tanggal, harga, kwh, sisa, totalAktif, durasi, kwhHari, isBoros, isInitial)
            }

            val serviceList = safeFetchCollection(fs, "service_logs", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val vId = doc.getSafeInt("vehicle_id", "vehicleId", default = 1)
                val tanggal = doc.getSafeLong("tanggal", "date", "timestamp", default = System.currentTimeMillis())
                val km = doc.getSafeInt("km_motor", "km", default = 0)
                val kategori = doc.getSafeString("kategori", "category", default = "Servis Rutin")
                val deskripsi = doc.getSafeString("deskripsi_item", "deskripsi", "description", default = "")
                val totalBiaya = doc.getSafeInt("total_biaya", "harga", "nominal", default = 0)
                ServiceLog(id, vId, tanggal, km, kategori, deskripsi, totalBiaya)
            }

            val socialList = safeFetchCollection(fs, "social_logs", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val tanggal = doc.getSafeLong("tanggal", "date", "timestamp", default = System.currentTimeMillis())
                val kategori = doc.getSafeString("kategori", "category", default = "Iuran Jimpitan Warga")
                val nominal = doc.getSafeInt("nominal", "harga", "total", default = 0)
                val keterangan = doc.getSafeString("keterangan", "catatan", "deskripsi", default = "")
                SocialLog(id, tanggal, kategori, nominal, keterangan)
            }

            val groceryList = safeFetchCollection(fs, "daily_grocery_logs", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val tanggal = doc.getSafeString("tanggal", "date", default = "")
                val modalAwal = doc.getSafeDouble("modalAwal", "modal", default = 0.0)
                val sisaUang = doc.getSafeDouble("sisaUang", "sisa", default = 0.0)
                val totalPengeluaran = doc.getSafeDouble("totalPengeluaran", "pengeluaran", default = 0.0)
                val rincian = doc.getSafeString("rincian", "items", default = "")
                val catatan = doc.getSafeString("catatan", "keterangan", default = "")
                val timestamp = doc.getSafeLong("timestamp", "tanggalLong", default = System.currentTimeMillis())
                DailyGroceryLog(id, tanggal, modalAwal, sisaUang, totalPengeluaran, rincian, catatan, timestamp)
            }

            val randomList = safeFetchCollection(fs, "random_expenses", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val tanggal = doc.getSafeString("tanggal", "date", default = "")
                val modalAwal = doc.getSafeDouble("modalAwal", "modal", default = 0.0)
                val sisaUang = doc.getSafeDouble("sisaUang", "sisa", default = 0.0)
                val totalPengeluaran = doc.getSafeDouble("totalPengeluaran", "pengeluaran", default = 0.0)
                val rincian = doc.getSafeString("rincian", "items", default = "")
                val catatan = doc.getSafeString("catatan", "keterangan", default = "")
                val timestamp = doc.getSafeLong("timestamp", "tanggalLong", default = System.currentTimeMillis())
                RandomExpense(id, tanggal, modalAwal, sisaUang, totalPengeluaran, rincian, catatan, timestamp)
            }

            val childList = safeFetchCollection(fs, "child_expenses", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val tanggal = doc.getSafeString("tanggal", "date", default = "")
                val modalAwal = doc.getSafeDouble("modalAwal", "modal", default = 0.0)
                val sisaUang = doc.getSafeDouble("sisaUang", "sisa", default = 0.0)
                val totalPengeluaran = doc.getSafeDouble("totalPengeluaran", "pengeluaran", default = 0.0)
                val rincian = doc.getSafeString("rincian", "items", default = "")
                val catatan = doc.getSafeString("catatan", "keterangan", default = "")
                val timestamp = doc.getSafeLong("timestamp", "tanggalLong", default = System.currentTimeMillis())
                ChildExpenseLog(id, tanggal, modalAwal, sisaUang, totalPengeluaran, rincian, catatan, timestamp)
            }

            val debtList = safeFetchCollection(fs, "warung_debts", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val tanggal = doc.getSafeString("tanggal", "date", default = "")
                val namaWarung = doc.getSafeString("namaWarung", "warung", "nama", default = "Warung")
                val nominal = doc.getSafeDouble("nominal", "total", default = 0.0)
                val alasan = doc.getSafeString("alasan", "catatan", "keterangan", default = "")
                val isLunas = doc.getSafeBoolean("isLunas", "lunas", default = false)
                val totalDibayar = doc.getSafeDouble("totalDibayar", "dibayar", default = 0.0)
                val timestamp = doc.getSafeLong("timestamp", "tanggalLong", default = System.currentTimeMillis())
                WarungDebt(id, tanggal, namaWarung, nominal, alasan, isLunas, totalDibayar, timestamp)
            }

            val paymentList = safeFetchCollection(fs, "warung_debt_payments", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val debtId = doc.getSafeInt("debtId", "debt_id", default = 0)
                val tanggal = doc.getSafeString("tanggal", "date", default = "")
                val nominalBayar = doc.getSafeDouble("nominalBayar", "nominal", default = 0.0)
                val catatan = doc.getSafeString("catatan", "keterangan", default = "")
                val timestamp = doc.getSafeLong("timestamp", "tanggalLong", default = System.currentTimeMillis())
                WarungDebtPayment(id, debtId, tanggal, nominalBayar, catatan, timestamp)
            }

            val shoppingList = safeFetchCollection(fs, "shopping_note_items", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val namaBarang = doc.getSafeString("namaBarang", "nama", "item", default = "")
                val prioritas = doc.getSafeString("prioritas", "priority", default = "Sedang")
                val isDone = doc.getSafeBoolean("isDone", "done", default = false)
                val estimasiHarga = doc.getSafeDouble("estimasiHarga", "harga", "estimasi", default = 0.0)
                val catatan = doc.getSafeString("catatan", "keterangan", default = "")
                val timestamp = doc.getSafeLong("timestamp", "tanggalLong", default = System.currentTimeMillis())
                ShoppingNoteItem(id, namaBarang, prioritas, isDone, estimasiHarga, catatan, timestamp)
            }

            val recipeList = safeFetchCollection(fs, "recipes", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val title = doc.getSafeString("title", "nama", "judul", default = "")
                val desc = doc.getSafeString("description", "deskripsi", default = "")
                val category = doc.getSafeString("category", "kategori", default = "Lainnya")
                val prepTime = doc.getSafeString("prepTime", default = "")
                val cookTime = doc.getSafeString("cookTime", default = "")
                val yields = doc.getSafeString("yields", default = "")
                val ingredients = doc.getSafeString("ingredients", "bahan", default = "")
                val directions = doc.getSafeString("directions", "langkah", default = "")
                val skillRating = doc.getSafeInt("skillRating", default = 0)
                val isFavorite = doc.getSafeBoolean("isFavorite", default = false)
                val flavorTag = doc.getSafeString("flavorTag", default = "")
                val source = doc.getSafeString("source", default = "")
                val isDeleted = doc.getSafeBoolean("isDeleted", default = false)
                val deletedAt = doc.getSafeLong("deletedAt", default = 0L)
                Recipe(
                    id = id,
                    title = title,
                    description = desc,
                    category = category,
                    prepTime = prepTime,
                    cookTime = cookTime,
                    yields = yields,
                    ingredients = ingredients,
                    directions = directions,
                    isDeleted = isDeleted,
                    deletedAt = deletedAt,
                    skillRating = skillRating,
                    isFavorite = isFavorite,
                    flavorTag = flavorTag,
                    source = source
                )
            }

            val mealPlanList = safeFetchCollection(fs, "meal_plan_items", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val dayOfWeek = doc.getSafeString("dayOfWeek", "hari", default = "Senin")
                val recipeId = doc.getSafeInt("recipeId", default = 0)
                val recipeTitle = doc.getSafeString("recipeTitle", "title", default = "")
                val mealType = doc.getSafeString("mealType", default = "Makan Siang/Malam")
                MealPlanItem(
                    id = id,
                    dayOfWeek = dayOfWeek,
                    recipeId = recipeId,
                    recipeTitle = recipeTitle,
                    mealType = mealType
                )
            }

            var downloadedSalaryConfig: MainSalaryConfig? = null
            try {
                val salaryDoc = fs.collection("main_salary_config").document("primary").get().await()
                if (salaryDoc.exists()) {
                    downloadedSalaryConfig = MainSalaryConfig(
                        id = salaryDoc.getSafeInt("id", default = 1),
                        nominal = salaryDoc.getSafeDouble("nominal", default = 0.0),
                        catatan = salaryDoc.getSafeString("catatan", default = ""),
                        updatedAt = salaryDoc.getSafeLong("updatedAt", default = System.currentTimeMillis())
                    )
                }
            } catch (e: Exception) {
                Log.w("FirestoreSyncManager", "Could not fetch main_salary_config: ${e.message}")
            }

            val additionalIncomeList = safeFetchCollection(fs, "additional_incomes", idSelector = { it.id }) { doc ->
                val defaultId = doc.id.toIntOrNull() ?: (doc.id.hashCode() and 0x7FFFFFFF)
                val id = doc.getSafeInt("id", default = defaultId)
                val judul = doc.getSafeString("judul", "nama", default = "Penghasilan")
                val kategori = doc.getSafeString("kategori", default = "Lemburan")
                val nominal = doc.getSafeDouble("nominal", "total", default = 0.0)
                val tanggal = doc.getSafeString("tanggal", "date", default = "")
                val timestamp = doc.getSafeLong("timestamp", default = System.currentTimeMillis())
                val isActive = doc.getSafeBoolean("isActive", "active", default = true)
                val targetCycleOffset = doc.getSafeInt("targetCycleOffset", default = 0)
                val targetCycleLabel = doc.getSafeString("targetCycleLabel", default = "")
                val catatan = doc.getSafeString("catatan", default = "")
                AdditionalIncome(
                    id = id,
                    judul = judul,
                    kategori = kategori,
                    nominal = nominal,
                    tanggal = tanggal,
                    timestamp = timestamp,
                    isActive = isActive,
                    targetCycleOffset = targetCycleOffset,
                    targetCycleLabel = targetCycleLabel,
                    catatan = catatan
                )
            }

            val totalDownloaded = vehicleList.size + fuelList.size + oilList.size +
                    elecList.size + serviceList.size + socialList.size +
                    groceryList.size + randomList.size + childList.size + debtList.size + paymentList.size + shoppingList.size +
                    recipeList.size + mealPlanList.size + (if (downloadedSalaryConfig != null) 1 else 0) +
                    additionalIncomeList.size

            val now = System.currentTimeMillis()
            if (totalDownloaded > 0) {
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
                    recipes = recipeList,
                    mealPlanItems = mealPlanList,
                    mainSalaryConfig = downloadedSalaryConfig,
                    additionalIncomes = additionalIncomeList
                )
                saveLastSyncTime(now)

                SyncStatus(
                    isSyncing = false,
                    lastSyncTime = now,
                    isSuccess = true,
                    message = "Berhasil mengunduh $totalDownloaded data dari Cloud Firestore ke database lokal."
                )
            } else {
                saveLastSyncTime(now)
                SyncStatus(
                    isSyncing = false,
                    lastSyncTime = now,
                    isSuccess = true,
                    message = "Koneksi ke Cloud berhasil."
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error downloading from Firestore", e)
            val parsedMsg = parseFirebaseException(e)
            SyncStatus(
                isSyncing = false,
                lastSyncTime = getLastSyncTime(),
                isSuccess = false,
                message = "Gagal Unduh Cloud: $parsedMsg"
            )
        }
    }

    suspend fun syncTwoWay(): SyncStatus {
        val downloadStatus = syncCloudToLocal()
        if (!downloadStatus.isSuccess) {
            return downloadStatus
        }
        val uploadStatus = syncLocalToCloud()
        if (!uploadStatus.isSuccess) {
            return uploadStatus
        }
        val now = System.currentTimeMillis()
        saveLastSyncTime(now)
        return SyncStatus(
            isSyncing = false,
            lastSyncTime = now,
            isSuccess = true,
            message = "Sinkronisasi dua arah (Two-Way Sync) berhasil! Data lokal & Cloud Firestore telah tersinkronkan."
        )
    }
}
