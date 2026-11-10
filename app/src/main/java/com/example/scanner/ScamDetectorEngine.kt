package com.example.scanner

import com.example.model.FindingSeverity
import com.example.model.ScanFinding
import com.example.model.ScanRisk
import com.example.model.ScamDetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

sealed class ScamValidationResult {
    data class Valid(val cleanText: String) : ScamValidationResult()
    data class Error(val message: String) : ScamValidationResult()
}

object ScamDetectorEngine {

    const val EMPTY_TEXT_MESSAGE = "Enter or paste the message or email text to scan."

    fun validateInput(text: String): ScamValidationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return ScamValidationResult.Error(EMPTY_TEXT_MESSAGE)
        }
        if (trimmed.length < 5) {
            return ScamValidationResult.Error("Please enter at least 5 characters for scam analysis.")
        }
        return ScamValidationResult.Valid(trimmed)
    }

    private data class ScamPattern(
        val category: String,
        val title: String,
        val pattern: Regex,
        val severity: FindingSeverity,
        val description: String,
        val weight: Int
    )

    private val SCAM_PATTERNS = listOf(
        // Urgency / Threats
        ScamPattern(
            category = "Urgency / Pressure",
            title = "Artificial Urgency & Deadline Pressure",
            pattern = Regex("(?:within|in)\\s+(?:24|48|12)\\s+hours|immediately|urgent\\s+action|account\\s+will\\s+be\\s+(?:suspended|closed|terminated)|warrant\\s+(?:issued|for\\s+your\\s+arrest)|police\\s+will\\s+arrive", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.WARNING,
            description = "Scammers manufacture urgent deadlines to induce panic and prevent rational verification.",
            weight = 25
        ),
        // Tech Support / Fake Invoices
        ScamPattern(
            category = "Tech Support & Fake Invoice",
            title = "Geek Squad / Antivirus Auto-Renewal Lure",
            pattern = Regex("geek\\s*squad|norton\\s*(?:lifelock|security)?|mcafee|auto-?renewal|subscription\\s+(?:renewed|charged)|charged\\s+\\$\\d{2,4}", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.DANGER,
            description = "Classic fake invoice scam claiming an unwanted auto-charge of hundreds of dollars, prompting a call to 'cancel'.",
            weight = 35
        ),
        // Remote Access Tools
        ScamPattern(
            category = "Remote Access Hijack",
            title = "Remote Desktop Tool Request",
            pattern = Regex("anydesk|teamviewer|quick\\s*assist|ultraviewer|zoho\\s*assist|logmein|remote\\s*access", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.DANGER,
            description = "Requests the victim install remote screen control tools, allowing attackers to access bank accounts and plant malware.",
            weight = 40
        ),
        // Gift Card & Untraceable Payments
        ScamPattern(
            category = "Payment Fraud",
            title = "Untraceable Payment Demand (Gift Cards / Crypto)",
            pattern = Regex("gift\\s*cards?|target\\s*gift|apple\\s*gift|google\\s*play\\s*card|bitcoin\\s*atm|crypto\\s*transfer|wire\\s*transfer\\s*only|zelle|cash\\s*app", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.DANGER,
            description = "Demanding payment via gift cards or crypto ATMs is 100% indicative of fraud. Legitimate organizations never accept gift cards.",
            weight = 45
        ),
        // Credential / 2FA / OTP Theft
        ScamPattern(
            category = "Credential Theft",
            title = "Two-Factor Auth / OTP Code Solicitation",
            pattern = Regex("(?:send|share|reply\\s+with)\\s+(?:the|your)?\\s*(?:6-?digit|code|otp|verification\\s*code|pin)|verify\\s+your\\s+identity\\s+immediately", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.DANGER,
            description = "Requests to share incoming verification codes aim to bypass two-factor authentication on personal accounts.",
            weight = 45
        ),
        // Government & Authority Impersonation
        ScamPattern(
            category = "Authority Impersonation",
            title = "Government Agency Impersonation (IRS / Law Enforcement)",
            pattern = Regex("irs\\s*(?:agent|department)?|internal\\s*revenue|social\\s*security\\s*administration|fbi\\s*(?:agent)?|customs\\s+and\\s+border|department\\s+of\\s+justice", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.DANGER,
            description = "Impersonation of federal agencies to threaten legal consequences unless payment is immediately remitted.",
            weight = 40
        ),
        // Crypto Investment / Giveaway
        ScamPattern(
            category = "Investment Fraud",
            title = "Cryptocurrency Multiplier / Guaranteed Returns",
            pattern = Regex("guaranteed\\s*(?:profit|returns?)|double\\s+your\\s+(?:bitcoin|crypto|investment)|passive\\s+income\\s+daily|crypto\\s*giveaway", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.WARNING,
            description = "Promises of guaranteed high returns or free crypto giveaways are fraudulent schemes designed to steal deposits.",
            weight = 30
        ),
        // Generic Phishing Call-to-action
        ScamPattern(
            category = "Phishing Lure",
            title = "Suspicious Verification Link Directive",
            pattern = Regex("click\\s+(?:here|the\\s+link)|confirm\\s+your\\s+(?:password|account|details)|login\\s+to\\s+prevent\\s+lockout|re-?activate\\s+now", RegexOption.IGNORE_CASE),
            severity = FindingSeverity.WARNING,
            description = "Directs user to follow external link to authenticate or update sensitive billing credentials.",
            weight = 20
        )
    )

    suspend fun analyzeScamText(
        inputText: String,
        onProgress: (stage: String, progress: Float, detail: String) -> Unit = { _, _, _ -> }
    ): ScamDetectionResult = withContext(Dispatchers.Default) {
        val validation = validateInput(inputText)
        if (validation is ScamValidationResult.Error) {
            throw IllegalArgumentException(validation.message)
        }

        val text = (validation as ScamValidationResult.Valid).cleanText

        onProgress("Text Ingestion", 0.20f, "Tokenizing message and extracting linguistic features...")
        delay(200)

        val findings = mutableListOf<ScanFinding>()
        val recommendations = mutableListOf<String>()
        var scoreAcc = 0
        val detectedCategories = mutableListOf<String>()

        onProgress("Heuristic Pattern Matching", 0.55f, "Matching against threat indicators and social engineering cues...")
        delay(250)

        for (pattern in SCAM_PATTERNS) {
            val match = pattern.pattern.find(text)
            if (match != null) {
                scoreAcc += pattern.weight
                detectedCategories.add(pattern.category)

                findings.add(
                    ScanFinding(
                        id = UUID.randomUUID().toString(),
                        title = pattern.title,
                        description = pattern.description,
                        severity = pattern.severity,
                        evidence = "Matched phrase: '${match.value}'",
                        remediation = "Do not comply with instructions in this message."
                    )
                )
            }
        }

        // Suspicious phone / contact trigger
        val phonePattern = Regex("(?:call|contact\\s+us\\s+at|helpline)\\s*[:]?\\s*(?:\\+?1[-. ]?)?\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})", RegexOption.IGNORE_CASE)
        val phoneMatch = phonePattern.find(text)
        if (phoneMatch != null) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Inbound Toll-Free Helpline Call-to-Action",
                    description = "Message urges calling a direct phone number (${phoneMatch.value}) rather than navigating to official company portals.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Call prompt: ${phoneMatch.value}",
                    remediation = "Never call phone numbers provided in unsolicited emails or SMS."
                )
            )
            scoreAcc += 15
        }

        onProgress("Behavioral Synthesis", 0.85f, "Calculating fraud probability and confidence rating...")
        delay(200)

        val confidence = minOf(100, maxOf(5, scoreAcc))
        val risk = when {
            confidence >= 60 || findings.any { it.severity == FindingSeverity.DANGER } -> ScanRisk.HIGH_RISK
            confidence >= 25 || findings.any { it.severity == FindingSeverity.WARNING } -> ScanRisk.SUSPICIOUS
            findings.isNotEmpty() -> ScanRisk.LOW_RISK
            else -> ScanRisk.SAFE
        }

        val primaryCategory = if (detectedCategories.isNotEmpty()) {
            detectedCategories.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "Suspicious Solicitation"
        } else if (risk == ScanRisk.SAFE) {
            "Benign Content"
        } else {
            "General Security Notice"
        }

        // Tailor actionable recommendations
        if (risk == ScanRisk.HIGH_RISK || risk == ScanRisk.SUSPICIOUS) {
            recommendations.add("Do not click any embedded links or call numbers listed in the message.")
            recommendations.add("Do not disclose passwords, one-time security codes (OTP), or personal details.")
            recommendations.add("If an alleged service is mentioned (e.g. PayPal, Bank, Geek Squad), open their official website independently to check your actual account status.")
            recommendations.add("Block the sender and report the communication as spam/phishing.")
        } else {
            recommendations.add("No definitive scam signals observed. Always exercise routine digital vigilance.")
        }

        val summary = when (risk) {
            ScanRisk.SAFE -> "No known social engineering, urgency tactics, or fraudulent payment indicators were identified."
            ScanRisk.LOW_RISK -> "Minor marketing or promotional markers identified, but no overt fraud detected."
            ScanRisk.SUSPICIOUS -> "Message contains indicators typical of social engineering lures or deceptive claims ($confidence% fraud confidence)."
            ScanRisk.HIGH_RISK -> "High probability scam ($confidence% confidence). Message matches confirmed deceptive extortion, fake invoice, or credential theft patterns."
        }

        onProgress("Analysis Complete", 1.0f, "Scam analysis complete.")

        ScamDetectionResult(
            inputSample = text.take(160) + if (text.length > 160) "..." else "",
            category = primaryCategory,
            confidenceScore = confidence,
            risk = risk,
            flags = findings,
            summary = summary,
            recommendations = recommendations
        )
    }
}
