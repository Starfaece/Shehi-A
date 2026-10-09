package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.SaleWithItems

object ReceiptHelper {
    fun buildReceiptText(saleWithItems: SaleWithItems, profile: BusinessProfileEntity): String {
        val sale = saleWithItems.sale
        val items = saleWithItems.items
        val currency = profile.currencySymbol

        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("         ${profile.businessName.uppercase()}\n")
        if (profile.address.isNotBlank()) {
            sb.append("    ${profile.address}\n")
        }
        if (profile.phoneNumber.isNotBlank()) {
            sb.append("    Tel: ${profile.phoneNumber}\n")
        }
        sb.append("========================================\n")
        sb.append("RECEIPT: ${sale.transactionNumber}\n")
        sb.append("DATE:    ${DateHelper.formatDateTime(sale.createdAt)}\n")
        if (sale.customerName.isNotBlank()) {
            sb.append("CUSTOMER: ${sale.customerName}")
            if (sale.customerPhone.isNotBlank()) {
                sb.append(" (${sale.customerPhone})")
            }
            sb.append("\n")
        }
        sb.append("PAYMENT: ${sale.paymentMethod}\n")
        if (sale.isReversed) {
            sb.append("STATUS:  *** REVERSED / REFUNDED ***\n")
            sb.append("REASON:  ${sale.reverseReason}\n")
        } else {
            sb.append("STATUS:  COMPLETED\n")
        }
        sb.append("----------------------------------------\n")
        sb.append(String.format("%-20s %3s %14s\n", "ITEM", "QTY", "AMOUNT"))
        sb.append("----------------------------------------\n")

        for (item in items) {
            val itemName = if (item.productSize.isNotBlank()) {
                "${item.productName} [Sz ${item.productSize}]"
            } else {
                item.productName
            }
            sb.append("${itemName.take(38)}\n")
            sb.append(
                String.format(
                    "  @ %s x %d%20s\n",
                    CurrencyHelper.formatNaira(item.unitSellingPrice, currency),
                    item.quantity,
                    CurrencyHelper.formatNaira(item.subtotalAmount, currency)
                )
            )
        }

        sb.append("----------------------------------------\n")
        sb.append(String.format("%-20s %18s\n", "Subtotal:", CurrencyHelper.formatNaira(sale.totalAmount, currency)))
        if (sale.discountAmount > 0) {
            sb.append(String.format("%-20s -%17s\n", "Discount:", CurrencyHelper.formatNaira(sale.discountAmount, currency)))
        }
        sb.append(String.format("%-20s %18s\n", "TOTAL PAID:", CurrencyHelper.formatNaira(sale.finalAmount, currency)))
        sb.append("========================================\n")
        if (profile.receiptFooterNote.isNotBlank()) {
            sb.append("${profile.receiptFooterNote}\n")
        }
        sb.append("========================================\n")
        return sb.toString()
    }

    fun shareReceipt(context: Context, receiptText: String, transactionId: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Receipt $transactionId - Shehi A. Footwears")
            putExtra(Intent.EXTRA_TEXT, receiptText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt via"))
    }

    fun shareCsvContent(context: Context, filename: String, csvContent: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "$filename - Shehi A. Footwears")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export $filename via"))
    }
}
