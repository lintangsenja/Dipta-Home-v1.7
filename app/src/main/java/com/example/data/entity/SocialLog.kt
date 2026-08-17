package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_logs")
data class SocialLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val kategori: String, // "Iuran Jimpitan Warga", "Tabungan Kurban", "Iuran Kebersihan", "Lain-lain"
    val nominal: Int,
    val keterangan: String = "",
    val tipe_transaksi: String = "Masuk" // "Masuk" or "Keluar"
)
