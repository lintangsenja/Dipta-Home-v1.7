package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_note_items")
data class ShoppingNoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaBarang: String,
    val prioritas: String = "Sedang", // "Tinggi/Wajib", "Sedang", "Opsional"
    val isDone: Boolean = false,
    val estimasiHarga: Double = 0.0,
    val catatan: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
