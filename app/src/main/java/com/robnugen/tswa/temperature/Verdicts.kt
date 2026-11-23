package com.robnugen.tswa.temperature

import kotlin.math.abs

// What a temperature means, shown under the converter's big number.
// The weather-range texts came over word for word from the web version;
// the far-cold and far-hot ones were added in mg #901.
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

// Above the hottest weather ever: a verdict covering every temperature
// from [fromC] up to the next one, so a value a little hotter than molten
// boron still reads "Boron melts", and 101 °C still "Water boils at sea level".
private data class Heat(
    val fromC: Double,
    val verdict: Verdict,
) {
    constructor(fromC: Double, title: String, detail: String) : this(fromC, Verdict(title, detail))
}

// A row of the Landmarks table. Most have no words of their own: the band
// or milestone at the same temperature speaks for them, so 70.0 and 70.2 °C
// (or -195.8 and -196.0) read the same. Only landmarks no band marks, like
// body temperature, bring their own [detail].
data class Landmark(
    val label: String,
    val celsius: Double,
    val detail: String? = null,
)

data class Preset(
    val label: String,
    val celsius: Double,
)

// Death Valley, 1913: where weather ends and kitchens and physics begin.
private const val HOTTEST_WEATHER_C = 56.7

object Verdicts {
    // Each verdict covers everything up to and including its limit (°C).
    private val bands: List<Band> =
        listOf(
            Band(
                upToC = -273.15,
                "Absolute zero",
                "Nothing moves. Nothing can be colder; this is the floor of physics.",
            ),
            Band(
                upToC = -270.42,
                "The afterglow of the Big Bang",
                "Deep space glows at 2.7 K, left over from the Big Bang. Down here is colder than empty space.",
            ),
            Band(
                upToC = -268.93,
                "Helium boils",
                "Liquid helium boils at 4.2 K, lower than anything else. It never freezes on its own.",
            ),
            Band(upToC = -252.88, "Hydrogen boils", "Liquid hydrogen, the rocket fuel, boils at 20 K."),
            Band(upToC = -233.15, "A day on Pluto", "About 40 K: a normal day on Pluto's surface."),
            Band(upToC = -218.79, "Oxygen freezes", "Even oxygen is solid ice down here."),
            Band(upToC = -210.0, "Nitrogen freezes", "Nitrogen, most of the air you breathe, turns to solid ice."),
            Band(
                upToC = -195.8,
                "Liquid nitrogen boils",
                "It boils at this temperature. Your drink would shatter, not freeze.",
            ),
            Band(upToC = -182.96, "Oxygen liquefies", "Air itself turns to liquid down here. Not picnic weather."),
            Band(upToC = -173.0, "Night on the Moon", "The Moon's night side drops to about -173 C / -280 F."),
            Band(
                upToC = -161.5,
                "Natural gas liquefies",
                "Methane turns liquid here, which is how LNG tankers carry it across oceans.",
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
            Band(
                upToC = -38.83,
                "Mercury freezes",
                "Thermometer mercury turns solid, so the coldest places use alcohol thermometers.",
            ),
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
                upToC = HOTTEST_WEATHER_C,
                "Extreme heat",
                "Approaching the hottest ever measured on Earth: 56.7 C / 134 F, Death Valley, 1913.",
            ),
        )
    private val heat: List<Heat> =
        listOf(
            Heat(
                fromC = HOTTEST_WEATHER_C,
                "Hotter than any weather on Earth",
                "No place on Earth gets this hot. From here on, it is kitchens and physics.",
            ),
            // Where water boils lower down the less air presses on it:
            // standard-atmosphere pressure at each height, rounded.
            Heat(
                fromC = 70.0,
                "Water boils on top of Everest",
                "Up at 8,849 m the thin air lets water boil at about 70 C / 158 F. Terrible for tea.",
            ),
            Heat(
                fromC = 80.0,
                "Water boils on Kilimanjaro",
                "On Africa's highest point, 5,895 m up, water boils at about 80 C / 176 F.",
            ),
            Heat(
                fromC = 84.0,
                "Water boils on Mont Blanc",
                "On top of the Alps, 4,806 m up, water boils at about 84 C / 183 F.",
            ),
            Heat(
                fromC = 87.6,
                "Water boils on Mount Fuji",
                "At Fuji's 3,776 m summit water boils at about 87.6 C / 190 F, so noodles take longer.",
            ),
            Heat(
                fromC = 92.8,
                "Water boils in Mexico City",
                "2,240 m up, Mexico City's water boils at about 93 C / 199 F.",
            ),
            Heat(
                fromC = 94.9,
                "Water boils in Denver",
                "In the Mile High City water boils at about 95 C / 203 F. Hence high-altitude baking instructions.",
            ),
            Heat(
                fromC = 100.0,
                "Water boils at sea level",
                "100 C / 212 F under the whole weight of the atmosphere. Kettle's done.",
            ),
            Heat(
                fromC = 101.4,
                "Water boils at the Dead Sea",
                "430 m below sea level, the lowest land on Earth, the extra air holds boiling off until about 101.4 C.",
            ),
            Heat(
                fromC = 121.0,
                "Pressure cooker",
                "A pressure cooker at full steam boils water at about 121 C / 250 F. That is why it cooks so fast.",
            ),
            Heat(fromC = 160.0, "Sugar caramelises", "Sugar starts browning into caramel around 160 C / 320 F."),
            Heat(fromC = 180.0, "A moderate oven", "180 C / 356 F: cakes, cookies and roasts."),
            Heat(fromC = 232.78, "Paper burns", "451 F, famously. Books and pizza crusts beware."),
            Heat(fromC = 260.0, "Pizza oven", "500 F territory. Ninety seconds to perfect leopard-spotting."),
            Heat(fromC = 327.46, "Lead melts", "Lead turns liquid at 327 C / 621 F."),
            Heat(fromC = 419.53, "Zinc melts", "Zinc, the coating on galvanised steel, melts at 419.5 C / 787 F."),
            Heat(
                fromC = 464.0,
                "The surface of Venus",
                "Venus is hotter than Mercury: 464 C / 867 F, day and night.",
            ),
            Heat(
                fromC = 525.0,
                "Everything glows red",
                "Around 525 C / 977 F anything solid starts to glow a dull red in the dark.",
            ),
            Heat(fromC = 660.32, "Aluminium melts", "Drink cans turn to liquid at 660 C / 1,221 F."),
            Heat(fromC = 1_064.18, "Gold melts", "Gold pours like honey at 1,064 C / 1,947 F."),
            Heat(
                fromC = 1_150.0,
                "Lava",
                "Runny lava flows out at about 1,150 C / 2,100 F.",
            ),
            Heat(fromC = 1_538.0, "Iron melts", "Iron turns liquid at 1,538 C / 2,800 F. Welcome to the steel works."),
            Heat(fromC = 2_076.0, "Boron melts", "Boron, one of the hardest elements, melts at 2,076 C / 3,769 F."),
            Heat(
                fromC = 3_422.0,
                "Tungsten melts",
                "3,422 C / 6,192 F: the highest melting point of any metal. Old light bulb filaments were tungsten.",
            ),
            Heat(
                fromC = 5_498.85,
                "Surface of the Sun",
                "The Sun's visible surface is about 5,500 C / 9,930 F (5,772 K).",
            ),
            Heat(
                fromC = 9_666.85,
                "The surface of Sirius",
                "Sirius, the brightest star in the night sky, is about 9,940 K: nearly twice the Sun. " +
                    "That's why it shines blue-white.",
            ),
            Heat(
                fromC = 20_000.0,
                "A plasma cutting torch",
                "A plasma cutter's arc runs around 20,000 C / 36,000 F, hot enough to slice through steel.",
            ),
            Heat(
                fromC = 29_726.85,
                "A lightning bolt",
                "A lightning channel reaches about 30,000 K, five times hotter than the Sun's surface.",
            ),
            Heat(
                fromC = 39_726.85,
                "A blue giant star",
                "The hottest blue stars, like Zeta Puppis, have surfaces around 40,000 K.",
            ),
            Heat(
                fromC = 99_726.85,
                "The hottest white dwarfs",
                "A dead star's leftover core can start out above 100,000 K, then cool for billions of years.",
            ),
            Heat(
                fromC = 1_000_000.0,
                "The Sun's corona",
                "The Sun's outer atmosphere is 1 to 3 million C, hundreds of times hotter than its surface. " +
                    "Nobody is sure why.",
            ),
            Heat(
                fromC = 15_000_000.0,
                "The core of the Sun",
                "About 15 million C, where hydrogen fuses into helium.",
            ),
            Heat(
                fromC = 100_000_000.0,
                "Fusion reactor plasma",
                "Tokamaks like Korea's KSTAR hold plasma at about 100 million C, far hotter than the Sun's core.",
            ),
            Heat(
                fromC = 100_000_000_000.0,
                "A newborn pulsar",
                "When a giant star collapses into a neutron star, its insides hit about 100 billion K. " +
                    "It cools fast, but keeps spinning and flashing for millions of years.",
            ),
            Heat(
                fromC = 10_000_000_000_000.0,
                "The quasar 3C 273",
                "Radio telescopes clocked this quasar's jet at over 10 trillion K, " +
                    "hotter than theory says it should be. " +
                    "If you know of anything hotter, send a PR.",
            ),
        )

    val landmarks: List<Landmark> =
        listOf(
            Landmark("Absolute zero", celsius = -273.15),
            Landmark("Afterglow of the Big Bang", celsius = -270.42),
            Landmark("Helium boils", celsius = -268.93),
            Landmark("Hydrogen boils", celsius = -252.88),
            Landmark("Pluto's surface", celsius = -233.15),
            Landmark("Oxygen freezes", celsius = -218.79),
            Landmark("Nitrogen freezes", celsius = -210.0),
            Landmark("Liquid nitrogen boils", celsius = -195.8),
            Landmark("Oxygen liquefies", celsius = -182.96),
            Landmark("Night on the Moon", celsius = -173.0),
            Landmark("Natural gas liquefies", celsius = -161.5),
            Landmark("Vostok record - coldest ever on Earth", celsius = -89.2),
            Landmark("Dry ice sublimates", celsius = -78.5),
            Landmark("F/C crossover - Celsius equals Fahrenheit", celsius = -40.0),
            Landmark("Mercury freezes", celsius = -38.83),
            Landmark("Freezing - water freezes", celsius = 0.0),
            Landmark("Body temp", celsius = 37.0, "37 C / 98.6 F: human body temperature."),
            Landmark(
                "Healthy chicken",
                celsius = 41.5,
                "About 41.5 C / 107 F: birds run hot. To a hen, you would feel cold.",
            ),
            Landmark("Death Valley record - hottest ever on Earth", celsius = HOTTEST_WEATHER_C),
            Landmark("Water boils on top of Everest", celsius = 70.0),
            Landmark("Water boils on Kilimanjaro", celsius = 80.0),
            Landmark("Water boils on Mont Blanc", celsius = 84.0),
            Landmark("Water boils on Mount Fuji", celsius = 87.6),
            Landmark("Water boils in Mexico City", celsius = 92.8),
            Landmark("Water boils in Denver", celsius = 94.9),
            Landmark("Boiling - water boils", celsius = 100.0),
            Landmark("Water boils at the Dead Sea", celsius = 101.4),
            Landmark("Pressure cooker", celsius = 121.0),
            Landmark("Sugar caramelises", celsius = 160.0),
            Landmark("Oven temperature", celsius = 180.0),
            Landmark("Paper burns", celsius = 232.78),
            Landmark("Pizza oven", celsius = 260.0),
            Landmark("Lead melts", celsius = 327.46),
            Landmark("Zinc melts", celsius = 419.53),
            Landmark("Surface of Venus", celsius = 464.0),
            Landmark("Things glow red", celsius = 525.0),
            Landmark("Aluminium melts", celsius = 660.32),
            Landmark("Gold melts", celsius = 1_064.18),
            Landmark("Lava", celsius = 1_150.0),
            Landmark("Iron melts", celsius = 1_538.0),
            Landmark("Boron melts", celsius = 2_076.0),
            Landmark("Tungsten melts", celsius = 3_422.0),
            Landmark("Surface of the Sun", celsius = 5_498.85),
            Landmark("Surface of Sirius", celsius = 9_666.85),
            Landmark("Plasma cutting torch", celsius = 20_000.0),
            Landmark("Lightning bolt", celsius = 29_726.85),
            Landmark("Blue giant star", celsius = 39_726.85),
            Landmark("Hottest white dwarfs", celsius = 99_726.85),
            Landmark("Sun's corona", celsius = 1_000_000.0),
            Landmark("Core of the Sun", celsius = 15_000_000.0),
            Landmark("Fusion reactor plasma", celsius = 100_000_000.0),
            Landmark("Newborn pulsar", celsius = 100_000_000_000.0),
            Landmark("Quasar 3C 273", celsius = 10_000_000_000_000.0),
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

    // Above the hottest weather ever, the hottest milestone passed. Below
    // it, a landmark with words of its own hit exactly; otherwise the first
    // band that holds it.
    fun forCelsius(celsius: Double): Verdict {
        val hit = landmarks.firstOrNull { abs(celsius - it.celsius) < LANDMARK_TOLERANCE_C }
        val words = hit?.detail
        return when {
            celsius > HOTTEST_WEATHER_C -> heat.last { celsius >= it.fromC }.verdict
            words != null -> Verdict(hit.label, words)
            else -> bands.first { celsius <= it.upToC }.verdict
        }
    }
}
