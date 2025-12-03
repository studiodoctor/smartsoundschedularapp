package com.android.smartsoundscheduler.di

import android.content.Context
import androidx.room.Room
import com.android.smartsoundscheduler.data.AppDatabase
import com.android.smartsoundscheduler.data.TimeSlotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sound_scheduler_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTimeSlotDao(database: AppDatabase): TimeSlotDao {
        return database.timeSlotDao()
    }
}