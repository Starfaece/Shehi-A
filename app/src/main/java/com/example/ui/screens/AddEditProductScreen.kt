package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.util.CurrencyHelper
import com.example.util.ImageStorageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    initialProduct: ProductEntity? = null,
    currencySymbol: String,
    onBack: () -> Unit,
    onSaveProduct: (
        id: Long,
        name: String,
        sku: String,
        category: String,
        size: String,
        color: String,
        brand: String,
        costPrice: Double,
        sellingPrice: Double,
        quantity: Int,
        lowStockThreshold: Int,
        photoUri: String?,
        description: String
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember {
        mutableStateOf(
            initialProduct?.sku ?: "SAF-${(100..999).random()}"
        )
    }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Men's Shoes") }
    var size by remember { mutableStateOf(initialProduct?.size ?: "42") }
    var color by remember { mutableStateOf(initialProduct?.color ?: "Black") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "Shehi Crafted") }

    var costPriceStr by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.costPrice > 0) initialProduct.costPrice.toInt().toString() else "")
    }
    var sellingPriceStr by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.sellingPrice > 0) initialProduct.sellingPrice.toInt().toString() else "")
    }
    var quantityStr by remember {
        mutableStateOf(if (initialProduct != null) initialProduct.quantity.toString() else "10")
    }
    var lowStockThresholdStr by remember {
        mutableStateOf(if (initialProduct != null) initialProduct.lowStockThreshold.toString() else "3")
    }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var photoUri by remember { mutableStateOf<String?>(initialProduct?.photoUri) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Men's Shoes",
        "Loafers",
        "Slippers & Half Shoes",
        "Sneakers",
        "Women's",
        "Sandals & Slides",
        "Boots",
        "Kids"
    )

    // Android system Photo Picker (no permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            photoUri = savedPath ?: uri.toString()
        }
    }

    // Live profit calculation
    val costPrice = CurrencyHelper.parseAmount(costPriceStr)
    val sellingPrice = CurrencyHelper.parseAmount(sellingPriceStr)
    val quantity = quantityStr.toIntOrNull() ?: 0
    val lowStockThreshold = lowStockThresholdStr.toIntOrNull() ?: 3

    val profitPerUnit = sellingPrice - costPrice
    val profitMarginPercent = if (costPrice > 0) (profitPerUnit / costPrice) * 100.0 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialProduct == null) "Add Footwear" else "Edit Footwear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Photo Selection Section
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Product Photo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate200)
                                .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Selected Footwear Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Add Photo",
                                        tint = Slate500,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap to choose photo",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (photoUri == null) "Select Photo" else "Change Photo", fontSize = 12.sp)
                            }

                            if (photoUri != null) {
                                OutlinedButton(
                                    onClick = { photoUri = null },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", color = ErrorRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Basic Information
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Footwear Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; validationError = null },
                            label = { Text("Product Name *") },
                            placeholder = { Text("e.g., Italian Leather Loafers") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // SKU with Auto-generate button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("SKU / Item Code") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_sku_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedButton(
                                onClick = {
                                    val prefix = category.take(2).uppercase()
                                    val rand = (100..999).random()
                                    sku = "SAF-$prefix-$rand"
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(54.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Generate SKU", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SKU", fontSize = 12.sp)
                            }
                        }

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("product_category_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Size and Color
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = size,
                                onValueChange = { size = it },
                                label = { Text("Shoe Size") },
                                placeholder = { Text("e.g. 42, 43") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_size_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = color,
                                onValueChange = { color = it },
                                label = { Text("Color") },
                                placeholder = { Text("e.g. Black, Brown") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_color_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Brand
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand / Maker") },
                            placeholder = { Text("e.g. Shehi Crafted, Clarks") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_brand_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Pricing and Profit Calculation
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pricing & Profit (₦)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Cost Price
                            OutlinedTextField(
                                value = costPriceStr,
                                onValueChange = { costPriceStr = it; validationError = null },
                                label = { Text("Cost Price ($currencySymbol)") },
                                placeholder = { Text("15000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_cost_price_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Selling Price
                            OutlinedTextField(
                                value = sellingPriceStr,
                                onValueChange = { sellingPriceStr = it; validationError = null },
                                label = { Text("Selling Price ($currencySymbol) *") },
                                placeholder = { Text("22000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_selling_price_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Live Automatic Profit Display Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (profitPerUnit >= 0) BrandGreenLight else Color(0xFFFEE2E2),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (profitPerUnit >= 0) BrandEmeraldGreen.copy(alpha = 0.5f) else ErrorRed.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Automated Profit / Pair",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (profitPerUnit >= 0) BrandEmeraldGreen else ErrorRed
                                    )
                                    Text(
                                        text = CurrencyHelper.formatNaira(profitPerUnit, currencySymbol),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (profitPerUnit >= 0) BrandEmeraldGreen else ErrorRed
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Margin",
                                        fontSize = 11.sp,
                                        color = Slate700
                                    )
                                    Text(
                                        text = String.format("%.1f%%", profitMarginPercent),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (profitPerUnit >= 0) BrandEmeraldGreen else ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Inventory & Stock
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Stock & Quantities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { quantityStr = it },
                                label = { Text("Initial Stock (Pairs)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_quantity_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = lowStockThresholdStr,
                                onValueChange = { lowStockThresholdStr = it },
                                label = { Text("Low Stock Alert (Pairs)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_low_stock_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Notes (Optional)") },
                            placeholder = { Text("e.g., Hand-stitched sole, genuine hide") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Error notice
            if (validationError != null) {
                item {
                    Text(
                        text = validationError!!,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            validationError = "Please enter a footwear product name."
                            return@Button
                        }
                        if (sellingPrice <= 0) {
                            validationError = "Selling price must be greater than zero."
                            return@Button
                        }

                        onSaveProduct(
                            initialProduct?.id ?: 0L,
                            name,
                            sku,
                            category,
                            size,
                            color,
                            brand,
                            costPrice,
                            sellingPrice,
                            quantity,
                            lowStockThreshold,
                            photoUri,
                            description
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_product_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRoyalBlue)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialProduct == null) "Save Footwear to Inventory" else "Update Footwear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
