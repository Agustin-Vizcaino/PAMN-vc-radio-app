package com.example.numberparser

fun parseNumberFromText(language: String, input: String): Result {
    val parsedValue = when (language) {
        "en-US" -> parseEnglishNumber(input)
        "es-ES" -> parseSpanishNumber(input)
        else -> return Result(false, errorCode = "UnsupportedLanguage")
    }

    return if (parsedValue != null) {
        Result(success = true, value = parsedValue)
    } else {
        Result(false, errorCode = "ParseError")
    }
}

private fun parseEnglishNumber(input: String): Int? {
    val numberMapEn = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
        "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19, "twenty" to 20, "thirty" to 30,
        "forty" to 40, "fifty" to 50, "sixty" to 60, "seventy" to 70,
        "eighty" to 80, "ninety" to 90, "hundred" to 100
    )

    return parseNumberFromMap(input, numberMapEn)
}

private fun parseSpanishNumber(input: String): Int? {
    val numberMapEs = mapOf(
        "cero" to 0, "uno" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4,
        "cinco" to 5, "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9,
        "diez" to 10, "once" to 11, "doce" to 12, "trece" to 13,
        "catorce" to 14, "quince" to 15, "dieciséis" to 16, "diecisiete" to 17,
        "dieciocho" to 18, "diecinueve" to 19, "veinte" to 20, "treinta" to 30,
        "cuarenta" to 40, "cincuenta" to 50, "sesenta" to 60, "setenta" to 70,
        "ochenta" to 80, "noventa" to 90, "cien" to 100
    )

    return parseNumberFromMap(input, numberMapEs)
}

private fun parseNumberFromMap(input: String, numberMap: Map<String, Int>): Int? {
    val words = input.lowercase().split(" ")
    var number = 0
    var temp = 0

    for (word in words) {
        when {
            word in numberMap -> {
                val value = numberMap[word]!!
                if (value == 100) {
                    temp *= value
                } else {
                    temp += value
                }
            }
            word == "y" -> continue // Skip "and"/"y" for Spanish
            else -> return null // Invalid word for a number
        }
    }

    number += temp
    return number
}

data class Result(
    val success: Boolean,
    val value: Int = 0,
    val errorCode: String? = null
)