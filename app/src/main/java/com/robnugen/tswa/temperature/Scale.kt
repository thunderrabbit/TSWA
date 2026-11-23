package com.robnugen.tswa.temperature

import java.math.BigDecimal
import java.math.RoundingMode

// 0 °C in kelvin. Top level, not in the companion, because the enum
// entries below need it before the companion object exists.
private const val KELVIN_AT_ZERO_C = 273.15

// The Fahrenheit (and Rankine) degree is 5/9 of a Celsius one.
private const val F_DEGREES_PER_C = 9.0 / 5.0
private const val FAHRENHEIT_AT_ZERO_C = 32.0

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

    // One decimal, rounded from the double's exact value as the web
    // version's toFixed(1) did (so absolute zero reads -273.1 °C), and
    // never "-0.0".
    fun format(
        celsius: Double,
        withSymbol: Boolean = true,
    ): String {
        val rounded = BigDecimal(convert(celsius)).setScale(1, RoundingMode.HALF_UP)
        val number = if (rounded.signum() == 0) "0.0" else rounded.toPlainString()
        return if (withSymbol) number + symbol else number
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
