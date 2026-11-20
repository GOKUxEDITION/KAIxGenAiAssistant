package com.kaixgen.assistant.domain.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * On-device Android Text-to-Speech controller supporting Hindi/Hinglish/English voices.
 */
class KaiTtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    @Volatile
    private var isReady: Boolean = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                tts?.language = Locale("en", "IN")
            }
        }
    }

    fun speak(text: String, languageCode: String = "en-IN", enabled: Boolean = true) {
        if (!enabled || !isReady || text.isBlank()) return
        try {
            val parts = languageCode.split("-")
            val locale = if (parts.size == 2) Locale(parts[0], parts[1]) else Locale(languageCode)
            val availability = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (availability >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = locale
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kai_utterance_${System.currentTimeMillis()}")
        } catch (_: Exception) {
            // Gracefully ignore TTS errors on headless/emulator devices
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
            // Ignore
        }
        tts = null
        isReady = false
    }
}
