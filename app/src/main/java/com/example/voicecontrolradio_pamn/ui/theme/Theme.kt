package com.example.voicecontrolradio_pamn.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.voicecontrolradio_pamn.ui.theme.app.DefaultGlobalPalette
import com.example.voicecontrolradio_pamn.ui.theme.app.GlobalColorsPalette

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun VoiceControlRadioPAMNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Para asegurarnos de usar nuestros colores
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentPalette = DefaultGlobalPalette

    CompositionLocalProvider(
        GlobalColorsPalette provides currentPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }

    /*MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )*/
}