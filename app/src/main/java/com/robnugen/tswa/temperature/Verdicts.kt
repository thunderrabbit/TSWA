package com.robnugen.tswa.temperature

import kotlin.math.abs

// What a temperature means, shown under the converter's big number.
// Texts and numbers are carried over word for word from the web version.
data class Verdict(
    val title: String,
    val detail: String,
)

// A verdict covering every temperature up to and including [upToC].
private data class Band(
    val upToC: Double,
    val verdict: Verdict,
) {
    constructor(upToC: Double, title: String, detail: String) : this(upToC, Verdict(title, detail))
}

data class Landmark(
    val label: String,
    val celsius: Double,
    val detail: String,
)

data class Preset(
    val label: String,
    val celsius: Double,
)

object Verdicts {
    // Each verdict covers everything up to and including its limit (°C).
    private val bands: List<Band> =
        listOf(
            Band(
                upToC = -273.15,
                "Absolute zero",
                "Nothing moves. Nothing can be colder; this is the floor of physics.",
            ),
            Band(upToC = -219.0, "Oxygen liquefies", "Air itself turns to liquid down here. Not picnic weather."),
            Band(
                upToC = -196.0,
                "Liquid nitrogen boils",
                "It boils at this temperature. Your drink would shatter, not freeze.",
            ),
            Band(
                upToC = -138.8,
                "Colder than anywhere on Earth, ever",
                "No weather station on Earth has ever recorded anything close to this.",
            ),
            Band(upToC = -89.2, "Vostok, Antarctica, 1983", "The coldest natural temperature ever measured on Earth."),
            Band(
                upToC = -78.5,
                "Dry ice sublimates",
                "Solid carbon dioxide turns straight to gas here. Spooky fog included.",
            ),
            Band(
                upToC = -40.0,
                "The crossover point",
                "-40 is the one temperature that is the same in Celsius and Fahrenheit.",
            ),
            Band(upToC = -39.9, "Dangerously cold", "Exposed skin can freeze in minutes. Stay inside."),
            Band(upToC = -0.1, "Below freezing", "Water is ice, roads are treacherous, pipes are nervous."),
            Band(upToC = 0.0, "Freezing point", "Water freezes at 0 C / 32 F. Exactly here."),
            Band(upToC = 8.0, "Cold", "Proper coat weather."),
            Band(upToC = 15.0, "Cool", "A jumper, maybe. Light jacket, definitely."),
            Band(upToC = 22.0, "Mild", "Perfectly pleasant nothing-to-complain-about weather."),
            Band(upToC = 27.0, "Pleasant", "T-shirt weather. Enjoy it."),
            Band(upToC = 32.0, "Warm", "Shorts weather for most humans."),
            Band(upToC = 38.0, "Hot", "Seek shade, drink water, slow down."),
            Band(upToC = 45.0, "Very hot", "Dangerous for exertion. Adelaide knows this one well."),
            Band(
                upToC = 56.7,
                "Extreme heat",
                "Approaching the hottest ever measured on Earth: 56.7 C / 134 F, Death Valley, 1913.",
            ),
            Band(
                upToC = 100.0,
                "Hotter than any weather on Earth",
                "No place on Earth gets this hot. From here on, it is kitchens and physics.",
            ),
        )
    private val beyond =
        Verdict("Oven territory", "Roasts, pizzas and burning paper live up here. Not weather any more.")

    val landmarks: List<Landmark> =
        listOf(
            Landmark(
                "Absolute zero",
                celsius = -273.15,
                "Nothing moves. Nothing can be colder; this is the floor of physics.",
            ),
            Landmark("Oxygen liquefies", celsius = -218.8, "Air itself turns to liquid at this temperature."),
            Landmark(
                "Liquid nitrogen boils",
                celsius = -195.8,
                "Liquid nitrogen boils here. Things dipped in it shatter like glass.",
            ),
            Landmark(
                "Vostok record - coldest ever on Earth",
                celsius = -89.2,
                "Vostok, Antarctica, 1983: the coldest natural temperature ever measured.",
            ),
            Landmark(
                "Dry ice sublimates",
                celsius = -78.5,
                "Solid carbon dioxide turns straight to gas. Spooky fog included.",
            ),
            Landmark(
                "F/C crossover - Celsius equals Fahrenheit",
                celsius = -40.0,
                "The one temperature that reads the same on both scales.",
            ),
            Landmark("Freezing - water freezes", celsius = 0.0, "Water freezes at 0 C / 32 F. Exactly here."),
            Landmark("Body temp", celsius = 37.0, "37 C / 98.6 F: human body temperature. You, right now."),
            Landmark(
                "Death Valley record - hottest ever on Earth",
                celsius = 56.7,
                "56.7 C / 134 F, Death Valley, 1913: the hottest ever reliably measured.",
            ),
            Landmark("Boiling - water boils", celsius = 100.0, "100 C / 212 F. Kettle's done."),
            Landmark("Oven temperature", celsius = 180.0, "A moderate oven. Cookies, not people."),
            Landmark("Paper burns", celsius = 232.78, "451 F, famously. Books and pizza crusts beware."),
            Landmark("Pizza oven", celsius = 260.0, "500 F territory. Ninety seconds to perfect leopard-spotting."),
        )

    val presets: List<Preset> =
        listOf(
            Preset("Absolute zero", celsius = -273.15),
            Preset("Vostok record", celsius = -89.2),
            Preset("F/C crossover", celsius = -40.0),
            Preset("Freezing", celsius = 0.0),
            Preset("Adelaide now-ish", celsius = 23.6),
            Preset("Body temp", celsius = 37.0),
            Preset("Death Valley record", celsius = 56.7),
            Preset("Boiling", celsius = 100.0),
            Preset("Paper burns", celsius = 232.78),
            Preset("Pizza oven", celsius = 260.0),
        )

    private const val LANDMARK_TOLERANCE_C = 0.005

    // A landmark hit exactly wins; otherwise the first band that holds it.
    fun forCelsius(celsius: Double): Verdict {
        landmarks.firstOrNull { abs(celsius - it.celsius) < LANDMARK_TOLERANCE_C }?.let {
            return Verdict(it.label, it.detail)
        }
        return bands.firstOrNull { celsius <= it.upToC }?.verdict ?: beyond
    }
}
