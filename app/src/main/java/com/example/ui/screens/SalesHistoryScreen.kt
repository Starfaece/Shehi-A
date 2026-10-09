package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SaleWithItems
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.DateFilterPeriod
import com.example.util.CurrencyHelper
import com.example.util.DateHelper

@Composable
fun SalesHistoryScreen(
    sales: List<SaleWithItems>,
    currencySymbol: String,
    dateFilter: DateFilterPeriod,
    onDateFilterChange: (DateFilterPeriod) -> Unit,
    paymentFilter: String,
    onPaymentFilterChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onViewReceipt: (SaleWithItems) -> Unit,
    onReverseSale: (saleId: Long, reason: String) -> Unit
) {
    var saleToReverse by remember { mutableStateOf<SaleWithItems?>(null) }
    var reversalReason by remember { mutableStateOf("") }

    val periodOptions = listOf(
        Pair(DateFilterPeriod.ALL_TIME, "All Time"),
        Pair(DateFilterPeriod.TODAY, "Today"),
        Pair(DateFilterPeriod.THIS_WEEK, "This Week"),
        Pair(DateFilterPeriod.THIS_MONTH, "This Month")
    )

    val paymentOptions = listOf("All", "Cash", "Bank Transfer")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Filters & Search Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by receipt #, shoe, customer...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sales_history_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date period chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(periodOptions) { (period, label) ->
                        val isSelected = dateFilter == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDateFilterChange(period) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Payment method chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(paymentOptions) { method ->
                        val isSelected = paymentFilter == method
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPaymentFilterChange(method) },
                            label = { Text(method, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }
        }

        // Summary Header for Filtered Sales
        val totalRevenue = sales.filter { !it.sale.isReversed }.sumOf { it.sale.finalAmount }
        val totalProfit = sales.filter { !it.sale.isReversed }.sumOf { it.sale.grossProfit }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sales.size} Transactions Recorded",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate600
            )

            Text(
                text = "Total: ${CurrencyHelper.formatNaira(totalRevenue, currencySymbol)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandNavy
            )
        }

        if (sales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = Slate400
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No sales found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate700
                    )
                    Text(
                        text = "Change your date filter or search query.",
                        fontSize = 13.sp,
                        color = Slate500
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sales, key = { it.sale.id }) { saleWithItems ->
                    SaleHistoryCard(
                        saleWithItems = saleWithItems,
                        currencySymbol = currencySymbol,
                        onViewReceipt = { onViewReceipt(saleWithItems) },
                        onReverseClick = { saleToReverse = saleWithItems }
                    )
                }
            }
        }
    }

    // Auditable Reversal / Refund Confirmation Dialog
    if (saleToReverse != null) {
        val s = saleToReverse!!
        AlertDialog(
            onDismissRequest = { saleToReverse = null },
            title = {
                Text("Reverse Transaction?")
            },
            text = {
                Column {
                    Text(
                        text = "Reversing sale #${s.sale.transactionNumber} will automatically restore the sold footwear items back into store inventory.",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = reversalReason,
                        onValueChange = { reversalReason = it },
                        label = { Text("Audit Reason *") },
                        placeholder = { Text("e.g. Customer returned wrong size / Cancelled") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = reversalReason.ifBlank { "Customer Return / Refund" }
                        onReverseSale(s.sale.id, reason)
                        saleToReverse = null
                        reversalReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Confirm Reversal & Restore Stock")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { saleToReverse = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SaleHistoryCard(
    saleWithItems: SaleWithItems,
    currencySymbol: String,
    onViewReceipt: () -> Unit,
    onReverseClick: () -> Unit
) {
    val sale = saleWithItems.sale
    val totalQty = saleWithItems.items.sumOf { it.quantity }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (sale.isReversed) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.transactionNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sale.transactionNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (sale.isReversed) ErrorRed else Slate900
                    )
                    Text(
                        text = DateHelper.formatDateTime(sale.createdAt),
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sale.isReversed) Color(0xFFFEE2E2) else BrandGreenLight
                ) {
                    Text(
                        text = if (sale.isReversed) "REVERSED" else "COMPLETED",
                        color = if (sale.isReversed) ErrorRed else BrandGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Line items breakdown
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                saleWithItems.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.productName} (Sz ${item.productSize}) x${item.quantity}",
                            fontSize = 12.sp,
                            color = Slate700,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CurrencyHelper.formatNaira(item.subtotalAmount, currencySymbol),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate800
                        )
                    }
                }
            }

            if (sale.customerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Customer: ${sale.customerName} ${if (sale.customerPhone.isNotBlank()) "(${sale.customerPhone})" else ""}",
                    fontSize = 11.sp,
                    color = Slate600
                )
            }

            if (sale.isReversed && sale.reverseReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reversal Reason: ${sale.reverseReason}",
                    fontSize = 11.sp,
                    color = ErrorRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(8.dp))

            // Total Amount, Profit & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) BrandEmeraldGreen.copy(alpha = 0.15f) else BrandRoyalBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) Icons.Default.Money else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) BrandEmeraldGreen else BrandRoyalBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sale.paymentMethod,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }

                    Text(
                        text = CurrencyHelper.formatNaira(sale.finalAmount, currencySymbol),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Receipt Action
                    Button(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Receipt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Reverse button if not already reversed
                    if (!sale.isReversed) {
                        OutlinedButton(
                            onClick = onReverseClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Refund",
                                color = ErrorRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
