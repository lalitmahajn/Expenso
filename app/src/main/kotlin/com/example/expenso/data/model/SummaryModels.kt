package com.example.expenso.data.model

data class CategorySpendSummary(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val iconName: String,
    val totalAmount: Double,
    val count: Int
)

data class EventSpendSummary(
    val event: EventEntity,
    val totalSpent: Double,
    val transactionCount: Int
) {
    val budgetProgress: Float?
        get() = event.targetBudget?.let { budget ->
            if (budget > 0) (totalSpent / budget).toFloat().coerceIn(0f, 1f) else 0f
        }

    val isOverBudget: Boolean
        get() = event.targetBudget?.let { budget -> totalSpent > budget } ?: false

    val remainingBudget: Double?
        get() = event.targetBudget?.let { budget -> (budget - totalSpent).coerceAtLeast(0.0) }
}

data class PaymentMethodSummary(
    val paymentMethod: PaymentMethod,
    val totalAmount: Double,
    val count: Int
)

data class OverallStats(
    val totalSpent: Double = 0.0,
    val transactionCount: Int = 0,
    val averageTransaction: Double = 0.0,
    val highestExpense: Double = 0.0
)

enum class EventSortOption(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
}
