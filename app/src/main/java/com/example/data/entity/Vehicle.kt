package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama_kendaraan: String,
    val nomor_plat: String = "",
    val jenis_kendaraan: String = "Motor", // "Motor", "Mobil", "Motor Matic", etc.
    val icon_type: String = "Motor", // "Motor" or "Mobil"
    val current_odometer: Int = 0,
    val tanggal_pajak_stnk: String = "",
    val catatan_sparepart: String = ""
)
