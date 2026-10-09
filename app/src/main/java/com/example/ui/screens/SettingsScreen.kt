package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.theme.BrandEmeraldGreen
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
import com.example.util.ReceiptHelper

@Composable
fun SettingsScreen(
    profile: BusinessProfileEntity,
    onSaveProfile: (BusinessProfileEntity) -> Unit,
    onExportBackupJson: ((String) -> Unit) -> Unit,
    onRestoreBackupJson: (String, (Boolean, String) -> Unit) -> Unit,
    onExportProductsCsv: ((String) -> Unit) -> Unit,
    onExportSalesCsv: ((String) -> Unit) -> Unit,
    onExportExpensesCsv: ((String) -> Unit) -> Unit,
    onLoadStarterCatalog: () -> Unit,
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var businessName by remember { mutableStateOf(profile.businessName) }
    var ownerName by remember { mutableStateOf(profile.ownerName) }
    var phone by remember { mutableStateOf(profile.phoneNumber) }
    var email by remember { mutableStateOf(profile.email) }
    var address by remember { mutableStateOf(profile.address) }
    var footerNote by remember { mutableStateOf(profile.receiptFooterNote) }
    var currencySymbol by remember { mutableStateOf(profile.currencySymbol) }
    var isPinEnabled by remember { mutableStateOf(profile.isPinEnabled) }
    var pinCode by remember { mutableStateOf(profile.pinCode) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showBackupSuccessDialog by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = BrandNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Business Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                    }

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business / Shop Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_business_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner / Manager") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Shop Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = footerNote,
                        onValueChange = { footerNote = it },
                        label = { Text("Receipt Footer Note") },
                        placeholder = { Text("Thank you for shopping at Shehi A. Footwears!") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("Currency Symbol") },
                        modifier = Modifier.fillMaxWidth(0.5f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val updated = profile.copy(
                                businessName = businessName.trim(),
                                ownerName = ownerName.trim(),
                                phoneNumber = phone.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                receiptFooterNote = footerNote.trim(),
                                currencySymbol = currencySymbol.trim().ifBlank { "₦" },
                                isPinEnabled = isPinEnabled,
                                pinCode = pinCode.trim()
                            )
                            onSaveProfile(updated)
                            Toast.makeText(context, "Business details saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_business_profile_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Business Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Security / PIN Lock Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BrandNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "4-Digit App Lock PIN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "Protect sales and financial reports",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { isPinEnabled = it },
                            modifier = Modifier.testTag("pin_lock_switch")
                        )
                    }

                    if (isPinEnabled) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 4) pinCode = it },
                            label = { Text("4-Digit PIN") },
                            placeholder = { Text("e.g. 1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_pin_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        TextButton(
                            onClick = {
                                if (pinCode.length == 4) {
                                    val updated = profile.copy(isPinEnabled = true, pinCode = pinCode)
                                    onSaveProfile(updated)
                                    Toast.makeText(context, "PIN updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Update PIN", color = BrandEmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Data Export & CSV Reports
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = BrandNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CSV Spreadsheet Export",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                    }

                    Text(
                        text = "Export records to Excel or Google Sheets for bookkeeping and tax records.",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onExportProductsCsv { csv ->
                                    ReceiptHelper.shareCsvContent(context, "shehi_footwears_products.csv", csv)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Products CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onExportSalesCsv { csv ->
                                    ReceiptHelper.shareCsvContent(context, "shehi_footwears_sales.csv", csv)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sales CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onExportExpensesCsv { csv ->
                                    ReceiptHelper.shareCsvContent(context, "shehi_footwears_expenses.csv", csv)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Expenses CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Local Database Backup & Restore
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = BrandNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Database Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                    }

                    Text(
                        text = "Secure local backup file preserving all footwear inventory, transactions, and settings.",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onExportBackupJson { json ->
                                    showBackupSuccessDialog = json
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_backup_button")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Catalog Management & Reset
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Data Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )

                    OutlinedButton(
                        onClick = {
                            onLoadStarterCatalog()
                            Toast.makeText(context, "Starter footwear catalog loaded!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Starter Footwear Catalog", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset / Clear All Store Data", color = ErrorRed, fontSize = 13.sp)
                    }
                }
            }
        }

        // About Shehi A. Footwears
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Shehi A. Footwears",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = "Version 1.0 • Built for Nigeria",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    Text(
                        text = "Offline-First Footwear Bookkeeping & Inventory",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Backup Success Dialog with Sharing & Copying
    if (showBackupSuccessDialog != null) {
        val json = showBackupSuccessDialog!!
        AlertDialog(
            onDismissRequest = { showBackupSuccessDialog = null },
            title = { Text("Database Backup Generated") },
            text = {
                Column {
                    Text(
                        text = "Full database backup is ready. You can copy the backup text or share it to Google Drive / WhatsApp for safekeeping.",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "shehi_footwears_backup.json")
                            putExtra(android.content.Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Save Backup File via"))
                        showBackupSuccessDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldGreen)
                ) {
                    Text("Share / Save Backup")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(json))
                        Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupSuccessDialog = null
                    }
                ) {
                    Text("Copy JSON")
                }
            }
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Database Backup") },
            text = {
                Column {
                    Text(
                        text = "Paste your previously exported JSON backup data below to restore all products, sales, and settings:",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("{\"version\": 1, ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            onRestoreBackupJson(restoreJsonText) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, "Database restored!", Toast.LENGTH_SHORT).show()
                                    showRestoreDialog = false
                                } else {
                                    Toast.makeText(context, "Failed: $msg", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRoyalBlue)
                ) {
                    Text("Restore Database")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Clear All Business Records?") },
            text = {
                Text("This will permanently remove all footwear inventory, transactions, and expenses. Are you sure you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "All records wiped.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Yes, Clear All Data")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
