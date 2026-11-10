package com.example.scanner

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleDataGenerator {

    /**
     * Requirement 12: Harmless sample ZIP containing suspicious-looking test keywords
     * and a non-executing JavaScript sample.
     */
    fun createHarmlessSampleZip(): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // Entry 1: Non-executing JavaScript sample with heuristic keywords
            val jsContent = """
                // CyberShield AI Harmless Verification Diagnostic Script
                // Non-executing sample strictly for scanner heuristic validation.
                function testDiagnosticScanner() {
                    // Suspicious-looking test pattern for heuristic detection:
                    var patternEval = "eval(diagnostic_eval_payload)";
                    var cmdInvocation = "cmd.exe /c echo Harmless";
                    console.log("Non-executing test script: " + patternEval);
                }
            """.trimIndent().toByteArray(Charsets.UTF_8)

            val jsEntry = ZipEntry("diagnostic_script.js")
            jsEntry.size = jsContent.size.toLong()
            zos.putNextEntry(jsEntry)
            zos.write(jsContent)
            zos.closeEntry()

            // Entry 2: Text file with suspicious security keywords
            val txtContent = """
                CyberShield AI Harmless Verification Sample
                ============================================
                This file contains harmless test strings to verify the scanner's
                signature & heuristic pattern matching engine.

                Test security keywords:
                - ransomware (heuristic test indicator)
                - mimikatz (heuristic test indicator)
                - payload (heuristic test indicator)

                Notice: This file contains zero executable exploits or malicious code.
            """.trimIndent().toByteArray(Charsets.UTF_8)

            val txtEntry = ZipEntry("test_indicators_readme.txt")
            txtEntry.size = txtContent.size.toLong()
            zos.putNextEntry(txtEntry)
            zos.write(txtContent)
            zos.closeEntry()

            // Entry 3: Safe benign configuration JSON
            val jsonContent = """
                {
                  "test_suite": "CyberShield AI Diagnostic",
                  "version": "1.0.0",
                  "harmless": true,
                  "status": "diagnostic_ready"
                }
            """.trimIndent().toByteArray(Charsets.UTF_8)

            val jsonEntry = ZipEntry("manifest.json")
            jsonEntry.size = jsonContent.size.toLong()
            zos.putNextEntry(jsonEntry)
            zos.write(jsonContent)
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    /**
     * Creates a test ZIP containing a Zip Slip path traversal entry
     * for verifying path traversal protection.
     */
    fun createPathTraversalSampleZip(): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            val content = "Harmless text inside traversal test entry".toByteArray(Charsets.UTF_8)
            val entry = ZipEntry("../../windows/system32/test_escaped.txt")
            entry.size = content.size.toLong()
            zos.putNextEntry(entry)
            zos.write(content)
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    /**
     * Creates a completely clean benign ZIP archive.
     */
    fun createCleanSampleZip(): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            val content = "Clean document content. Everything is normal and secure.".toByteArray(Charsets.UTF_8)
            val entry = ZipEntry("documents/notes.txt")
            entry.size = content.size.toLong()
            zos.putNextEntry(entry)
            zos.write(content)
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    // Sample URLs for Link Checker
    val SAMPLE_SUSPICIOUS_URL = "http://secure-login.paypal-security-update.xyz/verify-account"
    val SAMPLE_SAFE_URL = "https://www.google.com"
    val SAMPLE_IP_URL = "http://192.168.1.105/admin/login?redirect=http://evil.com"

    // Sample App Links
    val SAMPLE_TOKEN_APP_LINK = "myapp://oauth/callback?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0&redirect=https://partner.com"
    val SAMPLE_SAFE_APP_LINK = "https://example.com/products/view/42"
    val SAMPLE_INTENT_APP_LINK = "intent://scan/#Intent;scheme=zxing;package=com.google.zxing.client.android;end"

    // Sample Scam texts
    val SAMPLE_GEEK_SQUAD_SCAM = """
        GEEK SQUAD INVOICE #GS-88492
        Your annual subscription for Total Tech Support has been auto-renewed for $499.00.
        This amount has been deducted from your bank balance.
        If you did not authorize this charge, you must call our fraud helpline immediately at 1-800-555-0199 within 24 hours.
        Our representative will assist with AnyDesk remote session to cancel the charge and refund you via Target gift card.
    """.trimIndent()

    val SAMPLE_SAFE_MESSAGE = """
        Hi Sarah, the team project meeting is scheduled for tomorrow at 10:00 AM in Conference Room B.
        Please review the presentation slides before the meeting. See you then!
    """.trimIndent()

    val SAMPLE_CRYPTO_SCAM = """
        Elon Musk Special Crypto Giveaway!
        Double your Bitcoin & Ethereum instantly! Send 0.1 BTC to our official pool address and receive 0.2 BTC back immediately!
        Guaranteed returns, 24 hours only. Click here to confirm your wallet address now!
    """.trimIndent()
}
