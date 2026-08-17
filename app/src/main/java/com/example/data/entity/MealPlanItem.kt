package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plan_items")
data class MealPlanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dayOfWeek: String, // "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
    val recipeId: Int,
    val recipeTitle: String,
    val mealType: String = "Makan Siang/Malam", // "Sarapan", "Makan Siang", "Makan Malam", "Cemilan"
    val timestamp: Long = System.currentTimeMillis()
)
