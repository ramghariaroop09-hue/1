package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArchiveEntryInfo
import com.example.model.FileInspectionResult
import com.example.ui.CyberShieldViewModel
import com.example.ui.components.CyberDisclaimerNotice
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
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileInspectorScreen(
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    val fileState by viewModel.fileState.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = queryFileName(context, uri)
            viewModel.inspectFileUri(uri, name)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("file_inspector_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("file_inspector_header_card")
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCyanDim.copy(alpha = 0.25f))
                                    .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "File Inspector",
                                    color = CyberTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Real-time static archive & binary analysis",
                                    color = CyberTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Local privacy badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CyberSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = CyberSafe,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "100% Local",
                                    color = CyberSafe,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Supports ZIP, APK, EXE, PDF, TXT, and script files. Unpacks archives in memory with decompression limits and Zip Slip path traversal safeguards.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Upload Area & Quick Harmless Samples
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("file_upload_area")
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Upload",
                        tint = CyberCyan,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select Any File to Inspect",
                        color = CyberTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "ZIP, APK, EXE, PDF, TXT, JS, BAT, or DOCX",
                        color = CyberTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            filePicker.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("browse_files_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF001F26)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Browse Storage Files",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Samples Row for instantaneous verification
                    Text(
                        text = "PRELOADED VERIFICATION SAMPLES",
                        color = CyberTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.loadSampleHarmlessZip() },
                            modifier = Modifier.testTag("sample_harmless_zip_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Harmless Test ZIP", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadSamplePathTraversalZip() },
                            modifier = Modifier.testTag("sample_zip_slip_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberWarning),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberWarning.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Zip Slip Test", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadSampleCleanZip() },
                            modifier = Modifier.testTag("sample_clean_zip_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSafe),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSafe.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clean ZIP", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Scan Progress Indicator
        item {
            ScanProgressBar(progressState = fileState.progress)
        }

        // Error message if any
        if (fileState.errorMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_error_card")
                        .border(1.dp, CyberDanger, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CyberDanger,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = fileState.errorMessage ?: "",
                            color = CyberDanger,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Inspection Results Section
        val result = fileState.result
        if (result != null) {
            item {
                FileInspectionReportCard(result = result, onClear = { viewModel.clearFileInspector() })
            }

            item {
                FindingsSection(findings = result.findings)
            }

            if (result.isArchive && result.archiveEntries.isNotEmpty()) {
                item {
                    ArchiveEntriesSection(entries = result.archiveEntries)
                }
            }

            item {
                CyberDisclaimerNotice(text = result.disclaimerText)
            }
        }
    }
}

@Composable
fun FileInspectionReportCard(
    result: FileInspectionResult,
    onClear: () -> Unit
) {
    HudCornerFrame(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("file_inspection_report_card"),
        borderColor = if (result.risk == com.example.model.ScanRisk.HIGH_RISK) CyberDanger else CyberCyan
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name & Risk Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.fileName,
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatBytes(result.fileSizeBytes)} • ${result.detectedType}",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                RiskBadge(risk = result.risk)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // SHA-256 Digest Card
            Column {
                Text(
                    text = "SHA-256 HASH",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sha256_hash_box")
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceDark)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = result.sha256Hash,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inspection Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "Archive Entries",
                    value = if (result.isArchive) "${result.inspectedEntriesCount}" else "N/A",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Decompressed",
                    value = if (result.isArchive) formatBytes(result.totalDecompressedBytes) else formatBytes(result.fileSizeBytes),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Scan Time",
                    value = "${result.scanDurationMs}ms",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deep Telemetry: Shannon Entropy Analysis & Packing Detection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entropy_telemetry_box")
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceDark)
                    .border(
                        0.5.dp,
                        if (result.entropy >= 7.3) CyberDanger.copy(alpha = 0.5f) else CyberBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SHANNON ENTROPY (RANDOMNESS)",
                            color = CyberTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", result.entropy)} / 8.00",
                            color = if (result.entropy >= 7.3) CyberDanger else if (result.entropy >= 6.5) CyberWarning else CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (result.entropy / 8.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (result.entropy >= 7.3) CyberDanger else if (result.entropy >= 6.5) CyberWarning else CyberCyan,
                        trackColor = Color(0xFF1E2836)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Classification: ${result.entropyClassification}",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (result.isPathTraversalDetected) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF380E17))
                        .border(1.dp, CyberDanger, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "CRITICAL: Zip Slip Path Traversal Detected! This archive contains directory climbing entries targeting parent folders.",
                        color = CyberDanger,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceDark)
            .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = CyberTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = CyberTextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
fun ArchiveEntriesSection(entries: List<ArchiveEntryInfo>) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inspected Archive Entries (${entries.size})",
                color = CyberTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = if (expanded) "Hide details" else "View all",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entries.forEach { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                0.5.dp,
                                if (entry.isDangerousExtension || entry.isPathTraversalAttempt) CyberDanger else CyberBorder,
                                RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (entry.isDir) Icons.Default.FolderOpen else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (entry.isDangerousExtension) CyberDanger else CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = entry.name,
                                        color = if (entry.isPathTraversalAttempt) CyberDanger else CyberTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = formatBytes(entry.sizeBytes),
                                    color = CyberTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (entry.indicators.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    entry.indicators.forEach { ind ->
                                        Text(
                                            text = "• $ind",
                                            color = CyberWarning,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun queryFileName(context: Context, uri: Uri): String {
    var name = "uploaded_file"
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
    } catch (e: Exception) {
        name = uri.lastPathSegment ?: "file"
    }
    return name
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups.coerceAtMost(units.size - 1)])
}
