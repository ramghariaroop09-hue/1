package com.example.scanner

import com.example.model.ArchiveEntryInfo
import com.example.model.FileInspectionResult
import com.example.model.FindingSeverity
import com.example.model.ScanFinding
import com.example.model.ScanRisk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileInspectorEngine {

    const val MAX_ARCHIVE_ENTRIES = 250
    const val MAX_TOTAL_UNCOMPRESSED_BYTES = 50 * 1024 * 1024L // 50 MB
    const val MAX_ENTRY_TEXT_READ_BYTES = 128 * 1024 // 128 KB
    const val MAX_SAFE_COMPRESSION_RATIO = 100.0

    private val DANGEROUS_EXTENSIONS = setOf(
        "exe", "bat", "cmd", "vbs", "ps1", "scr", "pif", "dll", "hta",
        "cpl", "msi", "reg", "com", "dex", "gadget", "wsf"
    )

    private val ELEVATED_EXTENSIONS = setOf(
        "jar", "apk", "sh", "elf", "so", "dylib", "bin"
    )

    private val SUSPICIOUS_SCRIPT_PATTERNS = listOf(
        Regex("eval\\s*\\(", RegexOption.IGNORE_CASE) to "Dynamic code execution via eval()",
        Regex("exec\\s*\\(", RegexOption.IGNORE_CASE) to "Command execution invocation via exec()",
        Regex("<script[^>]*>", RegexOption.IGNORE_CASE) to "Embedded HTML/XML <script> tag",
        Regex("powershell(?:\\.exe)?\\s+-(?:enc|encodedcommand|executionpolicy)", RegexOption.IGNORE_CASE) to "Obfuscated PowerShell execution flags",
        Regex("cmd(?:\\.exe)?\\s+/c", RegexOption.IGNORE_CASE) to "Command shell spawning invocation",
        Regex("WScript\\.Shell", RegexOption.IGNORE_CASE) to "Windows Script Host shell automation object",
        Regex("ActiveXObject", RegexOption.IGNORE_CASE) to "Legacy ActiveX COM object invocation",
        Regex("DownloadString|DownloadFile", RegexOption.IGNORE_CASE) to "Remote payload download directive",
        Regex("FromBase64String|base64_decode", RegexOption.IGNORE_CASE) to "Base64 payload decoding routine",
        Regex("reverse_tcp|meterpreter", RegexOption.IGNORE_CASE) to "Penetration testing / remote payload token"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "ransomware" to "Ransomware signature keyword",
        "mimikatz" to "Credential extraction tool keyword (Mimikatz)",
        "keylogger" to "Keystroke interception keyword",
        "payload" to "Generic exploit payload indicator",
        "shadowcopy delete" to "VSS backup shadow copy deletion command",
        "vssadmin delete" to "Volume Shadow Copy deletion indicator",
        "DisableRealtimeMonitoring" to "Antivirus defense evasion keyword",
        "bypass_uac" to "User Account Control bypass keyword",
        "drop_privileges" to "Privilege modification indicator"
    )

    private val MALWARE_FAMILY_SIGNATURES = listOf(
        "pegasus" to "NSO Group Pegasus Spyware Artifact",
        "cobalt strike" to "Cobalt Strike Adversary Emulation / Beacon",
        "beacon.dll" to "Cobalt Strike Default Beacon Binary",
        "wannacry" to "WannaCry Ransomware Killswitch / Binary Artifact",
        "wanacrypt0r" to "WanaCrypt0r Ransomware Note / Locker",
        "lockbit" to "LockBit Enterprise Ransomware Artifact",
        "agenttesla" to "AgentTesla InfoStealer Trojan Token",
        "redline stealer" to "RedLine Stealer Credential Harvester",
        "qakbot" to "Qakbot Banking Trojan / Loader Signature",
        "blackcat" to "ALPHV / BlackCat Rust Ransomware Pattern",
        "mirai" to "Mirai IoT Botnet Scanner / Exploit Signature"
    )

    fun calculateShannonEntropy(bytes: ByteArray): Double {
        if (bytes.isEmpty()) return 0.0
        val frequency = IntArray(256)
        for (b in bytes) {
            frequency[b.toInt() and 0xFF]++
        }
        var entropy = 0.0
        val len = bytes.size.toDouble()
        for (count in frequency) {
            if (count > 0) {
                val p = count / len
                entropy -= p * (Math.log(p) / Math.log(2.0))
            }
        }
        return entropy
    }

    private val CREDENTIAL_PATTERNS = listOf(
        Regex("-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----") to "Exposed Private Cryptographic Key",
        Regex("ghp_[A-Za-z0-9_]{36}") to "Exposed GitHub Personal Access Token",
        Regex("AKIA[0-9A-Z]{16}") to "Exposed AWS Access Key ID",
        Regex("(?:password|passwd|api_key|secret)\\s*[:=]\\s*['\"][^'\"]{6,}['\"]", RegexOption.IGNORE_CASE) to "Hardcoded credential assignment"
    )

    suspend fun inspectFile(
        fileName: String,
        fileBytes: ByteArray,
        paceStages: Boolean = true,
        onProgress: (stage: String, progress: Float, detail: String) -> Unit = { _, _, _ -> }
    ): FileInspectionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        // Stage 1: Digest & Metadata + Entropy
        onProgress("Hashing & Entropy Analysis", 0.15f, "Computing SHA-256 digest & Shannon entropy...")
        if (paceStages) delay(250)
        val sha256 = computeSha256(fileBytes)
        val fileSizeBytes = fileBytes.size.toLong()
        val entropy = calculateShannonEntropy(fileBytes)
        val entropyClassification = when {
            entropy >= 7.3 -> "Extremely High (Packed / Encrypted / Suspicious)"
            entropy >= 6.5 -> "High (Compressed or Native Binary)"
            entropy >= 4.5 -> "Moderate (Executable / Compiled Code)"
            else -> "Normal (Structured Plain Text / Code)"
        }

        // Stage 2: Header Signature & Detection
        onProgress("Deep Threat Analysis", 0.35f, "Verifying magic byte signatures and packer headers...")
        if (paceStages) delay(300)
        val fileType = detectFileType(fileName, fileBytes)
        val findings = mutableListOf<ScanFinding>()

        // Check for High Entropy Anomaly (Packed / Encrypted)
        if (entropy >= 7.3 && !fileType.contains("ZIP") && !fileType.contains("APK") && !fileType.contains("PDF")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "High Shannon Byte Entropy (${String.format(Locale.US, "%.2f", entropy)} / 8.0)",
                    description = "Byte distribution indicates heavy encryption or packing, a primary evasion mechanism used by ransomware and stealth loaders.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Shannon Entropy: ${String.format(Locale.US, "%.2f", entropy)} / 8.0",
                    remediation = "Analyze binary in an isolated sandbox to inspect dynamic unpacking routines."
                )
            )
        }

        // Check for UPX Executable Packer
        val headerSnippet = try {
            String(fileBytes.take(4096).toByteArray(), Charsets.ISO_8859_1)
        } catch (_: Exception) { "" }
        if (headerSnippet.contains("UPX0") || headerSnippet.contains("UPX1") || headerSnippet.contains("UPX!")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "UPX Binary Packer Signature",
                    description = "Executable contains Ultimate Packer for eXecutables (UPX) headers. Threat actors routinely pack malware to evade antivirus definitions.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Packer Header: UPX signature detected in binary",
                    remediation = "Run automated unpacking or dynamic sandbox isolation before execution."
                )
            )
        }

        // Check known malware family signatures
        val lowerText = try {
            String(fileBytes.take(256 * 1024).toByteArray(), Charsets.UTF_8).lowercase(Locale.ROOT)
        } catch (_: Exception) { "" }
        for ((sig, desc) in MALWARE_FAMILY_SIGNATURES) {
            if (lowerText.contains(sig)) {
                findings.add(
                    ScanFinding(
                        id = UUID.randomUUID().toString(),
                        title = "Known Threat Signature: $desc",
                        description = "Detected string pattern associated with $desc.",
                        severity = FindingSeverity.DANGER,
                        evidence = "Matched signature token: '$sig'",
                        remediation = "Quarantine file immediately and submit for reverse-engineering analysis."
                    )
                )
            }
        }

        var isArchive = false
        var inspectedEntriesCount = 0
        var skippedEntriesCount = 0
        var totalDecompressed = 0L
        val archiveEntries = mutableListOf<ArchiveEntryInfo>()
        var pathTraversalDetected = false
        var decompressionLimitHit = false

        if (fileType == "ZIP Archive" || fileType == "APK Package" || isZipHeader(fileBytes)) {
            isArchive = true
            onProgress("Archive Inspection", 0.55f, "Enumerating archive entries and validating paths...")
            if (paceStages) delay(350)

            val zipResult = inspectZipContents(fileBytes, onProgress)
            inspectedEntriesCount = zipResult.inspectedCount
            skippedEntriesCount = zipResult.skippedCount
            totalDecompressed = zipResult.totalDecompressedBytes
            pathTraversalDetected = zipResult.pathTraversalDetected
            decompressionLimitHit = zipResult.decompressionLimitHit
            archiveEntries.addAll(zipResult.entries)
            findings.addAll(zipResult.findings)
        } else if (fileType.contains("PE Executable")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Executable Binary Format (PE)",
                    description = "File starts with Windows 'MZ' executable header. Native binaries can perform arbitrary system calls.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Magic bytes: MZ (0x4D 0x5A)",
                    remediation = "Run native binaries only from verified publishers and in sandboxed environments."
                )
            )
        } else if (fileType.contains("PDF Document")) {
            onProgress("PDF Stream Analysis", 0.60f, "Scanning PDF streams for active scripts and actions...")
            if (paceStages) delay(250)
            inspectPdfBytes(fileBytes, findings)
        } else {
            // Text or unknown stream
            onProgress("Content Heuristics", 0.65f, "Scanning contents for suspicious scripts & patterns...")
            if (paceStages) delay(250)
            inspectTextBytes(fileBytes, findings, sourceName = fileName)
        }

        // Stage 5: Final Evaluation & Risk Scoring
        onProgress("Report Generation", 0.95f, "Correlating findings and synthesizing evidence...")
        if (paceStages) delay(200)

        val risk = evaluateRisk(findings, pathTraversalDetected, decompressionLimitHit)
        val duration = System.currentTimeMillis() - startTime

        onProgress("Inspection Complete", 1.0f, "Scan finalized in ${duration}ms")

        FileInspectionResult(
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            sha256Hash = sha256,
            detectedType = fileType,
            isArchive = isArchive,
            inspectedEntriesCount = inspectedEntriesCount,
            skippedEntriesCount = skippedEntriesCount,
            totalDecompressedBytes = totalDecompressed,
            risk = risk,
            findings = findings,
            archiveEntries = archiveEntries,
            scanDurationMs = duration,
            isPathTraversalDetected = pathTraversalDetected,
            isDecompressionLimitHit = decompressionLimitHit,
            entropy = entropy,
            entropyClassification = entropyClassification
        )
    }

    private data class ZipInspectionInternal(
        val inspectedCount: Int,
        val skippedCount: Int,
        val totalDecompressedBytes: Long,
        val pathTraversalDetected: Boolean,
        val decompressionLimitHit: Boolean,
        val entries: List<ArchiveEntryInfo>,
        val findings: List<ScanFinding>
    )

    private fun inspectZipContents(
        fileBytes: ByteArray,
        onProgress: (stage: String, progress: Float, detail: String) -> Unit
    ): ZipInspectionInternal {
        val findings = mutableListOf<ScanFinding>()
        val entries = mutableListOf<ArchiveEntryInfo>()
        var inspectedCount = 0
        var skippedCount = 0
        var totalDecompressedBytes = 0L
        var pathTraversalDetected = false
        var decompressionLimitHit = false

        try {
            ZipInputStream(ByteArrayInputStream(fileBytes)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    inspectedCount++
                    val entryName = entry.name
                    val isDirectory = entry.isDirectory

                    if (inspectedCount > MAX_ARCHIVE_ENTRIES) {
                        decompressionLimitHit = true
                        findings.add(
                            ScanFinding(
                                id = UUID.randomUUID().toString(),
                                title = "Archive Entry Limit Exceeded",
                                description = "Archive contains more than $MAX_ARCHIVE_ENTRIES entries. Scanning stopped to protect against resource exhaustion.",
                                severity = FindingSeverity.WARNING,
                                evidence = "Entries observed: > $MAX_ARCHIVE_ENTRIES",
                                remediation = "Verify archive source and unpack in an isolated security environment."
                            )
                        )
                        break
                    }

                    // 1. Path Traversal & Zip Slip Check
                    val isTraversal = checkPathTraversal(entryName)
                    if (isTraversal) {
                        pathTraversalDetected = true
                        findings.add(
                            ScanFinding(
                                id = UUID.randomUUID().toString(),
                                title = "Zip Slip / Path Traversal Indicator",
                                description = "Entry specifies path climbing outside root directory: '$entryName'. This is a high-risk security hazard.",
                                severity = FindingSeverity.DANGER,
                                evidence = "Entry path: $entryName",
                                remediation = "Do not extract this archive onto the host filesystem without path sanitization."
                            )
                        )
                    }

                    // 2. Dangerous Extension Check
                    val ext = getExtension(entryName)
                    val isDangerousExt = DANGEROUS_EXTENSIONS.contains(ext)
                    val isElevatedExt = ELEVATED_EXTENSIONS.contains(ext)
                    val isDoubleExt = checkDoubleExtension(entryName)

                    val entryIndicators = mutableListOf<String>()
                    if (isTraversal) entryIndicators.add("Zip Slip Traversal")
                    if (isDangerousExt) entryIndicators.add("Executable extension: .$ext")
                    if (isElevatedExt) entryIndicators.add("Elevated script extension: .$ext")
                    if (isDoubleExt) entryIndicators.add("Double extension disguise")

                    if (isDangerousExt) {
                        findings.add(
                            ScanFinding(
                                id = UUID.randomUUID().toString(),
                                title = "Risky Executable in Archive",
                                description = "Archive bundles an executable format ($ext): '$entryName'.",
                                severity = FindingSeverity.DANGER,
                                evidence = "Entry: $entryName (Type: .$ext)",
                                remediation = "Inspect binary hash against reputation databases prior to execution."
                            )
                        )
                    }

                    if (isDoubleExt) {
                        findings.add(
                            ScanFinding(
                                id = UUID.randomUUID().toString(),
                                title = "Double Extension Disguise",
                                description = "Entry uses multiple extensions ($entryName) commonly seen in spear-phishing lures.",
                                severity = FindingSeverity.WARNING,
                                evidence = "Entry name: $entryName",
                                remediation = "Examine file true MIME type before opening."
                            )
                        )
                    }

                    // 3. Decompression & Content Inspection
                    val buffer = ByteArray(4096)
                    val entryBytes = mutableListOf<Byte>()
                    var entryDecompressed = 0L

                    var bytesRead: Int
                    while (zis.read(buffer).also { bytesRead = it } != -1) {
                        entryDecompressed += bytesRead
                        totalDecompressedBytes += bytesRead

                        if (entryBytes.size < MAX_ENTRY_TEXT_READ_BYTES) {
                            val toTake = Math.min(bytesRead, MAX_ENTRY_TEXT_READ_BYTES - entryBytes.size)
                            for (i in 0 until toTake) {
                                entryBytes.add(buffer[i])
                            }
                        }

                        if (totalDecompressedBytes > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            decompressionLimitHit = true
                            findings.add(
                                ScanFinding(
                                    id = UUID.randomUUID().toString(),
                                    title = "Decompression Size Limit Exceeded (Zip Bomb Shield)",
                                    description = "Total uncompressed payload exceeded ${MAX_TOTAL_UNCOMPRESSED_BYTES / (1024 * 1024)}MB. Inspection halted to safeguard memory.",
                                    severity = FindingSeverity.WARNING,
                                    evidence = "Decompressed bytes: > $totalDecompressedBytes",
                                    remediation = "Review archive size on a dedicated workstation."
                                )
                            )
                            break
                        }
                    }

                    // Compression ratio check
                    val compressedSize = entry.compressedSize
                    if (compressedSize > 0 && entryDecompressed > 1024 * 100) {
                        val ratio = entryDecompressed.toDouble() / compressedSize.toDouble()
                        if (ratio > MAX_SAFE_COMPRESSION_RATIO) {
                            findings.add(
                                ScanFinding(
                                    id = UUID.randomUUID().toString(),
                                    title = "Abnormal Compression Ratio",
                                    description = "Entry '$entryName' exhibits high compression ratio (${String.format(Locale.US, "%.1f", ratio)}:1), characteristic of compression bombs.",
                                    severity = FindingSeverity.WARNING,
                                    evidence = "Ratio: ${String.format(Locale.US, "%.1f", ratio)}:1 ($entryDecompressed vs $compressedSize)",
                                    remediation = "Handle archive with caution."
                                )
                            )
                        }
                    }

                    // Inspect text content of the entry if readable format
                    if (isTextOrScriptEntry(entryName) && entryBytes.isNotEmpty()) {
                        val contentFindings = inspectTextPayload(entryBytes.toByteArray(), entryName)
                        for (f in contentFindings) {
                            entryIndicators.add(f.title)
                            findings.add(f)
                        }
                    }

                    entries.add(
                        ArchiveEntryInfo(
                            name = entryName,
                            sizeBytes = entryDecompressed,
                            compressedSizeBytes = if (compressedSize >= 0) compressedSize else 0L,
                            isDir = isDirectory,
                            isDangerousExtension = isDangerousExt,
                            isPathTraversalAttempt = isTraversal,
                            indicators = entryIndicators
                        )
                    )

                    if (decompressionLimitHit) break
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Archive Parsing Notice",
                    description = "Archive stream encountered an exception during parsing: ${e.message ?: e.javaClass.simpleName}.",
                    severity = FindingSeverity.INFO,
                    evidence = "Error: ${e.message}",
                    remediation = "Ensure the archive is not corrupted or password-protected."
                )
            )
        }

        return ZipInspectionInternal(
            inspectedCount = inspectedCount,
            skippedCount = skippedCount,
            totalDecompressedBytes = totalDecompressedBytes,
            pathTraversalDetected = pathTraversalDetected,
            decompressionLimitHit = decompressionLimitHit,
            entries = entries,
            findings = findings
        )
    }

    fun checkPathTraversal(path: String): Boolean {
        val normalized = path.replace('\\', '/')
        return normalized.contains("../") ||
                normalized.startsWith("../") ||
                normalized.startsWith("/") ||
                normalized.contains("/../") ||
                normalized.endsWith("/..") ||
                normalized.contains(":\\") ||
                normalized.contains(":/")
    }

    private fun checkDoubleExtension(name: String): Boolean {
        val parts = name.split('.')
        if (parts.size >= 3) {
            val last = parts.last().lowercase(Locale.ROOT)
            val secondLast = parts[parts.size - 2].lowercase(Locale.ROOT)
            if (DANGEROUS_EXTENSIONS.contains(last) &&
                (secondLast in listOf("pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "txt"))
            ) {
                return true
            }
        }
        return false
    }

    private fun isTextOrScriptEntry(name: String): Boolean {
        val ext = getExtension(name)
        return ext in setOf(
            "js", "vbs", "ps1", "bat", "cmd", "sh", "txt", "html", "htm",
            "json", "xml", "py", "php", "rb", "pl", "ini", "yaml", "yml"
        )
    }

    private fun inspectTextPayload(bytes: ByteArray, sourceName: String): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()
        val text = try {
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            return findings
        }

        // Script patterns
        for ((pattern, desc) in SUSPICIOUS_SCRIPT_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                val preview = match.value.take(40)
                findings.add(
                    ScanFinding(
                        id = UUID.randomUUID().toString(),
                        title = "Suspicious Script Indicator",
                        description = "$desc found in '$sourceName'.",
                        severity = FindingSeverity.WARNING,
                        evidence = "Matched: '$preview' in $sourceName",
                        remediation = "Review whether script execution is authorized for this document."
                    )
                )
            }
        }

        // Suspicious keywords
        val lowerText = text.lowercase(Locale.ROOT)
        for ((kw, desc) in SUSPICIOUS_KEYWORDS) {
            if (lowerText.contains(kw)) {
                findings.add(
                    ScanFinding(
                        id = UUID.randomUUID().toString(),
                        title = "Security Keyword Indicator: $kw",
                        description = "$desc identified in '$sourceName'.",
                        severity = FindingSeverity.WARNING,
                        evidence = "Found string: '$kw' in $sourceName",
                        remediation = "Inspect context to distinguish security testing from genuine threat."
                    )
                )
            }
        }

        // Exposed credentials
        for ((pattern, desc) in CREDENTIAL_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                findings.add(
                    ScanFinding(
                        id = UUID.randomUUID().toString(),
                        title = "Credential Exposure Indicator",
                        description = "$desc detected in '$sourceName'.",
                        severity = FindingSeverity.DANGER,
                        evidence = "Matched token pattern: ${match.value.take(20)}...",
                        remediation = "Rotate exposed keys and credentials immediately."
                    )
                )
            }
        }

        return findings
    }

    private fun inspectPdfBytes(bytes: ByteArray, findings: MutableList<ScanFinding>) {
        val text = try {
            String(bytes.take(256 * 1024).toByteArray(), Charsets.ISO_8859_1)
        } catch (e: Exception) {
            return
        }

        if (text.contains("/JavaScript") || text.contains("/JS")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Embedded PDF JavaScript",
                    description = "PDF document contains embedded JavaScript streams (/JS or /JavaScript).",
                    severity = FindingSeverity.WARNING,
                    evidence = "Token '/JavaScript' present in PDF dictionary",
                    remediation = "Open PDF with JavaScript disabled in reader."
                )
            )
        }

        if (text.contains("/Launch")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "PDF /Launch Action",
                    description = "PDF document includes a /Launch action which can trigger external program execution.",
                    severity = FindingSeverity.DANGER,
                    evidence = "Token '/Launch' identified in PDF objects",
                    remediation = "Avoid clicking interactive links in untrusted PDF files."
                )
            )
        }

        if (text.contains("/OpenAction") || text.contains("/AA")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "PDF Auto-Execute OpenAction",
                    description = "PDF specifies an automated action to run upon opening.",
                    severity = FindingSeverity.INFO,
                    evidence = "Token '/OpenAction' detected",
                    remediation = "Inspect document in a sandboxed PDF viewer."
                )
            )
        }
    }

    private fun inspectTextBytes(bytes: ByteArray, findings: MutableList<ScanFinding>, sourceName: String) {
        val textFindings = inspectTextPayload(bytes.take(MAX_ENTRY_TEXT_READ_BYTES).toByteArray(), sourceName)
        findings.addAll(textFindings)
    }

    private fun evaluateRisk(
        findings: List<ScanFinding>,
        pathTraversal: Boolean,
        decompressionLimitHit: Boolean
    ): ScanRisk {
        if (pathTraversal) return ScanRisk.HIGH_RISK

        val hasDanger = findings.any { it.severity == FindingSeverity.DANGER }
        if (hasDanger) return ScanRisk.HIGH_RISK

        val warningCount = findings.count { it.severity == FindingSeverity.WARNING }
        if (warningCount >= 2) return ScanRisk.SUSPICIOUS
        if (warningCount == 1) return ScanRisk.LOW_RISK

        if (decompressionLimitHit) return ScanRisk.SUSPICIOUS

        return ScanRisk.SAFE
    }

    fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun detectFileType(fileName: String, bytes: ByteArray): String {
        if (bytes.size >= 4) {
            // ZIP / APK / JAR / DOCX: PK\x03\x04
            if (bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
            ) {
                val ext = getExtension(fileName)
                return when (ext) {
                    "apk" -> "APK Package"
                    "jar" -> "Java Archive (JAR)"
                    "docx" -> "Microsoft Word (DOCX)"
                    "xlsx" -> "Microsoft Excel (XLSX)"
                    else -> "ZIP Archive"
                }
            }

            // PDF: %PDF
            if (bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()
            ) {
                return "PDF Document"
            }

            // Windows PE Executable: MZ
            if (bytes[0] == 0x4D.toByte() && bytes[1] == 0x5A.toByte()) {
                return "Windows PE Executable (EXE/DLL)"
            }

            // Linux ELF: \x7fELF
            if (bytes[0] == 0x7F.toByte() && bytes[1] == 0x45.toByte() &&
                bytes[2] == 0x4C.toByte() && bytes[3] == 0x46.toByte()
            ) {
                return "Linux ELF Binary"
            }
        }

        val ext = getExtension(fileName)
        return when (ext) {
            "txt" -> "Plain Text Document"
            "js" -> "JavaScript Script"
            "vbs" -> "VBScript File"
            "ps1" -> "PowerShell Script"
            "bat" -> "Batch Script"
            "sh" -> "Shell Script"
            "json" -> "JSON Document"
            "apk" -> "APK Package"
            "exe" -> "Executable Binary"
            "pdf" -> "PDF Document"
            else -> if (ext.isNotEmpty()) "$ext File" else "Binary Data"
        }
    }

    private fun isZipHeader(bytes: ByteArray): Boolean {
        return bytes.size >= 4 &&
                bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
    }

    private fun getExtension(name: String): String {
        val i = name.lastIndexOf('.')
        return if (i > 0 && i < name.length - 1) name.substring(i + 1).lowercase(Locale.ROOT) else ""
    }
}
