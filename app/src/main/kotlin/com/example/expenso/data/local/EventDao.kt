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

    @Query("SELECT * FROM events WHERE parentEventId IS NULL ORDER BY isArchived ASC, startDate DESC")
    fun getParentEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE parentEventId IS NULL AND isArchived = 0 ORDER BY startDate DESC")
    fun getActiveParentEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE parentEventId IS NULL AND isArchived = 1 ORDER BY startDate DESC")
    fun getArchivedParentEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE parentEventId = :parentEventId ORDER BY startDate ASC, id ASC")
    fun getSubEvents(parentEventId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE parentEventId = :parentEventId ORDER BY startDate ASC, id ASC")
    suspend fun getSubEventsSync(parentEventId: Long): List<EventEntity>

    @Query("SELECT id FROM events WHERE parentEventId = :parentEventId")
    suspend fun getSubEventIds(parentEventId: Long): List<Long>

    @Query("SELECT * FROM events WHERE parentEventId IS NOT NULL ORDER BY startDate ASC")
    fun getAllSubEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdSync(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>): List<Long>

    @Query("SELECT * FROM events ORDER BY id ASC")
    suspend fun getAllEventsSync(): List<EventEntity>

    @Update
    suspend fun updateEvent(event: EventEntity): Int

    @Delete
    suspend fun deleteEvent(event: EventEntity): Int

    @Query("DELETE FROM events WHERE id = :eventId OR parentEventId = :eventId")
    suspend fun deleteEventAndChildren(eventId: Long): Int

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents(): Int

    @Query("UPDATE events SET isArchived = :isArchived WHERE id = :id")
    suspend fun setEventArchived(id: Long, isArchived: Boolean): Int

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE eventId = :eventId AND transactionType = 'EXPENSE'")
    fun getEventTotalSpent(eventId: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM expenses WHERE eventId = :eventId AND transactionType = 'EXPENSE'")
    fun getEventTransactionCount(eventId: Long): Flow<Int>
}
