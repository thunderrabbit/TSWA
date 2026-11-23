package com.robnugen.tswa.temperature

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// 0 °C in kelvin. Top level, not in the companion, because the enum
// entries below need it before the companion object exists.
private const val KELVIN_AT_ZERO_C = 273.15

// The Fahrenheit (and Rankine) degree is 5/9 of a Celsius one.
private const val F_DEGREES_PER_C = 9.0 / 5.0
private const val FAHRENHEIT_AT_ZERO_C = 32.0

// From here up a shown value keeps only ROUGH_FIGURES significant figures,
// and from a million up it reads in words, biggest first.
private val ROUGH_FROM = BigDecimal(5_000)
private const val ROUGH_FIGURES = 3
private val NUMBER_WORDS =
    listOf(
        BigDecimal("1e15") to "quadrillion",
        BigDecimal("1e12") to "trillion",
        BigDecimal("1e9") to "billion",
        BigDecimal("1e6") to "million",
    )

// Commas every three digits whatever the phone's language, trailing
// zeros after the point dropped: 1.80 reads "1.8".
private fun grouped(number: BigDecimal): String =
    DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US)).format(number.stripTrailingZeros())

// Celsius is the only stored temperature; every scale is a view of it.
// The order here is the order the tap steps through.
enum class Scale(
    val symbol: String,
    val fullName: String,
    private val fromCelsius: (Double) -> Double,
) {
    C("°C", "Celsius", { it }),
    F("°F", "Fahrenheit", { it * F_DEGREES_PER_C + FAHRENHEIT_AT_ZERO_C }),
    K("K", "Kelvin", { it + KELVIN_AT_ZERO_C }),
    R("°R", "Rankine", { (it + KELVIN_AT_ZERO_C) * F_DEGREES_PER_C }),
    ;

    fun convert(celsius: Double): Double = fromCelsius(celsius)

    // Below 5,000: one decimal, rounded half away from zero from the number
    // as it is written (Double.toString), not from the double's binary
    // value: -273.15 is stored as -273.1499999..., which would round to
    // -273.1. Never "-0.0". From 5,000 up the source figures are rough, so
    // only 3 significant figures show: "5,500°C", and from a million up in
    // words, "10 trillion °C", never "10000000000273.2K".
    fun format(
        celsius: Double,
        withSymbol: Boolean = true,
    ): String {
        val exact = BigDecimal(convert(celsius).toString())
        val rounded = exact.setScale(1, RoundingMode.HALF_UP)
        if (rounded.abs() < ROUGH_FROM) {
            val number = if (rounded.signum() == 0) "0.0" else rounded.toPlainString()
            return if (withSymbol) number + symbol else number
        }
        val rough = exact.round(MathContext(ROUGH_FIGURES, RoundingMode.HALF_UP))
        val word = NUMBER_WORDS.firstOrNull { rough.abs() >= it.first }
        if (word == null) {
            val number = grouped(rough)
            return if (withSymbol) number + symbol else number
        }
        val number = grouped(rough.divide(word.first)) + " " + word.second
        return if (withSymbol) "$number $symbol" else number
    }

    companion object {
        const val ABSOLUTE_ZERO_C = -KELVIN_AT_ZERO_C

        // On until Settings (mg #893) lets the person choose.
        val DEFAULT_ENABLED: Set<Scale> = setOf(C, F)

        // The scale after [current] in C, F, K, R order that is enabled,
        // wrapping round. With one scale (or none) enabled it stays put.
        fun next(
            current: Scale,
            enabled: Set<Scale>,
        ): Scale {
            val order = entries
            for (step in 1..order.size) {
                val candidate = order[(current.ordinal + step) % order.size]
                if (candidate in enabled) return candidate
            }
            return current
        }
    }
}
