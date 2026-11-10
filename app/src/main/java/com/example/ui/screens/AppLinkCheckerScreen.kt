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
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLinkScanResult
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
fun AppLinkCheckerScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val appLinkState by viewModel.appLinkState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_link_checker_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_link_header_card")
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
                                imageVector = Icons.Default.DeviceHub,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "App Link & Intent Checker",
                                color = CyberTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Deep link hijacking & intent redirection audit",
                                color = CyberTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Inspects Android deep links, custom schemes (app://), intent URIs, and universal links for credential leakage, redirection flaws, and interception vulnerabilities.",
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
                    .testTag("app_link_input_card")
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEEP LINK OR INTENT URI",
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = appLinkState.appLinkInput,
                        onValueChange = { viewModel.onAppLinkInputChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_link_input_field"),
                        placeholder = { Text("myapp://oauth/callback?token=...", color = CyberTextMuted) },
                        trailingIcon = {
                            if (appLinkState.appLinkInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onAppLinkInputChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = CyberTextMuted
                                    )
                                }
                            }
                        },
                        isError = appLinkState.validationError != null,
                        supportingText = {
                            if (appLinkState.validationError != null) {
                                Text(
                                    text = appLinkState.validationError ?: "",
                                    color = CyberDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier.testTag("app_link_validation_error")
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
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.scanAppLink() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_app_link_button"),
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
                            text = "Audit Deep Link Security",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TRY TEST DEEP LINKS",
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
                            onClick = { viewModel.setSampleAppLink(SampleDataGenerator.SAMPLE_TOKEN_APP_LINK) },
                            modifier = Modifier.testTag("sample_token_applink_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberDanger.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Token Leakage Link", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setSampleAppLink(SampleDataGenerator.SAMPLE_INTENT_APP_LINK) },
                            modifier = Modifier.testTag("sample_intent_applink_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberWarning),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberWarning.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Intent URI Sample", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setSampleAppLink(SampleDataGenerator.SAMPLE_SAFE_APP_LINK) },
                            modifier = Modifier.testTag("sample_safe_applink_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSafe),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSafe.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("App Link (HTTPS)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            ScanProgressBar(progressState = appLinkState.progress)
        }

        val result = appLinkState.result
        if (result != null) {
            item {
                AppLinkReportCard(result = result)
            }

            item {
                FindingsSection(findings = result.findings)
            }
        }
    }
}

@Composable
fun AppLinkReportCard(result: AppLinkScanResult) {
    HudCornerFrame(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_link_report_card"),
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
                        text = "Scheme: ${result.scheme.ifEmpty { "None" }}://",
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Host: ${result.host.ifEmpty { "N/A" }} • Path: ${result.path.ifEmpty { "/" }}",
                        color = CyberCyan,
                        fontSize = 12.sp
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
                Text(
                    text = "REMEDIATION RECOMMENDATIONS",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                result.recommendations.forEach { rec ->
                    Text(
                        text = "→ $rec",
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
