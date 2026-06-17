package com.tenli.oneview.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tenli.oneview.model.network.EventItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventItem>)

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    suspend fun getEventById(eventId: Long): EventItem?

    @Query("SELECT * FROM events WHERE deviceId IN (:deviceIds) ORDER BY eTimestamp DESC")
    fun getEventsByDevicesFlow(deviceIds: List<Int>): Flow<List<EventItem>>

    @Query("UPDATE events SET isNew = 0 WHERE id = :eventId")
    suspend fun markAsRead(eventId: Long)

    @Query("DELETE FROM events WHERE id NOT IN (SELECT id FROM events ORDER BY eTimestamp DESC LIMIT 100)")
    suspend fun deleteOldEvents()

    @Query("DELETE FROM events")
    suspend fun clearAll()

    @Query("SELECT * FROM events WHERE eProcessId = :monitorId ORDER BY eTimestamp DESC")
    fun getEventsByMonitorFlow(monitorId: Int): Flow<List<EventItem>>

    @Query("SELECT * FROM events ORDER BY eTimestamp DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int): List<EventItem>
}