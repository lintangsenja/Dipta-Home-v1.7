package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing additional income sources (lemburan, bonus, tunjangan, freelance, uang kaget, etc.).
 * Includes interactive ON/OFF toggle and cycle allocation (Bulan Berjalan vs Bulan Depan).
 */
@Entity(tableName = "additional_incomes")
data class AdditionalIncome(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val judul: String,
    val kategori: String = "Lemburan", // Lemburan, Bonus, Freelance, THR, Hadiah, Uang Kaget, Lainnya
    val nominal: Double = 0.0,
    val tanggal: String = "", // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true, // Toggle ON/OFF
    val targetCycleOffset: Int = 0, // 0 = Bulan Berjalan, 1 = Bulan Depan, etc.
    val targetCycleLabel: String = "", // e.g. "25 Agu - 24 Sep 2026"
    val catatan: String = ""
)
