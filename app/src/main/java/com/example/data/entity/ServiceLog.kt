package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicle_id: Int,
    val tanggal: Long = System.currentTimeMillis(),
    val km_motor: Int,
    val kategori: String, // "Servis Rutin", "Ganti Suku Cadang", "Perbaikan"
    val deskripsi_item: String, // Free text e.g., "Ganti ban, rantai, gir, dan kampas rem"
    val total_biaya: Int,
    val target_km_next: Int = 0,
    val interval_km: Int = 5000,
    val garansi_bengkel: String = ""
)
