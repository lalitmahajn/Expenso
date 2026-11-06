package com.example.expenso.ui.statement

import android.net.Uri
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseWithDetails

enum class StatementPeriod(val displayName: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_90_DAYS("Last 90 Days"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time"),
    CUSTOM("Custom Range")
}

enum class StatementFormat(val displayName: String, val extension: String, val mimeType: String) {
    PDF("PDF Document (Print / Share)", "pdf", "application/pdf"),
    CSV("CSV Spreadsheet (Excel / Sheets)", "csv", "text/csv")
}

data class StatementUiState(
    val selectedPeriod: StatementPeriod = StatementPeriod.THIS_MONTH,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val selectedAccountId: Long? = null,
    val selectedEventId: Long? = null,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedFormat: StatementFormat = StatementFormat.PDF,
    val currency: String = "$",
    val accounts: List<AccountEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val filteredExpenses: List<ExpenseWithDetails> = emptyList(),
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val isGenerating: Boolean = false,
    val generatedFileUri: Uri? = null,
    val userMessage: String? = null
)
