package com.ilseon.drift.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SleepUnlockReceiver(
    private val onFirstUnlock: () -> Unit
) : BroadcastReceiver() {

    private var hasTriggered = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_USER_PRESENT && !hasTriggered) {
            hasTriggered = true
            onFirstUnlock()
        }
    }
    fun reset() {
        hasTriggered = false
    }
}
