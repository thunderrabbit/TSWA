# TSWA — Tri-Scale Weather App

An Android weather app that shows the temperature in more than one
scale, made so Rob can tell family in the USA what the weather is in
Adelaide.

## What it does
- **Weather**: current conditions for a searched or saved place, from
  [Open-Meteo](https://open-meteo.com/) (forecast and geocoding APIs,
  no API key needed).
- **Converter**: converts between Celsius, Fahrenheit, Kelvin and
  Rankine, starting from the Weather page's temperature. Tap a
  temperature to cycle through the scales.
- **Settings**: choose which scales the tap cycle visits (Celsius and
  Fahrenheit are on by default), and save, sort and swipe away places.
- **Onboarding**: on first run the app asks about using your location.

## Status
Version 0.5.0 begins a rewrite of the whole app natively in Kotlin, on
the `rewrite-in-kotlin` branch. Until that lands, the app is a thin
Kotlin WebView shell (`MainActivity.kt`) around a single-page web UI in
`app/src/main/assets/www/index.html`.

## Building
Every push to `master` makes GitHub Actions build a signed release APK
and publish it as a GitHub Release. Pushes are gated on the quality
tools (ktlint, detekt, eslint).

- [docs/BUILDING.md](docs/BUILDING.md): CI and local builds
- [docs/QUALITY.md](docs/QUALITY.md): the quality tools and release gate
- [docs/SIGNING.md](docs/SIGNING.md): release signing secrets
- [docs/VERSIONING.md](docs/VERSIONING.md): version numbers and release tags

## License
GPL-3.0. See [LICENSE](LICENSE).
