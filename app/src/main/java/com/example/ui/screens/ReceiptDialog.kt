package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.SaleWithItems
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.CurrencyHelper
import com.example.util.DateHelper
import com.example.util.ReceiptHelper

@Composable
fun ReceiptDialog(
    saleWithItems: SaleWithItems,
    profile: BusinessProfileEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sale = saleWithItems.sale
    val currency = profile.currencySymbol

    val receiptText = ReceiptHelper.buildReceiptText(saleWithItems, profile)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Dismiss & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales Receipt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brand Emblem Header
                Surface(
                    shape = CircleShape,
                    color = BrandNavy,
                    modifier = Modifier.size(54.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.shehi_logo),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = profile.businessName.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = BrandNavyDark,
                    letterSpacing = 1.sp
                )

                if (profile.address.isNotBlank()) {
                    Text(
                        text = profile.address,
                        fontSize = 11.sp,
                        color = Slate600,
                        textAlign = TextAlign.Center
                    )
                }

                if (profile.phoneNumber.isNotBlank()) {
                    Text(
                        text = "Tel: ${profile.phoneNumber}",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Receipt Metadata
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReceiptRow(label = "Receipt #:", value = sale.transactionNumber)
                    ReceiptRow(label = "Date & Time:", value = DateHelper.formatDateTime(sale.createdAt))
                    if (sale.customerName.isNotBlank()) {
                        ReceiptRow(label = "Customer:", value = sale.customerName)
                    }
                    ReceiptRow(label = "Payment:", value = sale.paymentMethod)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Status:", fontSize = 12.sp, color = Slate600)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (sale.isReversed) Color(0xFFFEE2E2) else BrandGreenLight
                        ) {
                            Text(
                                text = if (sale.isReversed) "REVERSED / REFUNDED" else "PAID (COMPLETED)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sale.isReversed) ErrorRed else BrandGreenDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (sale.isReversed && sale.reverseReason.isNotBlank()) {
                        ReceiptRow(label = "Refund Reason:", value = sale.reverseReason, valueColor = ErrorRed)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Line Items
                Text(
                    text = "PURCHASED ITEMS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    modifier = Modifier.align(Alignment.Start),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                saleWithItems.items.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.productName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CurrencyHelper.formatNaira(item.subtotalAmount, currency),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sz ${item.productSize} • ${item.productColor} • @ ${CurrencyHelper.formatNaira(item.unitSellingPrice, currency)} x ${item.quantity}",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Totals
                ReceiptRow(label = "Subtotal:", value = CurrencyHelper.formatNaira(sale.totalAmount, currency))

                if (sale.discountAmount > 0) {
                    ReceiptRow(
                        label = "Discount:",
                        value = "-${CurrencyHelper.formatNaira(sale.discountAmount, currency)}",
                        valueColor = ErrorRed
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL PAID:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = CurrencyHelper.formatNaira(sale.finalAmount, currency),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BrandNavy
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (profile.receiptFooterNote.isNotBlank()) {
                    Text(
                        text = profile.receiptFooterNote,
                        fontSize = 11.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sharing & Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Share Receipt via WhatsApp/ShareSheet
                    Button(
                        onClick = {
                            ReceiptHelper.shareReceipt(context, receiptText, sale.transactionNumber)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Copy Text
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(receiptText))
                            Toast.makeText(context, "Receipt text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color = Slate800) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Slate600)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
