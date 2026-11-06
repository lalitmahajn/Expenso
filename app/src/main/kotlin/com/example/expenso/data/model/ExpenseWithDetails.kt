package com.example.expenso.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithDetails(
    @Embedded
    val expense: ExpenseEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "eventId",
        entityColumn = "id"
    )
    val event: EventEntity?,

    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity? = null,

    @Relation(
        parentColumn = "toAccountId",
        entityColumn = "id"
    )
    val toAccount: AccountEntity? = null
)
