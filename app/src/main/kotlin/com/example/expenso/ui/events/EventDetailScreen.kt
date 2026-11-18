package com.example.expenso.ui.events

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.EventSortOption
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.ui.common.Formatters
import com.example.expenso.ui.expenses.AddExpenseBottomSheet
import com.example.expenso.ui.expenses.ExpenseCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventDetailScreen(
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseWithDetails?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showAddSubEventDialog by remember { mutableStateOf(false) }
    var showEditEventDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

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

    val isSubEvent = event.parentEventId != null
    val parentEvent = uiState.selectedParentEvent
    var isSubEventsExpanded by rememberSaveable(event.id) { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSubEvent) {
                                val dotColor = event.colorHex?.let { Formatters.parseColor(it) }
                                    ?: MaterialTheme.colorScheme.primary
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = event.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val prefix = if (isSubEvent && parentEvent != null) "Part of ${parentEvent.name} • " else ""
                        val subEventSubtitle = if (!isSubEvent && summary.hasSubEvents) " • ${summary.subEventCount} sub-events" else ""
                        Text(
                            text = "$prefix${Formatters.formatAmount(summary.totalSpent, uiState.currency)} • ${summary.transactionCount} items$subEventSubtitle",
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
                    IconButton(onClick = { showEditEventDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (isSubEvent) "Edit Sub-Event" else "Edit Event",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleArchiveEvent(event) }) {
                        Icon(
                            imageVector = if (event.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = if (event.isArchived) "Unarchive Function" else "Archive Function",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
                onClick = {
                    expenseToEdit = null
                    showAddExpenseSheet = true
                },
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
            contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. ARCHIVED STATUS BANNER (If archived)
            if (event.isArchived) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "This event is marked as Completed / Archived",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            TextButton(
                                onClick = { viewModel.toggleArchiveEvent(event) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text("Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. DEDICATED SUB-EVENTS SECTION (For Parent Events) OR PARENT EVENT BREADCRUMB
            if (!isSubEvent) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { isSubEventsExpanded = !isSubEventsExpanded }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sub-Events",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${summary.subEventCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val rotation by animateFloatAsState(
                                        targetValue = if (isSubEventsExpanded) 180f else 0f,
                                        label = "subEventChevron"
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isSubEventsExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .graphicsLayer { rotationZ = rotation }
                                    )
                                }

                                Button(
                                    onClick = { showAddSubEventDialog = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Sub-Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            AnimatedVisibility(
                                visible = isSubEventsExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    if (summary.subEventSummaries.isEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Event,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Divide this event into sub-events (e.g. phases, legs, or days) with dedicated mini-budgets.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            summary.subEventSummaries.forEach { cSummary ->
                                                SubEventCard(
                                                    summary = cSummary,
                                                    currency = uiState.currency,
                                                    onClick = { viewModel.selectEvent(cSummary.event.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (parentEvent != null) {
                // Parent Event Quick-Back Breadcrumb Card
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectEvent(parentEvent.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Sub-Event of ${parentEvent.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Tap to view parent event summary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 3. CLEAN SINGLE-LINE SEARCH, SORT & FILTER BAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact Sleek Search Field
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setDetailSearchQuery(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search transactions...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setDetailSearchQuery("") },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sort Dropdown Button
                    var showSortMenu by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .height(44.dp)
                                .clickable { showSortMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.sortOption.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            EventSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setDetailSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Filter Button
                    val activeFilterCount = (if (uiState.filterCategoryId != null) 1 else 0) +
                        (if (uiState.filterPaymentMethod != null) 1 else 0) +
                        (if (uiState.filterSubEventId != null) 1 else 0)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { showFilterSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgedBox(
                                badge = {
                                    if (activeFilterCount > 0) {
                                        Badge { Text("$activeFilterCount") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filters",
                                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (activeFilterCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Filters",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // 4. ACTIVE FILTER CHIPS (Shown only when at least one filter is applied)
            val hasActiveFilterPills = uiState.filterCategoryId != null ||
                uiState.filterPaymentMethod != null ||
                uiState.filterSubEventId != null

            if (hasActiveFilterPills) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.filterSubEventId != null) {
                            val subEventName = if (uiState.filterSubEventId == -1L) "General Event"
                            else summary.subEventSummaries.find { it.event.id == uiState.filterSubEventId }?.event?.name ?: "Sub-Event"
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setDetailSubEvent(null) },
                                label = { Text(subEventName, fontSize = 11.sp) },
                                trailingIcon = {
                                    Icon(Icons.Default.Clear, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        if (uiState.filterCategoryId != null) {
                            val catName = uiState.categoryBreakdowns.find { it.categoryId == uiState.filterCategoryId }?.categoryName ?: "Category"
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setDetailCategory(null) },
                                label = { Text(catName, fontSize = 11.sp) },
                                trailingIcon = {
                                    Icon(Icons.Default.Clear, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        if (uiState.filterPaymentMethod != null) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setDetailPaymentMethod(null) },
                                label = { Text(uiState.filterPaymentMethod!!.displayName, fontSize = 11.sp) },
                                trailingIcon = {
                                    Icon(Icons.Default.Clear, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.setDetailSubEvent(null)
                                viewModel.setDetailCategory(null)
                                viewModel.setDetailPaymentMethod(null)
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Clear all", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // 5. FILTER SUMMARY LINE
            item {
                val hasActiveFilters = uiState.searchQuery.isNotBlank() ||
                    uiState.filterCategoryId != null ||
                    uiState.filterPaymentMethod != null ||
                    uiState.filterSubEventId != null ||
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

            // 6. HIGH-DENSITY TRANSACTIONS LIST
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
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
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
                items(
                    items = uiState.filteredExpenses,
                    key = { it.expense.id },
                    contentType = { "expense_card" }
                ) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                expenseToDelete = item.expense
                                false
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Delete",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    ) {
                        val subEvent = if (item.expense.eventId != event.id) {
                            summary.subEventSummaries.find { it.event.id == item.expense.eventId }?.event
                        } else null

                        ExpenseCard(
                            item = item,
                            currency = uiState.currency,
                            subEvent = subEvent,
                            showEventBadge = false,
                            onClick = {
                                expenseToEdit = item
                                showAddExpenseSheet = true
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }

    // Add Expense Sheet with this event and sub-events pre-loaded
    if (showAddExpenseSheet) {
        val sheetEvents = remember(event, summary.subEventSummaries, uiState.allEvents) {
            if (isSubEvent) {
                val parent = uiState.allEvents.find { it.id == event.parentEventId }
                if (parent != null) listOf(parent, event) else listOf(event)
            } else {
                listOf(event) + summary.subEventSummaries.map { it.event }
            }
        }
        AddExpenseBottomSheet(
            sheetState = addSheetState,
            categories = uiState.allCategories,
            events = sheetEvents,
            accounts = uiState.allAccounts,
            currency = uiState.currency,
            initialExpense = expenseToEdit,
            preselectedEventId = event.id,
            onDismiss = {
                scope.launch { addSheetState.hide() }.invokeOnCompletion {
                    showAddExpenseSheet = false
                    expenseToEdit = null
                }
            },
            onSave = { title, amount, categoryId, targetEventId, paymentMethod, note, accountId ->
                val editing = expenseToEdit
                val finalEventId = if (editing != null) targetEventId else (targetEventId ?: event.id)
                if (editing != null) {
                    viewModel.updateEventExpense(editing.expense, title, amount, categoryId, paymentMethod, note, accountId, eventId = finalEventId)
                } else {
                    viewModel.addExpenseToEvent(title, amount, categoryId, paymentMethod, note, accountId, eventId = finalEventId)
                }
                scope.launch { addSheetState.hide() }.invokeOnCompletion {
                    showAddExpenseSheet = false
                    expenseToEdit = null
                }
            },
            onDelete = { expense ->
                scope.launch { addSheetState.hide() }.invokeOnCompletion {
                    showAddExpenseSheet = false
                    expenseToEdit = null
                    viewModel.deleteExpense(expense)
                }
            },
            onAddCategory = { name, iconName, colorHex, callback ->
                viewModel.addCategory(name, iconName, colorHex, callback)
            }
        )
    }

    // Add Sub-Event Dialog
    if (showAddSubEventDialog) {
        AddSubEventDialog(
            currency = uiState.currency,
            onDismiss = { showAddSubEventDialog = false },
            onConfirm = { name, targetBudget, date, colorHex ->
                viewModel.createSubEvent(
                    parentEventId = event.id,
                    name = name,
                    targetBudget = targetBudget,
                    startDate = date,
                    colorHex = colorHex
                )
                isSubEventsExpanded = true
                showAddSubEventDialog = false
            }
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        EventFilterBottomSheet(
            uiState = uiState,
            summary = summary,
            onSelectSubEvent = { viewModel.setDetailSubEvent(it) },
            onSelectCategory = { viewModel.setDetailCategory(it) },
            onSelectPayment = { viewModel.setDetailPaymentMethod(it) },
            onResetAll = {
                viewModel.setDetailSubEvent(null)
                viewModel.setDetailCategory(null)
                viewModel.setDetailPaymentMethod(null)
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    // Edit Event / Sub-Event Dialog
    if (showEditEventDialog) {
        EditEventDialog(
            event = event,
            currency = uiState.currency,
            onDismiss = { showEditEventDialog = false },
            onConfirm = { newName, newBudget, newColor ->
                viewModel.updateEvent(event, newName, newBudget, colorHex = newColor)
                showEditEventDialog = false
            }
        )
    }

    // Delete Event Confirmation Dialog (Option B Explicit Cascade Warning)
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text(if (isSubEvent) "Delete Sub-Event '${event.name}'?" else "Delete '${event.name}'?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (isSubEvent) {
                            "This will permanently delete the sub-event '${event.name}' and all expenses logged to it."
                        } else {
                            "This will permanently delete '${event.name}' along with all of its child sub-events and all associated expenses."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Account balances will be automatically restored (Option B Cascade). This action cannot be undone.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
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
                    Text(if (isSubEvent) "Delete Sub-Event" else "Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Swipe-to-Delete Expense Confirmation Dialog
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete Expense?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = expense.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatAmount(expense.amount, uiState.currency),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This item will be permanently removed and your event total and account balance will be automatically recalculated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(expense)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { expenseToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubEventCard(
    summary: EventSpendSummary,
    currency: String,
    onClick: () -> Unit
) {
    val subEvent = summary.event
    val accent = subEvent.colorHex?.let { Formatters.parseColor(it) } ?: MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subEvent.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${summary.transactionCount} items",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Open Sub-Event",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Spend & Mini-budget info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Spent: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatAmount(summary.totalSpent, currency),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                if (subEvent.targetBudget != null && subEvent.targetBudget > 0) {
                    val remaining = summary.remainingBudget ?: 0.0
                    val isOver = summary.isOverBudget
                    Text(
                        text = if (isOver) "Over: ${Formatters.formatAmount(summary.totalSpent - subEvent.targetBudget, currency)}"
                               else "Left: ${Formatters.formatAmount(remaining, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) MaterialTheme.colorScheme.error else Color(0xFF00796B)
                    )
                } else {
                    Text(
                        text = "No limit set",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mini Progress Bar if budget set
            if (subEvent.targetBudget != null && subEvent.targetBudget > 0) {
                val progress = summary.budgetProgress ?: 0f
                val isOver = summary.isOverBudget
                val barColor = when {
                    isOver -> MaterialTheme.colorScheme.error
                    progress > 0.8f -> Color(0xFFFF9800)
                    else -> accent
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = barColor,
                    trackColor = barColor.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target: ${Formatters.formatAmount(subEvent.targetBudget, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${((summary.totalSpent / subEvent.targetBudget) * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EditEventDialog(
    event: EventEntity,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetBudget: Double?, colorHex: String?) -> Unit
) {
    var name by remember { mutableStateOf(event.name) }
    var budgetText by remember {
        mutableStateOf(
            event.targetBudget?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    val colors = listOf(
        "#FF9800", // Orange
        "#E91E63", // Rose Pink
        "#9C27B0", // Purple
        "#4CAF50", // Green
        "#2196F3", // Royal Blue
        "#00BCD4", // Cyan
        "#F44336", // Crimson Red
        "#795548"  // Earthy Brown
    )
    var selectedColor by remember { mutableStateOf(event.colorHex ?: colors[0]) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (event.parentEventId != null) "Edit Sub-Event" else "Edit Event",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Name") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Budget Limit ($currency) - Optional") },
                    placeholder = { Text("e.g. 5000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (event.parentEventId != null) {
                    Text(
                        text = "Theme Accent Color",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { hex ->
                            val col = Formatters.parseColor(hex)
                            val isSel = selectedColor.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSel) 2.5.dp else 1.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                        return@Button
                    }
                    val target = budgetText.toDoubleOrNull()
                    val finalColor = if (event.parentEventId != null) selectedColor else event.colorHex
                    onConfirm(name, target, finalColor)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSubEventDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetBudget: Double?, date: Long, colorHex: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    val colors = listOf(
        "#FF9800", // Orange
        "#E91E63", // Rose Pink
        "#9C27B0", // Purple
        "#4CAF50", // Green
        "#2196F3", // Royal Blue
        "#00BCD4", // Cyan
        "#F44336", // Crimson Red
        "#795548"  // Earthy Brown
    )
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Sub-Event",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (isError) isError = false
                    },
                    label = { Text("Sub-Event Name *") },
                    placeholder = { Text("e.g. Day 1, North Leg, Painting, Reception") },
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) { { Text("Name is required") } } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Target Mini-Budget (Optional)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text(currency) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        val color = Formatters.parseColor(hex)
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                        return@Button
                    }
                    val budget = budgetText.toDoubleOrNull()
                    onConfirm(name.trim(), budget, System.currentTimeMillis(), selectedColor)
                }
            ) {
                Text("Add Sub-Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFilterBottomSheet(
    uiState: EventsUiState,
    summary: EventSpendSummary,
    onSelectSubEvent: (Long?) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onSelectPayment: (PaymentMethod?) -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onResetAll) {
                    Text("Reset All", fontWeight = FontWeight.Bold)
                }
            }

            // Sub-Events (if parent event has sub-events)
            if (summary.hasSubEvents) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sub-Event",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterSubEventId == null,
                            onClick = { onSelectSubEvent(null) },
                            label = { Text("All (${summary.transactionCount})") }
                        )
                        FilterChip(
                            selected = uiState.filterSubEventId == -1L,
                            onClick = { onSelectSubEvent(-1L) },
                            label = { Text("General Event") }
                        )
                        summary.subEventSummaries.forEach { cSummary ->
                            val isSelected = uiState.filterSubEventId == cSummary.event.id
                            val accent = cSummary.event.colorHex?.let { Formatters.parseColor(it) }
                                ?: MaterialTheme.colorScheme.primary
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectSubEvent(cSummary.event.id) },
                                label = { Text("${cSummary.event.name} (${cSummary.transactionCount})") },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Category Breakdown
            if (uiState.categoryBreakdowns.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterCategoryId == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text("All Categories") }
                        )
                        uiState.categoryBreakdowns.forEach { cat ->
                            val isSelected = uiState.filterCategoryId == cat.categoryId
                            val catColor = Formatters.parseColor(cat.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(cat.categoryId) },
                                label = { Text(cat.categoryName) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Payment Method
            if (uiState.paymentBreakdowns.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterPaymentMethod == null,
                            onClick = { onSelectPayment(null) },
                            label = { Text("All Methods") }
                        )
                        uiState.paymentBreakdowns.forEach { pm ->
                            val isSelected = uiState.filterPaymentMethod == pm.paymentMethod
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectPayment(pm.paymentMethod) },
                                label = { Text(pm.paymentMethod.displayName) }
                            )
                        }
                    }
                }
            }

            // Apply Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Show Results (${uiState.filteredExpenses.size})", fontWeight = FontWeight.Bold)
            }
        }
    }
}
