package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "Shehi A. Footwears",
    val ownerName: String = "Shehi Abubakar",
    val phoneNumber: String = "+234 803 123 4567",
    val email: String = "shehifootwears@gmail.com",
    val address: String = "Main Market Plaza, Suite 42, Lagos / Kano, Nigeria",
    val receiptFooterNote: String = "Thank you for shopping at Shehi A. Footwears! No refund after 7 days without receipt.",
    val currencySymbol: String = "₦",
    val isPinEnabled: Boolean = false,
    val pinCode: String = "",
    val allowNegativeStockSale: Boolean = false
)
