package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AdditionalIncome
import com.example.data.entity.MainSalaryConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    // --- MAIN SALARY CONFIG ---
    @Query("SELECT * FROM main_salary_config ORDER BY id DESC LIMIT 1")
    fun getMainSalaryConfig(): Flow<MainSalaryConfig?>

    @Query("SELECT * FROM main_salary_config ORDER BY id DESC LIMIT 1")
    suspend fun getMainSalaryConfigDirect(): MainSalaryConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMainSalaryConfig(config: MainSalaryConfig): Long

    @Query("DELETE FROM main_salary_config")
    suspend fun clearMainSalaryConfig()

    // --- ADDITIONAL INCOMES ---
    @Query("SELECT * FROM additional_incomes ORDER BY timestamp DESC")
    fun getAllAdditionalIncomes(): Flow<List<AdditionalIncome>>

    @Query("SELECT * FROM additional_incomes ORDER BY timestamp DESC")
    suspend fun getAllAdditionalIncomesList(): List<AdditionalIncome>

    @Query("SELECT * FROM additional_incomes WHERE id = :id")
    suspend fun getAdditionalIncomeById(id: Int): AdditionalIncome?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdditionalIncome(income: AdditionalIncome): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAdditionalIncomes(incomes: List<AdditionalIncome>)

    @Update
    suspend fun updateAdditionalIncome(income: AdditionalIncome)

    @Query("DELETE FROM additional_incomes WHERE id = :id")
    suspend fun deleteAdditionalIncomeById(id: Int)

    @Query("DELETE FROM additional_incomes")
    suspend fun clearAllAdditionalIncomes()
}
