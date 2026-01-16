package com.ilseon.drift.ui.theme

import androidx.compose.ui.graphics.Color

val TealAccent = Color(0xFF00BFA5)

// Dark, low-saturation palette
val AccentRed = Color(0xFF8B0000) // Accent for FAB, as per mockup
val Teal = Color(0xFF03DAC5) // A subtle secondary accent

// New
// --- Color Palette Definitions ---
val MutedRed = Color(0xFFB35F5F)
// Shifted from 4C9A9B (Blue-Teal) to 5A9B80 (Green-Teal)
val MutedTeal = Color(0xFF5A9B80)
val BlueTeal = Color(0xFF4C9A9B)
val MutedGreen = Color(0xFF5A9B6E)
val BorderQuiet = Color(0xFF1F1F1F) // BorderQuiet
val MutedDetail = Color(0xFF888888)
val QuietAmber = Color(0xFFC08A3E)
val DarkGrey = Color(0xFF121212)
val LightGrey = Color(0xFF1E1E1E)
val SlateBlue = Color(0xFF5E6D7E)
val CustomTextPrimary = Color(0xFFB0B0B0)   // Primary text color (Your preferred soft white)
val CustomTextSecondary = Color(0xFF888888) // Secondary text color (Your muted detail color)

val TextPrimary = Color(0xFFE0E0E0)   // Primary text color
val TextSecondary = Color(0xFFB0B0B0) // Secondary text color

// Use your original "Low Sensory" Accents for Priority (The "Demands")
val PriorityHigh = MutedRed  // Muted Red (Urgent)
val PriorityMedium =QuietAmber // Quiet Amber (Important)
val PriorityLow = MutedDetail // Muted Detail (Backburner)

// --- The "Pulse" Spectrum  ---
val StatusHigh = Color(0xFFE2B05E) // Warm Ochre (Motivating, not alarming)
// MEDIUM: Balanced State (Sage)
val StatusMedium = Color(0xFFA3A991) // Muted Sage (Calm, steady)
// LOW: Low Energy / Low Priority (Slate/Clay)
val StatusLow = Color(0xFF7D8597) // Steel Blue/Grey (Low pressure)
// URGENT: The only "Warm" color (Deep Terracotta)
val StatusUrgent = Color(0xFFB35F5F) // Muted Red (Used sparingly)

val EnergyHigh = StatusHigh //BlueTeal
val EnergyMedium = StatusMedium //MutedGreen
val EnergyLow = StatusLow //SlateBlue