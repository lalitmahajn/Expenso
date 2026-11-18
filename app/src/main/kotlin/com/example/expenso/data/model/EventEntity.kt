package com.example.expenso.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentEventId")
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetBudget: Double? = null,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isArchived: Boolean = false,
    val parentEventId: Long? = null,
    val colorHex: String? = null
)
