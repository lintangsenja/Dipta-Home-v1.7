package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oil_logs")
data class OilLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicle_id: Int = 1,
    val tanggal: Long = System.currentTimeMillis(),
    val km_motor: Int,
    val jenis_oli: String, // "Oli Mesin" or "Oli Gardan"
    val harga: Int,
    val kapasitas_ml: Int,
    val target_km: Int = 0,
    val interval_km: Int = 3000,
    val garansi_bengkel: String = ""
)
