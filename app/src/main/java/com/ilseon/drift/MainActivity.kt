package com.ilseon.drift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ilseon.drift.ui.screen.MainScreen
import com.ilseon.drift.ui.theme.DriftTheme
import com.ilseon.drift.ui.viewmodels.CheckInViewModel
import com.ilseon.drift.ui.viewmodels.CheckInViewModelFactory

class MainActivity : ComponentActivity() {

    private val checkInViewModel: CheckInViewModel by viewModels {
        CheckInViewModelFactory((application as DriftApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriftTheme {
                MainScreen(checkInViewModel = checkInViewModel)
            }
        }
    }
}