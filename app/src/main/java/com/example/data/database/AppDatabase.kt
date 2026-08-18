package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ElectricityDao
import com.example.data.dao.FuelDao
import com.example.data.dao.IncomeDao
import com.example.data.dao.OilDao
import com.example.data.dao.RecipeDao
import com.example.data.dao.ServiceDao
import com.example.data.dao.SocialDao
import com.example.data.dao.VehicleDao
import com.example.data.dao.WarungDao
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

@Database(
    entities = [
        Vehicle::class,
        FuelLog::class,
        OilLog::class,
        ElectricityLog::class,
        ServiceLog::class,
        SocialLog::class,
        DailyGroceryLog::class,
        RandomExpense::class,
        ChildExpenseLog::class,
        WarungDebt::class,
        WarungDebtPayment::class,
        ShoppingNoteItem::class,
        Recipe::class,
        MealPlanItem::class,
        MainSalaryConfig::class,
        AdditionalIncome::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelDao(): FuelDao
    abstract fun oilDao(): OilDao
    abstract fun electricityDao(): ElectricityDao
    abstract fun serviceDao(): ServiceDao
    abstract fun socialDao(): SocialDao
    abstract fun warungDao(): WarungDao
    abstract fun recipeDao(): RecipeDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "keluarga_tracker_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
