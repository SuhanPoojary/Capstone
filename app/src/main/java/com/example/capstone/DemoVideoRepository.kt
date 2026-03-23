package com.example.capstone

import android.content.Context
import android.net.Uri
import com.example.capstone.model.LanguageOption
import java.util.Locale

/**
 * Offline video source backed by res/raw.
 *
 * Naming convention used by this repo:
 * - {prefix}_ch{n}_{phase}_{lang}
 *
 * Examples:
 * - flood_ch1_general_en.mp4
 * - flood_ch2_during_hi.mp4
 * - eq_ch3_after_mr.mp4
 */
object DemoVideoRepository {

    // Friendly labels for common language codes. Anything unknown will still show up.
    private val languageLabels: Map<String, String> = mapOf(
        "en" to "English",
        "hi" to "Hindi",
        "mr" to "Marathi",
        "ta" to "Tamil",
        "te" to "Telugu",
        "gu" to "Gujarati",
        "bn" to "Bengali",
        "kn" to "Kannada",
        "ml" to "Malayalam",
        "pa" to "Punjabi",
        "ur" to "Urdu",
        "or" to "Odia",
        "as" to "Assamese"
    )

    /**
     * chapterIndex: 0 = general (pre), 1 = during, 2 = after (post)
     */
    fun getVideoUri(context: Context, disasterKey: String, chapterIndex: Int, languageCode: String): Uri? {
        val prefix = disasterPrefix(disasterKey) ?: return null
        val phase = chapterPhase(chapterIndex) ?: return null

        // prefer language-specific resources
        val base = "${prefix}_ch${chapterIndex + 1}_${phase}_${languageCode.lowercase()}"
        var resId = context.resources.getIdentifier(base, "raw", context.packageName)

        // fallback to legacy naming without language
        if (resId == 0) {
            val legacy = "${prefix}_ch${chapterIndex + 1}_${phase}"
            resId = context.resources.getIdentifier(legacy, "raw", context.packageName)
        }

        if (resId == 0) return null
        return Uri.parse("android.resource://${context.packageName}/$resId")
    }

    /**
     * Returns only languages that actually exist in res/raw for this disaster+chapter.
     *
     * Dynamic: this scans all raw resource entry names, so adding e.g. *_gu, *_ta, *_fr
     * will automatically appear in the dropdown without changing code.
     */
    fun getAvailableLanguages(context: Context, disasterKey: String, chapterIndex: Int): List<LanguageOption> {
        val prefix = disasterPrefix(disasterKey) ?: return emptyList()
        val phase = chapterPhase(chapterIndex) ?: return emptyList()

        val rawFieldNames = try {
            // R.raw fields are the authoritative list of packaged resources.
            R.raw::class.java.fields.mapNotNull { it.name }
        } catch (_: Throwable) {
            emptyList()
        }

        val expectedStart = "${prefix}_ch${chapterIndex + 1}_${phase}_"
        val codes = rawFieldNames
            .asSequence()
            .filter { it.startsWith(expectedStart) }
            .map { it.removePrefix(expectedStart) }
            // safety: only treat a suffix as lang code if it looks like a code
            .map { suffix -> suffix.trim().lowercase(Locale.US) }
            .filter { code -> code.isNotBlank() && code.length in 2..8 }
            .toSet()

        if (codes.isNotEmpty()) {
            return codes
                .sorted()
                .map { code ->
                    LanguageOption(code, prettyLabel(code))
                }
        }

        // If none found (user only added non-suffixed files), show one pseudo language "Default"
        val legacy = "${prefix}_ch${chapterIndex + 1}_${phase}"
        val legacyExists = context.resources.getIdentifier(legacy, "raw", context.packageName) != 0
        return if (legacyExists) listOf(LanguageOption("default", "Default")) else emptyList()
    }

    private fun prettyLabel(code: String): String {
        val normalized = code.lowercase(Locale.US)
        languageLabels[normalized]?.let { return it }

        // Best-effort display name for standard language tags.
        return try {
            val locale = Locale.forLanguageTag(normalized)
            val name = locale.getDisplayLanguage(locale)
            if (name.isNullOrBlank()) normalized.uppercase(Locale.US)
            else name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        } catch (_: Throwable) {
            normalized.uppercase(Locale.US)
        }
    }

    private fun disasterPrefix(disasterKey: String): String? {
        return when (disasterKey.lowercase()) {
            "earthquake" -> "eq"
            "floods", "flood" -> "flood"
            "cyclone" -> "cyclone"
            "landslides", "landslide" -> "landslide"
            else -> null
        }
    }

    private fun chapterPhase(chapterIndex: Int): String? {
        return when (chapterIndex) {
            0 -> "general" // pre
            1 -> "during"
            2 -> "after" // post
            else -> null
        }
    }
}
