package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing the user's primary monthly base salary configuration.
 */
@Entity(tableName = "main_salary_config")
data class MainSalaryConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nominal: Double = 0.0,
    val catatan: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
