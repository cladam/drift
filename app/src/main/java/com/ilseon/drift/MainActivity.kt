package com.ilseon.drift

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.ilseon.drift.notifications.DriftNotificationManager
import com.ilseon.drift.ui.screen.MainScreen
import com.ilseon.drift.ui.theme.DriftTheme
import com.ilseon.drift.ui.viewmodels.CheckInViewModel
import com.ilseon.drift.ui.viewmodels.CheckInViewModelFactory

class MainActivity : ComponentActivity() {

    private val checkInViewModel: CheckInViewModel by viewModels {
        CheckInViewModelFactory((application as DriftApplication).repository)
    }

    private var showNotificationRationaleDialog by mutableStateOf(false)
    private var showAlarmPermissionDialog by mutableStateOf(false)
    private lateinit var notificationManager: DriftNotificationManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleNotifications()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                scheduleNotifications()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showNotificationRationaleDialog = true
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleNotifications()
        }
    }

    private fun scheduleNotifications() {
        if (notificationManager.canScheduleExactAlarms()) {
            notificationManager.scheduleMorningNotification()
            notificationManager.schedulePeriodicNotifications()
        } else {
            showAlarmPermissionDialog = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = DriftNotificationManager(this)
        enableEdgeToEdge()

        handleIntent(intent)
        askNotificationPermission()

        setContent {
            DriftTheme {
                val showCheckInFromWidget by checkInViewModel.showCheckInFromWidget.collectAsState()

                MainScreen(
                    checkInViewModel = checkInViewModel,
                    showCheckInFromNotification = showCheckInFromWidget,
                    onCheckInHandled = { checkInViewModel.onCheckInFromWidgetHandled() }
                )
            }

            if (showNotificationRationaleDialog) {
                NotificationRationaleDialog(
                    onConfirm = {
                        showNotificationRationaleDialog = false
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onDismiss = {
                        showNotificationRationaleDialog = false
                    }
                )
            }

            if (showAlarmPermissionDialog) {
                AlarmPermissionDialog(
                    onConfirm = {
                        showAlarmPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    },
                    onDismiss = {
                        showAlarmPermissionDialog = false
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getStringExtra("notification_action") == "check_in") {
            checkInViewModel.triggerCheckInFromWidget()
        }
    }
}

@Composable
private fun NotificationRationaleDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Notifications") },
        text = { Text("To receive check-in reminders, please enable notifications for Drift.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No thanks")
            }
        }
    )
}

@Composable
private fun AlarmPermissionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Alarms & Reminders") },
        text = { Text("To schedule the 6:00 AM notification, Drift needs permission to set exact alarms.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
