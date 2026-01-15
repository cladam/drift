package com.ilseon.drift

import android.app.Application
import com.ilseon.drift.data.DriftDatabase
import com.ilseon.drift.data.DriftRepository

class DriftApplication : Application() {
    val database by lazy { DriftDatabase.getDatabase(this) }
    val repository by lazy { DriftRepository(database.driftDao()) }
}
