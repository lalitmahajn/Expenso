package com.example.expenso.ui.events

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expenso.data.model.EventSortOption
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.ui.common.Formatters
import com.example.expenso.ui.expenses.AddExpenseBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventDetailScreen(
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // System Back Gesture/Button
    BackHandler {
        onBack()
    }

    val summary = uiState.selectedEventSummary
    val event = summary?.event

    if (summary == null || event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Function not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = event.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${Formatters.formatAmount(summary.totalSpent, uiState.currency)} • ${summary.transactionCount} items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Function",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. DENSE UNIFIED BUDGET & METRICS BANNER
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (event.description.isNotBlank()) {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // 4 Metrics in a Single Dense Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DenseStatCell("Spent", Formatters.formatAmount(summary.totalSpent, uiState.currency), isHighlight = true)
                            if (event.targetBudget != null && event.targetBudget > 0) {
                                DenseStatCell("Budget", Formatters.formatAmount(event.targetBudget, uiState.currency))
                                val remaining = summary.remainingBudget ?: 0.0
                                val isOver = summary.isOverBudget
                                DenseStatCell(
                                    label = if (isOver) "Over" else "Left",
                                    value = Formatters.formatAmount(if (isOver) summary.totalSpent - event.targetBudget else remaining, uiState.currency),
                                    textColor = if (isOver) MaterialTheme.colorScheme.error else Color(0xFF00796B)
                                )
                            }
                            DenseStatCell("Avg/Item", Formatters.formatAmount(uiState.stats.averageTransaction, uiState.currency))
                            DenseStatCell("Highest", Formatters.formatAmount(uiState.stats.highestExpense, uiState.currency))
                        }

                        // Slim Progress Bar (if budgeted)
                        if (event.targetBudget != null && event.targetBudget > 0) {
                            val progress = summary.budgetProgress ?: 0f
                            val isOver = summary.isOverBudget
                            val percentSpent = ((summary.totalSpent / event.targetBudget) * 100).toInt()

                            Spacer(modifier = Modifier.height(6.dp))
                            val barColor = when {
                                isOver -> MaterialTheme.colorScheme.error
                                progress > 0.85f -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = barColor.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$percentSpent% budget consumed",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isOver) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "OVER BUDGET",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. DENSE CATEGORY & PAYMENT PILLS (CATEGORIZATION ANALYSIS)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Category Breakdown Chips
                    if (uiState.categoryBreakdowns.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FilterChip(
                                selected = uiState.filterCategoryId == null,
                                onClick = { viewModel.setDetailCategory(null) },
                                label = { Text("All (${summary.transactionCount})", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
                            )

                            uiState.categoryBreakdowns.forEach { cat ->
                                val isSelected = uiState.filterCategoryId == cat.categoryId
                                val catColor = Formatters.parseColor(cat.colorHex)
                                val pct = if (summary.totalSpent > 0) ((cat.totalAmount / summary.totalSpent) * 100).toInt() else 0

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDetailCategory(cat.categoryId) },
                                    label = {
                                        Text(
                                            text = "${cat.categoryName} $pct%",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                    },
                                    modifier = Modifier.height(28.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = catColor.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    // Payment Method Breakdown Chips
                    if (uiState.paymentBreakdowns.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FilterChip(
                                selected = uiState.filterPaymentMethod == null,
                                onClick = { viewModel.setDetailPaymentMethod(null) },
                                label = { Text("All", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )

                            uiState.paymentBreakdowns.forEach { pm ->
                                val isSelected = uiState.filterPaymentMethod == pm.paymentMethod
                                val pct = if (summary.totalSpent > 0) ((pm.totalAmount / summary.totalSpent) * 100).toInt() else 0

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDetailPaymentMethod(pm.paymentMethod) },
                                    label = {
                                        Text(
                                            text = "${pm.paymentMethod.displayName} $pct%",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. DENSE SEARCH & SORT BAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setDetailSearchQuery(it) },
                        placeholder = { Text("Search items...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = if (uiState.searchQuery.isNotBlank()) {
                            {
                                IconButton(
                                    onClick = { viewModel.setDetailSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                }
                            }
                        } else null,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Sort Chips in a compact scroll
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EventSortOption.values().forEach { option ->
                            val isSelected = uiState.sortOption == option
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDetailSortOption(option) },
                                label = { Text(option.label, fontSize = 11.sp) },
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }
            }

            // 4. FILTER SUMMARY LINE
            item {
                val hasActiveFilters = uiState.searchQuery.isNotBlank() ||
                    uiState.filterCategoryId != null ||
                    uiState.filterPaymentMethod != null ||
                    uiState.sortOption != EventSortOption.NEWEST

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions (${uiState.filteredExpenses.size} of ${uiState.selectedEventExpenses.size}) • ${Formatters.formatAmount(uiState.filteredExpenses.sumOf { it.expense.amount }, uiState.currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (hasActiveFilters) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.clearDetailFilters() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // 5. HIGH-DENSITY TRANSACTIONS LIST
            if (uiState.filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions match filters",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredExpenses, key = { it.expense.id }) { item ->
                    DenseExpenseRow(
                        item = item,
                        currency = uiState.currency,
                        onDelete = { viewModel.deleteExpense(item.expense) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }

    // Add Expense Sheet with this event pre-selected
    if (showAddExpenseSheet) {
        AddExpenseBottomSheet(
            sheetState = addSheetState,
            categories = uiState.allCategories,
            events = listOf(event),
            accounts = uiState.allAccounts,
            currency = uiState.currency,
            onDismiss = {
                scope.launch { addSheetState.hide() }.invokeOnCompletion {
                    showAddExpenseSheet = false
                }
            },
            onSave = { title, amount, categoryId, _, paymentMethod, note, accountId ->
                viewModel.addExpenseToEvent(title, amount, categoryId, paymentMethod, note, accountId)
                scope.launch { addSheetState.hide() }.invokeOnCompletion {
                    showAddExpenseSheet = false
                }
            },
            onAddCategory = { name, iconName, colorHex, callback ->
                viewModel.addCategory(name, iconName, colorHex, callback)
            }
        )
    }

    // Delete Event Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete '${event.name}'?") },
            text = {
                Text("Delete this function? Expenses linked to it will remain in your transaction history.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent(event)
                        showDeleteConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DenseStatCell(
    label: String,
    value: String,
    textColor: Color = Color.Unspecified,
    isHighlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (isHighlight) 14.sp else 13.sp,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DenseExpenseRow(
    item: ExpenseWithDetails,
    currency: String,
    onDelete: () -> Unit
) {
    val category = item.category
    val catColor = category?.let { Formatters.parseColor(it.colorHex) } ?: Color(0xFF607D8B)
    val icon = category?.let { Formatters.getCategoryIcon(it.iconName) } ?: Icons.Default.ReceiptLong

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini Icon Circle
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(catColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title and Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.expense.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${category?.name ?: "Expense"} • ${item.expense.paymentMethod.displayName} • ${Formatters.formatTime(item.expense.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Delete
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatAmount(item.expense.amount, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
