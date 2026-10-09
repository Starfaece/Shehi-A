package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.CartItem
import com.example.util.CurrencyHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleScreen(
    catalogProducts: List<ProductEntity>,
    cartItems: List<CartItem>,
    profile: BusinessProfileEntity,
    onBack: () -> Unit,
    onAddToCart: (ProductEntity, Int) -> Unit,
    onUpdateQuantity: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onCompleteSale: (
        paymentMethod: String,
        discountAmount: Double,
        customerName: String,
        customerPhone: String,
        notes: String,
        allowOverride: Boolean
    ) -> Unit
) {
    val currency = profile.currencySymbol

    var productSearchQuery by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") } // "Cash", "Bank Transfer"
    var discountStr by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var saleNotes by remember { mutableStateOf("") }
    var allowStockOverride by remember { mutableStateOf(profile.allowNegativeStockSale) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val discount = CurrencyHelper.parseAmount(discountStr)
    val subtotalSelling = cartItems.sumOf { it.product.sellingPrice * it.quantity }
    val subtotalCost = cartItems.sumOf { it.product.costPrice * it.quantity }
    val finalTotal = (subtotalSelling - discount).coerceAtLeast(0.0)
    val grossProfit = finalTotal - subtotalCost

    val searchResults = remember(productSearchQuery, catalogProducts) {
        if (productSearchQuery.isBlank()) {
            emptyList()
        } else {
            catalogProducts.filter {
                it.name.contains(productSearchQuery, ignoreCase = true) ||
                        it.sku.contains(productSearchQuery, ignoreCase = true) ||
                        it.brand.contains(productSearchQuery, ignoreCase = true)
            }.take(5)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Record Quick Sale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = onClearCart) {
                            Text("Clear", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Footwear Selector & Quick Search
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select Footwear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = productSearchQuery,
                            onValueChange = { productSearchQuery = it; errorMessage = null },
                            placeholder = { Text("Search shoes to add to sale...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500)
                            },
                            trailingIcon = {
                                if (productSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { productSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sale_product_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Search Results dropdown list
                        if (searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate200.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    searchResults.forEach { shoe ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onAddToCart(shoe, 1)
                                                    productSearchQuery = ""
                                                }
                                                .padding(vertical = 8.dp, horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = shoe.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Slate900
                                                )
                                                Text(
                                                    text = "Sz ${shoe.size} • ${shoe.color} • Stock: ${shoe.quantity}",
                                                    fontSize = 11.sp,
                                                    color = if (shoe.isOutOfStock) ErrorRed else Slate600
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = CurrencyHelper.formatNaira(shoe.sellingPrice, currency),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = BrandNavy
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Add",
                                                    tint = BrandRoyalBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Slate200)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Pick from Stock Catalog Chips (if query is empty)
            if (productSearchQuery.isEmpty() && catalogProducts.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Quick Pick from Inventory",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            catalogProducts.take(3).forEach { product ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onAddToCart(product, 1) }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = product.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = CurrencyHelper.formatNaira(product.sellingPrice, currency),
                                            fontSize = 11.sp,
                                            color = BrandEmeraldGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${product.quantity} in stock",
                                            fontSize = 9.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cart Items List
            item {
                Text(
                    text = "Items in this Sale (${cartItems.sumOf { it.quantity }} pairs)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (cartItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.shehi_logo),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Slate500
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No shoes selected",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Slate700
                            )
                            Text(
                                text = "Use search above to select footwear to sell.",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            } else {
                items(cartItems) { cartItem ->
                    SaleCartItemRow(
                        cartItem = cartItem,
                        currency = currency,
                        allowOverride = allowStockOverride,
                        onQuantityChanged = { newQty ->
                            onUpdateQuantity(cartItem.product.id, newQty)
                        },
                        onRemove = {
                            onRemoveFromCart(cartItem.product.id)
                        }
                    )
                }
            }

            // Payment Method, Discount & Customer Details
            if (cartItems.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Payment & Customer Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            // Payment Method Segment (Cash vs Bank Transfer)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedPaymentMethod == "Cash") BrandEmeraldGreen.copy(alpha = 0.15f) else Slate200.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (selectedPaymentMethod == "Cash") 1.5.dp else 1.dp,
                                        if (selectedPaymentMethod == "Cash") BrandEmeraldGreen else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPaymentMethod = "Cash" }
                                        .testTag("payment_method_cash")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Money,
                                            contentDescription = null,
                                            tint = if (selectedPaymentMethod == "Cash") BrandEmeraldGreen else Slate600
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cash",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (selectedPaymentMethod == "Cash") BrandGreenDark else Slate800
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedPaymentMethod == "Bank Transfer") BrandRoyalBlue.copy(alpha = 0.15f) else Slate200.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (selectedPaymentMethod == "Bank Transfer") 1.5.dp else 1.dp,
                                        if (selectedPaymentMethod == "Bank Transfer") BrandRoyalBlue else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPaymentMethod = "Bank Transfer" }
                                        .testTag("payment_method_transfer")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = if (selectedPaymentMethod == "Bank Transfer") BrandRoyalBlue else Slate600
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Bank Transfer",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (selectedPaymentMethod == "Bank Transfer") BrandRoyalBlue else Slate800
                                        )
                                    }
                                }
                            }

                            // Discount Input
                            OutlinedTextField(
                                value = discountStr,
                                onValueChange = { discountStr = it },
                                label = { Text("Discount ($currency) - Optional") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sale_discount_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Customer Name & Phone
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Customer Name (Optional)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sale_customer_name_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = { customerPhone = it },
                                    label = { Text("Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = saleNotes,
                                onValueChange = { saleNotes = it },
                                label = { Text("Transaction Notes (Optional)") },
                                placeholder = { Text("e.g., Paid via GTBank, pickup later") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Stock override switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Allow stock override",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Sell even if recorded stock is zero/insufficient",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Switch(
                                    checked = allowStockOverride,
                                    onCheckedChange = { allowStockOverride = it },
                                    modifier = Modifier.testTag("stock_override_switch")
                                )
                            }
                        }
                    }
                }

                // Summary and Financial Breakdown Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Financial Calculation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal Selling Price:", color = Slate600, fontSize = 13.sp)
                                Text(
                                    text = CurrencyHelper.formatNaira(subtotalSelling, currency),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Discount Applied:", color = ErrorRed, fontSize = 13.sp)
                                    Text(
                                        text = "-${CurrencyHelper.formatNaira(discount, currency)}",
                                        color = ErrorRed,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Cost of Goods (COGS):", color = Slate600, fontSize = 13.sp)
                                Text(
                                    text = CurrencyHelper.formatNaira(subtotalCost, currency),
                                    fontSize = 13.sp,
                                    color = Slate700
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL PAYABLE:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = CurrencyHelper.formatNaira(finalTotal, currency),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = BrandNavy
                                )
                            }

                            // Gross Profit Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandGreenLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Gross Profit from this Sale:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandGreenDark
                                    )
                                    Text(
                                        text = CurrencyHelper.formatNaira(grossProfit, currency),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandEmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Error message
                if (errorMessage != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage!!, color = ErrorRed, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Submit Sale Button
                item {
                    Button(
                        onClick = {
                            // Check stock validation if override not permitted
                            if (!allowStockOverride) {
                                for (item in cartItems) {
                                    if (item.product.quantity < item.quantity) {
                                        errorMessage = "Stock insufficient for '${item.product.name}'. In stock: ${item.product.quantity}, Selected: ${item.quantity}. Enable override if authorized."
                                        return@Button
                                    }
                                }
                            }

                            onCompleteSale(
                                selectedPaymentMethod,
                                discount,
                                customerName,
                                customerPhone,
                                saleNotes,
                                allowStockOverride
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("complete_sale_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRoyalBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Sale & View Receipt (${CurrencyHelper.formatNaira(finalTotal, currency)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SaleCartItemRow(
    cartItem: CartItem,
    currency: String,
    allowOverride: Boolean,
    onQuantityChanged: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val p = cartItem.product
    val isInsufficient = !allowOverride && p.quantity < cartItem.quantity

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInsufficient) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isInsufficient) androidx.compose.foundation.BorderStroke(1.dp, ErrorRed) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = p.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${CurrencyHelper.formatNaira(p.sellingPrice, currency)} / pair • Stock: ${p.quantity}",
                    fontSize = 11.sp,
                    color = if (isInsufficient) ErrorRed else Slate500
                )
                if (isInsufficient) {
                    Text(
                        text = "Exceeds available stock!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        if (cartItem.quantity > 1) {
                            onQuantityChanged(cartItem.quantity - 1)
                        } else {
                            onRemove()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Slate700)
                }

                Text(
                    text = "${cartItem.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(
                    onClick = {
                        onQuantityChanged(cartItem.quantity + 1)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandRoyalBlue)
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed)
                }
            }
        }
    }
}
