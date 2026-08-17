package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "electricity_logs")
data class ElectricityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val harga: Int = 0,
    val jumlah_kwh: Float = 0f,
    val sisa_sebelumnya: Float = 0f,
    val total_kwh_aktif: Float = 0f,
    val durasi_hari: Int = 0,
    val kwh_per_hari: Float = 0f,
    val is_boros: Boolean = false,
    val is_initial: Boolean = false
)
