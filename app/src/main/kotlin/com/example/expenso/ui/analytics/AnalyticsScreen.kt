package com.example.expenso.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expenso.data.model.CategorySpendSummary
import com.example.expenso.data.model.EventSpendSummary
import com.example.expenso.data.model.PaymentMethodSummary
import com.example.expenso.data.repository.TimePeriodFilter
import com.example.expenso.ui.common.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCustomDateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Summaries & Reports",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Analyze spending patterns across periods, categories & functions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Period Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TimePeriodFilter.entries) { period ->
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
                            if (period == TimePeriodFilter.CUSTOM && uiState.customStartTime != null && isSelected) {
                                "Custom *"
                            } else {
                                period.displayName
                            }
                        )
                    }
                )
            }
        }

        // Custom Date Range Info Badge if active
        if (uiState.selectedPeriod == TimePeriodFilter.CUSTOM && uiState.customStartTime != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Key Metrics Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Total Spend Card (Hero)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Total Period Spend",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = Formatters.formatAmount(uiState.stats.totalSpent, uiState.currency),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 3 Sub-metrics
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricCard(
                                title = "Avg / Item",
                                value = Formatters.formatAmount(uiState.stats.averageTransaction, uiState.currency),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Peak Spend",
                                value = Formatters.formatAmount(uiState.stats.highestExpense, uiState.currency),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Transactions",
                                value = "${uiState.stats.transactionCount}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Visual Composition Sparkline / Segmented Bar
                if (uiState.categoryBreakdown.isNotEmpty() && uiState.stats.totalSpent > 0) {
                    item {
                        SegmentedSpendBar(
                            categories = uiState.categoryBreakdown,
                            totalSpend = uiState.stats.totalSpent
                        )
                    }
                }

                // Category Breakdown Section
                item {
                    Text(
                        text = "Category Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.categoryBreakdown.isEmpty()) {
                    item {
                        EmptySectionCard("No expenses in this period")
                    }
                } else {
                    items(uiState.categoryBreakdown) { summary ->
                        CategorySummaryRow(
                            summary = summary,
                            totalSpend = if (uiState.stats.totalSpent > 0) uiState.stats.totalSpent else 1.0,
                            currency = uiState.currency
                        )
                    }
                }

                // Event / Function Spending
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Function / Event Allocations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.eventBreakdown.isEmpty()) {
                    item {
                        EmptySectionCard("No function/event expenses in this period")
                    }
                } else {
                    items(uiState.eventBreakdown) { summary ->
                        EventSummaryRow(
                            summary = summary,
                            totalSpend = if (uiState.stats.totalSpent > 0) uiState.stats.totalSpent else 1.0,
                            currency = uiState.currency
                        )
                    }
                }

                // Payment Mode Breakdown
                if (uiState.paymentMethodBreakdown.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Payment Mode Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.paymentMethodBreakdown) { paymentSummary ->
                        PaymentMethodRow(
                            summary = paymentSummary,
                            totalSpend = if (uiState.stats.totalSpent > 0) uiState.stats.totalSpent else 1.0,
                            currency = uiState.currency
                        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SegmentedSpendBar(
    categories: List<CategorySpendSummary>,
    totalSpend: Double
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Spending Composition",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(6).forEach { cat ->
                    val pct = ((cat.totalAmount / totalSpend) * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Formatters.parseColor(cat.colorHex))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${cat.categoryName} $pct%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategorySummaryRow(
    summary: CategorySpendSummary,
    totalSpend: Double,
    currency: String
) {
    val catColor = Formatters.parseColor(summary.colorHex)
    val percentage = ((summary.totalAmount / totalSpend) * 100).toInt()
    val progress = (summary.totalAmount / totalSpend).toFloat().coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = summary.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${summary.count})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatAmount(summary.totalAmount, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = catColor,
                trackColor = catColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun EventSummaryRow(
    summary: EventSpendSummary,
    totalSpend: Double,
    currency: String
) {
    val percentage = ((summary.totalSpent / totalSpend) * 100).toInt()
    val progress = (summary.totalSpent / totalSpend).toFloat().coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = summary.event.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatAmount(summary.totalSpent, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun PaymentMethodRow(
    summary: PaymentMethodSummary,
    totalSpend: Double,
    currency: String
) {
    val percentage = ((summary.totalAmount / totalSpend) * 100).toInt()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = summary.paymentMethod.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${summary.count}x)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${Formatters.formatAmount(summary.totalAmount, currency)} ($percentage%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptySectionCard(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
