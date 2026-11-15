package com.example.no_definido.ui.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Asistente auditivo para Cal-mé-cac.
 * Permite que la aplicación sea 100% interactiva sin necesidad de que el niño sepa leer,
 * hablando en español con voz amigable y emitiendo tonos alegres de retroalimentación.
 *
 * Incluye una cola sincronizada para llamadas a speak() previas a la inicialización de TTS,
 * garantizando que nunca se pierdan las instrucciones de voz al entrar a las actividades.
 */
class SoundAndVoiceHelper(context: Context) {
    private val lock = Any()
    private var tts: TextToSpeech? = null
    @Volatile private var isTtsReady = false
    var isMuted: Boolean = false

    private val pendingSpeechQueue = mutableListOf<String>()

    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (_: Exception) {
        null
    }

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            synchronized(lock) {
                if (status == TextToSpeech.SUCCESS) {
                    val mxLocale = Locale.forLanguageTag("es-MX")
                    val esLocale = Locale.forLanguageTag("es-ES")
                    val generalEs = Locale.forLanguageTag("es")

                    var langResult = tts?.setLanguage(mxLocale)
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        langResult = tts?.setLanguage(esLocale)
                        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts?.setLanguage(generalEs)
                        }
                    }

                    // Tono ligeramente más cálido y pausado para niños
                    tts?.setPitch(1.12f)
                    tts?.setSpeechRate(0.92f)
                    isTtsReady = true

                    // Vaciar y reproducir mensajes acumulados antes de la inicialización
                    if (!isMuted && pendingSpeechQueue.isNotEmpty()) {
                        for ((index, text) in pendingSpeechQueue.withIndex()) {
                            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                            tts?.speak(text, queueMode, null, "calmecac_init_${System.currentTimeMillis()}_$index")
                        }
                    }
                    pendingSpeechQueue.clear()
                } else {
                    Log.w("SoundAndVoiceHelper", "Fallo al inicializar TextToSpeech. Status: $status")
                    isTtsReady = false
                    pendingSpeechQueue.clear()
                }
            }
        }
    }

    /**
     * Reproduce una instrucción de voz en español.
     * Si el motor TTS aún no ha terminado de inicializarse, encola el mensaje de forma segura
     * para reproducirlo inmediatamente en cuanto el motor esté listo.
     */
    fun speak(text: String, flush: Boolean = true) {
        if (isMuted || text.isBlank()) return
        synchronized(lock) {
            if (!isTtsReady) {
                if (flush) {
                    pendingSpeechQueue.clear()
                }
                pendingSpeechQueue.add(text)
                return
            }
            val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(text, queueMode, null, "calmecac_${System.currentTimeMillis()}")
        }
    }

    /**
     * Detiene la reproducción actual y limpia cualquier mensaje pendiente en cola,
     * sin destruir la conexión al servicio TTS (ideal para transiciones entre pantallas).
     */
    fun stop() {
        synchronized(lock) {
            pendingSpeechQueue.clear()
            try {
                tts?.stop()
            } catch (_: Exception) {}
        }
    }

    fun isReady(): Boolean = isTtsReady

    fun playPop() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        } catch (_: Exception) {}
    }

    fun playSuccess() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 160)
        } catch (_: Exception) {}
    }

    fun playGentleTryAgain() {
        if (isMuted) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
        } catch (_: Exception) {}
    }

    /**
     * Libera completamente los recursos de TTS y audio al finalizar la aplicación.
     */
    fun shutdown() {
        synchronized(lock) {
            pendingSpeechQueue.clear()
            isTtsReady = false
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (_: Exception) {}
            try {
                toneGenerator?.release()
            } catch (_: Exception) {}
            tts = null
        }
    }

    companion object {
        @Volatile
        private var instance: SoundAndVoiceHelper? = null

        fun getInstance(context: Context): SoundAndVoiceHelper {
            return instance ?: synchronized(this) {
                instance ?: SoundAndVoiceHelper(context.applicationContext).also { instance = it }
            }
        }

        fun destroyInstance() {
            synchronized(this) {
                instance?.shutdown()
                instance = null
            }
        }
    }
}

/**
 * Composable helper para obtener la instancia compartida y lista de [SoundAndVoiceHelper].
 */
@Composable
fun rememberSoundHelper(): SoundAndVoiceHelper {
    val context = LocalContext.current.applicationContext
    return remember { SoundAndVoiceHelper.getInstance(context) }
}
