package com.example.expenso.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetBudget: Double? = null,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isArchived: Boolean = false
)
