package com.kreeda.ankana.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreeda.ankana.data.model.Score
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: Score): Long

    @Query("SELECT * FROM scores ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<Score>>

    @Query("""
        SELECT * FROM scores
        WHERE LOWER(teamA) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(teamB) LIKE '%' || LOWER(:query) || '%'
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByTeam(query: String): Flow<List<Score>>

    @Query("DELETE FROM scores WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM scores WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): Score?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(score: Score): Long

    @Query("""
        UPDATE scores
           SET teamA = :teamA, teamB = :teamB, scoreA = :scoreA, scoreB = :scoreB,
               sport = :sport, date = :date, note = :note,
               createdAt = :createdAt, syncedAt = :syncedAt
         WHERE remoteId = :remoteId
    """)
    suspend fun updateByRemoteId(
        remoteId: String,
        teamA: String,
        teamB: String,
        scoreA: Int,
        scoreB: Int,
        sport: String,
        date: String,
        note: String?,
        createdAt: Long,
        syncedAt: Long
    ): Int

    @Query("UPDATE scores SET remoteId = :remoteId, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markSynced(id: Long, remoteId: String, syncedAt: Long)
}
