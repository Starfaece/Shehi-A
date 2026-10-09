package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Shop Rent", "Generator & Fuel", "Logistics & Delivery", "Packaging & Bags", "Staff & Apprentices", "Shoe Care & Polish", "General Maintenance", "Other"
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
