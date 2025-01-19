package com.example.voicecontrolradio_pamn

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_ADD
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.numberparser.parseNumberFromText
import com.example.voicecontrolradio_pamn.ui.theme.VoiceControlRadioPAMNTheme
import com.example.voicecontrolradio_pamn.ui.theme.app.GlobalColorsPalette
import de.sfuhrm.radiobrowser4j.ConnectionParams
import de.sfuhrm.radiobrowser4j.EndpointDiscovery
import de.sfuhrm.radiobrowser4j.RadioBrowser
import de.sfuhrm.radiobrowser4j.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.CountDownLatch
import kotlin.math.min


//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class MainActivity : ComponentActivity() {
    private var locale = "es-ES"
    private val CommandController = CommandController2(this, locale)
    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    val tg: ToneGenerator = ToneGenerator(AudioManager.STREAM_ACCESSIBILITY, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CommandController.initTTS()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
        val isProcessing = mutableStateOf(false)
        val level = mutableStateOf(0f)

        enableEdgeToEdge()
        setContent {
            VoiceControlRadioPAMNTheme {
                VoiceLockFrame {
                    SButton(
                        onRecognized = { recognizedText ->
                            CoroutineScope(Dispatchers.IO).launch {
                                CommandController.command(recognizedText)
                            }
                        },
                        provideSpeechLauncher = { launcher ->
                            CommandController.setListener(launcher)
                        },
                        startAnimation = { ->
                            isProcessing.value = true
                        },
                        isRecording = { ->
                            return@SButton isProcessing.value
                        },
                        animationRMS = { value ->
                            level.value = value
                        },
                        endAnimation = {
                            isProcessing.value = false
                        }
                    )
                    PulsatingRing(isProcessing.value, level.value) {
                        CenteredLogo()
                    }
                }
            }
        }
    }

    @Composable
    fun SButton(
        onRecognized: (String) -> Unit,
        provideSpeechLauncher: ((Intent) -> Unit) -> Unit,
        startAnimation: () -> Unit,
        isRecording: () -> Boolean,
        animationRMS: (Float) -> Unit,
        endAnimation: () -> Unit
    ) {
        val startSpeechRecognition: (Intent) -> Unit = { intent ->
            tg.startTone(ToneGenerator.TONE_DTMF_1,50)
            speechRecognizer.startListening(intent)
        }

        LaunchedEffect(Unit) {
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    startAnimation()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    animationRMS(rmsdB)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    endAnimation()
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP)
                }

                override fun onError(error: Int) {
                    endAnimation()
                    onRecognized("error")
                }

                override fun onResults(results: Bundle?) {
                    val recognizedText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: "error"
                    onRecognized(recognizedText)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    endAnimation()
                    onRecognized("error")
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            provideSpeechLauncher(startSpeechRecognition)
        }

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = Color.Transparent,
            onClick = {
                if (isRecording()) {
                    speechRecognizer.stopListening()
                } else {
                    CommandController.click()
                }
            }
        ) {}
    }

    @Composable
    fun PulsatingRing(isProcessing: Boolean, level: Float, content: @Composable () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                val infiniteTransition = rememberInfiniteTransition()
                //val scale = remember { mutableStateOf(1.1f + level/100) }//min(level/1000, 0.3f)) }
                val scale = infiniteTransition.animateFloat(
                    initialValue = 1.1f,
                    targetValue = 1.125f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(750, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = size.minDimension / 3 * (scale.value + min(level/40, 0.3f)),
                        center = center
                    )
                }
            }
            content()
        }
    }

    @Composable
    fun GradientBackground(Content: @Composable () -> Unit) {
        val gradientBrush = Brush.verticalGradient(
            0.5f to GlobalColorsPalette.current.backgroundStart,
            1.0f to GlobalColorsPalette.current.backgroundEnd
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)) {
            Content()
        }
    }

    @Composable
    fun VoiceLockFrame(Content: @Composable () -> Unit) {
        GradientBackground {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppFrame {
                        Content()
                    }
                }
            }
        }
    }

    @Composable
    fun AppFrame(Content: @Composable () -> Unit) {
        var frameColor = GlobalColorsPalette.current.backgroundFrame.copy(alpha = 0.25f)
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val margin = 10.dp.toPx()
                val rectWidth = size.width - 2 * margin
                val rectHeight = size.height - 2 * margin
                val cornerRadius = 10.dp.toPx()
                val color = frameColor

                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(margin, margin),
                    size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Content()
            }
        }

    }

    @Composable
    fun CenteredLogo() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = "Centered logo",
                modifier = Modifier.fillMaxSize(0.66f)
            )
        }
    }
}

@SuppressLint("NewApi")
class CommandController2 (private val context: Context, userLocale: String) {
    var CommandContext = "default"

    var locale = userLocale
    var speechLauncher: ((Intent) -> Unit)? = null
    var ttsIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
    }
    val tg: ToneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)

    var PlayState: String = "default"
    var SearchState: String = "default"
    var player = MediaPlayerManager
    var lastUtterance = ""

    var stationsState = emptyList<Station>()
    var stationsIterator: ListIterator<Station> = stationsState.listIterator()
    var index = 0

    private lateinit var textToSpeechEngine: TextToSpeech

    fun Silence(yesno: Boolean) {
        if (yesno) {
            player.pausePlayer()
        } else {
            player.resumePlayer()
        }
    }

    init {
        CommandContext = "default"
        PlayState = "default"
        SearchState = "default"
        MediaPlayerManager.stopPlayer()
    }

    fun initTTS() {
        textToSpeechEngine = TextToSpeech(context,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale(locale)
                }
            })
    }

    fun setListener(UserSpeechListener: (Intent) -> Unit) {
        speechLauncher = UserSpeechListener
    }

    fun click() {
        Silence(true)
        if (!textToSpeechEngine.isSpeaking()) listen()
        else textToSpeechEngine.stop()
    }

    fun listen() {
        Silence(true)
        CoroutineScope(Dispatchers.Main).launch {
            speechLauncher?.invoke(ttsIntent)
        }
    }

    private fun speak(text: String, keep: Boolean = true) {
        val utteranceId = "tts_${System.currentTimeMillis()}"
        val latch = CountDownLatch(1)

        textToSpeechEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(thisutteranceId: String?) {}

            override fun onDone(thisutteranceId: String?) {
                if (thisutteranceId == utteranceId) {
                    latch.countDown()
                }
            }

            override fun onStop(thisutteranceId: String?, interrupted: Boolean) {
                if (thisutteranceId == utteranceId) {
                    latch.countDown()
                }
            }

            override fun onError(thisutteranceId: String?) {
                if (thisutteranceId == utteranceId) {
                    latch.countDown()
                }
            }
        })

        if (keep) lastUtterance = text
        CoroutineScope(Dispatchers.IO).launch {
            textToSpeechEngine.speak(text, QUEUE_ADD, null, utteranceId)
        }

        try {
            latch.await()
        } catch (e: InterruptedException) {
            println(e.printStackTrace())
        }
    }

    fun fetchStationsAsync(term: String) {
        val myAgent = "pamn/vcr/1.0"
        CoroutineScope(Dispatchers.IO).launch {
            val endpoint = withContext(Dispatchers.IO) {
                EndpointDiscovery(myAgent).discover()
            }

            if (endpoint.isPresent) {
                val radioBrowser = withContext(Dispatchers.IO) {
                    RadioBrowser(
                        ConnectionParams.builder()
                            .apiUrl(endpoint.get())
                            .userAgent(myAgent)
                            .timeout(5000)
                            .build()
                    )
                }

                if (term != "error") {
                    val stations = withContext(Dispatchers.IO) {
                        radioBrowser.listStationsBy(de.sfuhrm.radiobrowser4j.SearchMode.BYNAME,term.replace(" ", "_")).toList()
                    }
                    stationsState = stations
                    stationsIterator = stationsState.listIterator()
                    stationSearchComplete()
                }
            } else {
                println("No endpoint discovered.")
            }
        }
    }

    private fun stationSearchComplete() {
        if (CommandContext == "search") {
            SearchState = "reporting"
            command("", "report")
        }
    }

    fun command(command: String, special: String = "") {
        if (command == "error") {
            speak("Error de escucha", false)
            Silence(false)
            return
        } else if (command == "cerrar") {
            speak("Cerrando aplicación", false)
            player.stopPlayer()
            textToSpeechEngine.shutdown()
            tg.release()
            System.exit(0)
        } else if (command == "repetir") {
            if (lastUtterance != "") speak(lastUtterance, false)
            // Esto es un poco "agresivo", pero no se me ocurre nada mejor
            else speak("Nada que repetir", false)
            return
        } else {
            when (CommandContext) {
                "default" -> command_default(command, special)
                "play" -> command_play(command, special)
                "search" -> command_search(command, special)
                else -> {
                    error("Invalid context. How did you even manage that?")
                }
            }
        }
    }

    private fun command_default(command: String, special: String) {
        when (command) {
            "buscar" -> {
                CommandContext = "search"
                SearchState = "default"
                speak("Indique el nombre de la estación que desea buscar")
                listen()
            }
            else -> {
                speak("$command, comando no reconocido", false)
            }
        }
    }

    private fun command_play(command: String, special: String) {
        if (PlayState == "volume") {
            if (command == "cancelar") {
                speak("Cambio de volumen cancelado")
                PlayState = "default"
            } else {
                val ParseResult = parseNumberFromText(locale,command)
                if (ParseResult.success) {
                    if (ParseResult.value > -1 && ParseResult.value <= 100) {
                        speak("Cambiando volumen a ${ParseResult.value}")
                        MediaPlayerManager.adjustVolume(ParseResult.value)
                        PlayState = "default"
                        MediaPlayerManager.resumePlayer()
                    } else {
                        speak("El nuevo volumen debe estar entre 0 y 100")
                        listen()
                        return
                    }
                } else {
                    speak("Por favor, indique un número entre 0 y 100")
                    listen()
                    return
                }
            }
        } else {
            if (command == "pausar") {
                speak("Pausando reproductor")
                MediaPlayerManager.pausePlayer(true)
            } else if (command == "continuar") {
                speak("Reanudando reproductor")
                MediaPlayerManager.resumePlayer(true)
            } else if (command == "salir") {
                MediaPlayerManager.stopPlayer()
                CommandContext = "default"
                speak("Cerrando reproducción")
            } else if (command == "buscar") {
                CommandContext = "search"
                SearchState = "default"
                speak("Indique el nombre de la estación que desea buscar")
                listen()
                return
            } else if (command == "volumen") {
                PlayState = "volume"
                speak("Indique el nuevo volumen deseado entre 0 y 100")
                listen()
                return
            }
        }
        Silence(false)
    }

    private fun command_search(command: String, special: String) {
        if (command == "cancelar") {
            speak("Búsqueda cancelada")
            SearchState = "default"
            CommandContext = "default"
        } else if (SearchState == "default") {
            SearchState = "searching"
            fetchStationsAsync(command)
            speak("Buscando emisoras con término: $command")
            return
        } else if (SearchState == "searching") {
            speak("Búsqueda en curso", false)
            return
        } else if (SearchState == "reporting") {
            if (special == "report") {
                if (!stationsIterator.hasNext()) {
                    SearchState = "default"
                    speak("No se han encontrado emisoras con el término de búsqueda especificado. Puede indicar otro término o cancelar la búsqueda")
                    listen()
                    return
                } else {
                    index = 0
                    speak("Búsqueda completada. ${stationsState.size} resultados", false)
                    speak("Diga 'siguiente' o 'anterior' para navegar la lista de resultados, 'seleccionar' para empezar la reproducción, 'cancelar' para cancelar")
                    listen()
                    return
                }
            } else {
                if (command == "siguiente") {
                    if (stationsIterator.hasNext()) {
                        index = stationsIterator.nextIndex()
                        val next = stationsIterator.next()
                        speak("Resultado ${index + 1}: ${next.name}")
                        listen()
                        return
                    } else {
                        speak("Fin de la lista de resultados", false)
                        listen()
                        return
                    }
                } else if (command == "anterior") {
                    if (stationsIterator.hasPrevious()) {
                        index = stationsIterator.previousIndex()
                        val previous = stationsIterator.previous()
                        speak("Resultado ${index + 1}: ${previous.name}")
                        listen()
                        return
                    } else {
                        speak("Fin de la lista de resultados", false)
                        listen()
                        return
                    }
                } else if (command == "seleccionar") {
                    CommandContext = "play"
                    PlayState = "default"
                    SearchState = "default"
                    speak("Iniciando reproducción de la emisora ${stationsState[index].name}")
                    MediaPlayerManager.initializePlayer(context, stationsState[index].url)
                }
            }
        }
        Silence(false)
    }
}