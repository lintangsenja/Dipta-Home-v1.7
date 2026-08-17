package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ChildExpenseLog
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.RandomExpense
import com.example.data.entity.ShoppingNoteItem
import com.example.data.entity.WarungDebt
import com.example.data.entity.WarungDebtPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface WarungDao {

    // --- Daily Grocery Logs ---
    @Query("SELECT * FROM daily_grocery_logs ORDER BY id DESC")
    fun getAllDailyGroceryLogs(): Flow<List<DailyGroceryLog>>

    @Query("SELECT * FROM daily_grocery_logs ORDER BY id DESC")
    suspend fun getAllDailyGroceryLogsList(): List<DailyGroceryLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyGroceryLog(log: DailyGroceryLog): Long

    @Update
    suspend fun updateDailyGroceryLog(log: DailyGroceryLog)

    @Query("DELETE FROM daily_grocery_logs WHERE id = :id")
    suspend fun deleteDailyGroceryLogById(id: Int)

    @Query("DELETE FROM daily_grocery_logs")
    suspend fun deleteAllDailyGroceryLogs()


    // --- Random Expenses ---
    @Query("SELECT * FROM random_expenses ORDER BY id DESC")
    fun getAllRandomExpenses(): Flow<List<RandomExpense>>

    @Query("SELECT * FROM random_expenses ORDER BY id DESC")
    suspend fun getAllRandomExpensesList(): List<RandomExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRandomExpense(log: RandomExpense): Long

    @Update
    suspend fun updateRandomExpense(log: RandomExpense)

    @Query("DELETE FROM random_expenses WHERE id = :id")
    suspend fun deleteRandomExpenseById(id: Int)

    @Query("DELETE FROM random_expenses")
    suspend fun deleteAllRandomExpenses()


    // --- Child Expenses (Anak) ---
    @Query("SELECT * FROM child_expenses ORDER BY id DESC")
    fun getAllChildExpenses(): Flow<List<ChildExpenseLog>>

    @Query("SELECT * FROM child_expenses ORDER BY id DESC")
    suspend fun getAllChildExpensesList(): List<ChildExpenseLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildExpense(log: ChildExpenseLog): Long

    @Update
    suspend fun updateChildExpense(log: ChildExpenseLog)

    @Query("DELETE FROM child_expenses WHERE id = :id")
    suspend fun deleteChildExpenseById(id: Int)

    @Query("DELETE FROM child_expenses")
    suspend fun deleteAllChildExpenses()


    // --- Warung Debts ---
    @Query("SELECT * FROM warung_debts ORDER BY id DESC")
    fun getAllWarungDebts(): Flow<List<WarungDebt>>

    @Query("SELECT * FROM warung_debts ORDER BY id DESC")
    suspend fun getAllWarungDebtsList(): List<WarungDebt>

    @Query("SELECT * FROM warung_debts WHERE id = :id LIMIT 1")
    suspend fun getWarungDebtById(id: Int): WarungDebt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarungDebt(debt: WarungDebt): Long

    @Update
    suspend fun updateWarungDebt(debt: WarungDebt)

    @Query("DELETE FROM warung_debts WHERE id = :id")
    suspend fun deleteWarungDebtById(id: Int)

    @Query("DELETE FROM warung_debts")
    suspend fun deleteAllWarungDebts()


    // --- Debt Payments ---
    @Query("SELECT * FROM warung_debt_payments ORDER BY id DESC")
    fun getAllWarungDebtPayments(): Flow<List<WarungDebtPayment>>

    @Query("SELECT * FROM warung_debt_payments WHERE debtId = :debtId ORDER BY id DESC")
    fun getPaymentsForDebt(debtId: Int): Flow<List<WarungDebtPayment>>

    @Query("SELECT * FROM warung_debt_payments ORDER BY id DESC")
    suspend fun getAllWarungDebtPaymentsList(): List<WarungDebtPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarungDebtPayment(payment: WarungDebtPayment): Long

    @Update
    suspend fun updateWarungDebtPayment(payment: WarungDebtPayment)

    @Query("DELETE FROM warung_debt_payments WHERE id = :id")
    suspend fun deleteWarungDebtPaymentById(id: Int)

    @Query("DELETE FROM warung_debt_payments")
    suspend fun deleteAllWarungDebtPayments()


    // --- Shopping Note Items ---
    @Query("SELECT * FROM shopping_note_items ORDER BY isDone ASC, CASE WHEN prioritas = 'Tinggi/Wajib' THEN 1 WHEN prioritas = 'Sedang' THEN 2 ELSE 3 END ASC, id DESC")
    fun getAllShoppingNoteItems(): Flow<List<ShoppingNoteItem>>

    @Query("SELECT * FROM shopping_note_items ORDER BY id DESC")
    suspend fun getAllShoppingNoteItemsList(): List<ShoppingNoteItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingNoteItem(item: ShoppingNoteItem): Long

    @Update
    suspend fun updateShoppingNoteItem(item: ShoppingNoteItem)

    @Query("DELETE FROM shopping_note_items WHERE id = :id")
    suspend fun deleteShoppingNoteItemById(id: Int)

    @Query("DELETE FROM shopping_note_items WHERE isDone = 1")
    suspend fun deleteCompletedShoppingNotes()

    @Query("DELETE FROM shopping_note_items")
    suspend fun deleteAllShoppingNoteItems()
}
