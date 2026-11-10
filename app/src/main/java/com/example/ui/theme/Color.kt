package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Quantum Obsidian & Futuristic Deep Space Palette
val CyberBgDark = Color(0xFF030712)
val CyberSurfaceDark = Color(0xFF080F1E)
val CyberSurfaceElevated = Color(0xFF0F172A)
val CyberSurfaceHighlight = Color(0xFF1E293B)
val CyberBorder = Color(0xFF1E2E48)
val CyberBorderGlow = Color(0xFF00F5FF)

// Futuristic Neon Hologram Accents
val CyberCyan = Color(0xFF00F5FF)
val CyberCyanDim = Color(0xFF00A3B5)
val CyberBlue = Color(0xFF38BDF8)
val CyberViolet = Color(0xFFA855F7)
val CyberPurple = Color(0xFF8B5CF6)
val CyberMagenta = Color(0xFFEC4899)
val CyberNeonGreen = Color(0xFF00FF9D)
val CyberElectricGold = Color(0xFFFFB800)

// Tactical Telemetry & Alert Colors
val CyberSafe = Color(0xFF00FF9D)
val CyberSafeBg = Color(0xFF022B1B)
val CyberWarning = Color(0xFFFFB800)
val CyberWarningBg = Color(0xFF2E1C03)
val CyberDanger = Color(0xFFFF2A6D)
val CyberDangerBg = Color(0xFF3B0818)
val CyberInfo = Color(0xFF00F5FF)
val CyberInfoBg = Color(0xFF032233)

// Text & HUD Data Hierarchy
val CyberTextPrimary = Color(0xFFF1F5F9)
val CyberTextSecondary = Color(0xFF94A3B8)
val CyberTextMuted = Color(0xFF64748B)

// Futuristic HUD Gradients & Brushes
val CyberNeonGradient = Brush.horizontalGradient(
    colors = listOf(CyberCyan, CyberViolet)
)

val CyberShieldGlowBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF00F5FF), Color(0xFF8B5CF6), Color(0xFF38BDF8))
)

val CyberCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0E1726), Color(0xFF070D18))
)

val CyberRadarSweepBrush = Brush.sweepGradient(
    listOf(
        Color(0x0000F5FF),
        Color(0x1500F5FF),
        Color(0x5500F5FF),
        Color(0xCC00F5FF)
    )
)

val CyberAlertSweepBrush = Brush.sweepGradient(
    listOf(
        Color(0x00FF2A6D),
        Color(0x20FF2A6D),
        Color(0x60FF2A6D),
        Color(0xDDFF2A6D)
    )
)
