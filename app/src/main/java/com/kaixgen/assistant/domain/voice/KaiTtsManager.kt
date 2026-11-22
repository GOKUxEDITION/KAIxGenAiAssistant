package com.kaixgen.assistant.domain.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class KaiTtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady: Boolean = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                tts?.language = Locale("hi", "IN")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun speak(
        text: String,
        languageCode: String = "auto",
        enabled: Boolean = true,
        speechRate: Float = 1.02f,
        pitch: Float = 1.0f
    ) {
        if (!enabled || !isReady || text.isBlank()) return
        val resolvedLocale = resolveLocale(text, languageCode)
        runCatching {
            val engine = tts ?: return
            if (engine.isLanguageAvailable(resolvedLocale) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = resolvedLocale
            } else {
                engine.language = Locale("en", "IN")
            }
            engine.setSpeechRate(speechRate.coerceIn(0.6f, 1.8f))
            engine.setPitch(pitch.coerceIn(0.6f, 1.6f))
            engine.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "kai_utterance_${System.currentTimeMillis()}"
            )
        }
    }

    private fun resolveLocale(text: String, languageCode: String): Locale {
        if (languageCode != "auto" && languageCode.contains("-")) {
            val parts = languageCode.split("-")
            return Locale(parts[0], parts[1])
        }
        val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
        val hinglishHints = listOf("hai", "karo", "ho gaya", "battery", "chalu", "band", "aapka", "mera")
        val lower = text.lowercase(Locale.ROOT)
        return if (hasDevanagari || hinglishHints.any { lower.contains(it) }) {
            Locale("hi", "IN")
        } else {
            Locale("en", "IN")
        }
    }

    fun stop() {
        runCatching {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
            _isSpeaking.value = false
        }
        tts = null
        isReady = false
    }
}
