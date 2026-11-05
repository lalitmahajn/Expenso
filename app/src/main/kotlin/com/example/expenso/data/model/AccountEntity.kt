package com.example.expenso.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType = AccountType.BANK,
    val balance: Double = 0.0,
    val colorHex: String = "#2196F3",
    val iconName: String = "AccountBalance",
    val accountNumberLast4: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
