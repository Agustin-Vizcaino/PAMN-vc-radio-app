package com.example.voicecontrolradio_pamn

import android.app.Activity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.voicecontrolradio_pamn.ui.theme.VoiceControlRadioPAMNTheme
import com.example.voicecontrolradio_pamn.ui.theme.app.GlobalColorsPalette
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.util.Locale

val language = arrayOf("es-ES", Locale.getDefault())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        enableEdgeToEdge()
        setContent {
            VoiceControlRadioPAMNTheme {
                VoiceLockFrame {
                    CenteredLogo()
                    Greeting(
                        name = "Android",
                        //modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            text = "Hello "
        )
        Text(
            text = name,
            color = GlobalColorsPalette.current.activeText
        )
        Text(
            text = "!"
        )
    }
}

@Composable
fun GradientBackground(modifier: Modifier = Modifier, Content: @Composable () -> Unit) {
    val gradientBrush = Brush.verticalGradient(
        0.5f to GlobalColorsPalette.current.backgroundStart,
        1.0f to GlobalColorsPalette.current.backgroundEnd
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
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
                .padding(10.dp) // Match the rectangle's margin
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

/*@Composable
fun CreateLauncher(): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val speechText = remember { mutableStateOf("Your speech will appear here.") }
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data = it.data
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            speechText.value = result?.get(0) ?: "No speech detected."
        } else {
            speechText.value = "[Speech recognition failed.]"
        }
    }
}

@Composable
fun speech() {
    val speechText = remember { mutableStateOf("Your speech will appear here.") }
    val launcher = CreateLauncher()

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    //intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", language);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
    //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Go on then, say something.")
    launcher.launch(intent)
}*/

suspend fun performSpeechRecognition(): String {
    val deferredResult = CompletableDeferred<String>()

    @Composable
    fun CreateLauncher(): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val recognizedText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                deferredResult.complete(recognizedText ?: "No speech detected.")
            } else {
                deferredResult.complete("[Speech recognition failed.]")
            }
        }
    }

    @Composable
    fun StartSpeechRecognition() {
        val launcher = CreateLauncher()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Go on then, say something.")
        }
        launcher.launch(intent)
    }

    StartSpeechRecognition()

    return deferredResult.await()
}

@Composable
fun SpeechRecognitionButton(onSpeechResult: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Button(onClick = {
        scope.launch {
            isLoading = true
            val recognizedSpeech = performSpeechRecognition()
            isLoading = false
            onSpeechResult(recognizedSpeech)
        }
    }) {
        if (isLoading) {
            Text("Listening...") // Replace with your loading animation
        } else {
            Text("Start Speech Recognition")
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val speechText = remember { mutableStateOf("Your speech will appear here.") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data = it.data
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            speechText.value = result?.get(0) ?: "No speech detected."
        } else {
            speechText.value = "[Speech recognition failed.]"
        }
    }
    Column(modifier = modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            //intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", language);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Go on then, say something.")
            launcher.launch(intent)
        }) {
            Text("Start speech recognition")
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Text(speechText.value)
    }
}

/*@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoiceControlRadioPAMNTheme {
        GradientBackground {
            Greeting("Android")
        }
    }
}*/