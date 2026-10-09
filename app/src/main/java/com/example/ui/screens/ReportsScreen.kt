package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandNavyDark
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
import com.example.ui.viewmodel.ReportsMetrics
import com.example.util.CurrencyHelper

@Composable
fun ReportsScreen(
    metrics: ReportsMetrics,
    profile: BusinessProfileEntity,
    selectedPeriod: DateFilterPeriod,
    onPeriodSelected: (DateFilterPeriod) -> Unit
) {
    val currency = profile.currencySymbol

    val periods = listOf(
        Pair(DateFilterPeriod.TODAY, "Today"),
        Pair(DateFilterPeriod.THIS_WEEK, "This Week"),
        Pair(DateFilterPeriod.THIS_MONTH, "This Month"),
        Pair(DateFilterPeriod.ALL_TIME, "All Time")
    )

    val totalCashTransfer = metrics.cashReceived + metrics.transferReceived
    val cashRatio = if (totalCashTransfer > 0) (metrics.cashReceived / totalCashTransfer).toFloat() else 0.5f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Filter Chips
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Report Period",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(periods) { (period, label) ->
                            val isSelected = selectedPeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPeriodSelected(period) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandNavy,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("report_period_$label")
                            )
                        }
                    }
                }
            }
        }

        // Executive Financial P&L Card (Gross Profit vs Net Profit)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = BrandNavy),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "FINANCIAL SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite.copy(alpha = 0.75f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = CurrencyHelper.formatNaira(metrics.totalRevenue, currency),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )

                    Text(
                        text = "${metrics.totalSalesCount} transactions completed",
                        fontSize = 12.sp,
                        color = PureWhite.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = PureWhite.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // COGS and Gross Profit Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Cost of Goods (COGS)",
                                fontSize = 11.sp,
                                color = PureWhite.copy(alpha = 0.7f)
                            )
                            Text(
                                text = CurrencyHelper.formatNaira(metrics.totalCostOfGoods, currency),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Gross Profit",
                                fontSize = 11.sp,
                                color = BrandGreenLight
                            )
                            Text(
                                text = CurrencyHelper.formatNaira(metrics.grossProfit, currency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandGreenLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = PureWhite.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Operating Expenses & Net Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Operating Expenses",
                                fontSize = 11.sp,
                                color = PureWhite.copy(alpha = 0.7f)
                            )
                            Text(
                                text = CurrencyHelper.formatNaira(metrics.totalExpenses, currency),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NET PROFIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.netProfit >= 0) BrandGreenLight else ErrorRed,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = CurrencyHelper.formatNaira(metrics.netProfit, currency),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (metrics.netProfit >= 0) BrandGreenLight else ErrorRed
                            )
                        }
                    }
                }
            }
        }

        // Sales & Profit Daily Chart
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Revenue & Profit Trend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = "Daily performance over the recent days",
                        fontSize = 11.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart via Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        SalesBarChart(
                            dailyStats = metrics.dailyRevenueBreakdown,
                            currency = currency
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(BrandNavy, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sales Revenue", fontSize = 11.sp, color = Slate600)
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(BrandEmeraldGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Gross Profit", fontSize = 11.sp, color = Slate600)
                        }
                    }
                }
            }
        }

        // Cash vs Bank Transfer Received Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Method Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom split bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate200)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(cashRatio.coerceAtLeast(0.01f))
                                    .background(BrandEmeraldGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((1f - cashRatio).coerceAtLeast(0.01f))
                                    .background(BrandRoyalBlue)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(BrandEmeraldGreen, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = "Cash Received", fontSize = 12.sp, color = Slate600)
                                Text(
                                    text = CurrencyHelper.formatNaira(metrics.cashReceived, currency),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(BrandRoyalBlue, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Bank Transfers", fontSize = 12.sp, color = Slate600)
                                Text(
                                    text = CurrencyHelper.formatNaira(metrics.transferReceived, currency),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }
                    }
                }
            }
        }

        // Best-Selling Footwear
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Best-Selling Footwear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                        Icon(Icons.Default.Star, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (metrics.topSellingProducts.isEmpty()) {
                        Text(
                            text = "No sales recorded yet for this period.",
                            fontSize = 12.sp,
                            color = Slate500,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        metrics.topSellingProducts.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (index) {
                                            0 -> BrandGold.copy(alpha = 0.2f)
                                            1 -> Slate400.copy(alpha = 0.2f)
                                            else -> Slate200
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "${item.quantitySold} pairs sold",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyHelper.formatNaira(item.totalRevenue, currency),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "+${CurrencyHelper.formatNaira(item.totalProfit, currency)}",
                                        fontSize = 10.sp,
                                        color = BrandEmeraldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (index < metrics.topSellingProducts.size - 1) {
                                HorizontalDivider(color = Slate200)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesBarChart(
    dailyStats: List<com.example.ui.viewmodel.DailyStat>,
    currency: String
) {
    val maxRevenue = dailyStats.maxOfOrNull { it.revenue }?.coerceAtLeast(1000.0) ?: 1000.0

    Canvas(modifier = Modifier.fillMaxSize()) {
        val count = dailyStats.size
        if (count == 0) return@Canvas

        val w = size.width
        val h = size.height - 35f // leave room for labels
        val slotWidth = w / count
        val barWidth = slotWidth * 0.35f

        for (i in 0 until count) {
            val stat = dailyStats[i]
            val x = i * slotWidth + (slotWidth / 2f)

            val revHeight = ((stat.revenue / maxRevenue) * h).toFloat().coerceIn(4f, h)
            val profitHeight = ((stat.profit / maxRevenue) * h).toFloat().coerceIn(2f, revHeight)

            // Background subtle grid column
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(x - barWidth, 0f),
                size = Size(barWidth * 2f, h),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Revenue bar
            drawRoundRect(
                color = BrandNavy,
                topLeft = Offset(x - barWidth, h - revHeight),
                size = Size(barWidth, revHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Profit bar
            drawRoundRect(
                color = BrandEmeraldGreen,
                topLeft = Offset(x + 2f, h - profitHeight),
                size = Size(barWidth, profitHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Day label
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(stat.dayLabel, x, size.height - 4f, paint)
            }
        }
    }
}
