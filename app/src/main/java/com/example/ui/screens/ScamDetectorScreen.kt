package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScamDetectionResult
import com.example.scanner.SampleDataGenerator
import com.example.ui.CyberShieldViewModel
import com.example.ui.components.FindingsSection
import com.example.ui.components.HudCornerFrame
import com.example.ui.components.RiskBadge
import com.example.ui.components.ScanProgressBar
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberSafe
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScamDetectorScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val scamState by viewModel.scamState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("scam_detector_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scam_detector_header_card")
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCyanDim.copy(alpha = 0.25f))
                                .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI Scam & Phishing Detector",
                                color = CyberTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Social engineering & financial fraud analysis",
                                color = CyberTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Analyzes emails, SMS, fake invoices, and chat messages for psychological manipulation tactics, fraudulent refund lures, remote access prompts, and gift card demands.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scam_input_card")
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUSPICIOUS MESSAGE TEXT",
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scamState.textInput,
                        onValueChange = { viewModel.onScamInputChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("scam_text_input_field"),
                        placeholder = {
                            Text(
                                "Paste suspicious email body, SMS text, or renewal invoice notice...",
                                color = CyberTextMuted
                            )
                        },
                        trailingIcon = {
                            if (scamState.textInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onScamInputChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = CyberTextMuted
                                    )
                                }
                            }
                        },
                        isError = scamState.validationError != null,
                        supportingText = {
                            if (scamState.validationError != null) {
                                Text(
                                    text = scamState.validationError ?: "",
                                    color = CyberDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.testTag("scam_validation_error")
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary,
                            cursorColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.scanScamText() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyze_scam_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF001F26)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyze Message for Deception",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TRY TEST SCAM MESSAGES",
                        color = CyberTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setSampleScam(SampleDataGenerator.SAMPLE_GEEK_SQUAD_SCAM) },
                            modifier = Modifier.testTag("sample_geek_squad_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberDanger.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Geek Squad Auto-Renewal", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setSampleScam(SampleDataGenerator.SAMPLE_CRYPTO_SCAM) },
                            modifier = Modifier.testTag("sample_crypto_scam_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberWarning),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberWarning.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Crypto Multiplier Lure", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setSampleScam(SampleDataGenerator.SAMPLE_SAFE_MESSAGE) },
                            modifier = Modifier.testTag("sample_safe_message_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSafe),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSafe.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Normal Meeting Note", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            ScanProgressBar(progressState = scamState.progress)
        }

        val result = scamState.result
        if (result != null) {
            item {
                ScamReportCard(result = result)
            }

            item {
                FindingsSection(findings = result.flags)
            }
        }
    }
}

@Composable
fun ScamReportCard(result: ScamDetectionResult) {
    HudCornerFrame(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scam_report_card"),
        borderColor = if (result.risk == com.example.model.ScanRisk.HIGH_RISK) CyberDanger else CyberCyan
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.category,
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Threat Likelihood: ${result.confidenceScore}%",
                        color = if (result.confidenceScore > 50) CyberDanger else CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                RiskBadge(risk = result.risk)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = result.summary,
                color = CyberTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (result.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "DEFENSIVE RECOMMENDATIONS",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                result.recommendations.forEach { rec ->
                    Text(
                        text = "🛡 $rec",
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
