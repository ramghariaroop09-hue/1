package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FindingSeverity
import com.example.model.ScanFinding
import com.example.model.ScanProgressState
import com.example.model.ScanRisk
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberDangerBg
import com.example.ui.theme.CyberInfo
import com.example.ui.theme.CyberInfoBg
import com.example.ui.theme.CyberNeonGradient
import com.example.ui.theme.CyberRadarSweepBrush
import com.example.ui.theme.CyberSafe
import com.example.ui.theme.CyberSafeBg
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning
import com.example.ui.theme.CyberWarningBg
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. FUTURISTIC ANIMATED TACTICAL RADAR HUD
// ==========================================

@Composable
fun FuturisticRadarSweep(
    modifier: Modifier = Modifier,
    threatCount: Int = 0,
    isScanning: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")

    // Continuous 360-degree sweep rotation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isScanning) 1800 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Breathing pulse for center core and nodes
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .testTag("futuristic_radar_hud"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 10f

            // 1. Outer tactical ring with compass notches
            drawCircle(
                color = CyberBorder,
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Inner range rings
            val ringCount = 3
            for (i in 1..ringCount) {
                val r = radius * (i.toFloat() / ringCount)
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.15f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }

            // 2. Crosshair guide lines
            drawLine(
                color = CyberCyan.copy(alpha = 0.25f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = CyberCyan.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1f
            )

            // 3. Degree tick notches around perimeter
            for (deg in 0 until 360 step 30) {
                val rad = Math.toRadians(deg.toDouble())
                val isMajor = deg % 90 == 0
                val tickLength = if (isMajor) 8f else 4f
                val startX = (center.x + (radius - tickLength) * cos(rad)).toFloat()
                val startY = (center.y + (radius - tickLength) * sin(rad)).toFloat()
                val endX = (center.x + radius * cos(rad)).toFloat()
                val endY = (center.y + radius * sin(rad)).toFloat()
                drawLine(
                    color = if (isMajor) CyberCyan.copy(alpha = 0.8f) else CyberTextMuted.copy(alpha = 0.4f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 1.5f else 1f
                )
            }

            // 4. Rotating sweep ray with gradient trail
            rotate(degrees = sweepAngle, pivot = center) {
                drawCircle(
                    brush = CyberRadarSweepBrush,
                    radius = radius,
                    center = center
                )
                // Leading high-intensity beam
                drawLine(
                    color = CyberCyan,
                    start = center,
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // 5. Active telemetry nodes (Blips)
            // Node 1: Green secure endpoint at 45 deg, 60% distance
            val n1Rad = Math.toRadians(45.0)
            val n1Pos = Offset(
                (center.x + radius * 0.62f * cos(n1Rad)).toFloat(),
                (center.y + radius * 0.62f * sin(n1Rad)).toFloat()
            )
            drawCircle(
                color = CyberSafe.copy(alpha = pulseAlpha),
                radius = 4f,
                center = n1Pos
            )
            drawCircle(
                color = CyberSafe.copy(alpha = pulseAlpha * 0.4f),
                radius = 8f,
                center = n1Pos,
                style = Stroke(width = 1f)
            )

            // Node 2: Secure cloud node at 210 deg, 75% distance
            val n2Rad = Math.toRadians(210.0)
            val n2Pos = Offset(
                (center.x + radius * 0.75f * cos(n2Rad)).toFloat(),
                (center.y + radius * 0.75f * sin(n2Rad)).toFloat()
            )
            drawCircle(
                color = CyberCyan.copy(alpha = pulseAlpha * 0.9f),
                radius = 3.5f,
                center = n2Pos
            )

            // Node 3: If threat detected or simulated, display alert node
            if (threatCount > 0) {
                val threatRad = Math.toRadians(310.0)
                val threatPos = Offset(
                    (center.x + radius * 0.55f * cos(threatRad)).toFloat(),
                    (center.y + radius * 0.55f * sin(threatRad)).toFloat()
                )
                drawCircle(
                    color = CyberDanger.copy(alpha = pulseAlpha),
                    radius = 5.5f,
                    center = threatPos
                )
                drawCircle(
                    color = CyberDanger.copy(alpha = pulseAlpha * 0.5f),
                    radius = 11f,
                    center = threatPos,
                    style = Stroke(width = 1.5f)
                )
            }

            // 6. Central quantum hub core
            drawCircle(
                color = CyberCyan.copy(alpha = 0.2f * pulseAlpha),
                radius = 14f,
                center = center
            )
            drawCircle(
                color = CyberCyan,
                radius = 4f,
                center = center
            )
        }

        // Overlay status text at bottom of radar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isScanning) "SCANNING..." else "RADAR ONLINE",
                color = if (isScanning) CyberWarning else CyberCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// 2. ANIMATED QUANTUM RISK & SECURITY GAUGE
// ==========================================

@Composable
fun AnimatedSecurityGauge(
    score: Int,
    label: String = "SECURITY SCORE",
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "gauge_score"
    )

    val gaugeColor = when {
        score >= 80 -> CyberSafe
        score >= 50 -> CyberWarning
        else -> CyberDanger
    }

    Box(
        modifier = modifier
            .size(110.dp)
            .testTag("animated_security_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeW = 9.dp.toPx()
            val arcSize = size.minDimension - strokeW

            // Background arc (240 degrees sweep from 150 deg)
            drawArc(
                color = CyberBorder,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(strokeW / 2f, strokeW / 2f),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Foreground animated glowing arc
            val sweep = (animatedScore / 100f).coerceIn(0f, 1f) * 240f
            drawArc(
                color = gaugeColor,
                startAngle = 150f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(strokeW / 2f, strokeW / 2f),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedScore.toInt()}",
                color = gaugeColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = CyberTextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ==========================================
// 3. SCI-FI HUD CORNER FRAME
// ==========================================

@Composable
fun HudCornerFrame(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberCyan,
    bracketSize: Float = 14f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(0.5.dp, borderColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .background(CyberSurfaceDark, RoundedCornerShape(10.dp))
            .padding(1.dp)
    ) {
        // Draw cyber brackets on top left, top right, bottom left, bottom right
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 2.dp.toPx()
            val len = bracketSize.dp.toPx()
            val color = borderColor.copy(alpha = 0.9f)

            // Top-Left corner
            drawLine(color, Offset(0f, 0f), Offset(len, 0f), stroke)
            drawLine(color, Offset(0f, 0f), Offset(0f, len), stroke)

            // Top-Right corner
            drawLine(color, Offset(size.width, 0f), Offset(size.width - len, 0f), stroke)
            drawLine(color, Offset(size.width, 0f), Offset(size.width, len), stroke)

            // Bottom-Left corner
            drawLine(color, Offset(0f, size.height), Offset(len, size.height), stroke)
            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - len), stroke)

            // Bottom-Right corner
            drawLine(color, Offset(size.width, size.height), Offset(size.width - len, size.height), stroke)
            drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - len), stroke)
        }

        content()
    }
}

// ==========================================
// 4. PULSING TELEMETRY BEACON
// ==========================================

@Composable
fun PulsingTelemetryBeacon(
    label: String = "ACTIVE DEFENSE",
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon_anim")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_wave"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_alpha"
    )

    val beaconColor = if (isAlert) CyberDanger else CyberSafe

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Expanding ripple wave
            Box(
                modifier = Modifier
                    .size((10 * waveScale).dp)
                    .clip(CircleShape)
                    .background(beaconColor.copy(alpha = waveAlpha))
            )
            // Core dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(beaconColor)
            )
        }

        Text(
            text = label,
            color = beaconColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

// ==========================================
// 5. STANDARD SECURITY FINDING CHIPS & BADGES
// ==========================================

@Composable
fun RiskBadge(risk: ScanRisk, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderColor, icon) = when (risk) {
        ScanRisk.SAFE -> Quadruple(CyberSafeBg, CyberSafe, CyberSafe.copy(alpha = 0.6f), Icons.Default.CheckCircle)
        ScanRisk.LOW_RISK -> Quadruple(CyberInfoBg, CyberInfo, CyberInfo.copy(alpha = 0.6f), Icons.Default.Info)
        ScanRisk.SUSPICIOUS -> Quadruple(CyberWarningBg, CyberWarning, CyberWarning.copy(alpha = 0.6f), Icons.Default.Warning)
        ScanRisk.HIGH_RISK -> Quadruple(CyberDangerBg, CyberDanger, CyberDanger.copy(alpha = 0.6f), Icons.Default.Error)
    }

    Surface(
        modifier = modifier
            .testTag("risk_badge_${risk.name.lowercase()}")
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = risk.label.uppercase(),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SeverityChip(severity: FindingSeverity) {
    val (color, bg, label) = when (severity) {
        FindingSeverity.INFO -> Triple(CyberInfo, CyberInfoBg, "INFO")
        FindingSeverity.WARNING -> Triple(CyberWarning, CyberWarningBg, "WARNING")
        FindingSeverity.DANGER -> Triple(CyberDanger, CyberDangerBg, "DANGER")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(0.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ==========================================
// 6. ANIMATED SCAN PROGRESS BAR WITH SCANNER LASER
// ==========================================

@Composable
fun ScanProgressBar(progressState: ScanProgressState, modifier: Modifier = Modifier) {
    if (!progressState.isScanning) return

    val animatedProgress by animateFloatAsState(
        targetValue = progressState.progress,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "scan_progress"
    )

    HudCornerFrame(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scan_progress_card"),
        borderColor = CyberCyan
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingTelemetryBeacon(label = progressState.stage.uppercase())
                }
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = CyberCyan,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyberCyan,
                trackColor = CyberBorder
            )

            if (progressState.detail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "► ${progressState.detail}",
                    color = CyberTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==========================================
// 7. FINDINGS SECTION & ACCORDION CARDS
// ==========================================

@Composable
fun FindingsSection(findings: List<ScanFinding>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SECURITY FINDINGS (${findings.size})",
                color = CyberTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
            if (findings.isEmpty()) {
                Text(
                    text = "NO THREATS DETECTED",
                    color = CyberSafe,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (findings.isEmpty()) {
            HudCornerFrame(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyberSafe
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberSafe,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Static pattern inspection and telemetry verification completed with clean results.",
                        color = CyberTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                findings.forEach { finding ->
                    FindingItemCard(finding = finding)
                }
            }
        }
    }
}

@Composable
fun FindingItemCard(finding: ScanFinding) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finding_card_${finding.id}")
            .clip(RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .border(1.dp, if (finding.severity == FindingSeverity.DANGER) CyberDanger.copy(alpha = 0.5f) else CyberBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityChip(severity = finding.severity)
                    Text(
                        text = finding.title,
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = CyberTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = finding.description,
                color = CyberTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (finding.evidence.isNotEmpty()) {
                        Text(
                            text = "TELEMETRY EVIDENCE:",
                            color = CyberTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceDark)
                                .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = finding.evidence,
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (finding.remediation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RECOMMENDED MITIGATION:",
                            color = CyberTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = finding.remediation,
                            color = CyberTextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. DISCLAIMER NOTICE
// ==========================================

@Composable
fun CyberDisclaimerNotice(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cyber_disclaimer_card")
            .border(1.dp, CyberInfo.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberInfoBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CyberInfo,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = CyberTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
