package com.kreeda.ankana.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChallengeStatus { OPEN, ACCEPTED, CLOSED }

/**
 * A "friendly match" challenge posted by one team to whoever wants to reply.
 */
@Entity(
    tableName = "challenges",
    indices = [androidx.room.Index(value = ["remoteId"], unique = true)]
)
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamName: String,
    val sport: Sport,
    /** Optional preferred date — null means "any day". ISO format. */
    val preferredDate: String? = null,
    /** Optional preferred hour 0–23, null means "any time". */
    val preferredHour: Int? = null,
    val note: String? = null,
    val status: ChallengeStatus = ChallengeStatus.OPEN,
    val acceptedBy: String? = null,
    val acceptedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val syncedAt: Long? = null
)
