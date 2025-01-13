package com.example.voicecontrolradio_pamn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicecontrolradio_pamn.ui.theme.VoiceControlRadioPAMNTheme
import com.example.voicecontrolradio_pamn.ui.theme.app.GlobalColorsPalette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceControlRadioPAMNTheme {
                GradientBackground {
                    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.Transparent) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            AppFrame {
                                Greeting(
                                    name = "Android",
                                    //modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
        /*setContent {
            VoiceControlRadioPAMNTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    GradientBackground()
                    Greeting(
                        name = "Android"
                    )
                }

                /*Scaffold(
                    Box(modifier = Modifier.padding(innerPadding)) {

                    }
                }*/
            }
        }*/
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
        /*colors = listOf(
            GlobalColorsPalette.current.backgroundStart,
            GlobalColorsPalette.current.backgroundEnd
        ),
        startY = 1000f*/
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Content()
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

/*@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoiceControlRadioPAMNTheme {
        GradientBackground {
            Greeting("Android")
        }
    }
}*/