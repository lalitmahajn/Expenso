package com.example.expenso.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expenso.data.model.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY isArchived ASC, startDate DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isArchived = 0 ORDER BY startDate DESC")
    fun getActiveEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isArchived = 1 ORDER BY startDate DESC")
    fun getArchivedEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: Long): Flow<EventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity): Int

    @Delete
    suspend fun deleteEvent(event: EventEntity): Int

    @Query("UPDATE events SET isArchived = :isArchived WHERE id = :id")
    suspend fun setEventArchived(id: Long, isArchived: Boolean): Int

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE eventId = :eventId AND transactionType = 'EXPENSE'")
    fun getEventTotalSpent(eventId: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM expenses WHERE eventId = :eventId AND transactionType = 'EXPENSE'")
    fun getEventTransactionCount(eventId: Long): Flow<Int>
}
