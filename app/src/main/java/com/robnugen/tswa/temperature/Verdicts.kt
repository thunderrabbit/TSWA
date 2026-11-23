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
                upToC = -196.0,
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
                upToC = 56.7,
                "Extreme heat",
                "Approaching the hottest ever measured on Earth: 56.7 C / 134 F, Death Valley, 1913.",
            ),
        )
    private val heat: List<Heat> =
        listOf(
            Heat(
                fromC = 56.7,
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
            Heat(fromC = 180.0, "A moderate oven", "180 C / 356 F: cakes, cookies and roasts. Not people."),
            Heat(fromC = 232.78, "Paper burns", "451 F, famously. Books and pizza crusts beware."),
            Heat(fromC = 260.0, "Pizza oven", "500 F territory. Ninety seconds to perfect leopard-spotting."),
            Heat(fromC = 327.46, "Lead melts", "Lead turns liquid at 327 C / 621 F."),
            Heat(
                fromC = 464.0,
                "The surface of Venus",
                "Venus is hotter than Mercury: 464 C / 867 F, day and night, hot enough to melt lead.",
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
                "Runny lava from Hawaiian volcanoes flows out at about 1,150 C / 2,100 F.",
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
                fromC = 29_726.85,
                "A lightning bolt",
                "A lightning channel reaches about 30,000 K, five times hotter than the Sun's surface.",
            ),
            Heat(
                fromC = 15_000_000.0,
                "The core of the Sun",
                "About 15 million C, where hydrogen fuses into helium. Hard to get hotter around here.",
            ),
        )

    val landmarks: List<Landmark> =
        listOf(
            Landmark(
                "Absolute zero",
                celsius = -273.15,
                "Nothing moves. Nothing can be colder; this is the floor of physics.",
            ),
            Landmark(
                "Afterglow of the Big Bang",
                celsius = -270.42,
                "2.7 K: the glow left over from the Big Bang, the temperature of deep space.",
            ),
            Landmark("Helium boils", celsius = -268.93, "4.2 K: the lowest boiling point of anything."),
            Landmark("Hydrogen boils", celsius = -252.88, "Liquid hydrogen, the rocket fuel, boils here."),
            Landmark("Pluto's surface", celsius = -233.15, "About 40 K: a normal day on Pluto."),
            Landmark("Oxygen freezes", celsius = -218.79, "Solid oxygen. It is pale blue."),
            Landmark("Nitrogen freezes", celsius = -210.0, "Most of the air you breathe turns to ice."),
            Landmark(
                "Liquid nitrogen boils",
                celsius = -195.8,
                "Liquid nitrogen boils here. Things dipped in it shatter like glass.",
            ),
            Landmark("Oxygen liquefies", celsius = -182.96, "Air itself turns to liquid at this temperature."),
            Landmark("Night on the Moon", celsius = -173.0, "The Moon's night side, about 100 K."),
            Landmark("Natural gas liquefies", celsius = -161.5, "Methane turns liquid, ready for an LNG tanker."),
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
            Landmark("Mercury freezes", celsius = -38.83, "Thermometer mercury turns solid."),
            Landmark("Freezing - water freezes", celsius = 0.0, "Water freezes at 0 C / 32 F. Exactly here."),
            Landmark(
                "Healthy elephant",
                celsius = 36.0,
                "About 36 C / 96.8 F: an elephant runs a little cooler than you.",
            ),
            Landmark("Body temp", celsius = 37.0, "37 C / 98.6 F: human body temperature. You, right now."),
            Landmark(
                "Death Valley record - hottest ever on Earth",
                celsius = 56.7,
                "56.7 C / 134 F, Death Valley, 1913: the hottest ever reliably measured.",
            ),
            Landmark(
                "Water boils on top of Everest",
                celsius = 70.0,
                "The thin air lets water boil at about 70 C / 158 F. Terrible for tea.",
            ),
            Landmark("Water boils on Kilimanjaro", celsius = 80.0, "5,895 m up, about 80 C / 176 F."),
            Landmark("Water boils on Mont Blanc", celsius = 84.0, "4,806 m up, about 84 C / 183 F."),
            Landmark("Water boils on Mount Fuji", celsius = 87.6, "3,776 m up, about 87.6 C / 190 F."),
            Landmark("Water boils in Mexico City", celsius = 92.8, "2,240 m up, about 93 C / 199 F."),
            Landmark("Water boils in Denver", celsius = 94.9, "One mile up, about 95 C / 203 F."),
            Landmark("Boiling - water boils", celsius = 100.0, "100 C / 212 F. Kettle's done."),
            Landmark("Water boils at the Dead Sea", celsius = 101.4, "430 m below sea level, about 101.4 C."),
            Landmark("Pressure cooker", celsius = 121.0, "About 121 C / 250 F at full steam."),
            Landmark("Sugar caramelises", celsius = 160.0, "Sugar browns into caramel."),
            Landmark("Oven temperature", celsius = 180.0, "A moderate oven. Cookies, not people."),
            Landmark("Paper burns", celsius = 232.78, "451 F, famously. Books and pizza crusts beware."),
            Landmark("Pizza oven", celsius = 260.0, "500 F territory. Ninety seconds to perfect leopard-spotting."),
            Landmark("Lead melts", celsius = 327.46, "Lead turns liquid."),
            Landmark("Surface of Venus", celsius = 464.0, "Hotter than Mercury, day and night."),
            Landmark("Things glow red", celsius = 525.0, "Anything solid starts to glow dull red in the dark."),
            Landmark("Aluminium melts", celsius = 660.32, "Drink cans turn to liquid."),
            Landmark("Gold melts", celsius = 1_064.18, "Gold pours like honey."),
            Landmark("Lava", celsius = 1_150.0, "Runny Hawaiian lava, about this hot."),
            Landmark("Iron melts", celsius = 1_538.0, "Welcome to the steel works."),
            Landmark("Boron melts", celsius = 2_076.0, "One of the hardest elements gives up."),
            Landmark("Tungsten melts", celsius = 3_422.0, "The highest melting point of any metal."),
            Landmark("Surface of the Sun", celsius = 5_498.85, "5,772 K: the Sun's visible surface."),
            Landmark("Lightning bolt", celsius = 29_726.85, "About 30,000 K, five times the Sun's surface."),
            Landmark("Core of the Sun", celsius = 15_000_000.0, "About 15 million C, where hydrogen fuses."),
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

    // A landmark hit exactly wins; otherwise the first band that holds it,
    // and above the hottest weather ever the hottest milestone passed.
    fun forCelsius(celsius: Double): Verdict {
        landmarks.firstOrNull { abs(celsius - it.celsius) < LANDMARK_TOLERANCE_C }?.let {
            return Verdict(it.label, it.detail)
        }
        return bands.firstOrNull { celsius <= it.upToC }?.verdict
            ?: heat.last { celsius >= it.fromC }.verdict
    }
}
