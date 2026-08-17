package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warung_debts")
data class WarungDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tanggal: String,
    val namaWarung: String = "Warung",
    val nominal: Double,
    val alasan: String = "",
    val isLunas: Boolean = false,
    val totalDibayar: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
