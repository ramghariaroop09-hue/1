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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LinkScanResult
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
fun LinkCheckerScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val linkState by viewModel.linkState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("link_checker_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("link_checker_header_card")
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
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Link Checker",
                                color = CyberTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "URL phishing, homographs & SSL probe",
                                color = CyberTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scans web links for brand typosquatting, IDN homograph punycode deception, high-risk TLDs, IP hosts, and missing security headers.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Dedicated URL Input Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("link_input_card")
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TARGET URL",
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = linkState.urlInput,
                        onValueChange = { viewModel.onUrlInputChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field"),
                        placeholder = { Text("https://example.com/login", color = CyberTextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = CyberCyan
                            )
                        },
                        trailingIcon = {
                            if (linkState.urlInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onUrlInputChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = CyberTextMuted
                                    )
                                }
                            }
                        },
                        isError = linkState.validationError != null,
                        supportingText = {
                            if (linkState.validationError != null) {
                                Text(
                                    text = linkState.validationError ?: "",
                                    color = CyberDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.testTag("url_validation_error")
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.scanUrl() }),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.scanUrl() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_url_button"),
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
                            text = "Analyze Link Security",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TRY TEST URL SAMPLES",
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
                            onClick = { viewModel.setSampleUrl(SampleDataGenerator.SAMPLE_SUSPICIOUS_URL) },
                            modifier = Modifier.testTag("sample_phish_url_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberDanger.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Phishing Lure Sample", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setSampleUrl(SampleDataGenerator.SAMPLE_IP_URL) },
                            modifier = Modifier.testTag("sample_ip_url_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberWarning),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberWarning.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("IP Host Sample", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setSampleUrl(SampleDataGenerator.SAMPLE_SAFE_URL) },
                            modifier = Modifier.testTag("sample_safe_url_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSafe),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSafe.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Google.com (Safe)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Progress bar
        item {
            ScanProgressBar(progressState = linkState.progress)
        }

        // Scan Results
        val result = linkState.result
        if (result != null) {
            item {
                LinkReportCard(result = result)
            }

            item {
                FindingsSection(findings = result.findings)
            }
        }
    }
}

@Composable
fun LinkReportCard(result: LinkScanResult) {
    HudCornerFrame(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("link_scan_report_card"),
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
                        text = result.host,
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Protocol: ${result.protocol} • Status: ${result.httpStatus ?: "Probed"}",
                        color = if (result.isHttps) CyberSafe else CyberWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                RiskBadge(risk = result.risk)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score and Latency Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceDark)
                        .border(
                            0.5.dp,
                            if (result.securityScore >= 80) CyberSafe else if (result.securityScore >= 50) CyberWarning else CyberDanger,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "SECURITY SCORE",
                            color = CyberTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.securityScore} / 100",
                            color = if (result.securityScore >= 80) CyberSafe else if (result.securityScore >= 50) CyberWarning else CyberDanger,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceDark)
                        .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "PROBE LATENCY",
                            color = CyberTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (result.latencyMs > 0) "${result.latencyMs} ms" else "Local Analysis",
                            color = CyberCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = result.summary,
                color = CyberTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (result.headersInspected.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SECURITY HEADERS OBSERVED",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                result.headersInspected.forEach { (name, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name, color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(text = value, color = CyberTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
