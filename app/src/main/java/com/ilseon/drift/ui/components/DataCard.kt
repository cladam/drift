package com.ilseon.drift.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal

@Composable
fun DataCard(title: String, value: String, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MutedTeal
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text = title, color = CustomTextSecondary)
                Text(text = value, color = CustomTextPrimary)
            }
        }
    }
}