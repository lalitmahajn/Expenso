package com.example.expenso.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expenso.data.model.CategorySpendSummary
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.PaymentMethodSummary
import com.example.expenso.data.repository.TimePeriodFilter
import com.example.expenso.ui.common.Formatters

enum class AnalyticsBreakdownTab(val title: String) {
    CATEGORIES("Categories"),
    EVENTS("Functions"),
    PAYMENTS("Payments")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var selectedBreakdownTab by rememberSaveable { mutableStateOf(AnalyticsBreakdownTab.CATEGORIES) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Unified Header Banner (Standardized across all screens)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Spending patterns & breakdowns",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = uiState.selectedPeriod.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Compact Time Period Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                items = TimePeriodFilter.entries,
                key = { it.name },
                contentType = { "period_filter_chip" }
            ) { period ->
                val isSelected = uiState.selectedPeriod == period
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (period == TimePeriodFilter.CUSTOM) {
                            showCustomDateDialog = true
                        } else {
                            viewModel.selectPeriod(period)
                        }
                    },
                    label = {
                        Text(
                            text = if (period == TimePeriodFilter.CUSTOM && uiState.customStartTime != null && isSelected) {
                                "Custom *"
                            } else {
                                period.displayName
                            },
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // Custom Date Range Info Badge if active
        if (uiState.selectedPeriod == TimePeriodFilter.CUSTOM && uiState.customStartTime != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${Formatters.formatDate(uiState.customStartTime ?: 0L)} – ${Formatters.formatDate(uiState.customEndTime ?: System.currentTimeMillis())}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    TextButton(
                        onClick = { showCustomDateDialog = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Consolidated 4-in-1 Financial Cockpit Card
                item {
                    ConsolidatedMetricsCard(uiState = uiState)
                }

                // 2. Interactive Segmented Breakdown Toggle (Categories | Functions | Payments)
                item {
                    SegmentedBreakdownTabSelector(
                        selectedTab = selectedBreakdownTab,
                        onSelectTab = { selectedBreakdownTab = it },
                        categoryCount = uiState.categoryBreakdown.size,
                        eventCount = uiState.eventBreakdown.size,
                        paymentCount = uiState.paymentMethodBreakdown.size
                    )
                }

                // 3. Unified Container Card for Selected Breakdown
                item {
                    when (selectedBreakdownTab) {
                        AnalyticsBreakdownTab.CATEGORIES -> {
                            CategoryBreakdownCard(
                                categories = uiState.categoryBreakdown,
                                totalSpend = uiState.stats.totalSpent,
                                currency = uiState.currency
                            )
                        }
                        AnalyticsBreakdownTab.EVENTS -> {
                            EventBreakdownCard(
                                events = uiState.eventBreakdown,
                                totalSpend = uiState.stats.totalSpent,
                                currency = uiState.currency
                            )
                        }
                        AnalyticsBreakdownTab.PAYMENTS -> {
                            PaymentBreakdownCard(
                                payments = uiState.paymentMethodBreakdown,
                                totalSpend = uiState.stats.totalSpent,
                                currency = uiState.currency
                            )
                        }
                    }
                }
            }
        }

        if (showCustomDateDialog) {
            CustomDateRangeDialog(
                onDismiss = { showCustomDateDialog = false },
                onSelectRange = { start, end ->
                    viewModel.setCustomDateRange(start, end)
                    showCustomDateDialog = false
                }
            )
        }
    }
}

@Composable
fun ConsolidatedMetricsCard(uiState: AnalyticsUiState) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Total Spend and Txn Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Period Spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Text(
                        text = Formatters.formatAmount(uiState.stats.totalSpent, uiState.currency),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${uiState.stats.transactionCount} txns",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                thickness = 0.8.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: 3 Compact Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Avg / Item",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = Formatters.formatAmount(uiState.stats.averageTransaction, uiState.currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(26.dp)
                        .padding(horizontal = 6.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                    thickness = 0.8.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Peak Spend",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = Formatters.formatAmount(uiState.stats.highestExpense, uiState.currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(26.dp)
                        .padding(horizontal = 6.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                    thickness = 0.8.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${uiState.categoryBreakdown.size} active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun SegmentedBreakdownTabSelector(
    selectedTab: AnalyticsBreakdownTab,
    onSelectTab: (AnalyticsBreakdownTab) -> Unit,
    categoryCount: Int,
    eventCount: Int,
    paymentCount: Int
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsBreakdownTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val count = when (tab) {
                    AnalyticsBreakdownTab.CATEGORIES -> categoryCount
                    AnalyticsBreakdownTab.EVENTS -> eventCount
                    AnalyticsBreakdownTab.PAYMENTS -> paymentCount
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tab.title} ($count)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryBreakdownCard(
    categories: List<CategorySpendSummary>,
    totalSpend: Double,
    currency: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses recorded in this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(modifier = Modifier.padding(12.dp)) {
                // Integrated Segmented Composition Bar
                if (totalSpend > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        categories.forEach { cat ->
                            val weight = (cat.totalAmount / totalSpend).toFloat()
                            if (weight > 0.005f) {
                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .background(Formatters.parseColor(cat.colorHex))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(6).forEach { cat ->
                            val pct = ((cat.totalAmount / totalSpend) * 100).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Formatters.parseColor(cat.colorHex))
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${cat.categoryName} $pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        thickness = 0.8.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Dense Category List
                categories.forEachIndexed { index, cat ->
                    val catColor = Formatters.parseColor(cat.colorHex)
                    val effectiveSpend = if (totalSpend > 0) totalSpend else 1.0
                    val percentage = ((cat.totalAmount / effectiveSpend) * 100).toInt()
                    val progress = (cat.totalAmount / effectiveSpend).toFloat().coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
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
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.categoryName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "(${cat.count})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Formatters.formatAmount(cat.totalAmount, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            color = catColor,
                            trackColor = catColor.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    if (index < categories.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventBreakdownCard(
    events: List<EventSpendSummary>,
    totalSpend: Double,
    currency: String
) {
    var expandedEventIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No function/event expenses in this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(modifier = Modifier.padding(12.dp)) {
                val effectiveSpend = if (totalSpend > 0) totalSpend else 1.0
                events.forEachIndexed { index, ev ->
                    val percentage = ((ev.totalSpent / effectiveSpend) * 100).toInt()
                    val progress = (ev.totalSpent / effectiveSpend).toFloat().coerceIn(0f, 1f)
                    val evColor = ev.event.colorHex?.let { Formatters.parseColor(it) } ?: MaterialTheme.colorScheme.primary
                    val isExpanded = expandedEventIds.contains(ev.event.id)
                    val hasSubEvents = ev.hasSubEvents
                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "eventChevron_${ev.event.id}"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        // Parent Event Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (hasSubEvents) {
                                        Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                expandedEventIds = if (isExpanded) {
                                                    expandedEventIds - ev.event.id
                                                } else {
                                                    expandedEventIds + ev.event.id
                                                }
                                            }
                                            .padding(vertical = 2.dp)
                                    } else Modifier
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = evColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ev.event.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "(${ev.transactionCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )

                                if (hasSubEvents) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = evColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${ev.subEventCount} sub-events",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = evColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = evColor,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .graphicsLayer { rotationZ = rotation }
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Formatters.formatAmount(ev.totalSpent, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            color = evColor,
                            trackColor = evColor.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )

                        // Expandable Sub-events Breakdown
                        if (hasSubEvents) {
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        val parentTotal = if (ev.totalSpent > 0) ev.totalSpent else 1.0

                                        // Direct / General expenses on the parent event (if any)
                                        if (ev.directSpent > 0) {
                                            val directCount = ev.transactionCount - ev.subEventSummaries.sumOf { it.transactionCount }
                                            val directPct = ((ev.directSpent / parentTotal) * 100).toInt()
                                            val directProgress = (ev.directSpent / parentTotal).toFloat().coerceIn(0f, 1f)

                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(evColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "General / Main Event",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "($directCount)",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 10.sp
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = Formatters.formatAmount(ev.directSpent, currency),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "$directPct%",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(3.dp))
                                                LinearProgressIndicator(
                                                    progress = { directProgress },
                                                    color = evColor,
                                                    trackColor = evColor.copy(alpha = 0.12f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(3.dp)
                                                        .clip(RoundedCornerShape(1.5.dp))
                                                )
                                            }

                                            if (ev.subEventSummaries.isNotEmpty()) {
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                    thickness = 0.5.dp,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // Child Sub-events
                                        ev.subEventSummaries.forEachIndexed { subIndex, sub ->
                                            val subColor = sub.event.colorHex?.let { Formatters.parseColor(it) } ?: evColor
                                            val subPct = ((sub.totalSpent / parentTotal) * 100).toInt()
                                            val subProgress = (sub.totalSpent / parentTotal).toFloat().coerceIn(0f, 1f)

                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(subColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = sub.event.name,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "(${sub.transactionCount})",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 10.sp
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = Formatters.formatAmount(sub.totalSpent, currency),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "$subPct%",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(3.dp))
                                                LinearProgressIndicator(
                                                    progress = { subProgress },
                                                    color = subColor,
                                                    trackColor = subColor.copy(alpha = 0.12f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(3.dp)
                                                        .clip(RoundedCornerShape(1.5.dp))
                                                )
                                            }

                                            if (subIndex < ev.subEventSummaries.size - 1) {
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                    thickness = 0.5.dp,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (index < events.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentBreakdownCard(
    payments: List<PaymentMethodSummary>,
    totalSpend: Double,
    currency: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No payment records in this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(modifier = Modifier.padding(12.dp)) {
                val effectiveSpend = if (totalSpend > 0) totalSpend else 1.0
                payments.forEachIndexed { index, pmt ->
                    val percentage = ((pmt.totalAmount / effectiveSpend) * 100).toInt()
                    val progress = (pmt.totalAmount / effectiveSpend).toFloat().coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pmt.paymentMethod.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "(${pmt.count} txns)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Formatters.formatAmount(pmt.totalAmount, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    if (index < payments.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onSelectRange: (Long, Long) -> Unit
) {
    val now = System.currentTimeMillis()
    val oneDay = 86_400_000L

    val presets = listOf(
        Pair("Last 7 Days", 7),
        Pair("Last 14 Days", 14),
        Pair("Last 30 Days", 30),
        Pair("Last 60 Days", 60),
        Pair("Last 90 Days", 90),
        Pair("Last 6 Months", 180),
        Pair("Last 1 Year", 365)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Custom Time Range", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose a duration preset to aggregate financial reports and summaries:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (label, days) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val start = now - (days * oneDay)
                                onSelectRange(start, now)
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
