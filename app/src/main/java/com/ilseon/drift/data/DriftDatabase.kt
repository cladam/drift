package com.ilseon.drift.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DriftLog::class], version = 1, exportSchema = false)
abstract class DriftDatabase : RoomDatabase() {

    abstract fun driftDao(): DriftDao

    companion object {
        @Volatile
        private var INSTANCE: DriftDatabase? = null

        fun getDatabase(context: Context): DriftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DriftDatabase::class.java,
                    "drift_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
