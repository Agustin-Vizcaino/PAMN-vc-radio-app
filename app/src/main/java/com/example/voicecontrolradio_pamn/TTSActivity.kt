package com.example.voicecontrolradio_pamn

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import java.util.Locale


class TTSActivity : ComponentActivity(), OnInitListener {
    private var tts: TextToSpeech? = null
    public var isTtsReady = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TTS
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        2/0
        if (status == TextToSpeech.SUCCESS) {
            // Set the default language to English
            val result = tts!!.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported or missing.")
                Toast.makeText(this, "TTS language not supported.", Toast.LENGTH_SHORT).show()
            } else {
                isTtsReady.value = true
                Toast.makeText(this, "TTS is ready to use.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("TTS", "Initialization failed.")
            Toast.makeText(this, "Failed to initialize TTS.", Toast.LENGTH_SHORT).show()
        }
    }

    // Speak a given text
    fun speak(text: String?) {
        if (isTtsReady.value) {
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            println("ERROR SPEAK NOT READY")
        }
    }

    // Change the language dynamically
    fun setLanguage(locale: Locale?) {
        if (isTtsReady.value) {
            val result = tts!!.setLanguage(locale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Selected language is not supported.")
                Toast.makeText(this, "Selected language is not supported.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Language changed successfully.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "TTS is not ready.", Toast.LENGTH_SHORT).show()
        }
    }

    // Stop speaking immediately
    fun stopSpeaking() {
        if (tts != null && isTtsReady.value) {
            tts!!.stop()
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    // Example usage methods
    fun sayHello() {
        speak("Hello! Welcome to the TTS Activity.")
    }

    fun changeToSpanish() {
        setLanguage(Locale("es", "ES"))
    }
}