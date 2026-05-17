package com.kreeda.ankana.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.ChallengeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: Challenge): Long

    @Query("SELECT * FROM challenges ORDER BY status ASC, createdAt DESC")
    fun observeAll(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: ChallengeStatus): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE LOWER(teamName) LIKE '%' || LOWER(:query) || '%' ORDER BY createdAt DESC")
    fun observeByTeam(query: String): Flow<List<Challenge>>

    @Query("UPDATE challenges SET status = :status, acceptedBy = :acceptedBy, acceptedAt = :acceptedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ChallengeStatus, acceptedBy: String?, acceptedAt: Long?)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM challenges WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): Challenge?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(challenge: Challenge): Long

    @Query("""
        UPDATE challenges
           SET teamName = :teamName, sport = :sport,
               preferredDate = :preferredDate, preferredHour = :preferredHour,
               note = :note, status = :status,
               acceptedBy = :acceptedBy, acceptedAt = :acceptedAt,
               createdAt = :createdAt, syncedAt = :syncedAt
         WHERE remoteId = :remoteId
    """)
    suspend fun updateByRemoteId(
        remoteId: String,
        teamName: String,
        sport: String,
        preferredDate: String?,
        preferredHour: Int?,
        note: String?,
        status: ChallengeStatus,
        acceptedBy: String?,
        acceptedAt: Long?,
        createdAt: Long,
        syncedAt: Long
    ): Int

    @Query("UPDATE challenges SET remoteId = :remoteId, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markSynced(id: Long, remoteId: String, syncedAt: Long)

    @Query("SELECT remoteId FROM challenges WHERE id = :id")
    suspend fun getRemoteId(id: Long): String?
}
