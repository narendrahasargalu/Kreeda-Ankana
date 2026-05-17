package com.kreeda.ankana.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A posted match result for the score wall.
 */
@Entity(
    tableName = "scores",
    indices = [androidx.room.Index(value = ["remoteId"], unique = true)]
)
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamA: String,
    val teamB: String,
    val scoreA: Int,
    val scoreB: Int,
    val sport: Sport,
    /** ISO date when match was played. */
    val date: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val syncedAt: Long? = null
) {
    val winner: String?
        get() = when {
            scoreA > scoreB -> teamA
            scoreB > scoreA -> teamB
            else -> null
        }
}
