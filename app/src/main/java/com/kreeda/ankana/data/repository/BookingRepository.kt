package com.kreeda.ankana.data.repository

import android.util.Log
import com.kreeda.ankana.data.local.BookingDao
import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.remote.DeviceIdProvider
import com.kreeda.ankana.data.remote.dto.BookingDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class BookingResult {
    data class Success(val id: Long) : BookingResult()
    data object SlotTaken : BookingResult()
    data class Error(val message: String) : BookingResult()
}

@Singleton
class BookingRepository @Inject constructor(
    private val dao: BookingDao,
    private val supabase: SupabaseClient,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val tag = "BookingRepo"

    fun observeForDate(date: String): Flow<List<Booking>> = dao.observeForDate(date)
    fun observeAll(): Flow<List<Booking>> = dao.observeAll()
    fun searchByTeam(query: String): Flow<List<Booking>> = dao.observeByTeam(query)

    suspend fun book(booking: Booking): BookingResult {
        if (dao.isSlotTaken(booking.date, booking.hour) > 0) return BookingResult.SlotTaken

        val remoteId = booking.remoteId ?: UUID.randomUUID().toString()
        val toInsert = booking.copy(remoteId = remoteId)

        val localId = runCatching { dao.insert(toInsert) }
            .getOrElse { return BookingResult.Error(it.message ?: "Could not save booking") }

        // Best-effort push; local is source of truth until next refresh.
        runCatching {
            supabase.from("bookings").insert(
                BookingDto(
                    id = remoteId,
                    teamName = toInsert.teamName,
                    sport = Sport.encode(toInsert.sport),
                    date = toInsert.date,
                    hour = toInsert.hour,
                    deviceId = deviceIdProvider.id,
                    createdAt = toInsert.createdAt
                )
            )
            dao.markSynced(localId, remoteId, System.currentTimeMillis())
        }.onFailure { Log.w(tag, "Supabase push failed for booking $remoteId", it) }

        return BookingResult.Success(localId)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    /** Pull latest bookings from Supabase and merge into local cache. */
    suspend fun refresh(): Result<Int> = runCatching {
        val rows = supabase.from("bookings")
            .select { order("date", Order.DESCENDING) }
            .decodeList<BookingDto>()
        val now = System.currentTimeMillis()
        var changed = 0
        for (dto in rows) {
            val existing = dao.findByRemoteId(dto.id)
            if (existing == null) {
                dao.insertIgnore(
                    Booking(
                        teamName = dto.teamName,
                        sport = Sport.decode(dto.sport),
                        date = dto.date,
                        hour = dto.hour,
                        createdAt = dto.createdAt,
                        remoteId = dto.id,
                        syncedAt = now
                    )
                ).also { if (it != -1L) changed++ }
            } else {
                changed += dao.updateByRemoteId(
                    remoteId = dto.id,
                    teamName = dto.teamName,
                    sport = dto.sport,
                    date = dto.date,
                    hour = dto.hour,
                    createdAt = dto.createdAt,
                    syncedAt = now
                )
            }
        }
        changed
    }
}
