package com.example

import com.example.data.local.entity.ProductEntity
import com.example.util.CurrencyHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testNairaFormatting() {
        val formatted = CurrencyHelper.formatNaira(28000.0, "₦")
        assertEquals("₦28,000", formatted)

        val formattedDecimals = CurrencyHelper.formatNaira(15500.50, "₦", showDecimals = true)
        assertEquals("₦15,500.50", formattedDecimals)

        val parsed = CurrencyHelper.parseAmount("₦25,000")
        assertEquals(25000.0, parsed, 0.001)
    }

    @Test
    fun testProductProfitCalculations() {
        val product = ProductEntity(
            id = 1,
            name = "Italian Leather Loafers",
            sku = "SAF-01",
            category = "Loafers",
            size = "43",
            color = "Black",
            brand = "Shehi Exclusive",
            costPrice = 18000.0,
            sellingPrice = 28000.0,
            quantity = 5,
            lowStockThreshold = 3
        )

        // Profit per unit = 28,000 - 18,000 = 10,000
        assertEquals(10000.0, product.profitPerUnit, 0.001)

        // Profit margin % = (10,000 / 18,000) * 100 = 55.555%
        assertEquals(55.555, product.profitMarginPercent, 0.01)

        // Stock checks
        assertFalse(product.isOutOfStock)
        assertFalse(product.isLowStock)

        val lowStockProduct = product.copy(quantity = 2)
        assertTrue(lowStockProduct.isLowStock)
        assertFalse(lowStockProduct.isOutOfStock)

        val outOfStockProduct = product.copy(quantity = 0)
        assertTrue(outOfStockProduct.isOutOfStock)
    }

    @Test
    fun testSaleFinancialTotalsCalculation() {
        val unitCost = 10000.0
        val unitSelling = 16000.0
        val qty = 3
        val discount = 2000.0

        val totalSelling = unitSelling * qty // 48,000
        val totalCost = unitCost * qty // 30,000
        val finalPayable = totalSelling - discount // 46,000
        val grossProfit = finalPayable - totalCost // 16,000

        assertEquals(48000.0, totalSelling, 0.001)
        assertEquals(30000.0, totalCost, 0.001)
        assertEquals(46000.0, finalPayable, 0.001)
        assertEquals(16000.0, grossProfit, 0.001)
    }

    @Test
    fun testNetProfitDistinctionWithExpenses() {
        val grossProfit = 50000.0
        val generatorFuelExpense = 8500.0
        val packagingBagsExpense = 5000.0
        val totalExpenses = generatorFuelExpense + packagingBagsExpense

        val netProfit = grossProfit - totalExpenses
        assertEquals(36500.0, netProfit, 0.001)
        assertTrue(netProfit < grossProfit)
    }
}
