package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val category: String, // Men's Shoes, Loafers, Slippers, Sneakers, Women's, Sandals, Boots, Kids
    val size: String,
    val color: String,
    val brand: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val quantity: Int,
    val lowStockThreshold: Int = 3,
    val photoUri: String? = null,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profitPerUnit: Double get() = sellingPrice - costPrice
    val profitMarginPercent: Double get() = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100.0 else 0.0
    val isOutOfStock: Boolean get() = quantity <= 0
    val isLowStock: Boolean get() = quantity > 0 && quantity <= lowStockThreshold
}
