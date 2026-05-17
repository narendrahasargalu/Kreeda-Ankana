package com.kreeda.ankana.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One booked time slot on the village ground.
 *
 * (date, hour) is unique — a slot can only be claimed by one team.
 */
@Entity(
    tableName = "bookings",
    indices = [
        Index(value = ["date", "hour"], unique = true),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamName: String,
    val sport: Sport,
    /** ISO date — e.g. "2026-05-05". */
    val date: String,
    /** 0–23, slot is one hour long starting at this hour. */
    val hour: Int,
    val createdAt: Long = System.currentTimeMillis(),
    /** Server-side UUID; null until pushed. Also the merge key for pulls. */
    val remoteId: String? = null,
    /** Epoch millis of last successful push, null = unsynced. */
    val syncedAt: Long? = null
)
