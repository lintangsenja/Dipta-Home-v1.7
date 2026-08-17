package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicle_id: Int = 1,
    val tanggal: Long = System.currentTimeMillis(),
    val km_motor: Int,
    val nominal: Int,
    val liter: Float,
    val jarak_tempuh: Int = 0,
    val km_per_liter: Float = 0f,
    val is_boros: Boolean = false,
    val jenis_bbm: String = "Pertalite",
    val harga_per_liter: Int = 0
)
