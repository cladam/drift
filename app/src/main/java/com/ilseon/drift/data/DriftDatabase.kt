package com.ilseon.drift.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DriftLog::class], version = 2, exportSchema = false)
abstract class DriftDatabase : RoomDatabase() {

    abstract fun driftDao(): DriftDao

    companion object {
        @Volatile
        private var INSTANCE: DriftDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drift_logs ADD COLUMN bpm INTEGER")
            }
        }

        fun getDatabase(context: Context): DriftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DriftDatabase::class.java,
                    "drift_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
