package com.example.scanner

import com.example.model.FindingSeverity
import com.example.model.LinkScanResult
import com.example.model.ScanFinding
import com.example.model.ScanRisk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class UrlValidationResult {
    data class Valid(val uri: URI, val normalizedUrl: String) : UrlValidationResult()
    data class Error(val message: String) : UrlValidationResult()
}

object LinkCheckerEngine {

    const val INVALID_URL_MESSAGE = "Enter a complete http:// or https:// URL."

    private val SUSPICIOUS_TLDS = setOf(
        "xyz", "top", "click", "live", "loan", "work", "download",
        "racing", "kim", "cfd", "sbs", "buzz", "gq", "ml", "tk", "cf"
    )

    private val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "buff.ly", "ow.ly",
        "cutt.ly", "shorturl.at", "rebrand.ly"
    )

    private val TARGET_BRANDS = listOf(
        "paypal", "google", "apple", "microsoft", "amazon", "netflix",
        "chase", "wellsfargo", "bankofamerica", "binance", "coinbase", "steam"
    )

    private val IP_HOST_PATTERN = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$")

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .followRedirects(false) // Inspect landing redirect behavior
        .build()

    fun validateUrl(input: String): UrlValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return UrlValidationResult.Error(INVALID_URL_MESSAGE)
        }

        if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return UrlValidationResult.Error(INVALID_URL_MESSAGE)
        }

        return try {
            val uri = URI(trimmed)
            if (uri.host.isNullOrBlank()) {
                UrlValidationResult.Error(INVALID_URL_MESSAGE)
            } else {
                UrlValidationResult.Valid(uri, trimmed)
            }
        } catch (e: Exception) {
            UrlValidationResult.Error(INVALID_URL_MESSAGE)
        }
    }

    suspend fun scanLink(
        urlInput: String,
        onProgress: (stage: String, progress: Float, detail: String) -> Unit = { _, _, _ -> }
    ): LinkScanResult = withContext(Dispatchers.IO) {
        val validation = validateUrl(urlInput)
        if (validation is UrlValidationResult.Error) {
            throw IllegalArgumentException(validation.message)
        }

        val valid = validation as UrlValidationResult.Valid
        val uri = valid.uri
        val host = uri.host ?: ""
        val scheme = uri.scheme ?: "http"
        val isHttps = scheme.equals("https", ignoreCase = true)

        val findings = mutableListOf<ScanFinding>()

        // Stage 1: Protocol & Scheme Security
        onProgress("Protocol Check", 0.20f, "Verifying transport layer encryption...")
        delay(200)

        if (!isHttps) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Unencrypted Protocol (Plain HTTP)",
                    description = "Connection is unencrypted. Traffic, credentials, and cookies can be intercepted via Man-In-The-Middle attacks.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Scheme: http://",
                    remediation = "Never submit sensitive information or passwords over unencrypted HTTP."
                )
            )
        }

        // Stage 2: Host & Domain Reputational Checks
        onProgress("Domain Heuristics", 0.45f, "Analyzing domain structure and brand similarity...")
        delay(250)

        val lowerHost = host.lowercase(Locale.ROOT)

        // IP Host check
        if (IP_HOST_PATTERN.matcher(lowerHost).matches()) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Numeric IP Address Hostname",
                    description = "URL uses a raw IP address ($lowerHost) instead of a registered domain name, a hallmark of evasion tactics.",
                    severity = FindingSeverity.DANGER,
                    evidence = "Host is IPv4: $lowerHost",
                    remediation = "Verify the destination identity before proceeding."
                )
            )
        }

        // URL Shortener
        if (URL_SHORTENERS.contains(lowerHost)) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "URL Shortener Detected",
                    description = "Destination URL is masked through shortener '$lowerHost', obscuring the final endpoint.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Shortener service: $lowerHost",
                    remediation = "Expand shortened URLs using link previewers before visiting."
                )
            )
        }

        // TLD Check
        val tld = lowerHost.substringAfterLast('.', "")
        if (SUSPICIOUS_TLDS.contains(tld)) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "High-Risk TLD: .$tld",
                    description = "Top-level domain .$tld has historically elevated spam and phishing incidence.",
                    severity = FindingSeverity.WARNING,
                    evidence = "Top-level domain: .$tld",
                    remediation = "Scrutinize site authenticity carefully."
                )
            )
        }

        // IDN Homograph / Punycode check
        if (lowerHost.startsWith("xn--") || lowerHost.contains(".xn--")) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Punycode / Internationalized Domain (IDN)",
                    description = "Domain utilizes Punycode ('xn--'), which can be used to impersonate characters visually (Homograph attack).",
                    severity = FindingSeverity.DANGER,
                    evidence = "Punycode host: $lowerHost",
                    remediation = "Ensure domain characters match genuine Latin alphabet expectations."
                )
            )
        }

        // Brand typosquatting / impersonation heuristic
        val squattedBrand = checkBrandImpersonation(lowerHost)
        if (squattedBrand != null) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Potential Brand Impersonation / Typosquatting",
                    description = "Host looks deceptively similar to prominent brand '$squattedBrand', but differs in spelling or subdomain chaining.",
                    severity = FindingSeverity.DANGER,
                    evidence = "Domain '$lowerHost' resembles protected brand '$squattedBrand'",
                    remediation = "Verify official domain URL directly rather than following external links."
                )
            )
        }

        // Embedded credentials in authority
        if (uri.userInfo != null && uri.userInfo.isNotEmpty()) {
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Credentials in URL Authority",
                    description = "URL contains userinfo authentication prefix, often used to camouflage true domain.",
                    severity = FindingSeverity.WARNING,
                    evidence = "UserInfo: ${uri.userInfo}@",
                    remediation = "Check the true destination domain."
                )
            )
        }

        // Open redirect parameter detection
        val query = uri.query
        if (query != null) {
            val redirectKeys = listOf("url=", "redirect=", "next=", "dest=", "return=", "target=", "goto=")
            for (key in redirectKeys) {
                if (query.lowercase(Locale.ROOT).contains(key)) {
                    findings.add(
                        ScanFinding(
                            id = UUID.randomUUID().toString(),
                            title = "Open Redirect Parameter",
                            description = "Query string contains parameter '$key' which may be leveraged to bounce users to untrusted destinations.",
                            severity = FindingSeverity.WARNING,
                            evidence = "Query snippet: $key in $query",
                            remediation = "Inspect the destination parameter value before opening."
                        )
                    )
                    break
                }
            }
        }

        // Stage 3: Live Connectivity & Headers Probe (Non-blocking fallback)
        onProgress("Security Header Probe", 0.75f, "Probing endpoint security response...")
        delay(250)

        var httpStatus: Int? = null
        val headersMap = mutableMapOf<String, String>()

        var probeLatency = 0L
        val probeStart = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(valid.normalizedUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; CyberShield Security Inspector 2.0)")
                .head()
                .build()

            httpClient.newCall(request).execute().use { response ->
                probeLatency = System.currentTimeMillis() - probeStart
                httpStatus = response.code

                val hsts = response.header("Strict-Transport-Security")
                val csp = response.header("Content-Security-Policy")
                val xcto = response.header("X-Content-Type-Options")
                val xfo = response.header("X-Frame-Options")

                if (hsts != null) headersMap["HSTS"] = hsts
                if (csp != null) headersMap["CSP"] = csp.take(60) + if (csp.length > 60) "..." else ""
                if (xcto != null) headersMap["X-Content-Type-Options"] = xcto
                if (xfo != null) headersMap["X-Frame-Options"] = xfo

                if (response.isRedirect) {
                    val location = response.header("Location")
                    findings.add(
                        ScanFinding(
                            id = UUID.randomUUID().toString(),
                            title = "HTTP Redirect Response (Code ${response.code})",
                            description = "Endpoint redirects traffic to another destination: ${location ?: "Unknown"}.",
                            severity = FindingSeverity.INFO,
                            evidence = "Redirect Location: $location",
                            remediation = "Verify the target redirection landing page."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Offline or timeout, keep static evaluation
            findings.add(
                ScanFinding(
                    id = UUID.randomUUID().toString(),
                    title = "Remote Probe Notice",
                    description = "Remote server probe was not completed (${e.message ?: "Network unreachable"}). Heuristic checks completed successfully.",
                    severity = FindingSeverity.INFO,
                    evidence = "Status: Local heuristic mode only",
                    remediation = "Ensure device has internet connectivity if remote headers are required."
                )
            )
        }

        onProgress("Evaluation Complete", 1.0f, "Link analysis complete.")

        val dangerCount = findings.count { it.severity == FindingSeverity.DANGER }
        val warningCount = findings.count { it.severity == FindingSeverity.WARNING }
        val infoCount = findings.count { it.severity == FindingSeverity.INFO }

        var calculatedScore = 100 - (dangerCount * 35) - (warningCount * 15) - (infoCount * 5)
        if (!isHttps) calculatedScore -= 20
        if (headersMap.containsKey("HSTS")) calculatedScore += 5
        val securityScore = calculatedScore.coerceIn(5, 100)

        val risk = when {
            findings.any { it.severity == FindingSeverity.DANGER } -> ScanRisk.HIGH_RISK
            findings.any { it.severity == FindingSeverity.WARNING } -> ScanRisk.SUSPICIOUS
            findings.any { it.severity == FindingSeverity.INFO } -> ScanRisk.LOW_RISK
            else -> ScanRisk.SAFE
        }

        val summary = when (risk) {
            ScanRisk.SAFE -> "No known malicious patterns or homograph indicators were detected for this URL (Security Score: $securityScore/100)."
            ScanRisk.LOW_RISK -> "Minor security notices identified (e.g. redirects or informational headers). Security Score: $securityScore/100."
            ScanRisk.SUSPICIOUS -> "URL displays indicators of evasion or missing transport security (Security Score: $securityScore/100)."
            ScanRisk.HIGH_RISK -> "Elevated threat detected: potential brand impersonation, raw IP host, or high-risk indicators (Security Score: $securityScore/100)."
        }

        LinkScanResult(
            url = valid.normalizedUrl,
            host = host,
            protocol = scheme.uppercase(Locale.ROOT),
            isHttps = isHttps,
            risk = risk,
            findings = findings,
            summary = summary,
            httpStatus = httpStatus,
            headersInspected = headersMap,
            securityScore = securityScore,
            latencyMs = probeLatency
        )
    }

    private fun checkBrandImpersonation(host: String): String? {
        // Strip common subdomains
        val stripped = host.removePrefix("www.")
            .removePrefix("m.")
            .removePrefix("login.")
            .removePrefix("secure.")

        for (brand in TARGET_BRANDS) {
            // Direct domain match is safe
            if (stripped == "$brand.com" || stripped.endsWith(".$brand.com") ||
                stripped == "$brand.net" || stripped == "$brand.org"
            ) {
                continue
            }

            // Checks if brand appears in hostname but not as official domain
            if (stripped.contains(brand)) {
                // e.g. paypal-security-update.com or paypal.verify-account.com
                return brand
            }

            // Levenshtein distance check on domain label
            val domainLabel = stripped.substringBeforeLast('.').substringAfterLast('.')
            if (domainLabel.length in (brand.length - 1)..(brand.length + 1) && domainLabel != brand) {
                val dist = levenshteinDistance(domainLabel, brand)
                if (dist == 1) {
                    return brand
                }
            }
        }
        return null
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
