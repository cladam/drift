package com.ilseon.drift.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey

@Composable
fun HelpIconWithModal(
    title: String,
    content: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Outlined.HelpOutline,
            contentDescription = "Learn more",
            tint = CustomTextSecondary
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = title) },
            text = content,
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("GOT IT")
                }
            },
            containerColor = LightGrey, // Match card color
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = CustomTextSecondary
        )
    }
}