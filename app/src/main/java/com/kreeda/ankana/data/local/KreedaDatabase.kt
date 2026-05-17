package com.kreeda.ankana.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.Score

@Database(
    entities = [Booking::class, Challenge::class, Score::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KreedaDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun scoreDao(): ScoreDao

    companion object {
        const val DB_NAME = "kreeda.db"
    }
}
