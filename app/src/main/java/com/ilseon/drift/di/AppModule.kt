package com.ilseon.drift.di

import android.content.Context
import com.ilseon.drift.data.DriftDao
import com.ilseon.drift.data.DriftDatabase
import com.ilseon.drift.data.DriftRepository
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
    fun provideDriftDatabase(@ApplicationContext appContext: Context): DriftDatabase {
        return DriftDatabase.getDatabase(appContext)
    }

    @Provides
    fun provideDriftDao(database: DriftDatabase): DriftDao {
        return database.driftDao()
    }

    @Provides
    @Singleton
    fun provideDriftRepository(driftDao: DriftDao): DriftRepository {
        return DriftRepository(driftDao)
    }
}
