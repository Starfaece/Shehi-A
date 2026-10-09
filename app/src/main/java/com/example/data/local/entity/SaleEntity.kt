package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionNumber: String,
    val totalAmount: Double,
    val totalCost: Double,
    val discountAmount: Double = 0.0,
    val finalAmount: Double,
    val grossProfit: Double,
    val paymentMethod: String, // "Cash", "Bank Transfer"
    val customerName: String = "",
    val customerPhone: String = "",
    val notes: String = "",
    val isReversed: Boolean = false,
    val reverseReason: String = "",
    val reversedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
