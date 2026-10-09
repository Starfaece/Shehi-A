package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {
    fun formatNaira(amount: Double, symbol: String = "₦", showDecimals: Boolean = false): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (showDecimals) 2 else 0
            maximumFractionDigits = 2
        }
        return "$symbol${formatter.format(amount)}"
    }

    fun parseAmount(input: String): Double {
        val sanitized = input.replace("₦", "").replace(",", "").trim()
        return sanitized.toDoubleOrNull() ?: 0.0
    }
}
