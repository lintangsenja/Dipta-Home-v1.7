package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warung_debt_payments")
data class WarungDebtPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val debtId: Int,
    val tanggal: String,
    val nominalBayar: Double,
    val catatan: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
