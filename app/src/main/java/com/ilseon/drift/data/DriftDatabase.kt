package com.ilseon.drift.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DriftLog::class], version = 4, exportSchema = false)
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drift_logs ADD COLUMN stressIndex INTEGER")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new table with REAL type for stressIndex
                db.execSQL("CREATE TABLE drift_logs_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, moodScore REAL, energyLevel TEXT, sleepDurationMinutes INTEGER, sleepStartTime INTEGER, sleepEndTime INTEGER, hrvValue REAL, bpm INTEGER, stressScore INTEGER, stressIndex REAL)")
                // Copy data from old table to new table
                db.execSQL("INSERT INTO drift_logs_new (id, timestamp, moodScore, energyLevel, sleepDurationMinutes, sleepStartTime, sleepEndTime, hrvValue, bpm, stressScore, stressIndex) SELECT id, timestamp, moodScore, energyLevel, sleepDurationMinutes, sleepStartTime, sleepEndTime, hrvValue, bpm, stressScore, CAST(stressIndex AS REAL) FROM drift_logs")
                // Remove the old table
                db.execSQL("DROP TABLE drift_logs")
                // Rename new table to original name
                db.execSQL("ALTER TABLE drift_logs_new RENAME TO drift_logs")
            }
        }

        fun getDatabase(context: Context): DriftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DriftDatabase::class.java,
                    "drift_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
