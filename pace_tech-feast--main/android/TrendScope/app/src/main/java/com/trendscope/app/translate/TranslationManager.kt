package com.trendscope.app.translate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslationManager {
    private val translators = mutableMapOf<String, Translator>()
    private val downloadLocks = mutableSetOf<String>()
    private val supportedTranslations = mapOf(
        "hi" to TranslateLanguage.HINDI,
        "bn" to TranslateLanguage.BENGALI,
        "te" to TranslateLanguage.TELUGU,
        "ta" to TranslateLanguage.TAMIL,
        "mr" to TranslateLanguage.MARATHI,
        "gu" to TranslateLanguage.GUJARATI,
        "kn" to TranslateLanguage.KANNADA
    )

    fun getDisplayName(code: String): String {
        return when (code) {
            "hi" -> "हिन्दी"
            "bn" -> "বাংলা"
            "te" -> "తెలుగు"
            "ta" -> "தமிழ்"
            "mr" -> "मराठी"
            "gu" -> "ગુજરાતી"
            "kn" -> "ಕನ್ನಡ"
            "ml" -> "മലയാളം"
            "pa" -> "ਪੰਜਾਬੀ"
            "ur" -> "اردو"
            else -> "English"
        }
    }

    fun getEnglishName(code: String): String {
        return when (code) {
            "hi" -> "Hindi"
            "bn" -> "Bengali"
            "te" -> "Telugu"
            "ta" -> "Tamil"
            "mr" -> "Marathi"
            "gu" -> "Gujarati"
            "kn" -> "Kannada"
            "ml" -> "Malayalam"
            "pa" -> "Punjabi"
            "ur" -> "Urdu"
            else -> "English"
        }
    }

    fun getSupportedLanguages(): List<String> {
        return listOf("hi", "bn", "te", "ta", "mr", "gu", "kn", "ml", "pa", "ur")
    }

    fun canTranslate(code: String): Boolean {
        return supportedTranslations.containsKey(code)
    }

    private fun getOrCreateTranslator(targetLang: String): Translator? {
        if (translators.containsKey(targetLang)) {
            return translators[targetLang]
        }
        val tag = supportedTranslations[targetLang] ?: return null
        return try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(tag)
                .build()
            val translator = Translation.getClient(options)
            translators[targetLang] = translator
            translator
        } catch (e: Exception) {
            null
        }
    }

    fun downloadModel(targetLang: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (!canTranslate(targetLang)) {
            onFailure(Exception("Translation not available for this language"))
            return
        }
        if (downloadLocks.contains(targetLang)) return
        downloadLocks.add(targetLang)
        val translator = getOrCreateTranslator(targetLang) ?: run {
            onFailure(Exception("Failed to create translator"))
            downloadLocks.remove(targetLang)
            return
        }
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                downloadLocks.remove(targetLang)
                onSuccess()
            }
            .addOnFailureListener { e ->
                downloadLocks.remove(targetLang)
                onFailure(e)
            }
    }

    fun translate(text: String, targetLang: String, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        if (text.isEmpty()) {
            onResult("")
            return
        }
        val translator = getOrCreateTranslator(targetLang) ?: run {
            onError(Exception("Translator not available"))
            return
        }
        translator.translate(text)
            .addOnSuccessListener { result ->
                onResult(result)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
        downloadLocks.clear()
    }
}
