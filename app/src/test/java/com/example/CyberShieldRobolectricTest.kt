package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.model.ScanRisk
import com.example.scanner.FileInspectorEngine
import com.example.scanner.LinkCheckerEngine
import com.example.scanner.SampleDataGenerator
import com.example.scanner.UrlValidationResult
import com.example.ui.CyberShieldViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CyberShieldRobolectricTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `confirm ZIP uploads never trigger URL checker or URL error`() = runTest(testDispatcher) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = CyberShieldViewModel(app)

        // Generate harmless test ZIP bytes
        val zipBytes = SampleDataGenerator.createHarmlessSampleZip()

        // Inspect file in File Inspector module without delays and await completion
        viewModel.inspectFileBytes("test_sample.zip", zipBytes, paceStages = false).join()
        advanceUntilIdle()
        ShadowLooper.idleMainLooper()
        advanceUntilIdle()

        // Verify File Inspector UI state has populated result
        val fileState = viewModel.fileState.value
        assertNotNull("File inspector result must not be null", fileState.result)
        assertNull("File inspector error must be null", fileState.errorMessage)
        assertEquals("test_sample.zip", fileState.currentFileName)

        // CRITICAL CHECK: Link checker UI state MUST NEVER be triggered or show URL error!
        val linkState = viewModel.linkState.value
        assertNull("Link checker validation error must be null when uploading ZIP", linkState.validationError)
        assertNotEquals(
            "Enter a complete http:// or https:// URL.",
            fileState.errorMessage
        )
        assertNull("Link scan result must be null", linkState.result)
        assertEquals("", linkState.urlInput)
    }

    @Test
    fun `url validator errors only on invalid url input`() {
        val emptyResult = LinkCheckerEngine.validateUrl("")
        assertTrue(emptyResult is UrlValidationResult.Error)
        assertEquals(
            "Enter a complete http:// or https:// URL.",
            (emptyResult as UrlValidationResult.Error).message
        )

        val nonUrlResult = LinkCheckerEngine.validateUrl("some_archive.zip")
        assertTrue(nonUrlResult is UrlValidationResult.Error)
        assertEquals(
            "Enter a complete http:// or https:// URL.",
            (nonUrlResult as UrlValidationResult.Error).message
        )

        val validResult = LinkCheckerEngine.validateUrl("https://example.com/security")
        assertTrue(validResult is UrlValidationResult.Valid)
    }

    @Test
    fun `harmless sample zip inspection detects suspicious patterns and script`() = runTest(testDispatcher) {
        val harmlessZip = SampleDataGenerator.createHarmlessSampleZip()

        val result = FileInspectorEngine.inspectFile("harmless_diagnostic_sample.zip", harmlessZip, paceStages = false)

        // 1. Verifies SHA-256 calculation
        assertNotNull(result.sha256Hash)
        assertEquals(64, result.sha256Hash.length)

        // 2. Verifies archive entry enumeration
        assertTrue("Must be classified as archive", result.isArchive)
        assertEquals(3, result.inspectedEntriesCount)
        assertEquals(3, result.archiveEntries.size)

        // 3. Verifies entry names
        val entryNames = result.archiveEntries.map { it.name }
        assertTrue(entryNames.contains("diagnostic_script.js"))
        assertTrue(entryNames.contains("test_indicators_readme.txt"))
        assertTrue(entryNames.contains("manifest.json"))

        // 4. Verifies finding detection
        val findingTitles = result.findings.map { it.title }
        val hasScriptIndicator = findingTitles.any { it.contains("Script Indicator") }
        val hasKeywordIndicator = findingTitles.any { it.contains("Security Keyword") }

        assertTrue("Must flag embedded script indicator in JS sample", hasScriptIndicator)
        assertTrue("Must flag test keyword indicators in readme", hasKeywordIndicator)

        // 5. Must not flag path traversal on valid sample
        assertFalse("Must not have path traversal on clean sample", result.isPathTraversalDetected)
    }

    @Test
    fun `zip slip path traversal is caught and flagged as high risk`() = runTest(testDispatcher) {
        val zipSlipBytes = SampleDataGenerator.createPathTraversalSampleZip()
        val result = FileInspectorEngine.inspectFile("malicious_traversal.zip", zipSlipBytes, paceStages = false)

        assertTrue("Path traversal must be detected", result.isPathTraversalDetected)
        assertEquals(ScanRisk.HIGH_RISK, result.risk)

        val traversalFinding = result.findings.find { it.title.contains("Zip Slip") || it.title.contains("Path Traversal") }
        assertNotNull("Must generate a Zip Slip finding", traversalFinding)
    }

    @Test
    fun `clean sample zip returns clean safe risk`() = runTest(testDispatcher) {
        val cleanBytes = SampleDataGenerator.createCleanSampleZip()
        val result = FileInspectorEngine.inspectFile("clean.zip", cleanBytes, paceStages = false)

        assertEquals(ScanRisk.SAFE, result.risk)
        assertTrue(result.findings.isEmpty())
        assertFalse(result.isPathTraversalDetected)
    }

    @Test
    fun `shannon entropy calculation identifies uniform vs random data`() {
        // Zero entropy for uniform byte stream
        val zeroEntropyBytes = ByteArray(256) { 0x41.toByte() }
        val zeroEntropy = FileInspectorEngine.calculateShannonEntropy(zeroEntropyBytes)
        assertEquals(0.0, zeroEntropy, 0.01)

        // Maximum entropy for evenly distributed 256 bytes
        val maxEntropyBytes = ByteArray(256) { it.toByte() }
        val maxEntropy = FileInspectorEngine.calculateShannonEntropy(maxEntropyBytes)
        assertEquals(8.0, maxEntropy, 0.05)
    }

    @Test
    fun `malware family signature detection flags known signatures`() = runTest(testDispatcher) {
        val pegasusPayload = "This diagnostic binary embeds pegasus_agent and cobaltstrike beacon indicators".toByteArray()
        val result = FileInspectorEngine.inspectFile("suspect_sample.bin", pegasusPayload, paceStages = false)

        val familyFindings = result.findings.filter { it.title.contains("Signature") }
        assertTrue("Must flag malware family signature match", familyFindings.isNotEmpty())
        assertEquals(ScanRisk.HIGH_RISK, result.risk)
    }

    @Test
    fun `app link checker audits custom scheme and intent URI`() = runTest(testDispatcher) {
        val result = com.example.scanner.AppLinkCheckerEngine.scanAppLink(
            "myapp://auth/callback?token=secret12345&redirect=http://evil.com"
        )
        assertNotNull(result)
        assertEquals("myapp", result.scheme)
        assertTrue("Must detect token or redirection flaw", result.findings.isNotEmpty())
        assertTrue("Risk must be suspicious or high", result.risk == ScanRisk.SUSPICIOUS || result.risk == ScanRisk.HIGH_RISK)
    }
}
