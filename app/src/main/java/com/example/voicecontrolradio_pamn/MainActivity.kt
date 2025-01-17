package com.example.voicecontrolradio_pamn

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import com.example.numberparser.parseNumberFromText
import com.example.voicecontrolradio_pamn.ui.theme.VoiceControlRadioPAMNTheme
import com.example.voicecontrolradio_pamn.ui.theme.app.GlobalColorsPalette
import de.sfuhrm.radiobrowser4j.ConnectionParams
import de.sfuhrm.radiobrowser4j.EndpointDiscovery
import de.sfuhrm.radiobrowser4j.FieldName
import de.sfuhrm.radiobrowser4j.ListParameter
import de.sfuhrm.radiobrowser4j.Paging
import de.sfuhrm.radiobrowser4j.RadioBrowser
import de.sfuhrm.radiobrowser4j.Station
import java.lang.String.valueOf
import java.util.Optional
import java.util.stream.Stream
import java.text.NumberFormat
import kotlin.streams.toList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class MainActivity : ComponentActivity() {
    //private var ReadCommand = mutableStateOf("Initial")
    private val CommandController = CommandController2(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        enableEdgeToEdge()
        setContent {
            VoiceControlRadioPAMNTheme {
                VoiceLockFrame {
                    SButton { recognizedText ->
                        //ReadCommand.value = recognizedText
                        CommandController.Silence(false)
                        CommandController.command(recognizedText)
                        //CommandController.fetchStationsAsync(recognizedText)
                    }
                    CenteredLogo()
                }
            }
        }
    }

    @Composable
    fun SButton(onRecognized: (String) -> Unit) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val recognizedText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                onRecognized(recognizedText ?: "error")
            } else {
                onRecognized("error")
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = Color.Transparent,
            onClick = {
                //ReadCommand.value = "Reading"
                CommandController.Silence(true)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                }
                launcher.launch(intent)
            }) {}
    }

    @Composable
    fun GradientBackground(modifier: Modifier = Modifier, Content: @Composable () -> Unit) {
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
                        StationListDisplay(CommandController.stationsState.value)
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
                contentDescription = "Centered Logo",
                modifier = Modifier.fillMaxSize(0.66f)
            )
        }
    }

    @Composable
    fun StationListDisplay(stations: List<Station>) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (stations.isEmpty()) {
                // Display a loading or empty state
                androidx.compose.material3.Text("Fetching stations...", color = Color.White)
            } else {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(stations) { station ->
                        androidx.compose.material3.Text(
                            text = "${station.name}: ${station.url}",
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// Link Radio Clásica
// https://dispatcher.rndfnk.com/crtve/rnerc/main/mp3/high

@SuppressLint("NewApi")
class CommandController2 (private val context: Context) {
    var CommandContext: String = "default"
    var PlayState: String = "default"
    var SearchState: String = "default"
    // Debería poder usar esto para matar a la app, pero no puedo acceder a esta propiedad, comprobar más tarde
    //val activity = context as Activity
    var player = MediaPlayerManager
    //Put API responses here
    var response: MutableList<Station>? = null

    public fun Silence(yesno: Boolean) {
        if (yesno) player.pausePlayer()
        else player.resumePlayer(true)
    }

    // Radio search stuff
    /*var endpoint: Optional<String?>? = EndpointDiscovery("pamn/vcr/0.1").discover()
    var radioBrowser: RadioBrowser = RadioBrowser(
        ConnectionParams.builder().apiUrl(endpoint?.get() ?: "error").userAgent("pamn/vcr/0.1").timeout(5000).build()
    )*/

    init {
        CommandContext = "default"
        /*var r: Stream<Station> = radioBrowser.listStationsBy(de.sfuhrm.radiobrowser4j.SearchMode.BYNAME,"radio clásica")
        // Esto puede causar problemas debido a la versión de la API...
        response = r.toList()
        error(response.toString())*/
    }

    /*public fun fetchStationsAsync() {
        val myAgent = "pamn/vcr/0.1"
        CoroutineScope(Dispatchers.Main).launch {
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

                val stations = withContext(Dispatchers.IO) {
                    radioBrowser.listStations(ListParameter.create().order(FieldName.NAME)).limit(64)
                }

                // Handle the stations on the main thread (UI thread)
                stations.forEach { station ->
                    println("${station.name}: ${station.url}")
                }
            } else {
                println("No endpoint discovered.")
            }
        }
    }*/

    var stationsState = mutableStateOf<List<Station>>(emptyList())


    public fun fetchStationsAsync(term: String) {
        val myAgent = "pamn/vcr/0.1"
        CoroutineScope(Dispatchers.Main).launch {
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
                        println(term)
                        radioBrowser.listStationsBy(de.sfuhrm.radiobrowser4j.SearchMode.BYNAME,term.replace(" ", "_")).toList()
                        //radioBrowser.listStations(ListParameter.create().order(FieldName.NAME)).limit(64).toList()
                    }

                    // Update the state with fetched stations
                    stationsState.value = stations
                }
            } else {
                println("No endpoint discovered.")
            }
        }
    }

    public fun command(command: String) {
        if (command == "cerrar") {
            player.stopPlayer()
            // La idea aquí es cerrar todo
            //finishAffinity(activity)
        }
        //print(CommandContext)
        when (CommandContext) {
            "default" -> command_default(command)
            "play" -> command_play(command)
            "search" -> command_search(command)
            else -> {
                error("Invalid context. How did you even manage that?")
            }
        }
    }

    private fun command_default(command: String) {
        when (command) {
            "buscar" -> CommandContext = "search"
            else -> {
                return
                TODO("Warn user of recog error")
            }
        }
    }

    private fun command_play(command: String) {
        if (PlayState == "volume") {
            if (command == "cancelar") {
                PlayState = "default"
                MediaPlayerManager.resumePlayer(true)
                return
                TODO("Tell user that volume change was canceled")
            } else {
                val ParseResult = parseNumberFromText("es-ES",command)
                if (ParseResult.success) {
                    if (ParseResult.value > -1 && ParseResult.value <= 100) {
                        print("cosa: $ParseResult.value")
                        MediaPlayerManager.adjustVolume(ParseResult.value)
                        PlayState = "default"
                        MediaPlayerManager.resumePlayer(true)
                    } else {
                        print("cosa: error $ParseResult.value")
                        return
                        TODO("Tell user that volume needs to be clamped")
                    }
                }
            }
        } else {
            if (command == "pausar") {
                MediaPlayerManager.pausePlayer(true)
            } else if (command == "continuar") {
                MediaPlayerManager.resumePlayer(true)
            } else if (command == "salir") {
                MediaPlayerManager.stopPlayer()
                CommandContext = "default"
            } else if (command == "buscar") {
                MediaPlayerManager.pausePlayer()
                CommandContext = "search"
                return
                TODO("Tell user we're searching")
            } else if (command == "volumen") {
                MediaPlayerManager.pausePlayer()
                PlayState = "volume"
                return
                TODO("Tell user to say the new volume value")
            }
        }
    }

    private fun command_search(command: String) {
        if (command == "cancelar") {
            CommandContext = "default"
            return
            TODO("Talk to user")
        } else if (stationsState.value.isEmpty()) {
            fetchStationsAsync(command)
        } else if (command == "seleccionar") {
            MediaPlayerManager.initializePlayer(context, stationsState.value[0].url)
            CommandContext = "play"
            PlayState = "default"
        } else if (command == "siguiente") {
            stationsState.value = stationsState.value.drop(1)
            if (stationsState.value.isEmpty()) {
                if (!player.checkPlayer()) CommandContext = "default"
                else {
                    CommandContext = "play"
                    player.resumePlayer(true)
                }
                return
                TODO("Tell the user that there's no more results")
            }
        }
        /*if (response != null)
        when (command) {
            //"cancel" ->
        }*/
    }
}


/*class CommandContextController {
    private var contextStack: ArrayDeque<Context> = ArrayDeque()

    init {
        contextStack.add(DefaultContext())
    }

    private interface Context {
        // Identificador del contexto
        val id: String
        // Palabras/comandos/tipos que acepta este contexto
        val trigger: Array<String>
        // Si este contexto puede ser cancelado
        val cancellable: Boolean
        // Contextos alcanzables desde este contexto, si el usuario usa un "trigger" aceptado por alguno de estos se pasa a ese contexto
        val children: Array<String>
        // Qué decirle al usuario si pasa un comando de voz no aceptado por ningún hijo
        val invalid: String

        // Qué hacer cuando se alcanza este contexto
        public fun onArrival()

        // Qué hacer cuando el usuario pasa un comando de voz
        public fun onCommand(command: String)

        public fun onPause()

        public fun onResume()

        public fun onDeparture()
    }

    private class DefaultContext : Context {
        override val id = "gen/def"
        override val trigger = arrayOf("")
        override val cancellable = false
        override val children = arrayOf("gen/search", "gen/play")
        override val invalid = "Lo siento, no he entendido el comando"

        override fun onArrival() {
            TODO("Not yet implemented")
        }

        override fun onCommand(command: String) {
            if (command == "search") {

            }
        }
    }
}

/*class CommandViewModel : ViewModel() {
    var commandContext by mutableStateOf<CommandContext>(CommandContext.Default)
        private set

    var feedbackMessage by mutableStateOf("Awaiting command...")
        private set

    fun processCommand(input: String) {
        when (commandContext) {
            is CommandContext.Default -> handleDefaultCommand(input)
            is CommandContext.VolumeAdjustment -> handleVolumeCommand(input)
            is CommandContext.Custom -> handleCustomCommand(input)
        }
    }

    private fun handleDefaultCommand(input: String) {
        when {
            input.contains("change volume", ignoreCase = true) -> {
                feedbackMessage = "Volume command detected. Please specify a level."
                commandContext = CommandContext.VolumeAdjustment
            }
            input.contains("play music", ignoreCase = true) -> {
                feedbackMessage = "Playing music..."
                playMusic()
            }
            else -> feedbackMessage = "Command not recognized."
        }
    }

    private fun handleVolumeCommand(input: String) {
        input.toIntOrNull()?.let { level ->
            feedbackMessage = "Setting volume to $level."
            setVolume(level)
            commandContext = CommandContext.Default
        } ?: run {
            feedbackMessage = "Invalid volume level. Try again."
        }
    }

    private fun handleCustomCommand(input: String) {
        feedbackMessage = "Custom command: $input"
    }

    private fun playMusic() {
        // Trigger music playback logic here
    }

    private fun setVolume(level: Int) {
        // Adjust volume logic here
    }
}*/