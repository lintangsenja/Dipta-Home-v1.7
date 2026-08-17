package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_grocery_logs")
data class DailyGroceryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tanggal: String,
    val modalAwal: Double,
    val sisaUang: Double,
    val totalPengeluaran: Double,
    val rincian: String = "",
    val catatan: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
