package com.ilseon.drift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ilseon.drift.ui.screen.MainScreen
import com.ilseon.drift.ui.theme.DriftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DriftTheme {
                MainScreen()
            }
        }
    }
}