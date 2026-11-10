package com.example.model

enum class ScanRisk(val label: String, val level: Int) {
    SAFE("Clean", 0),
    LOW_RISK("Low Risk", 1),
    SUSPICIOUS("Suspicious", 2),
    HIGH_RISK("High Risk", 3)
}

enum class FindingSeverity {
    INFO,
    WARNING,
    DANGER
}

data class ScanFinding(
    val id: String,
    val title: String,
    val description: String,
    val severity: FindingSeverity,
    val evidence: String = "",
    val remediation: String = ""
)

data class ArchiveEntryInfo(
    val name: String,
    val sizeBytes: Long,
    val compressedSizeBytes: Long,
    val isDir: Boolean,
    val isDangerousExtension: Boolean = false,
    val isPathTraversalAttempt: Boolean = false,
    val indicators: List<String> = emptyList()
)

data class FileInspectionResult(
    val fileName: String,
    val fileSizeBytes: Long,
    val sha256Hash: String,
    val detectedType: String,
    val isArchive: Boolean,
    val inspectedEntriesCount: Int,
    val skippedEntriesCount: Int,
    val totalDecompressedBytes: Long,
    val risk: ScanRisk,
    val findings: List<ScanFinding>,
    val archiveEntries: List<ArchiveEntryInfo> = emptyList(),
    val scanDurationMs: Long = 0,
    val isPathTraversalDetected: Boolean = false,
    val isDecompressionLimitHit: Boolean = false,
    val entropy: Double = 0.0,
    val entropyClassification: String = "Normal",
    val disclaimerText: String = "Indicators are identified via static heuristic analysis, entropy telemetry, and signature pattern matching. These are risk indicators, not definitive confirmation of malware."
)

data class LinkScanResult(
    val url: String,
    val host: String,
    val protocol: String,
    val isHttps: Boolean,
    val risk: ScanRisk,
    val findings: List<ScanFinding>,
    val summary: String,
    val httpStatus: Int? = null,
    val headersInspected: Map<String, String> = emptyMap(),
    val securityScore: Int = 85,
    val latencyMs: Long = 0
)

data class AppLinkScanResult(
    val rawUri: String,
    val scheme: String,
    val host: String,
    val path: String,
    val queryParams: Map<String, String>,
    val isIntentUri: Boolean,
    val risk: ScanRisk,
    val findings: List<ScanFinding>,
    val summary: String,
    val recommendations: List<String> = emptyList()
)

data class ScamDetectionResult(
    val inputSample: String,
    val category: String,
    val confidenceScore: Int,
    val risk: ScanRisk,
    val flags: List<ScanFinding>,
    val summary: String,
    val recommendations: List<String>
)

data class ScanProgressState(
    val isScanning: Boolean = false,
    val stage: String = "",
    val progress: Float = 0f,
    val detail: String = ""
)
