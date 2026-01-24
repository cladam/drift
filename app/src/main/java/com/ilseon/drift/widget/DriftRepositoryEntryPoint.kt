package com.ilseon.drift.widget

import com.ilseon.drift.data.DriftRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DriftRepositoryEntryPoint {
    fun driftRepository(): DriftRepository
}
