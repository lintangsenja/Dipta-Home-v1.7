package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.TrackerViewModel
import com.example.ui.anak.AnakScreen
import com.example.ui.bensin.BensinScreen
import com.example.ui.hub.HubScreen
import com.example.ui.jimpitan.JimpitanScreen
import com.example.ui.listrik.ListrikScreen
import com.example.ui.oli.OliScreen
import com.example.ui.penghasilan.PenghasilanScreen
import com.example.ui.resep.ResepScreen
import com.example.ui.servis.ServisScreen
import com.example.ui.warung.WarungScreen
import com.example.ui.theme.KeluargaTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KeluargaTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KeluargaTrackerApp(viewModel = viewModel)
                }
            }
        }
    }
}

object Routes {
    const val HUB = "hub"
    const val BENSIN = "bensin"
    const val OLI = "oli"
    const val SERVIS = "servis"
    const val LISTRIK = "listrik"
    const val JIMPITAN = "jimpitan"
    const val WARUNG = "warung"
    const val ANAK = "anak"
    const val RESEP = "resep"
    const val PENGHASILAN = "penghasilan"
}

@Composable
fun KeluargaTrackerApp(viewModel: TrackerViewModel) {
    val navController = rememberNavController()

    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val activeVehicleId by viewModel.activeVehicleId.collectAsStateWithLifecycle()
    val fuelLogs by viewModel.fuelLogs.collectAsStateWithLifecycle()
    val oilLogs by viewModel.oilLogs.collectAsStateWithLifecycle()
    val electricityLogs by viewModel.electricityLogs.collectAsStateWithLifecycle()
    val serviceLogs by viewModel.serviceLogs.collectAsStateWithLifecycle()
    val socialLogs by viewModel.socialLogs.collectAsStateWithLifecycle()
    val dailyGroceryLogs by viewModel.dailyGroceryLogs.collectAsStateWithLifecycle()
    val randomExpenses by viewModel.randomExpenses.collectAsStateWithLifecycle()
    val childExpenses by viewModel.childExpenses.collectAsStateWithLifecycle()
    val monthlyExpenseSummary by viewModel.monthlyExpenseSummary.collectAsStateWithLifecycle()
    val mainSalaryConfig by viewModel.mainSalaryConfig.collectAsStateWithLifecycle()
    val allAdditionalIncomes by viewModel.allAdditionalIncomes.collectAsStateWithLifecycle()
    val financialCycleSummary by viewModel.financialCycleSummary.collectAsStateWithLifecycle()
    val monthlyComparisonChartData by viewModel.monthlyComparisonChartData.collectAsStateWithLifecycle()
    val yearlyComparisonChartData by viewModel.yearlyComparisonChartData.collectAsStateWithLifecycle()
    val warungDebts by viewModel.warungDebts.collectAsStateWithLifecycle()
    val warungDebtPayments by viewModel.warungDebtPayments.collectAsStateWithLifecycle()
    val shoppingNoteItems by viewModel.shoppingNoteItems.collectAsStateWithLifecycle()
    val warungDebtLimit by viewModel.warungDebtLimit.collectAsStateWithLifecycle()
    val activeRecipes by viewModel.activeRecipes.collectAsStateWithLifecycle()
    val deletedRecipes by viewModel.deletedRecipes.collectAsStateWithLifecycle()
    val mealPlanItems by viewModel.mealPlanItems.collectAsStateWithLifecycle()
    val currentPaycheckPeriod by viewModel.currentPaycheckPeriod.collectAsStateWithLifecycle()
    val includePreviousSurplus by viewModel.includePreviousSurplus.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.HUB
    ) {
        composable(Routes.HUB) {
            HubScreen(
                vehicles = vehicles,
                activeVehicleId = activeVehicleId,
                latestFuel = fuelLogs.firstOrNull(),
                latestOil = oilLogs.firstOrNull(),
                latestElectricity = electricityLogs.firstOrNull(),
                latestService = serviceLogs.firstOrNull(),
                latestSocial = socialLogs.firstOrNull(),
                fuelLogs = fuelLogs,
                oilLogs = oilLogs,
                electricityLogs = electricityLogs,
                serviceLogs = serviceLogs,
                socialLogs = socialLogs,
                dailyGroceryLogs = dailyGroceryLogs,
                randomExpenses = randomExpenses,
                childExpenses = childExpenses,
                warungDebts = warungDebts,
                monthlyExpenseSummary = monthlyExpenseSummary,
                financialSummary = financialCycleSummary,
                mainSalaryConfig = mainSalaryConfig,
                additionalIncomes = allAdditionalIncomes,
                monthlyChartData = monthlyComparisonChartData,
                yearlyChartData = yearlyComparisonChartData,
                currentPaycheckPeriod = currentPaycheckPeriod,
                syncStatus = syncStatus,
                currentUser = currentUser,
                onSelectVehicle = { id -> viewModel.selectActiveVehicle(id) },
                onAddVehicle = { nama, plat, jenis, icon -> viewModel.addVehicle(nama, plat, jenis, icon) },
                onDeleteVehicle = { id -> viewModel.deleteVehicle(id) },
                onPrevPaycheckCycle = { viewModel.prevPaycheckCycle() },
                onNextPaycheckCycle = { viewModel.nextPaycheckCycle() },
                onResetPaycheckCycle = { viewModel.resetPaycheckCycle() },
                onUpdatePaycheckStartDay = { day -> viewModel.setPaycheckStartDay(day) },
                onNavigateToPenghasilan = { navController.navigate(Routes.PENGHASILAN) },
                onNavigateToBensin = { navController.navigate(Routes.BENSIN) },
                onNavigateToOli = { navController.navigate(Routes.OLI) },
                onNavigateToListrik = { navController.navigate(Routes.LISTRIK) },
                onNavigateToServis = { navController.navigate(Routes.SERVIS) },
                onNavigateToJimpitan = { navController.navigate(Routes.JIMPITAN) },
                onNavigateToWarung = { navController.navigate(Routes.WARUNG) },
                onNavigateToAnak = { navController.navigate(Routes.ANAK) },
                onNavigateToResep = { navController.navigate(Routes.RESEP) },
                onSyncToCloud = { viewModel.syncToCloud() },
                onSyncFromCloud = { viewModel.syncFromCloud() },
                onManualSync = { viewModel.triggerFullManualSync() },
                onConnectFirebase = { viewModel.connectFirebaseUser() },
                onSignOutFirebase = { viewModel.signOutUser() },
                onExportBackup = { uri, callback -> viewModel.exportBackupToJson(uri, callback) },
                onRestoreBackup = { uri, overwrite, callback -> viewModel.restoreBackupFromJson(uri, overwrite, callback) },
                onResetAllData = { callback -> viewModel.resetAllMasterData(callback) }
            )
        }

        composable(Routes.BENSIN) {
            BensinScreen(
                vehicles = vehicles,
                activeVehicleId = activeVehicleId,
                fuelLogs = fuelLogs,
                onAddLog = { vehicleId, km, nominal, liter, jenisBbm, hargaPerLiter, customTs ->
                    viewModel.addFuelLog(vehicleId, km, nominal, liter, jenisBbm, hargaPerLiter, customTs)
                },
                onUpdateLog = { log -> viewModel.updateFuelLog(log) },
                onDeleteLog = { id -> viewModel.deleteFuelLog(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.OLI) {
            OliScreen(
                vehicles = vehicles,
                activeVehicleId = activeVehicleId,
                oilLogs = oilLogs,
                onAddLog = { vehicleId, km, jenis, harga, kap, interval, garansi, customTs ->
                    viewModel.addOilLog(vehicleId, km, jenis, harga, kap, interval, garansi, customTs)
                },
                onUpdateLog = { log -> viewModel.updateOilLog(log) },
                onUpdateOdometer = { vehicleId, newOdometer ->
                    viewModel.updateVehicleOdometer(vehicleId, newOdometer)
                },
                onDeleteLog = { id -> viewModel.deleteOilLog(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SERVIS) {
            ServisScreen(
                vehicles = vehicles,
                activeVehicleId = activeVehicleId,
                serviceLogs = serviceLogs,
                onAddLog = { vehicleId, km, kategori, deskripsi, totalBiaya, interval, garansi, customTs ->
                    viewModel.addServiceLog(vehicleId, km, kategori, deskripsi, totalBiaya, interval, garansi, customTs)
                },
                onUpdateLog = { log -> viewModel.updateServiceLog(log) },
                onUpdateOdometer = { vehicleId, newOdometer ->
                    viewModel.updateVehicleOdometer(vehicleId, newOdometer)
                },
                onDeleteLog = { id -> viewModel.deleteServiceLog(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LISTRIK) {
            ListrikScreen(
                electricityLogs = electricityLogs,
                onAddLog = { harga, kwh, isInitial, customTs -> viewModel.addElectricityLog(harga, kwh, isInitial, customTs) },
                onUpdateLog = { log -> viewModel.updateElectricityLog(log) },
                onDeleteLog = { id -> viewModel.deleteElectricityLog(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.JIMPITAN) {
            JimpitanScreen(
                socialLogs = socialLogs,
                onAddLog = { kategori, nominal, keterangan, tipe, customTs -> viewModel.addSocialLog(kategori, nominal, keterangan, tipe, customTs) },
                onUpdateLog = { log -> viewModel.updateSocialLog(log) },
                onDeleteLog = { id -> viewModel.deleteSocialLog(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.WARUNG) {
            WarungScreen(
                dailyGroceryLogs = dailyGroceryLogs,
                randomExpenses = randomExpenses,
                warungDebts = warungDebts,
                warungDebtPayments = warungDebtPayments,
                shoppingNoteItems = shoppingNoteItems,
                warungDebtLimit = warungDebtLimit,
                remainingSalaryBudget = financialCycleSummary?.remainingBalance ?: 0.0,
                currentPeriod = currentPaycheckPeriod,
                onPrevPaycheckCycle = { viewModel.prevPaycheckCycle() },
                onNextPaycheckCycle = { viewModel.nextPaycheckCycle() },
                onResetPaycheckCycle = { viewModel.resetPaycheckCycle() },
                onUpdatePaycheckStartDay = { day -> viewModel.setPaycheckStartDay(day) },
                onAddDailyGroceryLog = { tanggal, modal, sisa, rincian, catatan ->
                    viewModel.addDailyGroceryLog(tanggal, modal, sisa, rincian, catatan)
                },
                onUpdateDailyGroceryLog = { log -> viewModel.updateDailyGroceryLog(log) },
                onDeleteDailyGroceryLog = { id -> viewModel.deleteDailyGroceryLog(id) },
                onAddRandomExpense = { tanggal, modal, sisa, rincian, catatan ->
                    viewModel.addRandomExpense(tanggal, modal, sisa, rincian, catatan)
                },
                onUpdateRandomExpense = { log -> viewModel.updateRandomExpense(log) },
                onDeleteRandomExpense = { id -> viewModel.deleteRandomExpense(id) },
                onAddWarungDebt = { tanggal, nama, nominal, alasan ->
                    viewModel.addWarungDebt(tanggal, nama, nominal, alasan)
                },
                onUpdateWarungDebt = { debt -> viewModel.updateWarungDebt(debt) },
                onDeleteWarungDebt = { id -> viewModel.deleteWarungDebt(id) },
                onAddWarungDebtPayment = { debtId, tanggal, nominal, catatan ->
                    viewModel.addWarungDebtPayment(debtId, tanggal, nominal, catatan)
                },
                onDeleteWarungDebtPayment = { payment -> viewModel.deleteWarungDebtPayment(payment) },
                onAddShoppingNoteItem = { nama, prioritas, estimasi, catatan ->
                    viewModel.addShoppingNoteItem(nama, prioritas, estimasi, catatan)
                },
                onUpdateShoppingNoteItem = { item -> viewModel.updateShoppingNoteItem(item) },
                onToggleShoppingNoteDone = { item -> viewModel.toggleShoppingNoteDone(item) },
                onDeleteShoppingNoteItem = { id -> viewModel.deleteShoppingNoteItem(id) },
                onClearCompletedShoppingNotes = { viewModel.clearCompletedShoppingNotes() },
                onUpdateWarungDebtLimit = { limit -> viewModel.updateWarungDebtLimit(limit) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ANAK) {
            AnakScreen(
                childExpenses = childExpenses,
                currentPeriod = currentPaycheckPeriod,
                onPrevPaycheckCycle = { viewModel.prevPaycheckCycle() },
                onNextPaycheckCycle = { viewModel.nextPaycheckCycle() },
                onResetPaycheckCycle = { viewModel.resetPaycheckCycle() },
                onUpdatePaycheckStartDay = { day -> viewModel.setPaycheckStartDay(day) },
                onAddChildExpense = { tanggal, modal, sisa, rincian, catatan ->
                    viewModel.addChildExpense(tanggal, modal, sisa, rincian, catatan)
                },
                onUpdateChildExpense = { log -> viewModel.updateChildExpense(log) },
                onDeleteChildExpense = { id -> viewModel.deleteChildExpense(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RESEP) {
            ResepScreen(
                activeRecipes = activeRecipes,
                deletedRecipes = deletedRecipes,
                mealPlanItems = mealPlanItems,
                onAddRecipe = { title, desc, cat, prep, cook, yield, ingr, dir, skill, fav, flavor, src ->
                    viewModel.addRecipe(title, desc, cat, prep, cook, yield, ingr, dir, skill, fav, flavor, src)
                },
                onUpdateRecipe = { recipe -> viewModel.updateRecipe(recipe) },
                onSoftDeleteRecipe = { id -> viewModel.softDeleteRecipe(id) },
                onRestoreRecipe = { id -> viewModel.restoreRecipe(id) },
                onHardDeleteRecipe = { id -> viewModel.hardDeleteRecipe(id) },
                onClearTrashRecipes = { viewModel.clearTrashRecipes() },
                onAddMealPlanItem = { day, recipeId, recipeTitle, mealType ->
                    viewModel.addMealPlanItem(day, recipeId, recipeTitle, mealType)
                },
                onDeleteMealPlanItem = { id -> viewModel.deleteMealPlanItem(id) },
                onClearMealPlanForDay = { day -> viewModel.clearMealPlanForDay(day) },
                onExportWeeklyMealPlanToShoppingList = { viewModel.exportWeeklyMealPlanToShoppingList() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PENGHASILAN) {
            PenghasilanScreen(
                mainSalaryConfig = mainSalaryConfig,
                additionalIncomes = allAdditionalIncomes,
                financialSummary = financialCycleSummary,
                currentPeriod = currentPaycheckPeriod,
                monthlyChartData = monthlyComparisonChartData,
                yearlyChartData = yearlyComparisonChartData,
                includePreviousSurplus = includePreviousSurplus,
                onToggleIncludePreviousSurplus = { enabled ->
                    viewModel.setIncludePreviousSurplus(enabled)
                },
                onSetMainSalary = { nominal, catatan ->
                    viewModel.setMainSalary(nominal, catatan)
                },
                onAddAdditionalIncome = { judul, kategori, nominal, tanggal, isActive, targetCycleOffset, targetCycleLabel, catatan ->
                    viewModel.addAdditionalIncome(
                        judul = judul,
                        kategori = kategori,
                        nominal = nominal,
                        tanggal = tanggal,
                        isActive = isActive,
                        targetCycleOffset = targetCycleOffset,
                        targetCycleLabel = targetCycleLabel,
                        catatan = catatan
                    )
                },
                onUpdateAdditionalIncome = { income ->
                    viewModel.updateAdditionalIncome(income)
                },
                onToggleAdditionalIncome = { id, isActive, targetCycleOffset, targetCycleLabel, catatan ->
                    viewModel.toggleAdditionalIncome(id, isActive, targetCycleOffset, targetCycleLabel, catatan)
                },
                onDeleteAdditionalIncome = { id ->
                    viewModel.deleteAdditionalIncome(id)
                },
                onPrevCycle = { viewModel.prevPaycheckCycle() },
                onNextCycle = { viewModel.nextPaycheckCycle() },
                onResetCycle = { viewModel.resetPaycheckCycle() },
                onUpdatePaycheckStartDay = { day -> viewModel.setPaycheckStartDay(day) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
