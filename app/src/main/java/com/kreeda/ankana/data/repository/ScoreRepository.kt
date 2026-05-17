package com.kreeda.ankana.data.repository

import android.util.Log
import com.kreeda.ankana.data.local.ScoreDao
import com.kreeda.ankana.data.model.Score
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.remote.DeviceIdProvider
import com.kreeda.ankana.data.remote.dto.ScoreDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepository @Inject constructor(
    private val dao: ScoreDao,
    private val supabase: SupabaseClient,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val tag = "ScoreRepo"

    fun observeAll(): Flow<List<Score>> = dao.observeAll()
    fun searchByTeam(query: String): Flow<List<Score>> = dao.observeByTeam(query)

    suspend fun post(score: Score): Long {
        val remoteId = score.remoteId ?: UUID.randomUUID().toString()
        val toInsert = score.copy(remoteId = remoteId)
        val localId = dao.insert(toInsert)

        runCatching {
            supabase.from("scores").insert(
                ScoreDto(
                    id = remoteId,
                    teamA = toInsert.teamA,
                    teamB = toInsert.teamB,
                    scoreA = toInsert.scoreA,
                    scoreB = toInsert.scoreB,
                    sport = Sport.encode(toInsert.sport),
                    date = toInsert.date,
                    note = toInsert.note,
                    deviceId = deviceIdProvider.id,
                    createdAt = toInsert.createdAt
                )
            )
            dao.markSynced(localId, remoteId, System.currentTimeMillis())
        }.onFailure { Log.w(tag, "Supabase push failed for score $remoteId", it) }

        return localId
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun refresh(): Result<Int> = runCatching {
        val rows = supabase.from("scores")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<ScoreDto>()
        val now = System.currentTimeMillis()
        var changed = 0
        for (dto in rows) {
            val existing = dao.findByRemoteId(dto.id)
            if (existing == null) {
                dao.insertIgnore(
                    Score(
                        teamA = dto.teamA,
                        teamB = dto.teamB,
                        scoreA = dto.scoreA,
                        scoreB = dto.scoreB,
                        sport = Sport.decode(dto.sport),
                        date = dto.date,
                        note = dto.note,
                        createdAt = dto.createdAt,
                        remoteId = dto.id,
                        syncedAt = now
                    )
                ).also { if (it != -1L) changed++ }
            } else {
                changed += dao.updateByRemoteId(
                    remoteId = dto.id,
                    teamA = dto.teamA,
                    teamB = dto.teamB,
                    scoreA = dto.scoreA,
                    scoreB = dto.scoreB,
                    sport = dto.sport,
                    date = dto.date,
                    note = dto.note,
                    createdAt = dto.createdAt,
                    syncedAt = now
                )
            }
        }
        changed
    }
}
