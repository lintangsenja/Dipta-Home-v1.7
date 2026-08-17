package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "",
    val prepTime: String = "",
    val cookTime: String = "",
    val yields: String = "",
    val ingredients: String = "",
    val directions: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val skillRating: Int = 0,
    val isFavorite: Boolean = false,
    val flavorTag: String = "",
    val source: String = ""
)
