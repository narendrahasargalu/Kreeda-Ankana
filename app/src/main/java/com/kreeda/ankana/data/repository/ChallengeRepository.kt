package com.kreeda.ankana.data.repository

import android.util.Log
import com.kreeda.ankana.data.local.ChallengeDao
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.ChallengeStatus
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.remote.DeviceIdProvider
import com.kreeda.ankana.data.remote.dto.ChallengeDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepository @Inject constructor(
    private val dao: ChallengeDao,
    private val supabase: SupabaseClient,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val tag = "ChallengeRepo"

    fun observeAll(): Flow<List<Challenge>> = dao.observeAll()
    fun observeByStatus(status: ChallengeStatus): Flow<List<Challenge>> = dao.observeByStatus(status)
    fun searchByTeam(query: String): Flow<List<Challenge>> = dao.observeByTeam(query)

    suspend fun post(challenge: Challenge): Long {
        val remoteId = challenge.remoteId ?: UUID.randomUUID().toString()
        val toInsert = challenge.copy(remoteId = remoteId)
        val localId = dao.insert(toInsert)

        runCatching {
            supabase.from("challenges").insert(toInsert.toDto(remoteId, deviceIdProvider.id))
            dao.markSynced(localId, remoteId, System.currentTimeMillis())
        }.onFailure { Log.w(tag, "Supabase push failed for challenge $remoteId", it) }

        return localId
    }

    suspend fun accept(id: Long, byTeam: String) {
        val acceptedAt = System.currentTimeMillis()
        dao.updateStatus(id, ChallengeStatus.ACCEPTED, byTeam, acceptedAt)

        val remoteId = dao.getRemoteId(id) ?: return
        runCatching {
            supabase.from("challenges").update({
                set("status", ChallengeStatus.ACCEPTED.name)
                set("accepted_by", byTeam)
                set("accepted_at", acceptedAt)
            }) {
                filter { eq("id", remoteId) }
            }
        }.onFailure { Log.w(tag, "Supabase update failed for challenge $remoteId", it) }
    }

    suspend fun close(id: Long) {
        dao.updateStatus(id, ChallengeStatus.CLOSED, null, null)
        val remoteId = dao.getRemoteId(id) ?: return
        runCatching {
            supabase.from("challenges").update({
                set("status", ChallengeStatus.CLOSED.name)
                set("accepted_by", null as String?)
                set("accepted_at", null as Long?)
            }) {
                filter { eq("id", remoteId) }
            }
        }.onFailure { Log.w(tag, "Supabase close failed for challenge $remoteId", it) }
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun refresh(): Result<Int> = runCatching {
        val rows = supabase.from("challenges")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<ChallengeDto>()
        val now = System.currentTimeMillis()
        var changed = 0
        for (dto in rows) {
            val existing = dao.findByRemoteId(dto.id)
            if (existing == null) {
                dao.insertIgnore(dto.toEntity(now)).also { if (it != -1L) changed++ }
            } else {
                changed += dao.updateByRemoteId(
                    remoteId = dto.id,
                    teamName = dto.teamName,
                    sport = dto.sport,
                    preferredDate = dto.preferredDate,
                    preferredHour = dto.preferredHour,
                    note = dto.note,
                    status = parseStatus(dto.status),
                    acceptedBy = dto.acceptedBy,
                    acceptedAt = dto.acceptedAt,
                    createdAt = dto.createdAt,
                    syncedAt = now
                )
            }
        }
        changed
    }

    private fun parseStatus(s: String): ChallengeStatus =
        runCatching { ChallengeStatus.valueOf(s) }.getOrDefault(ChallengeStatus.OPEN)

    private fun Challenge.toDto(remoteId: String, deviceId: String) = ChallengeDto(
        id = remoteId,
        teamName = teamName,
        sport = Sport.encode(sport),
        preferredDate = preferredDate,
        preferredHour = preferredHour,
        note = note,
        status = status.name,
        acceptedBy = acceptedBy,
        acceptedAt = acceptedAt,
        deviceId = deviceId,
        createdAt = createdAt
    )

    private fun ChallengeDto.toEntity(now: Long) = Challenge(
        teamName = teamName,
        sport = Sport.decode(sport),
        preferredDate = preferredDate,
        preferredHour = preferredHour,
        note = note,
        status = parseStatus(status),
        acceptedBy = acceptedBy,
        acceptedAt = acceptedAt,
        createdAt = createdAt,
        remoteId = id,
        syncedAt = now
    )
}
