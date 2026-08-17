package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_expenses")
data class ChildExpenseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tanggal: String,
    val modalAwal: Double = 0.0,
    val sisaUang: Double = 0.0,
    val totalPengeluaran: Double,
    val rincian: String = "",
    val catatan: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
