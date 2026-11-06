package com.example.expenso.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.expenso.data.model.TransactionType
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
    onSaveIncome: (title: String, amount: Double, categoryId: Long, accountId: Long?, paymentMethod: PaymentMethod, note: String, eventId: Long?) -> Unit = { _, _, _, _, _, _, _ -> },
    onSaveTransfer: (amount: Double, fromAccountId: Long, toAccountId: Long, note: String) -> Unit = { _, _, _, _ -> },
    onAddCategory: (name: String, iconName: String, colorHex: String, onCreated: (Long) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val isEditMode = initialExpense != null
    var selectedTab by remember {
        mutableStateOf(initialExpense?.expense?.transactionType ?: TransactionType.EXPENSE)
    }

    // Shared / Expense State
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Transfer Specific State
    var transferFromAccountId by remember {
        mutableStateOf<Long?>(initialExpense?.expense?.accountId ?: accounts.firstOrNull()?.id)
    }
    var transferToAccountId by remember {
        mutableStateOf<Long?>(
            initialExpense?.expense?.toAccountId
                ?: accounts.getOrNull(1)?.id
                ?: accounts.firstOrNull()?.id
        )
    }

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
                        text = if (isEditMode) "Edit Transaction" else when (selectedTab) {
                            TransactionType.EXPENSE -> "Record Expense"
                            TransactionType.INCOME -> "Record Income"
                            TransactionType.TRANSFER -> "Transfer Funds"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (selectedTab) {
                            TransactionType.EXPENSE -> "Log an outflow and debit balance"
                            TransactionType.INCOME -> "Log an inflow and credit balance"
                            TransactionType.TRANSFER -> "Move money between your accounts"
                        },
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

            // Top Segmented Tab Selector (only in create mode)
            if (!isEditMode) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TransactionTabButton(
                        selected = selectedTab == TransactionType.EXPENSE,
                        onClick = {
                            selectedTab = TransactionType.EXPENSE
                            isError = false
                            errorMessage = null
                        },
                        icon = Icons.Default.ArrowDownward,
                        label = "Expense",
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    TransactionTabButton(
                        selected = selectedTab == TransactionType.INCOME,
                        onClick = {
                            selectedTab = TransactionType.INCOME
                            isError = false
                            errorMessage = null
                        },
                        icon = Icons.Default.ArrowUpward,
                        label = "Income",
                        activeColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )

                    TransactionTabButton(
                        selected = selectedTab == TransactionType.TRANSFER,
                        onClick = {
                            selectedTab = TransactionType.TRANSFER
                            isError = false
                            errorMessage = null
                        },
                        icon = Icons.Default.SwapHoriz,
                        label = "Transfer",
                        activeColor = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab-Specific Accent Colors
            val tabAccentColor = when (selectedTab) {
                TransactionType.EXPENSE -> MaterialTheme.colorScheme.primary
                TransactionType.INCOME -> Color(0xFF2E7D32)
                TransactionType.TRANSFER -> Color(0xFF1565C0)
            }

            // Hero Amount & Merchant/Title Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    else tabAccentColor.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Amount Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prefix = when (selectedTab) {
                            TransactionType.EXPENSE -> currency
                            TransactionType.INCOME -> "+$currency"
                            TransactionType.TRANSFER -> "➔ $currency"
                        }

                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = tabAccentColor
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
                                        errorMessage = null
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
                                cursorBrush = SolidColor(tabAccentColor),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // Title / Description Row
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
                            val placeholder = when (selectedTab) {
                                TransactionType.EXPENSE -> "What was this for? (e.g. Coffee, Groceries)"
                                TransactionType.INCOME -> "Income Source (e.g. Salary, Client Payout, Bonus)"
                                TransactionType.TRANSFER -> "Reference or Note (e.g. ATM withdrawal, Card payment)"
                            }

                            if (title.isEmpty()) {
                                Text(
                                    text = placeholder,
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
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(tabAccentColor),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // CONTENT ACCORDING TO SELECTED TAB
            // ==========================================
            when (selectedTab) {
                TransactionType.EXPENSE -> {
                    // 1. Categories
                    CompactSectionHeader(title = "CATEGORY")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategoryId == category.id
                            val catColor = Formatters.parseColor(category.colorHex)
                            DenseChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = category.id },
                                label = category.name,
                                accentColor = catColor,
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                }
                            )
                        }
                        item {
                            DenseChip(
                                selected = false,
                                onClick = { showAddCategoryDialog = true },
                                label = "Add",
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Category",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Account to Debit
                    if (accounts.isNotEmpty()) {
                        CompactSectionHeader(title = "DEBIT FROM ACCOUNT")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                DenseChip(
                                    selected = selectedAccountId == null,
                                    onClick = { selectedAccountId = null },
                                    label = "None (Cash Flow Only)"
                                )
                            }
                            items(accounts) { account ->
                                val isSelected = selectedAccountId == account.id
                                val accColor = Formatters.parseColor(account.colorHex)
                                val balFormatted = Formatters.formatAmount(account.balance, currency)
                                DenseChip(
                                    selected = isSelected,
                                    onClick = { selectedAccountId = account.id },
                                    label = "${account.name} ($balFormatted)",
                                    accentColor = accColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 3. Payment Method
                    CompactSectionHeader(title = "PAYMENT METHOD")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PaymentMethod.values()) { method ->
                            DenseChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = method.displayName
                            )
                        }
                    }

                    // 4. Function / Event (Optional)
                    if (events.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CompactSectionHeader(title = "TAG TO EVENT / FUNCTION (OPTIONAL)")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                DenseChip(
                                    selected = selectedEventId == null,
                                    onClick = { selectedEventId = null },
                                    label = "General Expense"
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
                    }
                }

                TransactionType.INCOME -> {
                    // 1. Credit Into Account (Primary for Income)
                    if (accounts.isNotEmpty()) {
                        CompactSectionHeader(title = "DEPOSIT INTO ACCOUNT")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(accounts) { account ->
                                val isSelected = selectedAccountId == account.id
                                val accColor = Formatters.parseColor(account.colorHex)
                                val balFormatted = Formatters.formatAmount(account.balance, currency)
                                DenseChip(
                                    selected = isSelected,
                                    onClick = { selectedAccountId = account.id },
                                    label = "${account.name} ($balFormatted)",
                                    accentColor = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 2. Income Category
                    CompactSectionHeader(title = "INCOME SOURCE CATEGORY")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategoryId == category.id
                            val catColor = Formatters.parseColor(category.colorHex)
                            DenseChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = category.id },
                                label = category.name,
                                accentColor = catColor,
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Received Via
                    CompactSectionHeader(title = "RECEIVED VIA")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    items(
                        listOf(
                            PaymentMethod.BANK_TRANSFER to "Bank Transfer",
                            PaymentMethod.UPI to "UPI / Online",
                            PaymentMethod.CASH to "Cash",
                            PaymentMethod.CARD to "Card Payout",
                            PaymentMethod.OTHER to "Other"
                        )
                    ) { (method, label) ->
                        DenseChip(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method },
                            label = label,
                            accentColor = Color(0xFF2E7D32)
                        )
                    }
                    }

                    // 4. Function / Event (Optional)
                    if (events.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CompactSectionHeader(title = "TAG TO EVENT / FUNCTION (OPTIONAL)")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                DenseChip(
                                    selected = selectedEventId == null,
                                    onClick = { selectedEventId = null },
                                    label = "None (General Income)"
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
                    }
                }

                TransactionType.TRANSFER -> {
                    // 1. From Account (Source)
                    CompactSectionHeader(title = "FROM ACCOUNT (DEBIT)")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accounts) { account ->
                            val isSelected = transferFromAccountId == account.id
                            val accColor = Formatters.parseColor(account.colorHex)
                            val balFormatted = Formatters.formatAmount(account.balance, currency)
                            DenseChip(
                                selected = isSelected,
                                onClick = {
                                    transferFromAccountId = account.id
                                    isError = false
                                    errorMessage = null
                                },
                                label = "${account.name} ($balFormatted)",
                                accentColor = accColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. To Account (Destination)
                    CompactSectionHeader(title = "TO ACCOUNT (CREDIT)")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accounts) { account ->
                            val isSelected = transferToAccountId == account.id
                            val isSame = transferFromAccountId == account.id
                            val accColor = Formatters.parseColor(account.colorHex)
                            val balFormatted = Formatters.formatAmount(account.balance, currency)
                            DenseChip(
                                selected = isSelected,
                                onClick = {
                                    transferToAccountId = account.id
                                    isError = false
                                    errorMessage = null
                                },
                                label = if (isSame) "${account.name} (Same)" else "${account.name} ($balFormatted)",
                                accentColor = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes / Memo Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (note.isEmpty()) {
                            Text(
                                text = "Optional note or tags...",
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
                            cursorBrush = SolidColor(tabAccentColor),
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
                selectedTab == TransactionType.EXPENSE -> {
                    if (parsedAmount != null && parsedAmount > 0.0) "Record Expense • $currency$amountText"
                    else "Record Expense"
                }
                selectedTab == TransactionType.INCOME -> {
                    if (parsedAmount != null && parsedAmount > 0.0) "Record Income • +$currency$amountText"
                    else "Record Income"
                }
                selectedTab == TransactionType.TRANSFER -> {
                    if (parsedAmount != null && parsedAmount > 0.0) "Transfer Funds • $currency$amountText"
                    else "Transfer Funds"
                }
                else -> "Save"
            }

            Button(
                onClick = {
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        isError = true
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }

                    when (selectedTab) {
                        TransactionType.EXPENSE -> {
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
                        }
                        TransactionType.INCOME -> {
                            val finalTitle = title.ifBlank { "Income / Deposit" }
                            onSaveIncome(
                                finalTitle,
                                parsedAmount,
                                selectedCategoryId,
                                selectedAccountId,
                                selectedPaymentMethod,
                                note,
                                selectedEventId
                            )
                        }
                        TransactionType.TRANSFER -> {
                            if (transferFromAccountId == null || transferToAccountId == null) {
                                isError = true
                                errorMessage = "Please select both From and To accounts"
                                return@Button
                            }
                            if (transferFromAccountId == transferToAccountId) {
                                isError = true
                                errorMessage = "From and To accounts cannot be the same"
                                return@Button
                            }
                            onSaveTransfer(
                                parsedAmount,
                                transferFromAccountId!!,
                                transferToAccountId!!,
                                if (title.isNotBlank()) title else note
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tabAccentColor)
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
private fun TransactionTabButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    val contentColor = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    val elevation = if (selected) 2.dp else 0.dp

    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        color = bgColor,
        shadowElevation = elevation,
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
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
    accentColor: Color = MaterialTheme.colorScheme.primary,
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
