package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppLinkScanResult
import com.example.model.FileInspectionResult
import com.example.model.LinkScanResult
import com.example.model.ScanProgressState
import com.example.model.ScanRisk
import com.example.model.ScamDetectionResult
import com.example.scanner.AppLinkCheckerEngine
import com.example.scanner.AppLinkValidationResult
import com.example.scanner.FileInspectorEngine
import com.example.scanner.LinkCheckerEngine
import com.example.scanner.SampleDataGenerator
import com.example.scanner.ScamDetectorEngine
import com.example.scanner.ScamValidationResult
import com.example.scanner.UrlValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID

enum class CyberNavTab(val label: String, val shortDesc: String) {
    DASHBOARD("Command Center", "Real-time threat radar & metrics"),
    FILE_INSPECTOR("File Inspector", "Entropy telemetry & safe sandbox"),
    LINK_CHECKER("Link Shield", "Phishing, homographs & SSL probe"),
    APP_LINK_CHECKER("App Link Audit", "Deep link & intent redirection"),
    SCAM_DETECTOR("Fraud Radar", "Social engineering & spoof detection")
}

data class FileInspectorUiState(
    val currentFileName: String? = null,
    val currentFileSize: Long = 0,
    val progress: ScanProgressState = ScanProgressState(),
    val result: FileInspectionResult? = null,
    val errorMessage: String? = null
)

data class LinkCheckerUiState(
    val urlInput: String = "",
    val validationError: String? = null,
    val progress: ScanProgressState = ScanProgressState(),
    val result: LinkScanResult? = null,
    val errorMessage: String? = null
)

data class AppLinkUiState(
    val appLinkInput: String = "",
    val validationError: String? = null,
    val progress: ScanProgressState = ScanProgressState(),
    val result: AppLinkScanResult? = null,
    val errorMessage: String? = null
)

data class ScamDetectorUiState(
    val textInput: String = "",
    val validationError: String? = null,
    val progress: ScanProgressState = ScanProgressState(),
    val result: ScamDetectionResult? = null,
    val errorMessage: String? = null
)

data class DashboardStats(
    val totalScans: Int = 0,
    val threatsFlagged: Int = 0,
    val cleanScans: Int = 0
)

class CyberShieldViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentTab = MutableStateFlow(CyberNavTab.DASHBOARD)
    val currentTab: StateFlow<CyberNavTab> = _currentTab.asStateFlow()

    private val _fileState = MutableStateFlow(FileInspectorUiState())
    val fileState: StateFlow<FileInspectorUiState> = _fileState.asStateFlow()

    private val _linkState = MutableStateFlow(LinkCheckerUiState())
    val linkState: StateFlow<LinkCheckerUiState> = _linkState.asStateFlow()

    private val _appLinkState = MutableStateFlow(AppLinkUiState())
    val appLinkState: StateFlow<AppLinkUiState> = _appLinkState.asStateFlow()

    private val _scamState = MutableStateFlow(ScamDetectorUiState())
    val scamState: StateFlow<ScamDetectorUiState> = _scamState.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats(totalScans = 0, threatsFlagged = 0, cleanScans = 0))
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private var activeJob: Job? = null

    fun selectTab(tab: CyberNavTab) {
        _currentTab.value = tab
    }

    // ==========================================
    // MODULE 1: FILE INSPECTOR HANDLERS
    // (Never touches URL validation!)
    // ==========================================

    fun inspectFileUri(uri: Uri, nameHint: String? = null) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val fileName = nameHint ?: (uri.lastPathSegment ?: "uploaded_file")
                val stream: InputStream? = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    _fileState.update { it.copy(errorMessage = "Could not open file stream.") }
                    return@launch
                }
                val bytes = stream.use { it.readBytes() }
                inspectFileBytes(fileName, bytes)
            } catch (e: Exception) {
                _fileState.update {
                    it.copy(
                        errorMessage = "File inspection failed: ${e.message}",
                        progress = ScanProgressState()
                    )
                }
            }
        }
    }

    fun inspectFileBytes(fileName: String, bytes: ByteArray, paceStages: Boolean = true): Job {
        activeJob?.cancel()
        _fileState.update {
            it.copy(
                currentFileName = fileName,
                currentFileSize = bytes.size.toLong(),
                errorMessage = null,
                result = null,
                progress = ScanProgressState(isScanning = true, stage = "Initializing", progress = 0.05f)
            )
        }

        val job = viewModelScope.launch {
            try {
                val result = FileInspectorEngine.inspectFile(
                    fileName = fileName,
                    fileBytes = bytes,
                    paceStages = paceStages,
                    onProgress = { stage, progress, detail ->
                        _fileState.update {
                            it.copy(
                                progress = ScanProgressState(
                                    isScanning = true,
                                    stage = stage,
                                    progress = progress,
                                    detail = detail
                                )
                            )
                        }
                    }
                )

                _fileState.update {
                    it.copy(
                        result = result,
                        progress = ScanProgressState(isScanning = false)
                    )
                }

                recordScanResult(result.risk)
            } catch (e: Exception) {
                _fileState.update {
                    it.copy(
                        errorMessage = "File scan failed: ${e.message}",
                        progress = ScanProgressState(isScanning = false)
                    )
                }
            }
        }
        activeJob = job
        return job
    }

    fun loadSampleHarmlessZip() {
        val sampleBytes = SampleDataGenerator.createHarmlessSampleZip()
        inspectFileBytes("harmless_diagnostic_sample.zip", sampleBytes)
    }

    fun loadSamplePathTraversalZip() {
        val sampleBytes = SampleDataGenerator.createPathTraversalSampleZip()
        inspectFileBytes("zip_slip_traversal_test.zip", sampleBytes)
    }

    fun loadSampleCleanZip() {
        val sampleBytes = SampleDataGenerator.createCleanSampleZip()
        inspectFileBytes("clean_documents_sample.zip", sampleBytes)
    }

    fun clearFileInspector() {
        activeJob?.cancel()
        _fileState.value = FileInspectorUiState()
    }

    // ==========================================
    // MODULE 2: LINK CHECKER HANDLERS
    // (Dedicated URL validation & scan handler)
    // ==========================================

    fun onUrlInputChanged(newUrl: String) {
        _linkState.update {
            it.copy(
                urlInput = newUrl,
                validationError = null,
                errorMessage = null
            )
        }
    }

    fun scanUrl() {
        val input = _linkState.value.urlInput
        val validation = LinkCheckerEngine.validateUrl(input)
        if (validation is UrlValidationResult.Error) {
            _linkState.update { it.copy(validationError = validation.message) }
            return
        }

        activeJob?.cancel()
        _linkState.update {
            it.copy(
                validationError = null,
                errorMessage = null,
                result = null,
                progress = ScanProgressState(isScanning = true, stage = "Connecting", progress = 0.1f)
            )
        }

        activeJob = viewModelScope.launch {
            try {
                val result = LinkCheckerEngine.scanLink(
                    urlInput = input,
                    onProgress = { stage, progress, detail ->
                        _linkState.update {
                            it.copy(
                                progress = ScanProgressState(
                                    isScanning = true,
                                    stage = stage,
                                    progress = progress,
                                    detail = detail
                                )
                            )
                        }
                    }
                )

                _linkState.update {
                    it.copy(
                        result = result,
                        progress = ScanProgressState(isScanning = false)
                    )
                }

                recordScanResult(result.risk)
            } catch (e: Exception) {
                _linkState.update {
                    it.copy(
                        errorMessage = "Link scan failed: ${e.message}",
                        progress = ScanProgressState(isScanning = false)
                    )
                }
            }
        }
    }

    fun setSampleUrl(url: String) {
        onUrlInputChanged(url)
        scanUrl()
    }

    fun clearLinkChecker() {
        activeJob?.cancel()
        _linkState.value = LinkCheckerUiState()
    }

    // ==========================================
    // MODULE 3: APP LINK CHECKER HANDLERS
    // ==========================================

    fun onAppLinkInputChanged(newLink: String) {
        _appLinkState.update {
            it.copy(
                appLinkInput = newLink,
                validationError = null,
                errorMessage = null
            )
        }
    }

    fun scanAppLink() {
        val input = _appLinkState.value.appLinkInput
        val validation = AppLinkCheckerEngine.validateAppLink(input)
        if (validation is AppLinkValidationResult.Error) {
            _appLinkState.update { it.copy(validationError = validation.message) }
            return
        }

        activeJob?.cancel()
        _appLinkState.update {
            it.copy(
                validationError = null,
                errorMessage = null,
                result = null,
                progress = ScanProgressState(isScanning = true, stage = "Analyzing URI", progress = 0.15f)
            )
        }

        activeJob = viewModelScope.launch {
            try {
                val result = AppLinkCheckerEngine.scanAppLink(
                    rawInput = input,
                    onProgress = { stage, progress, detail ->
                        _appLinkState.update {
                            it.copy(
                                progress = ScanProgressState(
                                    isScanning = true,
                                    stage = stage,
                                    progress = progress,
                                    detail = detail
                                )
                            )
                        }
                    }
                )

                _appLinkState.update {
                    it.copy(
                        result = result,
                        progress = ScanProgressState(isScanning = false)
                    )
                }

                recordScanResult(result.risk)
            } catch (e: Exception) {
                _appLinkState.update {
                    it.copy(
                        errorMessage = "App link scan failed: ${e.message}",
                        progress = ScanProgressState(isScanning = false)
                    )
                }
            }
        }
    }

    fun setSampleAppLink(link: String) {
        onAppLinkInputChanged(link)
        scanAppLink()
    }

    fun clearAppLinkChecker() {
        activeJob?.cancel()
        _appLinkState.value = AppLinkUiState()
    }

    // ==========================================
    // MODULE 4: SCAM DETECTOR HANDLERS
    // ==========================================

    fun onScamInputChanged(text: String) {
        _scamState.update {
            it.copy(
                textInput = text,
                validationError = null,
                errorMessage = null
            )
        }
    }

    fun scanScamText() {
        val input = _scamState.value.textInput
        val validation = ScamDetectorEngine.validateInput(input)
        if (validation is ScamValidationResult.Error) {
            _scamState.update { it.copy(validationError = validation.message) }
            return
        }

        activeJob?.cancel()
        _scamState.update {
            it.copy(
                validationError = null,
                errorMessage = null,
                result = null,
                progress = ScanProgressState(isScanning = true, stage = "Analyzing Semantics", progress = 0.15f)
            )
        }

        activeJob = viewModelScope.launch {
            try {
                val result = ScamDetectorEngine.analyzeScamText(
                    inputText = input,
                    onProgress = { stage, progress, detail ->
                        _scamState.update {
                            it.copy(
                                progress = ScanProgressState(
                                    isScanning = true,
                                    stage = stage,
                                    progress = progress,
                                    detail = detail
                                )
                            )
                        }
                    }
                )

                _scamState.update {
                    it.copy(
                        result = result,
                        progress = ScanProgressState(isScanning = false)
                    )
                }

                recordScanResult(result.risk)
            } catch (e: Exception) {
                _scamState.update {
                    it.copy(
                        errorMessage = "Scam detection failed: ${e.message}",
                        progress = ScanProgressState(isScanning = false)
                    )
                }
            }
        }
    }

    fun setSampleScam(text: String) {
        onScamInputChanged(text)
        scanScamText()
    }

    fun clearScamDetector() {
        activeJob?.cancel()
        _scamState.value = ScamDetectorUiState()
    }

    private fun recordScanResult(risk: ScanRisk) {
        _stats.update {
            val total = it.totalScans + 1
            val threats = if (risk == ScanRisk.HIGH_RISK || risk == ScanRisk.SUSPICIOUS) it.threatsFlagged + 1 else it.threatsFlagged
            val clean = if (risk == ScanRisk.SAFE || risk == ScanRisk.LOW_RISK) it.cleanScans + 1 else it.cleanScans
            it.copy(totalScans = total, threatsFlagged = threats, cleanScans = clean)
        }
    }
}
