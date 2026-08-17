package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MealPlanItem
import com.example.data.entity.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // --- Recipes ---
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllActiveRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY id DESC")
    suspend fun getAllActiveRecipesList(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    suspend fun getDeletedRecipesList(): List<Recipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("UPDATE recipes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteRecipe(id: Int, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE recipes SET isDeleted = 0, deletedAt = 0 WHERE id = :id")
    suspend fun restoreRecipe(id: Int)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun hardDeleteRecipe(id: Int)

    @Query("DELETE FROM recipes WHERE isDeleted = 1")
    suspend fun clearTrash()

    @Query("DELETE FROM recipes")
    suspend fun clearAllRecipes()

    // --- Meal Plan ---
    @Query("SELECT * FROM meal_plan_items ORDER BY id ASC")
    fun getAllMealPlanItems(): Flow<List<MealPlanItem>>

    @Query("SELECT * FROM meal_plan_items ORDER BY id ASC")
    suspend fun getAllMealPlanItemsList(): List<MealPlanItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanItem(item: MealPlanItem): Long

    @Query("DELETE FROM meal_plan_items WHERE id = :id")
    suspend fun deleteMealPlanItem(id: Int)

    @Query("DELETE FROM meal_plan_items WHERE dayOfWeek = :dayOfWeek")
    suspend fun clearMealPlanForDay(dayOfWeek: String)

    @Query("DELETE FROM meal_plan_items")
    suspend fun clearAllMealPlans()
}
