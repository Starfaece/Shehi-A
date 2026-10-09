package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandGold
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
import com.example.ui.theme.Slate900
import com.example.util.CurrencyHelper

@Composable
fun ProductListScreen(
    products: List<ProductEntity>,
    currencySymbol: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    stockFilter: String,
    onStockFilterChange: (String) -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (ProductEntity) -> Unit,
    onDeleteProductClick: (ProductEntity) -> Unit,
    onAdjustStock: (Long, Int) -> Unit
) {
    val categories = listOf(
        "All",
        "Loafers",
        "Slippers & Half Shoes",
        "Men's Shoes",
        "Sneakers",
        "Women's",
        "Sandals & Slides",
        "Boots",
        "Kids"
    )

    val stockOptions = listOf("All", "In Stock", "Low Stock", "Out of Stock")

    var productToAdjustStock by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Filter Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_search_input"),
                    placeholder = { Text("Search shoes by name, SKU, brand...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate500
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = PureWhite
                            ),
                            modifier = Modifier.testTag("filter_chip_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Stock status chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stockOptions) { opt ->
                        val isSelected = stockFilter == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStockFilterChange(opt) },
                            label = { Text(opt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (opt) {
                                    "Low Stock" -> Color(0xFFF59E0B)
                                    "Out of Stock" -> ErrorRed
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }

            // Products Count Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${products.size} Products Listed",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Slate600
                )
                Text(
                    text = "Total Stock: ${products.sumOf { it.quantity }} pairs",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }

            // Product Cards List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Slate200,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.shehi_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Slate400
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No footwear matching criteria",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing filters or tap '+' below to add footwear to your store.",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onAddProductClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRoyalBlue)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Product")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCardItem(
                            product = product,
                            currencySymbol = currencySymbol,
                            onEditClick = { onEditProductClick(product) },
                            onDeleteClick = { productToDelete = product },
                            onAdjustStockClick = { productToAdjustStock = product }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddProductClick,
            containerColor = BrandRoyalBlue,
            contentColor = PureWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_product")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    // Stock Adjustment Dialog
    if (productToAdjustStock != null) {
        val p = productToAdjustStock!!
        var adjustAmount by remember { mutableIntStateOf(1) }

        AlertDialog(
            onDismissRequest = { productToAdjustStock = null },
            title = { Text("Adjust Stock: ${p.name}") },
            text = {
                Column {
                    Text(
                        text = "Current Stock: ${p.quantity} pairs in inventory",
                        fontSize = 14.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (adjustAmount > 1) adjustAmount-- },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$adjustAmount",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        IconButton(
                            onClick = { adjustAmount++ },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdjustStock(p.id, adjustAmount)
                        productToAdjustStock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldGreen)
                ) {
                    Text("Add +$adjustAmount Pairs")
                }
            },
            dismissButton = {
                if (p.quantity >= adjustAmount) {
                    OutlinedButton(
                        onClick = {
                            onAdjustStock(p.id, -adjustAmount)
                            productToAdjustStock = null
                        }
                    ) {
                        Text("Deduct -$adjustAmount Pairs")
                    }
                } else {
                    TextButton(onClick = { productToAdjustStock = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val p = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = {
                Text("Are you sure you want to remove '${p.name}' from inventory? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProductClick(p)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductCardItem(
    product: ProductEntity,
    currencySymbol: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAdjustStockClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.sku.ifBlank { product.id.toString() }}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail with fallback
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate200),
                contentAlignment = Alignment.Center
            ) {
                if (!product.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback brand thumbnail
                    Image(
                        painter = painterResource(id = R.drawable.shehi_logo),
                        contentDescription = "Footwear Placeholder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Out of stock overlay
                if (product.isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OUT",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Three-dot action menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Slate500
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Product") },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = BrandRoyalBlue)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Adjust Stock (+/-)") },
                                onClick = {
                                    menuExpanded = false
                                    onAdjustStockClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = BrandEmeraldGreen)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Product", color = ErrorRed) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                                }
                            )
                        }
                    }
                }

                // Category and Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                    if (product.brand.isNotBlank()) {
                        Text(text = " • ", fontSize = 11.sp, color = Slate500)
                        Text(text = product.brand, fontSize = 11.sp, color = Slate500)
                    }
                    if (product.size.isNotBlank()) {
                        Text(text = " • Sz ${product.size}", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Profit Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = CurrencyHelper.formatNaira(product.sellingPrice, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cost: ${CurrencyHelper.formatNaira(product.costPrice, currencySymbol)}",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }

                    // Stock pill badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            product.isOutOfStock -> Color(0xFFFEE2E2)
                            product.isLowStock -> Color(0xFFFEF3C7)
                            else -> BrandGreenLight
                        }
                    ) {
                        Text(
                            text = when {
                                product.isOutOfStock -> "Out of Stock"
                                product.isLowStock -> "${product.quantity} left (Low)"
                                else -> "${product.quantity} in stock"
                            },
                            color = when {
                                product.isOutOfStock -> ErrorRed
                                product.isLowStock -> Color(0xFFB45309)
                                else -> BrandEmeraldGreen
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Unit Profit badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandEmeraldGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "+${CurrencyHelper.formatNaira(product.profitPerUnit, currencySymbol)} profit / pair",
                            color = BrandEmeraldGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
