package com.callmate.ai.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    var onSpeechCompleted: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun initialize() {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                onError?.invoke("Language not supported for Text-to-Speech")
            } else {
                isInitialized = true
                setupProgressListener()
            }
        } else {
            onError?.invoke("TTS initialization failed with code: $status")
        }
    }

    private fun setupProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeechCompleted?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                onError?.invoke("Error synthesizing voice response")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                onError?.invoke("TTS Error ($errorCode)")
            }
        })
    }

    fun speak(text: String, pitch: Float = 1.0f, rate: Float = 1.0f) {
        if (!isInitialized || textToSpeech == null) {
            initialize()
            // If not yet ready, still inform completion after a brief delay
            return
        }
        textToSpeech?.apply {
            setPitch(pitch)
            setSpeechRate(rate)
            val utteranceId = "utterance_" + System.currentTimeMillis()
            speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            // ignore
        }
    }

    fun release() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
            _isSpeaking.value = false
        } catch (e: Exception) {
            // ignore
        }
    }
}
