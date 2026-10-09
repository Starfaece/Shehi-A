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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleWithItems
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.ScreenTab
import com.example.util.CurrencyHelper
import com.example.util.DateHelper

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    profile: BusinessProfileEntity,
    lowStockItems: List<ProductEntity>,
    recentSales: List<SaleWithItems>,
    onNavigateTab: (ScreenTab) -> Unit,
    onQuickRecordSale: () -> Unit,
    onQuickAddProduct: () -> Unit,
    onQuickAddExpense: () -> Unit,
    onInspectSale: (SaleWithItems) -> Unit
) {
    val currency = profile.currencySymbol

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Business Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = BrandNavy),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_banner_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandNavyDark, BrandNavy, BrandRoyalBlue)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = profile.businessName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    ),
                                    color = PureWhite
                                )
                                Text(
                                    text = "Today • ${DateHelper.formatDate(System.currentTimeMillis())}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = PureWhite.copy(alpha = 0.8f)
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandEmeraldGreen.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmeraldGreen)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(BrandEmeraldGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Store Open",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Large Today Revenue & Gross Profit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S SALES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite.copy(alpha = 0.75f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = CurrencyHelper.formatNaira(metrics.todayRevenue, currency),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp
                                    ),
                                    color = PureWhite
                                )
                                Text(
                                    text = "${metrics.todaySalesCount} sales recorded today",
                                    fontSize = 12.sp,
                                    color = PureWhite.copy(alpha = 0.8f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "GROSS PROFIT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandGreenLight,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = CurrencyHelper.formatNaira(metrics.todayGrossProfit, currency),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = BrandGreenLight
                                )
                                Text(
                                    text = "COGS: ${CurrencyHelper.formatNaira(metrics.todayCostOfGoods, currency)}",
                                    fontSize = 11.sp,
                                    color = PureWhite.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // Net Profit notice if expenses recorded
                        if (metrics.todayExpenses > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = PureWhite.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Today's Expenses: ${CurrencyHelper.formatNaira(metrics.todayExpenses, currency)}",
                                    fontSize = 12.sp,
                                    color = PureWhite.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "Net Profit: ${CurrencyHelper.formatNaira(metrics.todayNetProfit, currency)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (metrics.todayNetProfit >= 0) BrandGreenLight else ErrorRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Buttons (Large, Accessible)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Record Sale Primary Button
                    Button(
                        onClick = onQuickRecordSale,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(54.dp)
                            .testTag("dashboard_quick_record_sale_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandRoyalBlue,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Sale",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Add Product Button
                    OutlinedButton(
                        onClick = onQuickAddProduct,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("dashboard_quick_add_product_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Shoe",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Secondary row: Record Expense & View Inventory
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onQuickAddExpense,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dashboard_quick_expense_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Log Expense",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = { onNavigateTab(ScreenTab.PRODUCTS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dashboard_quick_inventory_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Stock",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Inventory Value & Health Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Inventory Stock
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Footwear In Stock",
                    value = "${metrics.totalInventoryUnits} Pairs",
                    subtitle = "Valued at ${CurrencyHelper.formatNaira(metrics.totalInventoryCostValue, currency)} (Cost)",
                    icon = Icons.Default.Inventory,
                    iconBgColor = BrandNavy.copy(alpha = 0.1f),
                    iconTintColor = BrandNavy,
                    onClick = { onNavigateTab(ScreenTab.PRODUCTS) }
                )

                // Cash vs Bank Transfer split
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Payment Split",
                    value = "Cash: ${CurrencyHelper.formatNaira(metrics.todayCashReceived, currency)}",
                    subtitle = "Transfer: ${CurrencyHelper.formatNaira(metrics.todayTransferReceived, currency)}",
                    icon = Icons.Default.CreditCard,
                    iconBgColor = BrandEmeraldGreen.copy(alpha = 0.12f),
                    iconTintColor = BrandEmeraldGreen,
                    onClick = { onNavigateTab(ScreenTab.SALES) }
                )
            }
        }

        // Low-stock / Out-of-stock warning banner
        if (metrics.lowStockCount > 0 || metrics.outOfStockCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (metrics.outOfStockCount > 0) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (metrics.outOfStockCount > 0) Color(0xFFFCA5A5) else Color(0xFFFCD34D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(ScreenTab.PRODUCTS) }
                        .testTag("dashboard_stock_alert_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (metrics.outOfStockCount > 0) ErrorRed.copy(alpha = 0.15f) else BrandGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Stock Alert",
                                    tint = if (metrics.outOfStockCount > 0) ErrorRed else BrandGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restock Notice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = buildString {
                                    if (metrics.outOfStockCount > 0) {
                                        append("${metrics.outOfStockCount} shoes out of stock. ")
                                    }
                                    if (metrics.lowStockCount > 0) {
                                        append("${metrics.lowStockCount} items running low.")
                                    }
                                },
                                fontSize = 12.sp,
                                color = Slate700
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go to Products",
                            tint = Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = { onNavigateTab(ScreenTab.SALES) },
                    modifier = Modifier.testTag("view_all_sales_button")
                ) {
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (recentSales.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No sales recorded yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Slate700
                        )
                        Text(
                            text = "Tap 'Record Sale' above to make your first sale.",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            }
        } else {
            items(recentSales.take(5)) { saleWithItems ->
                RecentSaleItemRow(
                    saleWithItems = saleWithItems,
                    currency = currency,
                    onClick = { onInspectSale(saleWithItems) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBgColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentSaleItemRow(
    saleWithItems: SaleWithItems,
    currency: String,
    onClick: () -> Unit
) {
    val sale = saleWithItems.sale
    val totalQty = saleWithItems.items.sumOf { it.quantity }
    val primaryItemName = saleWithItems.items.firstOrNull()?.productName ?: "Footwear"
    val summary = if (saleWithItems.items.size > 1) {
        "$primaryItemName + ${saleWithItems.items.size - 1} more"
    } else {
        primaryItemName
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recent_sale_${sale.transactionNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) BrandEmeraldGreen.copy(alpha = 0.12f) else BrandRoyalBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) Icons.Default.Money else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) BrandEmeraldGreen else BrandRoyalBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = summary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "x$totalQty",
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sale.transactionNumber,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                    Text(text = " • ", fontSize = 11.sp, color = Slate500)
                    Text(
                        text = sale.paymentMethod,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (sale.paymentMethod.equals("Cash", ignoreCase = true)) BrandGreenDark else BrandRoyalBlue
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyHelper.formatNaira(sale.finalAmount, currency),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (sale.isReversed) {
                    Text(
                        text = "REVERSED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                } else {
                    Text(
                        text = "+${CurrencyHelper.formatNaira(sale.grossProfit, currency)} profit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandEmeraldGreen
                    )
                }
            }
        }
    }
}

private val Slate400 = Color(0xFF94A3B8)
