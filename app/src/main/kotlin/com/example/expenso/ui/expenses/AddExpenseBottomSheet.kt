package com.example.expenso.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.ui.common.AddCategoryDialog
import com.example.expenso.ui.common.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    sheetState: SheetState,
    categories: List<CategoryEntity>,
    events: List<EventEntity>,
    accounts: List<AccountEntity> = emptyList(),
    currency: String,
    initialExpense: ExpenseWithDetails? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, categoryId: Long, eventId: Long?, paymentMethod: PaymentMethod, note: String, accountId: Long?) -> Unit,
    onAddCategory: (name: String, iconName: String, colorHex: String, onCreated: (Long) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val isEditMode = initialExpense != null
    var title by remember { mutableStateOf(initialExpense?.expense?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            initialExpense?.expense?.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var selectedCategoryId by remember {
        mutableStateOf(initialExpense?.expense?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var selectedEventId by remember {
        mutableStateOf<Long?>(initialExpense?.expense?.eventId ?: events.firstOrNull()?.takeIf { events.size == 1 }?.id)
    }
    var selectedAccountId by remember {
        mutableStateOf<Long?>(initialExpense?.expense?.accountId ?: accounts.firstOrNull()?.id)
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(initialExpense?.expense?.paymentMethod ?: PaymentMethod.CASH)
    }
    var note by remember { mutableStateOf(initialExpense?.expense?.note ?: "") }
    var isError by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditMode) "Edit Expense" else "New Expense",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quick transaction record",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Modern Hero Amount & Merchant Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Amount Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            if (amountText.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    )
                                )
                            }
                            BasicTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() || it == '.' }
                                    if (filtered.count { it == '.' } <= 1) {
                                        amountText = filtered
                                        isError = false
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // Title / Merchant Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            if (title.isEmpty()) {
                                Text(
                                    text = "What was this for? (e.g. Coffee, Uber)",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (isError) {
                Text(
                    text = "Please enter an amount greater than 0",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CATEGORY SECTION (Single Compact Horizontal LazyRow)
            CompactSectionHeader(title = "CATEGORY")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryId == category.id
                    val catColor = Formatters.parseColor(category.colorHex)
                    DenseChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = category.id },
                        accentColor = catColor,
                        label = category.name,
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                        }
                    )
                }

                item {
                    // + Custom Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { showAddCategoryDialog = true }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACCOUNT SECTION (If accounts exist)
            if (accounts.isNotEmpty()) {
                CompactSectionHeader(title = "DEBIT ACCOUNT")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        DenseChip(
                            selected = selectedAccountId == null,
                            onClick = { selectedAccountId = null },
                            label = "None"
                        )
                    }
                    items(accounts) { account ->
                        val isSelected = selectedAccountId == account.id
                        val accColor = Formatters.parseColor(account.colorHex)
                        DenseChip(
                            selected = isSelected,
                            onClick = { selectedAccountId = account.id },
                            accentColor = accColor,
                            label = "${account.name} • ${Formatters.formatAmount(account.balance, currency)}",
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(accColor)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // PAYMENT METHOD SECTION
            CompactSectionHeader(title = "PAYMENT METHOD")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(PaymentMethod.entries) { method ->
                    DenseChip(
                        selected = selectedPaymentMethod == method,
                        onClick = { selectedPaymentMethod = method },
                        label = method.displayName
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // OCCASION / EVENT SECTION
            CompactSectionHeader(title = "EVENT / OCCASION")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    DenseChip(
                        selected = selectedEventId == null,
                        onClick = { selectedEventId = null },
                        label = "General Daily"
                    )
                }
                items(events) { event ->
                    DenseChip(
                        selected = selectedEventId == event.id,
                        onClick = { selectedEventId = event.id },
                        label = event.name
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compact Note Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (note.isEmpty()) {
                            Text(
                                text = "Optional note or receipt tag...",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button with live amount preview
            val parsedAmount = amountText.toDoubleOrNull()
            val buttonText = when {
                isEditMode -> "Save Changes"
                parsedAmount != null && parsedAmount > 0.0 -> "Record Expense • $currency$amountText"
                else -> "Record Expense"
            }

            Button(
                onClick = {
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        isError = true
                        return@Button
                    }
                    val finalTitle = title.ifBlank {
                        categories.find { it.id == selectedCategoryId }?.name ?: "Expense"
                    }
                    onSave(
                        finalTitle,
                        parsedAmount,
                        selectedCategoryId,
                        selectedEventId,
                        selectedPaymentMethod,
                        note,
                        selectedAccountId
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onCategoryCreated = { name, iconName, colorHex ->
                    onAddCategory(name, iconName, colorHex) { newId ->
                        selectedCategoryId = newId
                        showAddCategoryDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun CompactSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DenseChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val backgroundColor = if (selected) {
        accentColor.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val borderColor = if (selected) {
        accentColor
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val textColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            leadingContent?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}
