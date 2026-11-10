package com.example.scanner

import android.net.Uri
import com.example.model.AppLinkScanResult
import com.example.model.FindingSeverity
import com.example.model.ScanFinding
import com.example.model.ScanRisk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

sealed class AppLinkValidationResult {
    data class Valid(val uri: Uri, val rawInput: String) : AppLinkValidationResult()
    data class Error(val message: String) : AppLinkValidationResult()
}

object AppLinkCheckerEngine {

    const val INVALID_APP_LINK_MESSAGE = "Enter a valid deep link, app scheme (e.g. app://path), or intent:// URI."

    fun validateAppLink(input: String): AppLinkValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return AppLinkValidationResult.Error(INVALID_APP_LINK_MESSAGE)
        }

        return try {
            val uri = Uri.parse(trimmed)
            val scheme = uri.scheme
            if (scheme.isNullOrBlank()) {
                AppLinkValidationResult.Error(INVALID_APP_LINK_MESSAGE)
            } else {
                AppLinkValidationResult.Valid(uri, trimmed)
            }
        } catch (e: Exception) {
            AppLinkValidationResult.Error(INVALID_APP_LINK_MESSAGE)
        }
    }

    suspend fun scanAppLink(
        rawInput: String,
        onProgress: (stage: String, progress: Float, detail: String) -> Unit = { _, _, _ -> }
    ): AppLinkScanResult = withContext(Dispatchers.Default) {
        val validation = validateAppLink(rawInput)
        if (validation is AppLinkValidationResult.Error) {
            throw IllegalArgumentException(validation.message)
        }

        val valid = validation as AppLinkValidationResult.Valid
        val uri = valid.uri
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: ""
        val host = uri.host ?: ""
        val path = uri.path ?: ""
        val isIntentUri = rawInput.startsWith("intent:", ignoreCase = true)

        val findings = mutableListOf<ScanFinding>()
        val recommendations = mutableListOf<String>()

        onProgress("Scheme Analysis", 0.25f, "Inspecting URI scheme and intent semantics...")
        delay(200)

        // 1. Custom scheme vs Verified App Link
        if (scheme == "http" || scheme == "https") {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Universal App Link (HTTP/S)",
                    description = "URI uses standard Web protocol. On Android, this should be anchored with verified Digital Asset Links (.well-known/assetlinks.json).",
                    severity = FindingSeverity.INFO,
                    evidence = "Scheme: $scheme://$host",
                    remediation = "Ensure autoVerify=true is configured in the AndroidManifest and assetlinks.json is hosted."
                )
            )
            recommendations.add("Verify domain ownership via https://$host/.well-known/assetlinks.json.")
        } else if (isIntentUri) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Android Intent URI Format",
                    description = "Explicit Intent URI detected. Intent URIs can launch specific activities, services, or broadcasts directly.",
                    severity = FindingSeverity.WARNING,
                    evidence = rawInput.take(60),
                    remediation = "Ensure intent filters reject unvalidated explicit components."
                )
            )
            inspectIntentPayload(rawInput, findings, recommendations)
        } else {
            // Custom Scheme (e.g. myapp://, fb://, slack://)
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Custom Scheme Interception Risk",
                    description = "Custom scheme '$scheme://' is not centrally claimed on Android. Any malicious app on device can declare the same scheme to hijack user data.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Scheme declared: $scheme://",
                    remediation = "Migrate to Android App Links (HTTPS with Digital Asset Links) to prevent deep link interception."
                )
            )
            recommendations.add("Upgrade from custom scheme '$scheme://' to verified Android App Links.")
        }

        // 2. Query Parameter & Token Leakage Inspection
        onProgress("Parameter Audit", 0.60f, "Auditing query parameters for sensitive credentials...")
        delay(250)

        val queryParamMap = mutableMapOf<String, String>()
        try {
            val queryNames = uri.queryParameterNames
            for (name in queryNames) {
                val value = uri.getQueryParameter(name) ?: ""
                queryParamMap[name] = value

                val lowerName = name.lowercase(Locale.ROOT)

                // Sensitive token passing
                if (lowerName in listOf("token", "access_token", "auth_token", "secret", "password", "api_key", "session", "code")) {
                    findings.add(
                        ScanFinding(
                            id = UUID.randomUUID().toString(),
                            title = "Sensitive Token in Query Parameters: '$name'",
                            description = "Sensitive credential parameter '$name' is passed in plain query string. Deep link queries are logged in system history and intent dumps.",
                            severity = FindingSeverity.DANGER,
                            evidence = "Parameter $name=${value.take(4)}... (length: ${value.length})",
                            remediation = "Use PKCE (Proof Key for Code Exchange) or short-lived authorization codes exchanged via secure POST."
                        )
                    )
                    recommendations.add("Remove sensitive tokens from deep link query strings.")
                }

                // Arbitrary redirect parameter
                if (lowerName in listOf("url", "redirect", "redirect_url", "dest", "target", "webview_url")) {
                    findings.add(
                        ScanFinding(
                            id = UUID.randomUUID().toString(),
                            title = "Open Deep Link Redirect Target: '$name'",
                            description = "Parameter '$name' takes an external URL ($value) which might cause internal WebViews or browser intents to navigate to untrusted targets.",
                            severity = FindingSeverity.WARNING,
                            evidence = "Parameter $name=$value",
                            remediation = "Strictly whitelist allowed redirect domains in the app code."
                        )
                    )
                    recommendations.add("Validate and whitelist '$name' URL parameters against trusted hosts.")
                }
            }
        } catch (e: Exception) {
            // Malformed query
        }

        onProgress("Risk Synthesis", 0.95f, "Finalizing deep link security posture...")
        delay(200)

        val risk = when {
            findings.any { it.severity == FindingSeverity.DANGER } -> ScanRisk.HIGH_RISK
            findings.any { it.severity == FindingSeverity.WARNING } -> ScanRisk.SUSPICIOUS
            findings.any { it.severity == FindingSeverity.INFO } -> ScanRisk.LOW_RISK
            else -> ScanRisk.SAFE
        }

        val summary = when (risk) {
            ScanRisk.SAFE -> "Deep link appears compliant with standard deep linking conventions."
            ScanRisk.LOW_RISK -> "Standard deep link structure identified with minor best practice recommendations."
            ScanRisk.SUSPICIOUS -> "Deep link uses unverified custom schemes or open redirect targets susceptible to interception."
            ScanRisk.HIGH_RISK -> "High risk deep link: contains plain sensitive tokens or unconstrained intent launch components."
        }

        AppLinkScanResult(
            rawUri = rawInput,
            scheme = scheme,
            host = host,
            path = path,
            queryParams = queryParamMap,
            isIntentUri = isIntentUri,
            risk = risk,
            findings = findings,
            summary = summary,
            recommendations = recommendations.distinct()
        )
    }

    private fun inspectIntentPayload(
        intentString: String,
        findings: MutableList<ScanFinding>,
        recommendations: MutableList<String>
    ) {
        if (!intentString.contains(";end")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Malformed Intent Termination",
                    description = "Intent URI lacks terminating ';end' delimiter.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Lacks ';end' closure",
                    remediation = "Format Intent URIs strictly per Android Intent.toUri(URI_INTENT_SCHEME)."
                )
            )
        }

        if (intentString.contains("component=", ignoreCase = true)) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Explicit Component Declaration in Intent",
                    description = "Intent explicitly specifies internal target component. Ensure target activity is exported and guarded with permissions.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Contains 'component=' directive",
                    remediation = "Prefer implicit actions or verify caller permissions."
                )
            )
            recommendations.add("Guard targeted activity with signature-level permissions.")
        }

        if (intentString.contains("action=android.intent.action.VIEW", ignoreCase = true)) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Standard View Intent Action",
                    description = "Intent triggers standard ACTION_VIEW handler.",
                    severity = FindingSeverity.INFO,
                    evidence = "Action: android.intent.action.VIEW",
                    remediation = "Ensure internal WebViews validate destination URLs."
                )
            )
        }
    }
}
