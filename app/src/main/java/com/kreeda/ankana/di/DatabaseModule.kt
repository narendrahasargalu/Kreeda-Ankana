package com.kreeda.ankana.di

import android.content.Context
import androidx.room.Room
import com.kreeda.ankana.data.local.BookingDao
import com.kreeda.ankana.data.local.ChallengeDao
import com.kreeda.ankana.data.local.KreedaDatabase
import com.kreeda.ankana.data.local.ScoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KreedaDatabase =
        Room.databaseBuilder(context, KreedaDatabase::class.java, KreedaDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBookingDao(db: KreedaDatabase): BookingDao = db.bookingDao()
    @Provides fun provideChallengeDao(db: KreedaDatabase): ChallengeDao = db.challengeDao()
    @Provides fun provideScoreDao(db: KreedaDatabase): ScoreDao = db.scoreDao()
}
