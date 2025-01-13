package com.example.voicecontrolradio_pamn.ui.theme.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GlobalColors (
    val backgroundStart: Color = Color.Unspecified,
    val backgroundEnd: Color = Color.Unspecified,
    val backgroundFrame: Color = Color.Unspecified,

    val headerColor: Color = Color.Unspecified,

    val activeText: Color = Color.Unspecified,
    val inactiveText: Color = Color.Unspecified,
    val success: Color = Color.Unspecified,
    val error: Color = Color.Unspecified,

    val recordingAnimation: Color = Color.Unspecified
)

val BaseOrange = Color(color = 0xFF7F4001)
val BaseBlue = Color(color = 0xFF10345D)
val DimBlue = Color(color = 0xFF4C5E73)

val BaseClear = Color(color = 0xFFD9D9D9)

val ActiveGreen = Color(color = 0xFF12FC35)
val InactiveRed = Color(color = 0xFFFF0000)

val DefaultGlobalPalette = GlobalColors(
    backgroundStart = BaseBlue,
    backgroundEnd = BaseOrange,
    backgroundFrame = BaseClear,
    headerColor = DimBlue,
    activeText = ActiveGreen,
    inactiveText = InactiveRed,
    success = ActiveGreen,
    error = InactiveRed,
    recordingAnimation = BaseClear
)

val GlobalColorsPalette = staticCompositionLocalOf { GlobalColors() }