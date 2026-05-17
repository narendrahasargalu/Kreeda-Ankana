package com.kreeda.ankana.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreeda.ankana.data.model.Booking
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(booking: Booking): Long

    @Query("SELECT * FROM bookings WHERE date = :date ORDER BY hour ASC")
    fun observeForDate(date: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings ORDER BY date DESC, hour ASC")
    fun observeAll(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE LOWER(teamName) LIKE '%' || LOWER(:query) || '%' ORDER BY date DESC, hour ASC")
    fun observeByTeam(query: String): Flow<List<Booking>>

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM bookings WHERE date = :date AND hour = :hour")
    suspend fun isSlotTaken(date: String, hour: Int): Int

    @Query("SELECT * FROM bookings WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): Booking?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(booking: Booking): Long

    @Query("UPDATE bookings SET teamName = :teamName, sport = :sport, date = :date, hour = :hour, createdAt = :createdAt, syncedAt = :syncedAt WHERE remoteId = :remoteId")
    suspend fun updateByRemoteId(
        remoteId: String,
        teamName: String,
        sport: String,
        date: String,
        hour: Int,
        createdAt: Long,
        syncedAt: Long
    ): Int

    @Query("UPDATE bookings SET remoteId = :remoteId, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markSynced(id: Long, remoteId: String, syncedAt: Long)
}
