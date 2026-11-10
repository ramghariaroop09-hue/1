package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScanRisk
import com.example.ui.CyberNavTab
import com.example.ui.CyberShieldViewModel
import com.example.ui.components.AnimatedSecurityGauge
import com.example.ui.components.CyberDisclaimerNotice
import com.example.ui.components.FuturisticRadarSweep
import com.example.ui.components.HudCornerFrame
import com.example.ui.components.PulsingTelemetryBeacon
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberCardGradient
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberNeonGradient
import com.example.ui.theme.CyberSafe
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.CyberWarning

@Composable
fun DashboardScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val fileState by viewModel.fileState.collectAsState()
    val linkState by viewModel.linkState.collectAsState()

    val isAnyScanning = fileState.progress.isScanning || linkState.progress.isScanning
    val totalThreats = stats.threatsFlagged

    val infiniteTransition = rememberInfiniteTransition(label = "hud_scanline")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Futuristic Tactical Command Deck
        item {
            HudCornerFrame(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                borderColor = if (totalThreats > 0) CyberDanger else CyberCyan,
                bracketSize = 16f
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Bar with Telemetry Beacon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .border(1.dp, CyberCyan.copy(alpha = glowAlpha), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "CYBERSHIELD // HUD",
                                    color = CyberTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "ENDPOINT SECURITY DEFENSE MATRIX",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        PulsingTelemetryBeacon(
                            label = if (totalThreats > 0) "ALERT" else "ONLINE",
                            isAlert = totalThreats > 0
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Center Radar & Security Score Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated Tactical Radar Scanner
                        FuturisticRadarSweep(
                            threatCount = totalThreats,
                            isScanning = isAnyScanning
                        )

                        // Real-Time System Integrity Gauge
                        val computedIntegrity = if (stats.totalScans == 0) 100 else {
                            val ratio = (stats.cleanScans.toFloat() / stats.totalScans.toFloat()).coerceIn(0f, 1f)
                            (ratio * 100).toInt()
                        }
                        AnimatedSecurityGauge(
                            score = computedIntegrity,
                            label = "INTEGRITY"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Telemetry Metric Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TacticalStatTile(
                            label = "TOTAL AUDITS",
                            value = "${stats.totalScans}",
                            color = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        TacticalStatTile(
                            label = "THREATS FLAGGED",
                            value = "${stats.threatsFlagged}",
                            color = if (stats.threatsFlagged > 0) CyberDanger else CyberTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        TacticalStatTile(
                            label = "VERIFIED SAFE",
                            value = "${stats.cleanScans}",
                            color = CyberSafe,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Real-Time Telemetry Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CyberSafe,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "AIR-GAPPED LOCAL SANDBOX // ZERO TELEMETRY LEAKAGE",
                            color = CyberTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "256-BIT",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Security Modules Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEFENSE PROTOCOL MODULES",
                    color = CyberTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "4 ACTIVE ENGINES",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Module 1: File Inspector
        item {
            TacticalModuleCard(
                title = "File Inspector & Sandbox",
                subtitle = "Shannon entropy analysis, Zip Slip protection & UPX packer detector",
                badge = "DEEP BYTECODE AUDIT",
                icon = Icons.Default.Archive,
                accentColor = CyberCyan,
                testTag = "nav_file_inspector_card",
                onClick = { viewModel.selectTab(CyberNavTab.FILE_INSPECTOR) }
            )
        }

        // Module 2: Link Shield
        item {
            TacticalModuleCard(
                title = "Link Shield & Protocol Probe",
                subtitle = "Punycode homograph detection, typosquatting & live SSL latency",
                badge = "0-100 SECURITY GAUGE",
                icon = Icons.Default.Link,
                accentColor = CyberSafe,
                testTag = "nav_link_checker_card",
                onClick = { viewModel.selectTab(CyberNavTab.LINK_CHECKER) }
            )
        }

        // Module 3: App Link & Intent Checker
        item {
            TacticalModuleCard(
                title = "App Link & Intent Interceptor",
                subtitle = "Audit custom URI schemes, autoVerify domains & token leakage",
                badge = "SCHEME SHIELD",
                icon = Icons.Default.DeviceHub,
                accentColor = CyberWarning,
                testTag = "nav_app_link_card",
                onClick = { viewModel.selectTab(CyberNavTab.APP_LINK_CHECKER) }
            )
        }

        // Module 4: Scam & Social Engineering Radar
        item {
            TacticalModuleCard(
                title = "Fraud & Social Engineering Radar",
                subtitle = "Urgency pressure heuristics, fake invoices & remote screen hijacking",
                badge = "TACTICAL RADAR",
                icon = Icons.Default.Psychology,
                accentColor = CyberViolet,
                testTag = "nav_scam_detector_card",
                onClick = { viewModel.selectTab(CyberNavTab.SCAM_DETECTOR) }
            )
        }

        // Privacy Guarantee
        item {
            CyberDisclaimerNotice(
                text = "Privacy Mandate: File Inspector and Fraud Radar operate 100% inside local device RAM. Uploaded binaries and scanned links are never transmitted to external hosts without explicit user instruction."
            )
        }
    }
}

@Composable
fun TacticalStatTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceDark)
            .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = CyberTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun TacticalModuleCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = CyberTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(0.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
